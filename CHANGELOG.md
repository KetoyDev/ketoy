# Changelog

All notable changes to the Ketoy project are documented in this file.

---

## [0.1-beta.1] — 2025-01-XX

### Improvements

#### Enhanced Scroll Modifier System

The scroll modifier system has been completely overhauled to support rich customization per axis:

**New `KScrollConfig` Data Class:**
```kotlin
@Serializable
data class KScrollConfig(
    val enabled: Boolean = true,
    val reverseScrolling: Boolean = false,
    val flingBehavior: String? = null  // "default" or "none"
)
```

**Fling Behavior Presets:**
- `KScrollConfig.FLING_DEFAULT` (`"default"`) — Standard Android fling physics
- `KScrollConfig.FLING_NONE` (`"none"`) — No fling; scrolling stops immediately

**JSON Wire Format:**
```json
// Boolean shorthand (backward compatible)
{ "verticalScroll": true }

// Full object format with customization
{
  "verticalScroll": {
    "enabled": true,
    "reverseScrolling": true,
    "flingBehavior": "none"
  }
}
```

**DSL Usage:**
```kotlin
val scrollableColumn = KColumn(
    modifier = kModifier(
        verticalScroll = kScrollConfig(
            enabled = true,
            reverseScrolling = false,
            flingBehavior = KScrollConfig.FLING_DEFAULT
        )
    )
)
```

**Breaking Changes:**
- `KModifier.verticalScroll` type changed from `Boolean?` to `KScrollConfig?`
- `KModifier.horizontalScroll` type changed from `Boolean?` to `KScrollConfig?`
- Removed redundant fields: `verticalScrollReverse`, `horizontalScrollReverse`, `scrollDirection`

**Migration:** Existing JSON with boolean scroll values (`"verticalScroll": true`) continues to work via the custom serializer.

---

#### Gradle Plugin Fixes

- **Signing fix for Gradle Plugin Portal:** Signing is now conditionally required only for Maven Central publishing. The `publishPlugins` task to Gradle Plugin Portal no longer fails due to missing GPG keys.

- **ketoyExport task UP-TO-DATE fix:** Export tasks now correctly force test re-execution when running exports. Previously, `ketoyExport` and `ketoyExportProd` would skip exports when tests were UP-TO-DATE from a prior run.

#### Code Cleanup

- **Root build.gradle.kts cleanup:** Removed ~500 lines of legacy duplicate cloud task definitions. The root build file now only contains the plugins block with a comment pointing to the plugin.

---

## [0.1-beta] — 2025-01-01

### Initial Release

The first public beta release of **Ketoy** — an open-source, server-driven UI engine for Jetpack Compose. Write K-DSL, convert to JSON, render native Android UI — no Play Store approvals needed.

**Build Configuration:**
- Compile SDK: 36  
- Min SDK: 26  
- Kotlin: 2.0.21  
- Compose BOM: 2025.01.01  
- Java Target: 11  
- Serialization: kotlinx-serialization-json 1.6.0  
- Navigation: androidx.navigation-compose 2.8.9  
- Image Loading: Coil Compose 2.7.0  
- WebSocket: OkHttp 4.12.0  

---

### Ketoy SDK (`dev.ketoy:sdk:0.1-beta`)

#### Initialization

- **`Ketoy.initialize()`** — Central entry point to configure the SDK.
  - `context` — Android Context (required for cloud features)
  - `widgetParsers` — Custom `KetoyWidgetParser<*>` list
  - `actionParsers` — Custom `KetoyActionParser<*>` list
  - `screens` — Pre-register `KetoyScreen` list
  - `cloudConfig` — `KetoyCloudConfig` for server-driven UI
  - `cacheConfig` — `KetoyCacheConfig` (default: `NETWORK_FIRST`, 30-day max age, 50 MB)
  - `force` — Force re-initialization

Initialization sequence: KComponentRegistry → clear registries → register parsers → register built-in parsers (NavigateActionParser, CallFunctionActionParser) → register screens → configure cloud/cache.

---

#### DSL Builders

Type-safe Kotlin DSL for building UI trees that serialize to JSON:

