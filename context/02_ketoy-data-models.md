# Ketoy Data Models — KNode Hierarchy & Serialization

Model files live in: `Ketoy/ketoy-sdk/src/main/java/com/developerstring/ketoy/model/`

This file covers the serialization contract — what JSON looks like, how the node tree is structured, and how props/modifiers work. Critical for adding new components, modifying renderers, or working with exported JSON.

---

## KNode — Root Sealed Class

`model/KNode.kt` — all UI elements are subtypes with `@Serializable` + `@SerialName`.

Polymorphic deserialization key: `@JsonClassDiscriminator("type")` on the sealed class.

**JSON example**:
```json
{
  "type": "Column",
  "props": { "verticalArrangement": "Center", "modifier": { "fillMaxWidth": true } },
  "children": [
    { "type": "Text", "props": { "text": "Hello {{data:user:name}}" } }
  ]
}
```

---

## Node Categories

### Layout Containers (have `children: List<KNode>`)

| SerialName | Kotlin Class | Props Class |
|---|---|---|
| `"Column"` | `KColumnNode` | `KColumnProps` |
| `"Row"` | `KRowNode` | `KRowProps` |
| `"Box"` | `KBoxNode` | `KBoxProps` |
| `"LazyColumn"` | `KLazyColumnNode` | `KLazyColumnProps` |
| `"LazyRow"` | `KLazyRowNode` | `KLazyRowProps` |
| `"Button"` | `KButtonNode` | `KButtonProps` |
| `"Card"` | `KCardNode` | `KCardProps` |

Note: `KCardNode.children` is `MutableList<KNode>` (not `List`).

### Leaf Widgets (no children)

| SerialName | Kotlin Class | Props Class |
|---|---|---|
| `"Text"` | `KTextNode` | `KTextProps` |
| `"Spacer"` | `KSpacerNode` | `KSpacerProps` |
| `"Image"` | `KImageNode` | `KImageProps` |
| `"Icon"` | `KIconNode` | `KIconProps` |
| `"IconButton"` | `KIconButtonNode` | `KIconButtonProps` |
| `"TextField"` | `KTextFieldNode` | `KTextFieldProps` |

### Scaffold Components

| SerialName | Kotlin Class |
|---|---|
| `"Scaffold"` | `KScaffoldNode` |
| `"TopAppBar"` | `KTopAppBarNode` |
| `"BottomAppBar"` | `KBottomAppBarNode` |
| `"NavigationBar"` | `KNavigationBarNode` |
| `"NavigationBarItem"` | `KNavigationBarItemNode` |
| `"FloatingActionButton"` | `KFloatingActionButtonNode` |
| `"SnackBar"` | `KSnackBarNode` |
| `"SnackBarHost"` | `KSnackBarHostNode` |
| `"ModalBottomSheet"` | `KModalBottomSheetNode` |
| `"NavigationRail"` | `KNavigationRailNode` |
| `"NavigationRailItem"` | `KNavigationRailItemNode` |
| `"NavigationDrawerItem"` | `KNavigationDrawerItemNode` |
| `"CustomNavigationItem"` | `KCustomNavigationItemNode` |
| `"AppBarAction"` | `KAppBarActionNode` |

### Custom & Data Nodes

| SerialName | Purpose |
|---|---|
| `"Component"` | Custom registered component (`KComponentNode`) |
| `"DataClass"` | Data binding node (`KDataClassNode`) |
| `"Enum"` | Enum selection (`KEnumNode`) |
| `"DataList"` | Data-driven list repetition (`KDataListNode`) |

---

## KModifier — `model/KModifier.kt`

Applied via `props.modifier: KModifier?` on any node.

Supported fields (all optional):
- **Size**: `fillMaxWidth`, `fillMaxHeight`, `width`, `height`, `size`
- **Spacing**: `padding` (all/each side), `margin`
- **Visual**: `border`, `shadow`, `cornerRadius`, `alpha`, `scale`, `rotation`, `skew`
- **Gradient**: `gradient: KGradient?`
- **Scroll**: `scroll: KScrollConfig?`
- **Layout**: `weight` (for Row/Column children)

