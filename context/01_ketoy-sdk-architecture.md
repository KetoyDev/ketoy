# Ketoy SDK — Architecture & Key Patterns

Module path: `Ketoy/ketoy-sdk/`
Package root: `com.developerstring.ketoy`
Published as: `dev.ketoy:sdk:0.1.3-beta.2`

The SDK owns: JSON→Compose rendering engine, screen lifecycle, navigation system, cloud sync, dev tools, DSL builders, extension registries.
It does NOT own: host app theme, authentication, network security policy.

---

## Package Structure & Responsibilities

```
ketoy-sdk/src/main/java/com/developerstring/ketoy/
├── Ketoy.kt                    ← Singleton entry point
├── annotation/                 ← @KComponent compile-time marker
├── cloud/                      ← Server-driven screen fetching + cache
├── core/                       ← Action/variable/state registries
├── dsl/                        ← Kotlin DSL builders
├── devtools/                   ← Live hot-reload dev server client
├── export/                     ← Production JSON export system
├── model/                      ← KNode data model hierarchy (see file 02)
├── navigation/                 ← Type-safe nav system
├── parser/                     ← String/JSON → Compose type parsers
├── registry/                   ← Component & function registries
├── renderer/                   ← JSON → Compose rendering engine
├── screen/                     ← Screen lifecycle & container
├── theme/                      ← Material 3 theme + 35 tokens
├── util/                       ← Colors, icons, shapes, helpers
└── widget/                     ← Custom widget/action extension interfaces
```

---

## Entry Point: `Ketoy.kt`

```kotlin
Ketoy.initialize(
    context, widgetParsers, actionParsers, screens,
    cloudConfig?,   // optional: enables cloud sync
    cacheConfig?,   // optional: cache strategy config
    force = false   // set true to re-initialize
)
```

- **Idempotent** — safe to call multiple times; no-op unless `force=true`
- **Swallows exceptions** — catches and prints, letting host app decide crash policy
- Initializes: `KComponentRegistry`, `ActionRegistry`, `KetoyVariableRegistry`, `KetoyFunctionRegistry`

---

## Rendering Engine: `renderer/`

`KetoyRenderer.kt` — central dispatch composable.

Entry: `JSONStringToUI(jsonString: String, colorScheme?: KetoyColorScheme)`

Flow:
1. Deserializes JSON string → `KNode` sealed class via `KetoyJson`
2. Wraps in `KetoyThemeProvider`
3. Recursively walks node tree dispatching to specialized renderers:
   - `LayoutRenderer` → Column, Row, Box, LazyColumn, LazyRow
   - `WidgetRenderer` → Text, Button, Spacer, Card, Image, Icon, IconButton
   - `ScaffoldRenderer` → Scaffold, TopAppBar, BottomAppBar, NavigationBar, FAB, SnackBar, ModalBottomSheet, Drawer
   - `TextFieldRenderer` → TextField with state management
   - `ComponentRenderer` → Custom registered components
   - `OnClickResolver` → Resolves action IDs → lambdas from ActionRegistry

Template resolution happens at **render time** (not parse time): `{{data:userId:name}}` → lookup in `KetoyVariableRegistry`.

---

## Screen System: `screen/`

`KetoyScreen` — container for a named UI screen. Supports multiple named **content blocks** within a single screen.

Key composables:
- `ProvideKetoyScreen(name)` — creates/retrieves screen from registry, provides via `LocalKetoyScreen`
- `KetoyContent(contentName)` — renders a named content block (DSL-built or JSON)
- `KetoyView` — standalone composables for JSON/asset/network sources

One physical screen can render multiple `KetoyContent` blocks independently (e.g., "header", "body", "actions" blocks on the same screen). This is a common pattern in the demo app.

---

## State Management: `core/`

**`ActionRegistry`** — maps string IDs (`"action_0"`, `"action_1"`) to `() -> Unit` or `(String) -> Unit` lambdas. IDs are auto-generated during DSL building. JSON references actions by ID.

**`KetoyVariableRegistry`** — Compose-backed variable store.
- Variables: immutable (`KetoyVariable`) or mutable (`KetoyMutableVariable`)
- Template format: `{{data:groupKey:fieldName}}`, `{{enum:status:value}}`
- `revision: Long` backed by `mutableLongStateOf` — reading it inside a Composable subscribes to ALL variable changes, triggering recomposition automatically