| Builder | Parameters |
|---------|-----------|
| `KText` | `text`, `modifier`, `fontSize`, `fontWeight`, `color`, `textAlign`, `maxLines`, `overflow`, `fontStyle`, `letterSpacing`, `lineHeight` |
| `KButton` | `onClick`, `modifier`, `enabled`, `containerColor`, `contentColor`, `elevation`, `border`, `children` |
| `KCard` | `onClick`, `modifier`, `enabled`, `backgroundColor`, `elevation`, `border`, `children` |
| `KColumn` | `modifier`, `verticalArrangement`, `horizontalAlignment`, `children` |
| `KRow` | `modifier`, `horizontalArrangement`, `verticalAlignment`, `children` |
| `KBox` | `modifier`, `contentAlignment`, `children` |
| `KLazyColumn` | `modifier`, `verticalArrangement`, `horizontalAlignment`, `contentPadding`, `children` |
| `KLazyRow` | `modifier`, `horizontalArrangement`, `verticalAlignment`, `contentPadding`, `children` |
| `KImage` | `contentDescription`, `modifier`, `contentScale`, `alignment` |
| `KIcon` | `iconName`, `contentDescription`, `modifier`, `size`, `tint` |
| `KIconButton` | `onClick`, `actionId`, `modifier`, `enabled`, `containerColor`, `contentColor`, `children` |
| `KSpacer` | `modifier`, `width`, `height` |
| `KDivider` | `modifier`, `thickness`, `color` |
| `KComponent` | `type` (String), `props` (Map) — custom component instantiation |
| `KFunctionCall` | `functionName`, `arguments` — function closure helper |

---

#### Widget & Node Model Types

**Container Nodes** (support child nodes):
- `KColumnNode` — Vertical layout
- `KRowNode` — Horizontal layout
- `KBoxNode` — Overlay/stacking layout
- `KLazyColumnNode` — Virtualized vertical list
- `KLazyRowNode` — Virtualized horizontal list
- `KButtonNode` — Clickable container
- `KCardNode` — Material 3 card
- `KScaffoldNode` — Top-level scaffold

**Leaf Nodes:**
- `KTextNode`, `KImageNode`, `KIconNode`, `KSpacerNode`, `KDividerNode`, `KComponentNode`

All nodes have corresponding serializable props classes (`KTextProps`, `KButtonProps`, `KCardProps`, etc.) with full modifier support.

---

#### Screen System

**`KetoyScreen`** — Core screen abstraction supporting multiple content blocks.

Factory methods:
- `KetoyScreen(screenName, displayName, description, version, cloudEnabled, colorScheme)`
- `KetoyScreen.create(screenName) { content { ... } }` — DSL builder
- `KetoyScreen.fromNode(screenName, nodeBuilder)` — From node builder lambda
- `KetoyScreen.fromJson(screenName, jsonString)` — From raw JSON (supports multi-content wrapper)
- `KetoyScreen.fromComposable(screenName, builder)` — From @Composable lambda
- `KetoyScreen.fromAsset(screenName, assetPath)` — From local asset

Content management:
- `addContent(name, nodeBuilder)` — Add named content block
- `buildExportJson()` — Serialize screen to JSON with wrapper format
- `buildAllJson()` — Map of all content blocks' JSON
- `setDevOverride(name, json)` / `setScreenDevOverride(json)` — Dev server hot-reload injection

**`KetoyScreenRegistry`** — Singleton screen store:
- `register()`, `registerAll()`, `get()`, `getAll()`, `isRegistered()`, `clear()`
- `registerFromJsonMap()` — Bulk-load from JSON map
- `registerFromAssets()` — Bulk-load from asset paths

**Composables:**
- `ProvideKetoyScreen(screenName)` — Entry point; provides `KetoyScreen` via CompositionLocal
- `KetoyContent(name, nodeBuilder, dslBuilder)` — Child composable for per-block rendering; auto-registers with parent

---

#### Theme System

**`KetoyThemeProvider(themeMode, content)`** — Wraps the app to provide theme context.

**`KetoyThemeMode`** (Sealed class):
- `System` — Follow device dark/light setting
- `Light` — Force light
- `Dark` — Force dark
- `Custom(lightScheme, darkScheme)` — Custom schemes with system-based switching

**`KetoyColorScheme`** — 35+ Material 3 color tokens:

