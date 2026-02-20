<p align="center">
  <h1 align="center">Ketoy</h1>
  <p align="center"><b>Server-Driven UI for Jetpack Compose</b></p>
  <p align="center">Build dynamic, real-time UIs from JSON — no app updates needed.</p>
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin&logoColor=white" alt="Kotlin"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4.svg?logo=android&logoColor=white" alt="Compose"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License"/></a>
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Android%208)-brightgreen.svg" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Server%20Driven-UI-orange.svg" alt="SDUI"/>
</p>

---

## Table of Contents

- [What is Ketoy?](#what-is-ketoy)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Modules Overview](#modules-overview)
    - [`ketoy-sdk` — Core SDUI Library](#ketoy-sdk--core-sdui-library)
    - [`ketoy-devtools` — Hot-Reload Android Library](#ketoy-devtools--hot-reload-android-library)
    - [`ketoy-devtools-server` — JVM Dev Server](#ketoy-devtools-server--jvm-dev-server)
    - [Gradle Plugin Tasks](#gradle-plugin-tasks)
- [Screen System](#screen-system)
- [Navigation](#navigation)
  - [Nav Graph Export & Live Reload](#navigation-graph-export--live-reload)
  - [Demo: Test Live Screen-to-Screen Navigation](#demo-test-live-screen-to-screen-navigation)
- [DSL Reference](#dsl-reference)
- [Custom Action IDs](#custom-action-ids)
- [Action System](#action-system)
- [Function Registry](#function-registry)
- [Custom Widgets](#custom-widgets)
- [Custom Compose Components](#custom-compose-components)
- [Cloud / Remote UI](#cloud--remote-ui)
- [Theme System](#theme-system)
- [DevTools & Hot Reload](#devtools--hot-reload)
- [Gradle CLI Commands](#gradle-cli-commands)
- [JSON Wire Format](#json-wire-format)
- [UI Samples](#ui-samples)
- [Contributing](#contributing)
- [License](#license)

---

## What is Ketoy?

Ketoy lets you define your Android UI using a type-safe **Kotlin DSL**, serialise it to **JSON**, and render it back into native **Jetpack Compose** components — all at runtime.

| Capability | Description |
|---|---|
| **Zero App Updates** | Ship UI changes from your server without a Play Store release |
| **Type-Safe DSL** | Full IDE auto-complete and compile-time checks |
| **Material 3** | Every component maps directly to Material 3 Compose widgets |
| **Full Navigation** | Type-safe + string-based routing with JSON navigate actions |
| **Custom Components** | Register your own Compose composables for SDUI rendering |
| **Custom Action IDs** | Assign named IDs to onClick handlers for server-side control |
| **Function Registry** | Register Kotlin functions and call them from SDUI JSON with arguments |
| **Cloud Sync** | Fetch & cache screen JSON from your API with 5 cache strategies |
| **Hot Reload** | Live-preview changes via the DevTools server |
| **A/B Testing** | Serve different JSON payloads to different user segments |
| **Lightweight** | Only `kotlinx-serialization` and `coil-compose` as transitive deps |

```
┌──────────────┐      JSON       ┌──────────────┐      Compose        ┌──────────┐
│  K-DSL Code  │ ──────────────▶ │  Ketoy SDK   │ ──────────────────▶ │  Screen  │
│  (or Server) │                 │  Renderer    │                     │  Pixels  │
└──────────────┘                 └──────────────┘                     └──────────┘
```

---

## Architecture

```
ketoy-sdk/                          # Core SDUI library
├── Ketoy.kt                       # Entry point — initialize(), reset()
├── annotation/
│   └── KComponent.kt              # @KComponent annotation
├── cloud/
│   ├── KetoyCloudService.kt       # Cloud fetch + 5 cache strategies
│   ├── KetoyCloudConfig.kt        # API configuration
│   ├── KetoyApiClient.kt          # HTTP client
│   └── KetoyCacheStore.kt         # Disk/memory cache
├── core/
│   ├── ActionRegistry.kt          # onClick / onValueChange callback registry
│   ├── KetoyJson.kt               # Global Json instance
│   ├── KetoyJsonUtils.kt          # toJson(), parseKetoyJson(), toEnhancedJson()
│   └── KetoyVariableRegistry.kt   # Template variable resolution
├── dsl/
│   ├── KUniversalScope.kt         # DSL scope — all component builder functions
│   ├── KLazyListScope.kt          # item {} / items {} for lazy lists
│   ├── KScaffoldScopes.kt         # Scaffold, AppBar, Navigation scopes
│   ├── KTextFieldScope.kt         # TextField slot scopes
│   └── TopLevelBuilders.kt        # Top-level KColumn / KRow / KBox / KScaffold
├── model/                         # Data classes for every UI element
├── navigation/
│   ├── KetoyNavController.kt      # NavHostController wrapper
│   ├── KetoyNavHost.kt            # Navigation host composable
│   ├── KetoyNavGraph.kt           # Serializable nav graph model + registry
│   ├── KetoyNavDevOverrides.kt    # Live-reloadable nav graph overrides
│   ├── KetoyComposableRegistry.kt # Route → @Composable destination registry
│   ├── KetoyNavigator.kt          # Static navigation action builder
│   ├── KetoyNavigationExecutor.kt # Executes navigation actions
│   ├── KetoyRoute.kt              # Type-safe route marker interface
│   └── NavigationModels.kt        # NavigationStyle, KNavigateAction
├── parser/                        # JSON → Compose converters
├── registry/
│   ├── KComponentRegistry.kt      # Custom component registry
│   └── KetoyFunctionRegistry.kt   # Register & call Kotlin functions from JSON
├── renderer/
│   ├── KetoyRenderer.kt           # Central JSON → Composable dispatch
│   ├── LayoutRenderer.kt          # Column, Row, Box, Lazy lists
│   ├── WidgetRenderer.kt          # Text, Button, Spacer, Card, Image
│   ├── ScaffoldRenderer.kt        # Scaffold + sub-components
│   ├── TextFieldRenderer.kt       # TextField renderer
│   ├── ComponentRenderer.kt       # Custom component renderer
│   └── OnClickResolver.kt         # Unified onClick → callback resolver
├── screen/
│   ├── KetoyScreen.kt             # Screen class with multi-content support & 7-step resolution
│   ├── KetoyScreenRegistry.kt     # Global screen registry
│   ├── KetoyScreenAnnotations.kt  # @KScreen annotation with name param
│   └── KetoyContent.kt            # KetoyContent composable (name-based child)
├── theme/
│   └── KetoyTheme.kt              # Theme modes, colour schemes, provider
├── util/                          # Constants, colours, shapes, icons, helpers
└── widget/
    ├── KetoyWidgetParser.kt        # Custom widget parser interface
    ├── KetoyWidgetRegistry.kt      # Widget parser registry
    ├── KetoyActionParser.kt        # Action parser interface + ActionContext
    ├── KetoyActionRegistry.kt      # Action parser registry
    └── builtin/
        ├── NavigateActionParser.kt # Built-in "navigate" action parser
        └── CallFunctionActionParser.kt # Built-in "callFunction" action parser

ketoy-devtools/                     # Hot-reload Android library
├── KetoyDevClient.kt              # WebSocket connection to dev server
├── KetoyDevWrapper.kt             # Composable wrapper injecting live JSON
├── KetoyDevActivity.kt            # Dev-tools activity
├── KetoyDevConnectScreen.kt       # Connection UI with QR scan
├── KetoyDevConfig.kt              # Dev configuration
├── KetoyDevStorage.kt             # Persistent settings store
├── KetoyDevOverlay.kt             # Debug overlay
└── KetoyDevExporter.kt            # JSON exporter

ketoy-devtools-server/              # JVM dev server
├── Main.kt                        # CLI entry point
├── KetoyDevServer.kt              # HTTP + WebSocket server
├── FileWatcher.kt                 # JSON file watcher
├── SourceWatcher.kt               # Kotlin source watcher
├── ScreenManager.kt               # Screen JSON management
├── NetworkUtils.kt                # IP / port utilities
└── QRCodeGenerator.kt             # QR code for device pairing
```

---

## Getting Started

### Modules Overview

Ketoy is organised into four modules, each serving a specific role in the SDUI pipeline:

#### `ketoy-sdk` — Core SDUI Library

The main library that powers server-driven UI. Includes:

| Package | Purpose |
|---------|---------|
| `model/` | Serializable data classes for every UI element (`KNode`, `KModifier`, `KProps`, etc.) |
| `dsl/` | Type-safe Kotlin DSL for building UI trees (`KColumn`, `KRow`, `KText`, `KButton`, etc.) |
| `renderer/` | `@Composable` functions that convert `KNode` trees into native Jetpack Compose UI |
| `parser/` | JSON value → Compose type converters (colours, shapes, arrangements, modifiers) |
| `navigation/` | Type-safe + string-based routing, nav graph export, live-reloadable navigation |
| `cloud/` | Remote screen fetching with 5 cache strategies, cloud nav graph sync |
| `screen/` | Screen resolution chain (7 sources), registry, annotations, multi-content support |
| `widget/` | Extensible widget/action parser system for custom components |
| `registry/` | Component registry, function registry for server-callable Kotlin functions |
| `core/` | Action registry, variable registry with template resolution, JSON utilities |
| `theme/` | Material 3 theme system with 28 colour tokens |
| `export/` | Production export pipeline for screens and navigation manifests |
| `util/` | Constants, colour palette, icon helpers, shape factories, modifier builders |
| `annotation/` | `@KComponent` and `@KScreen` annotations for discoverability |

**Dependency:** `kotlinx-serialization-json`, `coil-compose`, `navigation-compose`, Material Icons

#### `ketoy-devtools` — Hot-Reload Android Library

Debug-only Android library that enables live UI preview during development. Include it as `debugImplementation` so it's stripped from release builds.

| Class | Purpose |
|-------|---------|
| `KetoyDevWrapper` | Composable wrapper that intercepts screen rendering and injects live JSON from the dev server |
| `KetoyDevClient` | WebSocket client that connects to `ketoy-devtools-server` and receives real-time updates |
| `KetoyDevActivity` | Standalone activity for dev tools (connection management, screen list) |
| `KetoyDevConnectScreen` | QR code scanner + manual IP entry UI for pairing with the dev server |
| `KetoyDevOverlay` | Debug overlay showing connection status, screen name, and render timing |
| `KetoyDevExporter` | Exports registered `@KScreen` composables to JSON for the dev server |
| `KetoyDevStorage` | `SharedPreferences`-backed storage for dev server connection settings |
| `KetoyDevConfig` | Configuration for dev server host, port, and auto-connect behaviour |

**Dependency:** `ketoy-sdk`, `okhttp3` (WebSocket), `lifecycle-process`

#### `ketoy-devtools-server` — JVM Dev Server

A pure Kotlin/JVM application (no Android dependencies) that runs on your development machine. It serves screen JSON via HTTP + WebSocket and pushes updates to connected devices in real time.

| Class | Purpose |
|-------|---------|
| `Main.kt` | CLI entry point — parses `--port`, `--watch`, `--auto-export` flags |
| `KetoyDevServer` | Combined HTTP + WebSocket server. Serves screen JSON and pushes live updates |
| `FileWatcher` | Monitors `ketoy-screens/` for `.json` file changes and triggers broadcasts |
| `SourceWatcher` | Monitors Kotlin source files for `@KScreen` changes and triggers re-export + broadcast |
| `ScreenManager` | In-memory store for screen JSON and navigation graphs, with versioning |
| `NetworkUtils` | Local IP address detection for device pairing |

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/screens` | List all available screens |
| `GET` | `/screens/{name}` | Get a specific screen's JSON |
| `GET` | `/bundle` | Get all screens + nav graphs bundled |
| `GET` | `/status` | Server health check |
| `GET` | `/` | Dashboard HTML page |
| `WS` | `/ws` | WebSocket for real-time push updates |

**Dependency:** `kotlinx-coroutines-core`, `Java-WebSocket`

#### Gradle Plugin Tasks

Ketoy registers custom Gradle tasks in the root `build.gradle.kts` under the `ketoy` group. These tasks handle the full lifecycle of screen management — from local export to cloud deployment.

| Task | Description |
|------|-------------|
| `ketoyDev` | Start dev server with auto-export (edit DSL → live app update) |
| `ketoyServe` | Start the dev server only (watches JSON files) |
| `ketoyExport` | Export `@KScreen` DSL screens to `ketoy-screens/` JSON files |
| `ketoyExportProd` | Export production-ready screens + navigation to `ketoy-export/` |
| `ketoyPush` | Upload a single screen JSON to the Ketoy cloud server |
| `ketoyPushAll` | Upload all screens from `ketoy-screens/` at once |
| `ketoyListScreens` | List all screens deployed for this app on the cloud |
| `ketoyScreenVersions` | List all versions of a specific screen |
| `ketoyScreenDetails` | Get full details of a screen including JSON content |
| `ketoyRollback` | Rollback a screen to a previous version |
| `ketoyDeleteScreen` | Delete a screen and all its versions from the cloud |

Run `./gradlew tasks --group=ketoy` to see all available tasks.

---

### 1. Add the SDK module

```kotlin
// settings.gradle.kts
include(":ketoy-sdk")

// app/build.gradle.kts
dependencies {
    implementation(project(":ketoy-sdk"))
}
```

### 2. Initialise

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Ketoy.initialize()
    }
}
```

### 3. Build → Serialise → Render

```kotlin
// Build UI with the DSL
val ui = KColumn(
    modifier = kModifier(fillMaxSize = 1f, background = "#F5F5F5"),
    verticalArrangement = KArrangements.Center,
    horizontalAlignment = KAlignments.CenterHorizontally
) {
    KText("Hello, Ketoy!", fontSize = 24, fontWeight = KFontWeights.Bold)
    KSpacer(height = 16)
    KButton(containerColor = KColors.Blue) {
        KText("Tap Me", color = KColors.White)
    }
}

// Serialise to JSON (send to / receive from server)
val json = ui.toJson()

// Render
@Composable
fun MyScreen() {
    JSONStringToUI(value = json)
}
```

---

## Screen System

Ketoy screens provide a unified content resolution chain. Each screen tries 7 sources in order:

1. **Dev-server override** (hot-reload JSON from DevTools)
2. **Cloud JSON** (fetched from your API)
3. **Local JSON string** (embedded in the screen)
4. **Asset file** (loaded from `assets/`)
5. **Composable lambda** (native Compose UI)
6. **DSL / KNode builder** (built with the Ketoy DSL)
7. **Empty placeholder**

### Screen Metadata

Screen metadata (name, display name, description, version) lives on `KetoyScreen`, **not** on individual content blocks. A single `KetoyScreen` can hold **multiple `KetoyContent` blocks**, each identified by a `name`. `KetoyContent` is a lightweight child that can be freely interleaved with native Jetpack Compose code.

### Creating Screens

```kotlin
// From DSL builder (with metadata)
val homeScreen = KetoyScreen.create(
    screenName = "home",
    displayName = "Home",
    description = "Main landing screen",
    version = "1.0.0"
) {
    KColumn {
        KText("Welcome Home", fontSize = 24)
    }
}

// From a raw JSON string
val dynamicScreen = KetoyScreen.fromJson("profile", jsonString)

// From an asset file
val assetScreen = KetoyScreen.fromAsset("settings", "screens/settings.json")

// From a Compose lambda
val nativeScreen = KetoyScreen.fromComposable("about") {
    AboutPageComposable()
}

// Multi-content: add extra content blocks
val dashboard = KetoyScreen(
    screenName = "dashboard",
    displayName = "Dashboard",
    description = "Overview with header and body"
).addContent(name = "header", nodeBuilder = { buildDashboardHeader() })
 .addContent(name = "body",   nodeBuilder = { buildDashboardBody() })
```

### Using `@KScreen` + `KetoyContent`

The recommended approach for app screens. The `@KScreen` annotation marks a composable as a Ketoy screen, and `KetoyContent` provides the DSL content block(s). Screen-level settings (`screenName`, `cloudEnabled`, `colorScheme`) live on `@KScreen`, while each `KetoyContent` is a lightweight child that can be freely interleaved with native Jetpack Compose code.

#### Single content (default `"main"`)

```kotlin
@KScreen(name = "home")
@Composable
fun HomeScreen() {
    KetoyContent(
        nodeBuilder = { buildHomeUI() }
    )
}

fun buildHomeUI() = KColumn {
    KText("Hello from DSL!")
}
```

#### Mixed Compose + DSL (multiple KetoyContent blocks)

```kotlin
@KScreen(name = "home")
@Composable
fun HomeScreen() {
    Column {
        // DSL section — hot-reloadable from dev-server / cloud
        KetoyContent(name = "cards", nodeBuilder = { buildCards() })

        // Native Compose section — not managed by Ketoy
        Text("Expenses: $2,150.00")

        // Another DSL section
        KetoyContent(name = "transactions", nodeBuilder = { buildTransactions() })

        // Native Compose buttons
        Button(onClick = {}) { Text("Add Money") }
    }
}
```

When exported, the screen produces a single JSON file with all `KetoyContent` blocks wrapped by name:

```json
{
  "screenName": "home",
  "displayName": "Home",
  "version": "1.0.0",
  "contents": {
    "cards": { "type": "Column", "children": [...] },
    "transactions": { "type": "Column", "children": [...] }
  }
}
```

### Screen Registry

```kotlin
// Manual registration
KetoyScreenRegistry.register(myScreen)

// Bulk from assets
KetoyScreenRegistry.registerFromAssets(mapOf(
    "home" to "screens/home.json",
    "profile" to "screens/profile.json"
))

// During initialization
Ketoy.initialize(
    screens = listOf(homeScreen, profileScreen)
)
```

### Screen DSL

Register screens with metadata using the `ketoyScreens` DSL:

```kotlin
ketoyScreens {
    screen("home") {
        displayName("Home")
        description("Main landing screen")
        version("2.0.0")
        fromJson(homeJson)
    }
    screen("profile") {
        dsl { KColumn { KText("Profile") } }
    }
}
```

---

## Navigation

Ketoy supports **two navigation styles** that can be used together:

### Type-Safe Routes (Compose Navigation 2-style)

```kotlin
@Serializable data object HomeRoute : KetoyRoute
@Serializable data class DetailRoute(val id: String) : KetoyRoute

KetoyNavHost(startRoute = HomeRoute) {
    screen<HomeRoute> { HomeScreen() }
    screen<DetailRoute> { route -> DetailScreen(id = route.id) }
}
```

### String-Based Routes (JSON-driven)

```kotlin
KetoyNavHost(startRoute = "home")
// All screens from KetoyScreenRegistry are auto-registered
```

### Navigating Programmatically

```kotlin
// Access nav controller from any composable inside KetoyNavHost
val navController = LocalKetoyNavController.current

// Type-safe
navController.navigate(DetailRoute(id = "123"))
navController.navigateAndReplace(HomeRoute)
navController.navigateAndClearBackStack(LoginRoute)

// String-based
navController.navigateToRoute("detail", mapOf("id" to "123"))
navController.popBackStack()
navController.popToRoot()
```

### JSON Navigate Actions

Navigation works from server-driven JSON — buttons can trigger navigation without app updates:

```json
{
    "type": "Button",
    "props": {
        "onClick": {
            "actionType": "navigate",
            "routeName": "detail_screen",
            "navigationStyle": "push",
            "arguments": { "id": "123" }
        }
    },
    "children": [
        { "type": "Text", "props": { "text": "Go to Detail" } }
    ]
}
```

Supported `navigationStyle` values:

| Style | Description |
|---|---|
| `navigate` / `push` | Standard forward navigation |
| `navigateAndReplace` / `pushReplacement` | Replace current screen |
| `navigateAndClearBackStack` / `pushAndRemoveAll` | Clear entire back stack |
| `popBackStack` / `pop` | Go back |
| `popToRoot` / `popAll` | Pop to the root screen |

### Building Navigation Actions in Kotlin

```kotlin
// Navigate to a registered screen
val action = KetoyNavigator.navigateToScreen("detail", args = mapOf("id" to "42"))

// Navigate to inline JSON
val action = KetoyNavigator.navigateToJson(jsonString, style = NavigationStyle.NavigateAndReplace)

// Pop back
val action = KetoyNavigator.popBackStack(result = mapOf("status" to "done"))
```

### Navigation Graph Export & Live Reload

Ketoy can export your app's navigation graph to JSON and live-reload it via the dev server. This lets you **reorder, add, or remove destinations** (tabs, bottom-bar items) without recompiling.

#### How it works

```
ExportScreensTest            ketoy-screens/          Dev Server         App
     │                            │                      │                │
     │── export nav graph  ──────▶│ nav_main.json        │                │
     │                            │                      │                │
     │                            │──── file watcher ───▶│                │
     │                            │                      │── WebSocket ──▶│
     │                            │                      │   nav_update   │── update bottom bar
```

1. **Define** routes in `app/.../navigation/AppNavigation.kt`
2. **Define** the nav graph in `ExportScreensTest`
3. **Export** via `./gradlew ketoyExport`
4. **Serve** via `./gradlew ketoyDev` (or `./ketoy-serve.sh`)
5. **Connect** your app to the dev server
6. **Edit** `ketoy-screens/nav_main.json` → changes appear instantly

#### Step 1 — Define Type-Safe Routes

In `app/src/main/java/.../navigation/AppNavigation.kt`:

```kotlin
@Serializable data object HomeRoute : KetoyRoute
@Serializable data object AnalyticsRoute : KetoyRoute
@Serializable data object CardsRoute : KetoyRoute
@Serializable data object HistoryRoute : KetoyRoute
@Serializable data object ProfileRoute : KetoyRoute

// Bottom nav item descriptor (UI metadata)
data class BottomNavItem<T : KetoyRoute>(
    val route: T,
    val label: String,
    val icon: KIconRef,
    val selectedIcon: KIconRef,
)

val bottomNavItems = listOf(
    BottomNavItem(HomeRoute, "Home", KIcons.Outlined.Home, KIcons.Filled.Home),
    BottomNavItem(AnalyticsRoute, "Analytics", KIcons.Outlined.Insights, KIcons.Filled.Insights),
    BottomNavItem(CardsRoute, "Cards", KIcons.Outlined.CreditCard, KIcons.Filled.CreditCard),
    BottomNavItem(HistoryRoute, "History", KIcons.Outlined.Schedule, KIcons.Filled.Schedule),
    BottomNavItem(ProfileRoute, "Profile", KIcons.Outlined.Person, KIcons.Filled.Person),
)
```

#### Step 2 — Define the Nav Graph for Export

In `ExportScreensTest.kt`, add a nav graph definition alongside your screen exports:

```kotlin
private val navGraphs = listOf(
    KetoyNavGraph(
        navHostName = "main",        // must match KetoyNavHost(navHostName = "main")
        startRoute = "home",
        destinations = listOf(
            KetoyNavDestination(
                route = "home",             // string key for this destination
                screenName = "home",        // matches ProvideKetoyScreen(screenName = ...)
                label = "Home",
                icon = "home",              // icon name from KIcons
                selectedIcon = "home",
                isStartDestination = true
            ),
            KetoyNavDestination(
                route = "analytics", screenName = "analytics",
                label = "Analytics", icon = "insights", selectedIcon = "insights"
            ),
            KetoyNavDestination(
                route = "cards", screenName = "cards",
                label = "Cards", icon = "credit_card", selectedIcon = "credit_card"
            ),
            KetoyNavDestination(
                route = "history", screenName = "history_screen",
                label = "History", icon = "schedule", selectedIcon = "schedule"
            ),
            KetoyNavDestination(
                route = "profile", screenName = "profile",
                label = "Profile", icon = "person", selectedIcon = "person"
            ),
        )
    )
)
```

#### Step 3 — Wire KetoyNavHost

In your `MainActivity`, use `KetoyNavHost` with the `navHostName` parameter:

```kotlin
KetoyNavHost(
    startRoute = HomeRoute,
    navHostName = "main",       // links to nav_main.json
    modifier = Modifier.fillMaxSize().padding(innerPadding),
    navController = navController
) {
    screen<HomeRoute> { HomeScreen(...) }
    screen<AnalyticsRoute> { AnalyticsScreen(...) }
    screen<CardsRoute> { CardsScreen(...) }
    screen<HistoryRoute> { HistoryScreen(...) }
    screen<ProfileRoute> { ProfileScreen(...) }
}
```

#### Step 4 — Bottom Bar with Nav Override Support

The bottom bar reads from `KetoyNavDevOverrides` when connected to the dev server, falling back to hardcoded items otherwise:

```kotlin
val navOverride = KetoyNavDevOverrides.overrides["main"]

NavigationBar {
    val navDests = navOverride?.destinations
    if (navDests != null && navDests.isNotEmpty()) {
        // Live-reload path: render from nav_main.json
        navDests.forEach { dest ->
            val selected = backStackEntry?.destination
                ?.isRouteSelected(dest.route) ?: false
            NavigationBarItem(
                selected = selected,
                onClick = {
                    resolveTypeSafeRoute(dest.route)?.let { route ->
                        navController.navigate(route) { ... }
                    }
                },
                icon = { resolveIcon(dest.icon)?.let { Icon(it, dest.label) } },
                label = { Text(dest.label) }
            )
        }
    } else {
        // Compile-time path: hardcoded bottomNavItems
        bottomNavItems.forEach { item -> ... }
    }
}
```

#### Step 5 — Route Resolution Helpers

Add these to your `MainActivity.kt` to map override route strings ↔ type-safe routes:

```kotlin
/** Map nav-override route string → type-safe route object. */
private fun resolveTypeSafeRoute(route: String): Any? = when (route) {
    "home" -> HomeRoute
    "analytics" -> AnalyticsRoute
    "cards" -> CardsRoute
    "history" -> HistoryRoute
    "profile" -> ProfileRoute
    else -> null
}

/** Check if a NavDestination matches an override route string. */
private fun NavDestination.isRouteSelected(route: String): Boolean = when (route) {
    "home" -> hasRoute<HomeRoute>()
    "analytics" -> hasRoute<AnalyticsRoute>()
    "cards" -> hasRoute<CardsRoute>()
    "history" -> hasRoute<HistoryRoute>()
    "profile" -> hasRoute<ProfileRoute>()
    else -> false
}
```

> **Adding a new route?** Add entries in all three places:
> 1. `AppNavigation.kt` — `@Serializable data object NewRoute : KetoyRoute` + add to `bottomNavItems`
> 2. `ExportScreensTest` — add a `KetoyNavDestination` to the nav graph
> 3. `MainActivity.kt` — add cases to `resolveTypeSafeRoute()` and `isRouteSelected()`

#### The nav_main.json Format

After running `./gradlew ketoyExport`, the file `ketoy-screens/nav_main.json` is generated. Edit it live while the dev server is running:

```json
{
    "navHostName": "main",
    "startRoute": "home",
    "destinations": [
        {
            "route": "home",
            "screenName": "home",
            "label": "Home",
            "icon": "home",
            "selectedIcon": "home",
            "isStartDestination": true
        }
    ]
}
```

| Field | Description |
|---|---|
| `navHostName` | Must match the `navHostName` param on `KetoyNavHost` |
| `startRoute` | Default start destination route string |
| `route` | Unique route key used for bottom-bar mapping |
| `screenName` | Matches `ProvideKetoyScreen(screenName = ...)` for hot-reload |
| `label` | Display text in the bottom bar |
| `icon` / `selectedIcon` | Icon name from `KIcons` (e.g. `"home"`, `"person"`, `"credit_card"`) |
| `isStartDestination` | Mark one destination as the start route |

#### Quick Reference

| Task | Command / Location |
|---|---|
| Define routes | `app/.../navigation/AppNavigation.kt` |
| Define nav graph for export | `ExportScreensTest.kt` → `navGraphs` list |
| Export nav JSON | `./gradlew ketoyExport` |
| Live-serve nav JSON | `./gradlew ketoyDev` |
| Edit nav live | Modify `ketoy-screens/nav_main.json` while server runs |
| Multiple nav hosts | Add more `KetoyNavGraph` entries with different `navHostName` |

### Demo: Test Live Screen-to-Screen Navigation

The **Nav Demo** demonstrates Ketoy's key navigation feature: **define the nav graph in JSON** while the **screen content is native Compose**. It uses `KetoyComposableRegistry` to map JSON route strings to real `@Composable` screen functions, and `KetoyNavHost` to render them with full screen-to-screen navigation.

#### How it works

1. **4 Compose screens** (`DemoExploreScreen`, `DemoFavoritesScreen`, `DemoNotificationsScreen`, `DemoSettingsScreen`) are proper full-page Compose UIs with navigation buttons.
2. **`KetoyComposableRegistry`** maps route strings (`"explore"`, `"favorites"`, etc.) to those Compose functions.
3. **`KetoyNavHost(navHostName = "demo")`** reads `nav_demo.json` via `KetoyNavDevOverrides["demo"]` and resolves each destination to the registered composable.
4. Screens navigate to each other using `LocalKetoyNavController.current?.navigateToRoute("route")`.

#### Quick start

```bash
# 1. Export screens + nav graphs
./gradlew ketoyExport

# 2. Start the dev server
./gradlew ketoyDev

# 3. Run the app, connect to the dev server
# 4. Open the side drawer → tap "Nav Demo"
```

You'll see the Explore screen with navigation cards leading to Favorites, Notifications, and Settings. Now edit `ketoy-screens/nav_demo.json` while the server is running:

#### Things to try

| Change | What to edit in `nav_demo.json` |
|---|---|
| **Change start screen** | Change `"startRoute": "explore"` → `"startRoute": "favorites"` |
| **Remove a destination** | Delete one destination object from the `destinations` array |
| **Add a destination** | Add a new destination with a registered route |
| **Rename a screen** | Change `"label": "Settings"` → `"label": "Preferences"` |

Each change appears instantly in the running app — no rebuild needed.

#### Composable Destination Registry

Register native Compose screens for JSON-defined routes:

```kotlin
// Register composable destinations
KetoyComposableRegistry.register("explore") { ExploreScreen() }
KetoyComposableRegistry.register("favorites") { FavoritesScreen() }

// nav_demo.json can now reference these routes:
// { "route": "explore", "screenName": "explore", "label": "Explore", ... }

// KetoyNavHost resolves destinations in this order:
// 1. KetoyComposableRegistry (native Compose screens)
// 2. KetoyScreenRegistry (JSON-rendered SDUI screens)
// 3. Fallback
```

#### Data-Driven Navigation with `LocalKetoyNavGraph`

Composable screens can read the active nav graph via `LocalKetoyNavGraph` — this makes navigation targets live-editable from JSON instead of hardcoded in Kotlin:

```kotlin
@Composable
fun MyHubScreen() {
    val nav = LocalKetoyNavController.current
    val navGraph = LocalKetoyNavGraph.current
    val destinations = navGraph?.destinations?.filter { !it.isStartDestination } ?: emptyList()

    // Navigation cards built from JSON — edit nav graph, UI updates live
    destinations.forEach { dest ->
        Button(onClick = { nav?.navigateToRoute(dest.route) }) {
            Text(dest.label)
        }
    }
}
```

This is how the demo Explore screen works: zero hardcoded route strings, all navigation targets come from `nav_demo.json`.

#### Where the code lives

| File | Purpose |
|---|---|
| `ketoy-sdk/.../KetoyComposableRegistry.kt` | Maps route strings to `@Composable` functions |
| `app/.../screens/demo/DemoScreens.kt` | 4 Compose screens — Explore reads `LocalKetoyNavGraph` for data-driven nav |
| `app/.../screens/DemoNavScreen.kt` | Hosts `KetoyNavHost(navHostName = "demo")` + registers composables |
| `app/.../navigation/AppNavigation.kt` | `DemoNavRoute` — route to reach the demo screen |
| `ExportScreensTest.kt` | Exports `nav_demo.json` (the `"demo"` nav graph) |
| `ketoy-screens/nav_demo.json` | The file you edit for live changes |

---

## DSL Reference

### Layout Components

<details>
<summary><b>KColumn</b> — Vertical layout</summary>

```kotlin
KColumn(
    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)),
    verticalArrangement = KArrangements.SpaceBetween,
    horizontalAlignment = KAlignments.CenterHorizontally
) {
    KText("First")
    KText("Second")
    KText("Third")
}
```
</details>

<details>
<summary><b>KRow</b> — Horizontal layout</summary>

```kotlin
KRow(
    modifier = kModifier(fillMaxWidth = 1f),
    horizontalArrangement = KArrangements.SpaceBetween,
    verticalAlignment = KAlignments.CenterVertically
) {
    KText("Left")
    KText("Right")
}
```
</details>

<details>
<summary><b>KBox</b> — Stacked / overlapping layout</summary>

```kotlin
KBox(
    modifier = kModifier(size = 200, background = "#EEEEEE"),
    contentAlignment = KAlignments.Center
) {
    KText("Centered in box")
}
```
</details>

<details>
<summary><b>KLazyColumn / KLazyRow</b> — Scrolling lists</summary>

```kotlin
KLazyColumn(
    modifier = kModifier(fillMaxSize = 1f),
    verticalArrangement = "spacedBy_8"
) {
    items(dataList) { item ->
        KCard(modifier = kModifier(fillMaxWidth = 1f), elevation = 2) {
            KText(item.title, fontSize = 16, fontWeight = KFontWeights.Bold)
        }
    }
}
```
</details>

### Widget Components

<details>
<summary><b>KText</b> — Text display</summary>

```kotlin
KText(
    text = "Hello World",
    fontSize = 18,
    fontWeight = KFontWeights.Bold,
    color = "#333333",
    textAlign = KTextAlign.Center,
    maxLines = 2,
    letterSpacing = 0.5f
)
```
</details>

<details>
<summary><b>KButton</b> — Clickable button</summary>

```kotlin
KButton(
    onClick = { /* handle click */ },
    containerColor = KColors.Blue,
    contentColor = KColors.White,
    shape = KShapes.Rounded12,
    elevation = 4,
    actionId = "submit_order"  // Optional: custom action ID for server-side control
) {
    KText("Submit", fontWeight = KFontWeights.Bold)
}
```
</details>

<details>
<summary><b>KCard</b> — Material card container</summary>

```kotlin
KCard(
    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 12)),
    containerColor = "#FFFFFF",
    shape = KShapes.Rounded16,
    elevation = 4,
    border = kBorder(1, "#E0E0E0"),
    onClick = { /* handle tap */ },
    actionId = "product_card"  // Optional: custom action ID
) {
    KText("Card content", fontSize = 14)
}
```
</details>

<details>
<summary><b>KImage</b> — Async image loading</summary>

```kotlin
// From URL
KImage(
    source = kImageUrl("https://example.com/photo.jpg"),
    modifier = kModifier(size = 120, shape = KShapes.Circle),
    contentDescription = "Profile photo",
    scaleType = KScaleType.CenterCrop
)

// From drawable resource
KImage(source = kImageRes("ic_logo"))

// Material icon
KImage(source = kImageIcon("Home", KIcons.STYLE_FILLED))
```
</details>

<details>
<summary><b>KIcon / KIconButton</b> — Material icons</summary>

```kotlin
// Standalone icon
KIcon(icon = KIcons.Filled.Home, size = 24, color = KColors.Blue)

// Icon button with custom action ID
KIconButton(
    icon = KIcons.Outlined.Favorite,
    onClick = { toggleFavorite() },
    actionId = "favorite_toggle",
    iconColor = KColors.Red
)
```
</details>

<details>
<summary><b>KTextField</b> — Text input</summary>

```kotlin
KTextField(
    value = "current text",
    onValueChange = { newText -> /* handle */ },
    modifier = kModifier(fillMaxWidth = 1f),
    singleLine = true,
    keyboardOptions = KKeyboardOptions(keyboardType = "email", imeAction = "done")
) {
    label { KText("Email") }
    placeholder { KText("you@example.com") }
    leadingIcon { KIcon(icon = KIcons.Filled.Email) }
}
```
</details>

<details>
<summary><b>KSpacer</b> — Empty space</summary>

```kotlin
KSpacer(height = 24)
KSpacer(width = 16)
KSpacer(modifier = kModifier(weight = 1f))
```
</details>

### Scaffold Components

<details>
<summary><b>KScaffold</b> — Full screen scaffold</summary>

```kotlin
KScaffold(
    containerColor = "#FFFFFF",
    topBar = {
        KTopAppBar(
            type = KTopAppBarType.CenterAligned,
            title = { KText("My App", fontWeight = KFontWeights.Bold) },
            navigationIcon = {
                KIconButton(icon = KIcons.Filled.ArrowBack, onClick = { navBack() })
            },
            actions = {
                KAppBarAction(onClick = { openSettings() }) {
                    KIcon(icon = KIcons.Filled.Settings)
                }
            }
        )
    },
    bottomBar = {
        KNavigationBar {
            KNavigationBarItem(
                selected = true,
                onClick = { navigateTo("home") },
                actionId = "nav_home",
                icon = { KIcon(icon = KIcons.Outlined.Home) },
                selectedIcon = { KIcon(icon = KIcons.Filled.Home) },
                label = { KText("Home") }
            )
        }
    },
    floatingActionButton = {
        KFloatingActionButton(onClick = { addItem() }, actionId = "add_item") {
            KIcon(icon = KIcons.Filled.Add, color = KColors.White)
        }
    }
) {
    // Body content
    KColumn(modifier = kModifier(padding = kPadding(all = 16))) {
        KText("Page content goes here")
    }
}
```
</details>

### Modifier System

<details>
<summary><b>kModifier()</b> — Complete modifier reference</summary>

```kotlin
kModifier(
    // Sizing
    fillMaxSize = 1f,
    fillMaxWidth = 1f,
    fillMaxHeight = 1f,
    weight = 0.5f,
    size = 100,
    width = 200,
    height = 150,

    // Spacing
    padding = kPadding(all = 16),
    margin = kMargin(horizontal = 8, vertical = 4),

    // Appearance
    background = "#FF5722",
    gradient = KGradients.linear(listOf("#FF5722", "#E91E63")),
    border = kBorder(width = 2, color = "#000000"),
    shape = KShapes.Rounded16,
    cornerRadius = 12,

    // Effects
    shadow = kShadow(elevation = 8, color = "#33000000"),
    alpha = 0.8f,
    scale = 1.2f,
    rotation = 45f,

    // Scroll
    verticalScroll = true,
    horizontalScroll = true,

    // Interaction
    clickable = true
)
```
</details>

<details>
<summary><b>Constants & Utilities</b></summary>

```kotlin
// Arrangements
KArrangements.Center / .SpaceBetween / .SpaceEvenly / .SpaceAround / .Top / .Bottom / .Start / .End
// Custom spacing: "spacedBy_8", "spacedBy_16"

// Alignments
KAlignments.Center / .CenterHorizontally / .CenterVertically / .TopStart / .BottomEnd / ...

// Colours
KColors.Blue / .Red / .Green / .Orange / .Purple / .Teal / .Black / .White / .Transparent
KColors.hex("FF5722")
KColors.withAlpha(KColors.Blue, 0.5f)

// Shapes
KShapes.Circle / .Rectangle / .Rounded4 / .Rounded8 / .Rounded12 / .Rounded16 / .Rounded20 / .Rounded24
KShapes.rounded(18)
KShapes.rounded(topStart = 24, topEnd = 24, bottomEnd = 0, bottomStart = 0)

// Font weights
KFontWeights.Light / .Normal / .Medium / .SemiBold / .Bold

// Gradients
KGradients.linear(listOf("#FF5722", "#E91E63"))
KGradients.linear(listOf("#4ECDC4", "#556270"), KGradients.Directions.Horizontal)
KGradients.radial(listOf("#FFFFFF", "#000000"), radius = 200f)
KGradients.sweep(listOf("#FF0000", "#00FF00", "#0000FF"))
```
</details>

### Dynamic / Conditional

```kotlin
KColumn {
    KIf(user.isPremium) {
        KText("Premium Badge", color = KColors.Purple)
    }

    KForEach(items) { item ->
        KCard { KText(item.name) }
    }

    KRepeat(5) { index ->
        KText("Item #$index")
    }
}
```

---

## Custom Action IDs

By default, `KButton`, `KIconButton`, `KCard`, `KFloatingActionButton`, and other interactive components auto-generate action IDs like `"action_0"`, `"action_1"`, etc. These IDs appear in the serialised JSON.

You can assign **custom action IDs** so that server-side JSON can reference specific buttons by name. This enables changing button behaviour from the server without changing the app code.

### DSL Usage

```kotlin
KButton(
    onClick = { placeOrder() },
    actionId = "place_order"   // <── Custom ID
) {
    KText("Place Order")
}
```

### Serialised JSON

```json
{
    "type": "Button",
    "props": {
        "onClick": "place_order"
    },
    "children": [
        { "type": "Text", "props": { "text": "Place Order" } }
    ]
}
```

### Server-Side Override

Your server can change the action by sending a different JSON action:

```json
{
    "type": "Button",
    "props": {
        "onClick": {
            "actionType": "navigate",
            "routeName": "checkout_v2"
        }
    },
    "children": [
        { "type": "Text", "props": { "text": "Place Order" } }
    ]
}
```

### Components with `actionId` Support

| DSL Function | Default Behaviour |
|---|---|
| `KButton` | Auto `"action_N"` |
| `KIconButton` | Auto `"action_N"` |
| `KCard` | Auto `"action_N"` |
| `KFloatingActionButton` | Auto `"action_N"` |
| `KModalBottomSheet` | Auto `"action_N"` (for `onDismissRequest`) |
| `KAppBarAction` | Auto `"action_N"` |
| `KNavigationBarItem` | Auto `"action_N"` |
| `KNavigationDrawerItem` | Auto `"action_N"` |
| `KCustomNavigationItem` | Auto `"action_N"` |
| `KNavigationRailItem` | Auto `"action_N"` |
| `KEnum` | Auto `"action_N"` (for `onSelectionChange`) |

All accept an optional `actionId: String? = null` parameter to override the auto-generated ID.

---

## Action System

Ketoy provides a two-tier action system:

### 1. Legacy Action IDs (DSL)

When building UI with the Kotlin DSL, `onClick` lambdas are registered in `ActionRegistry` and assigned IDs. The renderer looks them up by ID at interaction time.

### 2. JSON Action Objects (Server-Driven)

Server-driven JSON can include structured action definitions:

```json
{
    "onClick": {
        "actionType": "navigate",
        "routeName": "profile"
    }
}
```

```json
{
    "onClick": {
        "actionType": "callFunction",
        "functionName": "addToCart",
        "arguments": { "itemId": "SKU-123", "quantity": 1 }
    }
}
```

### Built-in Action Types

| `actionType` | Parameters | Description |
|---|---|---|
| `navigate` | `routeName`, `arguments`, `navigationStyle`, `widgetJson`, `assetPath` | Navigate to a screen |
| `callFunction` | `functionName`, `arguments` (object) | Call a registered Kotlin function (see [Function Registry](#function-registry)) |

### Custom Action Parsers

```kotlin
class ShareActionParser : KetoyActionParser<ShareAction> {
    override val actionType = "share"

    override fun getModel(json: JsonObject) = ShareAction(
        text = json["text"]?.jsonPrimitive?.content ?: "",
        title = json["title"]?.jsonPrimitive?.content
    )

    override fun onCall(model: ShareAction, context: ActionContext) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, model.text)
        }
        context.androidContext.startActivity(Intent.createChooser(intent, model.title))
    }
}

// Register during initialization
Ketoy.initialize(
    actionParsers = listOf(ShareActionParser())
)
```

Then use it from JSON:

```json
{
    "onClick": {
        "actionType": "share",
        "text": "Check out this app!",
        "title": "Share via"
    }
}
```

---

## Function Registry

Ketoy's **Function Registry** lets you register plain Kotlin/JVM functions and invoke them directly from SDUI JSON — similar to how custom Compose components work but for business logic rather than UI.

### Registering Functions

```kotlin
// Function with parameters
KetoyFunctionRegistry.register(
    name = "addToCart",
    parameterTypes = mapOf("itemId" to "String", "quantity" to "Int"),
    description = "Add an item to the shopping cart"
) { args ->
    val itemId = args["itemId"] as String
    val quantity = args["quantity"] as Int
    cartViewModel.addItem(itemId, quantity)
}

// Function without parameters
KetoyFunctionRegistry.register(
    name = "logout",
    description = "Sign out the current user"
) {
    authManager.signOut()
}
```

### Calling from JSON

Use the built-in `callFunction` action type:

```json
{
    "type": "Button",
    "props": {
        "onClick": {
            "actionType": "callFunction",
            "functionName": "addToCart",
            "arguments": {
                "itemId": "SKU-42",
                "quantity": 2
            }
        }
    },
    "children": [
        { "type": "Text", "props": { "text": "Add to Cart" } }
    ]
}
```

### DSL Helper — `KFunctionCall`

The DSL provides a convenience method to wire up function calls inline:

```kotlin
KButton(containerColor = KColors.Blue) {
    KFunctionCall("addToCart", "itemId" to "SKU-42", "quantity" to 2)
    KText("Add to Cart", color = KColors.White)
}
```

This serialises the onClick as a `callFunction` action in JSON, so servers can see which function is invoked and with what arguments.

### Querying the Registry

```kotlin
KetoyFunctionRegistry.isRegistered("addToCart")    // true
KetoyFunctionRegistry.getAllNames()                 // ["addToCart", "logout", ...]
KetoyFunctionRegistry.get("addToCart")              // FunctionEntry (name, parameterTypes, description, handler)
```

> **Lifecycle:** `KetoyFunctionRegistry.clear()` is called automatically when you call `Ketoy.initialize()` or `Ketoy.reset()`, so you can re-register functions each session without stale entries.

---

## Custom Widgets

For components that need full control over parsing and rendering, implement `KetoyWidgetParser<T>`:

```kotlin
class RatingBarParser : KetoyWidgetParser<RatingBarModel> {
    override val type = "RatingBar"

    override fun getModel(json: JsonObject) = RatingBarModel(
        rating = json["rating"]?.jsonPrimitive?.floatOrNull ?: 0f,
        maxStars = json["maxStars"]?.jsonPrimitive?.intOrNull ?: 5
    )

    @Composable
    override fun parse(model: RatingBarModel) {
        Row {
            repeat(model.maxStars) { i ->
                Icon(
                    imageVector = if (i < model.rating.toInt()) Icons.Filled.Star
                                  else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700)
                )
            }
        }
    }
}

// Register
Ketoy.initialize(widgetParsers = listOf(RatingBarParser()))
```

JSON:

```json
{ "type": "RatingBar", "props": { "rating": 4.5, "maxStars": 5 } }
```

---

## Custom Compose Components

Register any `@Composable` function for SDUI rendering via `KComponentRegistry`. This is **the recommended way** to use existing Jetpack Compose composables in Ketoy.

### Registering a Component

```kotlin
// Your existing composable
@Composable
fun UserCard(name: String, age: Int, isVip: Boolean) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$name, age $age")
            if (isVip) Text("VIP Member", color = Color.Red)
        }
    }
}

// Register it for SDUI
KComponentRegistry.register(
    name = "UserCard",
    renderer = { props ->
        UserCard(
            name = props["name"] as? String ?: "",
            age  = props["age"]  as? Int ?: 0,
            isVip = props["isVip"] as? Boolean ?: false
        )
    },
    parameterTypes = mapOf(
        "name"  to "String",
        "age"   to "Int",
        "isVip" to "Boolean"
    )
)
```

### Using it from JSON

```json
{
    "type": "UserCard",
    "props": {
        "name": "Alice",
        "age": 30,
        "isVip": true
    }
}
```

Or via the `"component"` type with explicit `componentName`:

```json
{
    "type": "component",
    "props": {
        "componentName": "UserCard",
        "properties": {
            "name": "Alice",
            "age": 30,
            "isVip": true
        }
    }
}
```

### Using it from DSL

```kotlin
KColumn {
    KComponent(
        name = "UserCard",
        "name" to "Alice",
        "age" to 30,
        "isVip" to true
    )
}
```

### How Parameters Work

- JSON primitive values are automatically converted to their Kotlin types:
  - `String`, `Int`, `Float`, `Double`, `Boolean`
- The renderer receives `Map<String, Any>` — cast values to their expected types
- The `parameterTypes` map is for documentation / schema generation
- Complex objects are passed as their JSON string representation

### The `@KComponent` Annotation

Optionally annotate composables for discoverability:

```kotlin
@KComponent(name = "UserCard", description = "Displays user info card")
@Composable
fun UserCard(name: String, age: Int, isVip: Boolean) { ... }
```

> **Note:** The annotation serves as a runtime marker for documentation and discovery.
> Components must still be registered via `KComponentRegistry.register()`.

---

## Cloud / Remote UI

Fetch and cache screen JSON from your backend API.

### Configuration

```kotlin
val cloudConfig = KetoyCloudConfig(
    baseUrl = "https://api.example.com",
    apiKey = "your-api-key",
    cacheDuration = 30.minutes,
    cacheStrategy = CacheStrategy.NETWORK_FIRST
)

Ketoy.initialize(cloudConfig = cloudConfig)
```

### Cache Strategies

| Strategy | Behaviour |
|---|---|
| `NETWORK_FIRST` | Try network first, fall back to cache on failure |
| `CACHE_FIRST` | Use valid cache, fall back to network, optional background refresh |
| `OPTIMISTIC` | Return cache instantly, refresh in background |
| `CACHE_ONLY` | Only use cached data |
| `NETWORK_ONLY` | Always fetch from network |

### Screen-Level Cloud Toggle

```kotlin
@KScreen(name = "home")
@Composable
fun HomeScreen() {
    // cloudEnabled is controlled at the KetoyScreen level
    KetoyContent(
        nodeBuilder = { buildHomeUI() }
    )
}
```

---

## Theme System

Ketoy provides a full Material 3 theme system with 28 colour tokens.

### Theme Modes

```kotlin
// Use system light/dark
KetoyThemeProvider(mode = KetoyThemeMode.System) {
    // Your content
}

// Force light or dark
KetoyThemeProvider(mode = KetoyThemeMode.Light) { ... }
KetoyThemeProvider(mode = KetoyThemeMode.Dark) { ... }

// Custom colour schemes
KetoyThemeProvider(
    mode = KetoyThemeMode.Custom(
        lightScheme = KetoyColorScheme(
            primary = "#6200EE",
            onPrimary = "#FFFFFF",
            // ... 28 colour tokens
        ),
        darkScheme = KetoyColorScheme(
            primary = "#BB86FC",
            onPrimary = "#000000",
            // ...
        )
    )
) { ... }
```

### Colour Tokens in JSON

Components can reference theme colours by token name:

```json
{
    "type": "Text",
    "props": {
        "text": "Themed Text",
        "color": "primary"
    }
}
```

Available tokens: `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`, `tertiary`, `surface`, `onSurface`, `surfaceVariant`, `onSurfaceVariant`, `error`, `onError`, `outline`, `outlineVariant`, `background`, `onBackground`, and more.

---

## DevTools & Hot Reload

Ketoy includes a built-in hot-reload system for rapid UI development.

### Setup

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation(project(":ketoy-devtools"))
}
```

### Wrap Your App

```kotlin
@Composable
fun MyApp() {
    KetoyDevWrapper {
        // Your normal app content
        KetoyNavHost(startRoute = HomeRoute) {
            screen<HomeRoute> { HomeScreen() }
        }
    }
}
```

### Start the Dev Server

```bash
# From the project root
./gradlew :ketoy-devtools-server:run
```

The server:
- Watches JSON files in your project for changes
- Watches Kotlin source files for `@KScreen` annotated composables
- Serves screen JSON via HTTP + WebSocket
- Generates a QR code for easy device pairing
- Pushes updates in real-time to connected devices

### How It Works

1. The dev server watches for file changes
2. When a JSON file changes, it pushes the update over WebSocket
3. `KetoyDevWrapper` receives the update and injects the new JSON into `KetoyScreenRegistry`
4. The screen's `Content()` picks up the dev override (highest priority in the resolution chain)
5. The UI recomposes with the new JSON — instant preview

### Connecting Your Device

1. Start the dev server (`./gradlew :ketoy-devtools-server:run`)
2. Launch your app in debug mode
3. The `KetoyDevConnectScreen` appears with a QR code scanner and manual IP entry
4. Scan the QR code shown in the server terminal, or enter the IP manually
5. Done — changes push instantly to your device

---

## Gradle CLI Commands

Ketoy registers a set of Gradle tasks under the `ketoy` group. Run `./gradlew tasks --group=ketoy` to see them all.

### Setup — `local.properties`

Cloud commands (`ketoyPush`, `ketoyListScreens`, etc.) read credentials from `local.properties` so you never have to pass API keys on the command line:

```properties
# local.properties  (do NOT commit this file)
KETOY_DEVELOPER_API_KEY=api_key
KETOY_BASE_URL= (get base url from ketoy.dev)
KETOY_PACKAGE_NAME=com.yourcompany.app
```

| Variable | Required | Description |
|---|---|---|
| `KETOY_DEVELOPER_API_KEY` | ✔ | Your developer API key (from `/api/developers/register`) |
| `KETOY_BASE_URL` | ✔ | Base URL of the Ketoy Node.js server (e.g. `https://api.ketoy.dev`) |
| `KETOY_PACKAGE_NAME` | ✔ | Your app's package name (must match a registered app on the server) |

### Quick Reference

| Command | Description |
|---|---|
| `./gradlew ketoyDev` | Start dev server with auto-export — edit DSL → live app update |
| `./gradlew ketoyServe` | Start the dev server only (no auto-export) |
| `./gradlew ketoyExport` | Export DSL screens to `ketoy-screens/` JSON files |
| `./gradlew ketoyExportProd` | Export production-ready screens + navigation to `ketoy-export/` |
| `./gradlew ketoyPush` | Upload a single screen JSON to the cloud server |
| `./gradlew ketoyPushAll` | Upload **all** screens from `ketoy-screens/` at once |
| `./gradlew ketoyListScreens` | List all screens deployed for this app |
| `./gradlew ketoyScreenVersions` | List all versions of a specific screen |
| `./gradlew ketoyScreenDetails` | Get full details of a screen (including JSON) |
| `./gradlew ketoyRollback` | Rollback a screen to a previous version |
| `./gradlew ketoyDeleteScreen` | Delete a screen and all its versions |

### `ketoyDev` — Live Development

The recommended single command for hot-reload development. It watches your Kotlin DSL source files, re-exports JSON on change, and pushes updates to connected devices automatically.

```bash
./gradlew ketoyDev
```

Under the hood this starts the dev server with the `--auto-export` flag which enables the `SourceWatcher`.

### `ketoyServe` — Dev Server Only

Starts the dev server without auto-export. Useful if you're editing JSON files directly.

```bash
./gradlew ketoyServe
# With custom port / watch directory:
./gradlew ketoyServe --args="--port 9090 --watch ./my-screens"
```

### `ketoyExport` — DSL → JSON

Exports all `@KScreen` annotated screens to JSON files in `ketoy-screens/`.

```bash
./gradlew ketoyExport
```

This runs the `ExportScreensTest` unit test which invokes each screen builder and writes the serialised JSON.

### `ketoyExportProd` — Production Export

Exports all screens and navigation graphs to `ketoy-export/` in a production-ready format. Unlike `ketoyExport` (which targets the dev server), this generates clean, deployment-ready JSON with manifests.

```bash
./gradlew ketoyExportProd
```

**Output structure:**

```
ketoy-export/
├── home.json                   # Individual screen JSON
├── profile.json
├── analytics.json
├── cards.json
├── history_screen.json
├── nav_main.json               # Navigation graph
├── nav_demo.json
├── navigation_manifest.json    # Combined nav graph manifest
└── screen_manifest.json        # Screen index manifest
```

**Key differences from `ketoyExport`:**

| Aspect | `ketoyExport` | `ketoyExportProd` |
|--------|---------------|-------------------|
| Output directory | `ketoy-screens/` | `ketoy-export/` |
| Purpose | Dev server hot-reload | Production bundling / Cloud push |
| Navigation manifests | ✖ | ✔ (`navigation_manifest.json`, `screen_manifest.json`) |
| Variable templates | Resolved values | Keeps `{{data:...}}` placeholders |

**Typical workflow:**

```bash
# Export production JSON
./gradlew ketoyExportProd

# Push all to cloud
./gradlew ketoyPushAll -Pversion=1.0.0

# Or bundle into app assets
cp -r ketoy-export/* app/src/main/assets/screens/
```

### `ketoyPush` — Upload a Screen

Uploads a single screen JSON from `ketoy-screens/` to the Ketoy cloud server.

```bash
# Basic usage
./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0

# With optional metadata
./gradlew ketoyPush \
  -PscreenName=home \
  -Pversion=2.0.0 \
  -PdisplayName="Home Screen" \
  -Pdescription="Main landing page" \
  -Pcategory=main \
  -Ptags=home,landing,main
```

| Parameter | Required | Default | Description |
|---|---|---|---|
| `-PscreenName` | ✔ | — | Screen name (must match a `.json` file in `ketoy-screens/`) |
| `-Pversion` | ✔ | — | Semantic version (e.g. `1.0.0`) |
| `-PdisplayName` | | auto from name | Human-readable display name |
| `-Pdescription` | | `""` | Screen description |
| `-Pcategory` | | `""` | Category tag for metadata |
| `-Ptags` | | — | Comma-separated tags (e.g. `home,landing`) |

The task reads the JSON file, escapes it into a JSON string (as required by the API's `jsonContent` field), and sends it to `POST /api/screens/{packageName}/upload`.

### `ketoyPushAll` — Upload All Screens

Uploads every `.json` file in `ketoy-screens/` in one go.

```bash
./gradlew ketoyPushAll -Pversion=1.0.0
```

### `ketoyListScreens` — List Deployed Screens

```bash
./gradlew ketoyListScreens
```

Calls `GET /api/screens/{packageName}` and prints the list of all screens for your app.

### `ketoyScreenVersions` — Version History

```bash
./gradlew ketoyScreenVersions -PscreenName=home
```

Calls `GET /api/screens/{packageName}/{screenName}/versions`.

### `ketoyScreenDetails` — Screen Details

```bash
./gradlew ketoyScreenDetails -PscreenName=home
```

Calls `GET /api/screens/{packageName}/{screenName}/details?includeJson=true`.

### `ketoyRollback` — Rollback a Screen

Rollback creates a new patch version with the content of the specified older version.

```bash
./gradlew ketoyRollback -PscreenName=home -Pversion=1.0.0
```

Calls `POST /api/screens/{packageName}/{screenName}/rollback/{version}`.

### `ketoyDeleteScreen` — Delete a Screen

```bash
./gradlew ketoyDeleteScreen -PscreenName=home
```

Calls `DELETE /api/screens/{packageName}/{screenName}`. This removes all versions.

---

## JSON Wire Format

The DSL serialises to a standard JSON tree. You can also build this JSON on the server.

<details>
<summary><b>Example: Column with text & button</b></summary>

```json
{
    "type": "Column",
    "props": {
        "modifier": {
            "fillMaxSize": 1.0,
            "background": "#F5F5F5"
        },
        "verticalArrangement": "center",
        "horizontalAlignment": "centerHorizontally"
    },
    "children": [
        {
            "type": "Text",
            "props": {
                "text": "Hello, Ketoy!",
                "fontSize": 24,
                "fontWeight": "bold"
            }
        },
        {
            "type": "Spacer",
            "props": { "height": 16 }
        },
        {
            "type": "Button",
            "props": {
                "onClick": "place_order",
                "containerColor": "#FF2196F3"
            },
            "children": [
                { "type": "Text", "props": { "text": "Tap Me", "color": "#FFFFFFFF" } }
            ]
        }
    ]
}
```
</details>

<details>
<summary><b>Supported component types</b></summary>

| Type | Description |
|---|---|
| `Column` | Vertical layout |
| `Row` | Horizontal layout |
| `Box` | Stacked layout |
| `LazyColumn` | Scrolling vertical list |
| `LazyRow` | Scrolling horizontal list |
| `Text` | Text display |
| `Button` | Material button |
| `Card` | Material card |
| `Spacer` | Empty space |
| `Image` | Async image (URL, res, icon, base64) |
| `Icon` | Material icon |
| `IconButton` | Icon button |
| `TextField` | Text input |
| `Scaffold` | Full-screen scaffold |
| `TopAppBar` | Top app bar (small, center-aligned, medium, large) |
| `BottomAppBar` | Bottom app bar |
| `NavigationBar` | Bottom navigation |
| `NavigationBarItem` | Bottom nav item |
| `NavigationDrawerItem` | Drawer nav item |
| `NavigationRail` | Side navigation rail |
| `NavigationRailItem` | Rail nav item |
| `FloatingActionButton` | FAB (regular, small, large, extended) |
| `SnackBar` | Snackbar |
| `SnackBarHost` | Snackbar host |
| `ModalBottomSheet` | Bottom sheet |
| `component` | Custom registered component (by `componentName`) |
| `*` (any name) | Looked up in `KetoyWidgetRegistry` then `KComponentRegistry` |
</details>

<details>
<summary><b>JSON Actions</b></summary>

```json
// Single action
{ "onClick": { "actionType": "navigate", "routeName": "detail" } }

// Call a registered function
{ "onClick": {
    "actionType": "callFunction",
    "functionName": "addToCart",
    "arguments": { "itemId": "SKU-42", "quantity": 1 }
}}

// Legacy action ID (from DSL)
{ "onClick": "action_42" }

// Custom action ID (from DSL with actionId parameter)
{ "onClick": "submit_order" }
```
</details>

---

## UI Samples

<details>
<summary><b>Bank Wallet Card</b></summary>

```kotlin
KCard(
    modifier = kModifier(fillMaxWidth = 1f),
    containerColor = "#1A1A2E",
    shape = KShapes.Rounded24,
    elevation = 8
) {
    KColumn(
        modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 24)),
        verticalArrangement = "spacedBy_8"
    ) {
        KText("Total Balance", fontSize = 14, color = "#AAAACC")
        KText("$24,562.80", fontSize = 36, fontWeight = KFontWeights.Bold, color = "#FFFFFF")
        KSpacer(height = 8)
        KRow(
            modifier = kModifier(fillMaxWidth = 1f),
            horizontalArrangement = KArrangements.SpaceBetween
        ) {
            KColumn {
                KText("Savings", fontSize = 12, color = "#AAAACC")
                KText("$18,240", fontSize = 16, fontWeight = KFontWeights.Bold, color = "#4ECDC4")
            }
            KColumn {
                KText("Checking", fontSize = 12, color = "#AAAACC")
                KText("$6,322", fontSize = 16, fontWeight = KFontWeights.Bold, color = "#FFD93D")
            }
        }
    }
}
```
</details>

<details>
<summary><b>Login Form</b></summary>

```kotlin
KColumn(
    modifier = kModifier(fillMaxSize = 1f, padding = kPadding(all = 24), verticalScroll = true),
    verticalArrangement = "spacedBy_16",
    horizontalAlignment = KAlignments.CenterHorizontally
) {
    KText("Welcome Back", fontSize = 28, fontWeight = KFontWeights.Bold)
    KSpacer(height = 8)
    KTextField(
        value = "", onValueChange = { },
        modifier = kModifier(fillMaxWidth = 1f),
        singleLine = true,
        keyboardOptions = KKeyboardOptions(keyboardType = "email")
    ) {
        label { KText("Email") }
        placeholder { KText("you@example.com") }
    }
    KTextField(
        value = "", onValueChange = { },
        modifier = kModifier(fillMaxWidth = 1f),
        singleLine = true,
        visualTransformation = KVisualTransformation(type = "password")
    ) {
        label { KText("Password") }
    }
    KButton(
        modifier = kModifier(fillMaxWidth = 1f),
        containerColor = KColors.Blue,
        shape = KShapes.Rounded12,
        actionId = "sign_in"
    ) {
        KText("Sign In", color = KColors.White, fontWeight = KFontWeights.Bold)
    }
}
```
</details>

<details>
<summary><b>Gradient Header</b></summary>

```kotlin
KBox(
    modifier = kModifier(
        fillMaxWidth = 1f, height = 200,
        gradient = KGradients.linear(listOf("#6C63FF", "#3F3D9E"), KGradients.Directions.Horizontal),
        shape = KShapes.rounded(bottomStart = 32, bottomEnd = 32)
    ),
    contentAlignment = KAlignments.Center
) {
    KText("Dashboard", fontSize = 28, fontWeight = KFontWeights.Bold, color = KColors.White)
}
```
</details>

---

## Contributing

We welcome contributions! Please follow these guidelines:

### Checklist

- [ ] Fork the repo and create your branch from `main`
- [ ] Follow the existing package structure (`model/`, `dsl/`, `parser/`, `renderer/`, etc.)
- [ ] Each new widget must include:
  - A `KNode` subclass in `model/KNode.kt`
  - A Props data class in `model/KProps.kt` (or its own file for complex components)
  - A DSL builder function in `dsl/KUniversalScope.kt`
  - A top-level builder in `dsl/TopLevelBuilders.kt` (if it can be a root node)
  - A renderer `@Composable` in `renderer/`
  - A parser function in `parser/` (if it has custom JSON parsing)
- [ ] All public API must have KDoc comments
- [ ] Run `./gradlew build` and confirm zero errors before submitting a PR
- [ ] Ensure min SDK 26 compatibility

### Code Style

- Use Kotlin idioms (data classes, sealed classes, extension functions)
- Prefer `const val` for string constants
- Keep DSL function signatures consistent: `modifier` first, `content` last
- Use `internal` visibility for renderer/parser functions
- Use `actionId` parameter (not hardcoded strings) for onClick in DSL

### Running Tests

```bash
# Unit tests
./gradlew :ketoy-sdk:testDebugUnitTest

# Instrumented / UI tests
./gradlew :ketoy-sdk:connectedDebugAndroidTest
```

---

## License

```
MIT License

Copyright (c) 2024 developerchunk

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