---

## Navigation: `navigation/`

Type-safe navigation built on `navigation-compose`.

Key types:
- `KetoyRoute` — `@Serializable` route definition
- `KetoyNavGraph` — graph structure with destinations + actions
- `KetoyNavHost` — Composable host; provides `LocalKetoyNavController`, `LocalKetoyNavGraph`, `LocalKetoyNavHostName`
- `KetoyNavController` — API: `navigate(route)`, `popBackStack()`, etc.
- `KetoyComposableRegistry` — maps route strings to composables
- `KetoyNavigationExecutor` — runs navigation actions from JSON

Dev/cloud overrides: `KetoyNavDevOverrides` and `KetoyCloudNavOverrides` allow runtime nav graph replacement without redeployment.

---

## Cloud: `cloud/`

`KetoyCloudService` fetches screens from Ketoy cloud API.

**5 cache strategies** (`KetoyCacheStrategy`):
- `NETWORK_FIRST` — try network, fall back to cache
- `CACHE_FIRST` — try cache, fall back to network
- `OPTIMISTIC` — return cache immediately + refresh in background
- `CACHE_ONLY` — only cache, no network
- `NETWORK_ONLY` — always network, never cache

`KetoyCloudScreen` — Composable that fetches + renders a named screen from cloud.
`KetoyCloudConfig(apiKey, packageName, baseUrl)` — required for cloud features.

---

## Extension System: `widget/` + `registry/`

Four extension points:
- `KetoyWidgetParser` — parse custom JSON widget types → Composable
- `KetoyActionParser` — handle custom action types from JSON
- `KComponentRegistry` — register named custom components (with optional renderer)
- `KetoyFunctionRegistry` — register callable functions from JSON

Built-ins in `widget/builtin/`: `NavigateActionParser`, `CallFunctionActionParser`.

---

## DSL Builders: `dsl/`

Kotlin builder functions that produce `KNode` trees programmatically:
- `KColumn { }`, `KRow { }`, `KBox { }`, `KLazyColumn { }`, `KLazyRow { }`
- `KText()`, `KButton { }`, `KImage()`, `KIcon()`, `KSpacer()`
- `KetoyScreenDsl` — `ProvideKetoyScreen` + `KetoyContent` builder API

DSL-built screens are serialized to JSON via `ketoyExport` Gradle task.

---

## Dev Tools: `devtools/`

Live hot-reload dev workflow:
- `KetoyDevWrapper` — wraps app in dev mode, connects to local server
- `KetoyDevClient` — WebSocket client receiving JSON updates from Gradle plugin server
- `KetoyDevOverlay` — debug overlay showing render timing, active screen info
- `KetoyDevActivity` — standalone dev-mode activity

Dev server requires: `KetoyDevConfig(host, port)` + cleartext network security config (only in debug).

---

## Theme: `theme/`

`KetoyTheme` provides 35+ color tokens via `KetoyThemeProvider`:
- Full Material 3 set (primary, secondary, tertiary, surface variants, error, outline)
- Additional: surface containers (dim/bright/low/high)
- Custom success token
- Accessed in renderers via CompositionLocal, not passed explicitly

---

## Parsers: `parser/`

String → Compose type converters:
- `ArrangementParser` — `"spacedBy_8"` → `Arrangement.spacedBy(8.dp)`
- `ColorParser` — `"#FF0000"` or named color → `Color`
- `GradientParser`, `ModifierParser`, `ShapeParser`, `ScaffoldParser`, `TextFieldParser`

---

## CompositionLocals (Key Access Points)

| Local | Provides |
|---|---|
| `LocalKetoyScreen` | Current `KetoyScreen` instance |
| `LocalKetoyNavController` | Navigation API |
| `LocalKetoyNavGraph` | Current route definitions |
| `LocalKetoyNavHostName` | Dev-mode host targeting |

---

## Non-Obvious Gotchas

- `KCardNode.children` is `MutableList<KNode>` (not immutable) — can modify post-construction
- Gradle plugin is **not required** for SDK to work; it only adds dev/export/cloud tasks
- Cloud config is fully optional; all cloud APIs are gated on `cloudConfig != null`
- Action IDs (`"action_0"`) are positional/auto-generated during DSL → only stable within a serialization session
- Variable template resolution is lazy at render time, so variables can change after JSON is parsed