| Category | Tokens |
|----------|--------|
| Primary | `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer` |
| Secondary | `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer` |
| Tertiary | `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer` |
| Error | `error`, `onError`, `errorContainer`, `onErrorContainer` |
| Background | `background`, `onBackground` |
| Surface | `surface`, `onSurface`, `surfaceVariant`, `onSurfaceVariant` |
| Surface Containers | `surfaceContainer`, `surfaceContainerLow`, `surfaceContainerHigh`, `surfaceContainerHighest`, `surfaceContainerLowest` |
| Surface Variants | `surfaceBright`, `surfaceDim`, `surfaceTint` |
| Outline | `outline`, `outlineVariant` |
| Inverse | `inversePrimary`, `inverseSurface`, `inverseOnSurface` |
| Success (Custom) | `success`, `onSuccess`, `successContainer`, `onSuccessContainer` |

- `resolve(token: String)` — Semantic color lookup by `@theme/` token name
- `fromMaterial(colorScheme)` — Convert from Material 3 `ColorScheme`
- Success tokens default to green values (`#1B7D46`, `#A8F5C4`) since Material 3 has no native success colors

**`LocalKetoyDarkTheme`** — `CompositionLocal<Boolean>` for dark mode state.

---

#### Type-Safe Theme Tokens (`KColors`)

All theme color references are type-safe constants:

```kotlin
KColors.Primary          // "@theme/primary"
KColors.OnPrimary        // "@theme/onPrimary"
KColors.Background       // "@theme/background"
KColors.Surface          // "@theme/surface"
KColors.Error            // "@theme/error"
KColors.Success          // "@theme/success"
KColors.SurfaceContainer // "@theme/surfaceContainer"
// ... 35+ tokens total
```

Additional named colors: `Blue`, `Red`, `Green`, `Yellow`, `Orange`, `Purple`, `White`, `Black`, `Gray`, `Transparent`.

---

#### Function Registry

**`KetoyFunctionRegistry`** — Register and call named functions from JSON:

```kotlin
data class FunctionInfo(
    val name: String,
    val handler: (Map<String, Any>) -> Unit,
    val parameterTypes: Map<String, String>,
    val description: String
)
```

- `register(name, handler, parameterTypes, description)` — Register function
- `call(functionName, arguments)` — Execute by name
- `get()`, `exists()`, `getAll()`, `clear()`

**`ActionRegistry`** — Auto-ID'd action closures for DSL-generated onClick:
- `register(action) → String` — Returns auto-generated ID (e.g., `"action_42"`)
- `registerAction(id, action)` — Register with custom ID
- `execute(id)`, `get(id)`

---

#### Variable System & Data Binding

**`KetoyVariableRegistry`** — Template variable store with reactive Compose state:

Variable types:
- `KetoyVariable.Immutable<T>(id, value)` — Fixed value
- `KetoyVariable.Mutable<T>(id, value)` — Updatable value with atomic revision tracking

API:
- `register(variable)`, `getValue(id)`, `updateValue(id, newValue)`, `clear()`
- `resolve(template)` — Template string resolution

Reactive recomposition:
- Backed by `mutableLongStateOf(0L)` — revision counter increments on `register()`, `updateValue()`, and `clear()`
- `revision: Long` — Public read property; triggers Compose state invalidation when read in composition
- Screens use `key(json, varRevision)` to auto-recompose when variables change

Template patterns:
- `{{data:id:field}}` → Looks up `"id.field"` (e.g., `{{data:user:name}}` → `user.name`)
- `{{enum:id:property}}` → Looks up `"id.property"`
- Missing variables resolve to `"[Missing: key]"`

**`KData`** — Type-safe template builders:
- `KData.ref(prefix, field)` → `"{{data:prefix:field}}"`
- `KData.user(field)` → `"{{data:user:field}}"`
- `KData.analytics(field)` → `"{{data:analytics:field}}"`

**`KetoyDataProvider`** — Manages persistence and coordination with the variable registry.

---

#### Navigation System

**Type-safe routes** using Navigation Compose 2.8+ (hybrid string and type-safe support):

**`KetoyRoute`** — Marker interface for type-safe routes (use with `@Serializable data object`/`data class`).

**`KetoyNavDestination`:**
- `id`, `route`, `screenName`, `label`, `icon`, `selectedIcon`, `isStartDestination`
- `resolvedId` — Returns `id` if non-empty, else `route`

**`KetoyNavAction`:**
- `id`, `route`, `label`