---

## KGradient — `model/KGradient.kt`

Types: `LINEAR`, `RADIAL`, `SWEEP`

```json
{
  "type": "LINEAR",
  "stops": [
    { "offset": 0.0, "color": "#FF6B6B" },
    { "offset": 1.0, "color": "#4ECDC4" }
  ],
  "angle": 45.0
}
```

---

## KImageSource — `model/KImageSource.kt`

Supports multiple image source types:
- `URL` — remote image via Coil
- `RESOURCE` — Android resource ID
- `ASSET` — bundled asset file
- `BASE64` — inline encoded image
- `ICON` — Material icon name

---

## Props Classes — `model/KProps.kt`

Each node type has a dedicated props class. Common props pattern:

```kotlin
@Serializable
data class KColumnProps(
    val modifier: KModifier? = null,
    val verticalArrangement: String? = null,  // parsed by ArrangementParser
    val horizontalAlignment: String? = null,
    val onClick: String? = null              // action ID → ActionRegistry
)
```

String-typed fields that get parsed at render time (not at deserialization):
- `verticalArrangement`, `horizontalAlignment` → `ArrangementParser`, `AlignmentParser`
- `color`, `backgroundColor` → `ColorParser` (hex strings or named tokens)
- `onClick` → `OnClickResolver` (resolves action ID to lambda)
- `shape` → `ShapeParser`

**`KTextFieldProps`** (`model/KTextFieldProps.kt`) — more complex:
- `keyboardType`, `imeAction`, `visualTransformation`
- `validation` — regex + error message
- `onValueChange` — action ID fired on each keystroke

---

## KetoyVariable — `model/KetoyVariable.kt`

Two wrappers for state-driven UI:
- `KetoyVariable<T>` — immutable snapshot value
- `KetoyMutableVariable<T>` — mutable, backed by Compose state

Template resolution in JSON strings:
- `{{data:groupKey:fieldName}}` — reads from `KetoyVariableRegistry`
- `{{enum:groupKey:value}}` — reads enum selection

The registry's `revision: Long` (backed by `mutableLongStateOf`) causes automatic recomposition whenever any variable changes.

---

## KComponent Models — `model/KComponentModels.kt`

Custom component registration metadata:

```kotlin
data class KComponentInfo(
    val name: String,               // matches KComponentNode.componentName
    val metadata: KComponentMetadata,
    val renderer: (@Composable (KNode, List<KNode>) -> Unit)? = null
)
```

`KetoyJsonSchema` — introspection schema for tooling (describes expected props).

---

## Exported JSON Screen Format

Generated by `ketoyExport` task into `ketoy-export/`:

```json
{
  "screenName": "home",
  "displayName": "Home",
  "description": "Main dashboard",
  "version": "1.0.3",
  "contents": {
    "body": { "type": "Column", "props": {...}, "children": [...] },
    "header": { "type": "Row", "props": {...}, "children": [...] }
  }
}
```

`contents` is a map of content name → root `KNode`. Multiple content blocks per screen. Manifests: `screen_manifest.json`, `navigation_manifest.json`.

---

## Serialization Utilities

- `KetoyJson.kt` — singleton `Json` instance configured for polymorphism + lenient parsing
- `KetoyJsonUtils.kt` — helpers for safe field extraction, null handling
- `KetoyDataProvider.kt` — interface for external data sources that feed into variable registry

---

## Adding a New Node Type (Checklist)

1. Add `@Serializable @SerialName("YourType") data class KYourNode(val props: KYourProps, ...) : KNode()` in `KNode.kt`
2. Add `KYourProps` in `KProps.kt`
3. Add renderer branch in the appropriate renderer file (`WidgetRenderer`, `LayoutRenderer`, etc.)
4. Add DSL builder in `dsl/` if needed
5. Add `@Serializable data class KYourProps(...)` — all fields nullable with defaults
6. Register in `KetoyRenderer.kt` dispatch when if it's a new category