**`NavigationStyle`** (Enum — lowercase JSON serialization):
- `Navigate`, `PopBackStack`, `NavigateAndReplace`, `NavigateAndClearBackStack`, `PopToRoot`

**`KNavigateAction`** — Server-driven navigation:
- `routeName` — Registered Ketoy screen route
- `widgetJson` — Inline JSON widget tree
- `assetPath` — Local asset path
- `navigationStyle` — One of the `NavigationStyle` values
- `arguments` — Key-value map
- `popBackStackRoute` — For `PopBackStack` style

**`KetoyNavGraph`:**
- `name`, `startDestinationRoute`, `destinations`, `navigations`
- `buildActionRemaps()`, `toJson()`, `fromJson()`

**Composables & Controllers:**
- `KetoyNavHost(startRoute, navController, content)` — Navigation container
- `KetoyNavController` — Type-safe + string-based navigation:
  - `navigate(route)`, `navigateAndReplace(route)`, `navigateAndClearBackStack(route)`, `popBackStack()`, `popToRoot()`
  - `currentRouteAs<T>()`, `currentRoute()`
- `LocalKetoyNavController` — CompositionLocal access

**Supporting:**
- `KetoyNavigationExecutor` — Parses `KNavigateAction` and delegates routing
- `KetoyNavigator` — Higher-level navigation wrapper
- `KetoyComposableRegistry` — Maps route strings to `@Composable` builders
- `KetoyCloudNavOverrides` / `KetoyNavDevOverrides` — Remote/dev nav graph injection

---

#### Renderers

- **`KetoyRenderer`** — Main entry: `JSONStringToUI(json)` → `@Composable`
- **`WidgetRenderer`** — `RenderText`, `RenderButton`, `RenderCard`, `RenderImage`, `RenderIcon`, `RenderSpacer`, `RenderDivider`
- **`LayoutRenderer`** — `RenderColumn`, `RenderRow`, `RenderBox`
- **`ScaffoldRenderer`** — `RenderScaffold`
- **`ComponentRenderer`** — Dispatches `KComponentNode` to `KComponentRegistry`
- **`TextFieldRenderer`** — `RenderTextField`
- **`OnClickResolver`** — Resolves onClick from 3 patterns: string → `ActionRegistry`, JSON object → `KetoyActionRegistry`, JSON array → multi-action sequential

---

#### Parser & Action System

**`KetoyWidgetParser<T>`** — Interface for custom widget rendering:
```kotlin
interface KetoyWidgetParser<T> {
    val typeName: String
    fun parse(props: JsonObject): @Composable (Modifier) -> Unit
}
```

**`KetoyActionParser<T>`** — Interface for custom action handling:
```kotlin
interface KetoyActionParser<T> {
    val actionType: String
    fun deserialize(json: JsonObject): T
    fun onCall(action: T, context: Any?, navController: KetoyNavController?)
}
```

Built-in action parsers:
- `NavigateActionParser` — Handles `"navigate"` action type
- `CallFunctionActionParser` — Handles `"callFunction"` action type

Registries:
- `KetoyWidgetRegistry` — `register()`, `registerAll()`, `get()`, `parseWidget()`
- `KetoyActionRegistry` — `register()`, `registerAll()`, `get()`

---

#### Custom Component Registration

**`KComponentRegistry`** — Register and render custom @Composable components:

```kotlin
KComponentRegistry.register("Badge") { modifier ->
    MyCustomBadgeComposable(modifier = modifier)
}
```

- `register(name, builder)`, `get(name)`, `isAvailable(name)`, `getAll()`, `initialize()`
- `getMetadata(name)` / `loadFromMetadata(metadata)` — IDE support

**`ComponentMetadata`:**
- `name`, `description`, `parameters: Map<String, ParameterInfo>`
- `ParameterInfo(name, type, required, default)`

---

#### Export System

**`KetoyProductionExport`** — Base class for production JSON export:

```kotlin
class AppExports : KetoyProductionExport() {
    override fun registerScreens() {
        screen("home", "Home Screen", "Main landing page") {
            content("cards") { buildHomeCards(...) }
            content("transactions") { buildHomeTransactions(...) }
        }
    }
    override fun registerNavGraphs() {
        navGraph("main") { ... }
    }
}
```

- `buildExport() → ExportResult` — Builds screens & nav graphs
- `buildScreenManifest()` — Index of all screens
- `buildNavigationManifest()` — Combined nav graph manifest
- `ExportResult.writeTo(directory)` — Write all JSONs to disk

**`KetoyExportRegistry`** — DSL-based registration via `ketoyExport {}`:
```kotlin
KetoyExportRegistry.ketoyExport {
    screen("home", "Home Screen") {
        content("cards") { buildHomeCards(...) }
    }
}
```

**`KetoyAutoExportRunner`** — Auto-export triggered by Gradle plugin on source changes:
- Rebuilds JSON on-the-fly
- Broadcasts to dev server for hot-reload

**Export pipelines:**
- Dev: `ketoyExport` task → `ketoy-screens/*.json`
- Prod: `ketoyExportProd` task → `ketoy-export/*.json` + `screen_manifest.json` + `navigation_manifest.json`

---

#### Cloud Service

**`KetoyCloudConfig`:**
- `apiKey`, `packageName`, `baseUrl` (default: `https://api.ketoy.dev`)

**`KetoyCacheConfig`:**
- `strategy` — `NETWORK_FIRST` | `CACHE_FIRST` | `NETWORK_ONLY`
- `maxAge` — Default 30 days
- `maxSize` — Default 50 MB

**Components:**
- `KetoyCloudService` — Cloud screen fetching & management
- `KetoyApiClient` — JDK HTTP client (header: `x-developer-api-key`)
- `KetoyCacheStore` — Room-based disk cache
- `KetoyCloudScreen` — Composable that fetches + renders from cloud

---

#### Utility Token Objects

| Object | Purpose | Key Constants |
|--------|---------|---------------|
| `KColors` | Theme color tokens | 35+ `@theme/` tokens + named colors |
| `KIcons` | Material Design icon names | `Home`, `Settings`, `Search`, `Menu`, `Close`, `Plus`, `Person`, `Favorite`, `Share`, `Edit`, `Delete`, etc. |
| `KShapes` | Shape constants | `Circle`, `RoundedRect(radius)`, `CutCorner(radius)` |
| `KArrangements` | Layout arrangements | `Start`, `End`, `Center`, `spacedBy()`, `spaceEvenly`, `spaceBetween`, `spaceAround` |
| `KAlignments` | Alignment constants | `TopStart`, `Center`, `BottomEnd`, etc. (9 positions) |
| `KFontWeights` | Font weights | `Thin`, `Light`, `Normal`, `Medium`, `SemiBold`, `Bold`, `ExtraBold`, `Black` |
| `KData` | Template builders | `ref()`, `user()`, `analytics()` |

---

### Ketoy DevTools Integration (`ketoy-sdk`)

#### KetoyDevWrapper

Wraps the entire app for hot-reload development:

```kotlin
KetoyDevWrapper(
    config = KetoyDevConfig(
        host = "192.168.1.100",
        port = 8484,
        autoConnect = true,
        showOverlay = true,
        shakeToDisconnect = true
    )
) {
    MyApp()
}
```

- Connects to the Gradle plugin dev server via WebSocket
- Injects screen JSON as dev-overrides into registered `KetoyScreen`s
- Parses & injects nav graphs into `KetoyNavDevOverrides`
- Clears overrides on disconnect

#### Supporting DevTools

- **`KetoyDevClient`** — WebSocket client managing `screens` and `navGraphs` state
  - `connect(host, port)`, `disconnect()`
- **`KetoyDevOverlay`** — Floating status pill showing connection state
- **`KetoyDevConnectScreen`** — Manual server connection UI with host/port input
- **`KetoyDevPreviewScreen`** — Live preview of screens by name
- **`KetoyDevStorage`** — Persistent storage for dev settings
- **`KetoyDevExporter`** — Local JSON export utility
- **`KetoyDevActivity`** — Activity entry point for devtools UI

---

### Ketoy Gradle Plugin (`dev.ketoy.devtools:0.1-beta`)

#### Plugin Setup

```kotlin
// settings.gradle.kts
includeBuild("ketoy-gradle-plugin")

// app/build.gradle.kts
plugins {
    id("dev.ketoy.devtools")
}
```

#### Extension DSL

```kotlin
ketoyDev {
    apiKey = "your-api-key"                        // Required for cloud tasks
    packageName = "com.example.app"                // Required for cloud tasks
    baseUrl = "https://api.ketoy.dev"              // Cloud API base URL
    screensDir = "ketoy-screens"                   // Dev export output directory
    prodExportDir = "ketoy-export"                 // Prod export output directory
    exportTestClass = "KetoyAutoExportTest"        // Test class for auto-export
    appModule = "app"                              // App module name
    testTaskName = "testDebugUnitTest"             // Test task to run exports
    serverPort = 8484                              // Dev server HTTP port
}
```

Property resolution order: DSL value → Gradle `-P` flag → `local.properties` → built-in defaults.

#### Gradle Tasks (11 total)

**Export Tasks:**

| Task | Description |
|------|-------------|
| `ketoyExport` | Export DSL screens to JSON (`ketoy-screens/`) |
| `ketoyExportProd` | Export with screen + navigation manifests (`ketoy-export/`) |

**Server Tasks:**

| Task | Description |
|------|-------------|
| `ketoyServe` | Start dev server (HTTP + WebSocket, no auto-export) |
| `ketoyDev` | Start dev server with auto-export on `.kt`/`.kts` changes |

**Cloud Tasks:**

| Task | Description |
|------|-------------|
| `ketoyPush` | Upload single screen to cloud |
| `ketoyPushAll` | Upload all screens |
| `ketoyListScreens` | List deployed screens |
| `ketoyScreenVersions` | List screen version history |
| `ketoyScreenDetails` | Get screen details with JSON content |
| `ketoyRollback` | Rollback to a previous screen version |
| `ketoyDeleteScreen` | Delete screen and all its versions |

#### Dev Server

**HTTP Server** (default port 8484):

| Endpoint | Description |
|----------|-------------|
| `GET /` | HTML dashboard |
| `GET /status` | Health check (JSON) |
| `GET /screens` | List all screen names |
| `GET /screen?name=X` | Single screen JSON |
| `GET /navs` | List nav graph names |
| `GET /nav?name=X` | Single nav graph JSON |
| `GET /bundle` | All screens + nav graphs |
| `GET /poll?v=N` | Long-poll (30s timeout) for updates |

**WebSocket Server** (default port 8485):
- Real-time push on screen/nav updates
- Commands: `ping`, `getScreen`

#### File & Source Watchers

- **FileWatcher** — Monitors `.json` files via NIO WatchService (100ms debounce)
- **SourceWatcher** — Monitors `.kt`/`.kts` files (1500ms debounce), triggers `gradlew ketoyExport`, thread-safe with `AtomicBoolean`/`AtomicLong`

#### Screen Manager

- In-memory `ConcurrentHashMap` cache for screens + nav graphs
- Atomic version counter for change detection
- Load/reload methods for hot-reload pipeline

---

### Testing

- **SDK Unit Tests** — Comprehensive coverage for renderers, parsers, registry, variable resolution, theme tokens, navigation, screen APIs, export
- **Plugin Unit Tests** — 11+ tests covering all task registration, extension defaults, property resolution
- **Plugin Functional Tests** — 6+ TestKit-based tests verifying actual Gradle task execution
- **Auto-Export Tests** — `KetoyAutoExportTest` validates DSL → JSON round-trip for all registered screens
- **Total:** 42+ plugin tests + SDK test suite — all passing

---

### Architecture

**Three-Layer Stack:**

1. **DSL Layer** — Kotlin type-safe builders (`KText`, `KColumn`, etc.) → `KNode` tree
2. **Serialization Layer** — `KNode` ↔ JSON via kotlinx.serialization
3. **Rendering Layer** — JSON → native Jetpack Compose UI via `KetoyRenderer`

**Key Design Decisions:**

- Template-only export: DSL builds with template strings (`{{data:user:name}}`); variable values are resolved at render time, not export time
- Registration-based architecture: Screens, functions, variables, components, widgets, actions all use singleton registries
- Reactive variable system: Compose state-backed revision counter ensures UI recomposes when variable values change
- Hybrid navigation: Both type-safe (`KetoyRoute`) and string-based routes supported simultaneously
- Multi-content screens: A single `KetoyScreen` can contain multiple named content blocks, each independently overridable by dev server
- Three-tier property resolution: DSL → Gradle flags → local.properties → defaults

---

### License

Apache License, Version 2.0
