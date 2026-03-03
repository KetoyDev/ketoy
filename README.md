<p align="center">
  <h1 align="center">Ketoy</h1>
  <p align="center"><b>Server-Driven UI for Jetpack Compose</b></p>
  <p align="center">Build dynamic, real-time UIs from JSON — no app updates needed.</p>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/dev.ketoy/sdk"><img src="https://img.shields.io/maven-central/v/dev.ketoy/sdk?label=Maven%20Central&color=blue" alt="Maven Central"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin&logoColor=white" alt="Kotlin"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4.svg?logo=android&logoColor=white" alt="Compose"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License"/></a>
  <img src="https://img.shields.io/badge/Min%20SDK-26-brightgreen.svg" alt="Min SDK"/>
</p>

<p align="center">
  <a href="https://ketoy.dev">Website</a> · <a href="https://docs.ketoy.dev">Documentation</a> · <a href="https://central.sonatype.com/artifact/dev.ketoy/sdk">Maven Central</a>
</p>

---

## What is Ketoy?

Ketoy is an open-source SDUI framework that lets you define Android UI in a **Kotlin DSL**, serialize it to **JSON**, and render it as native **Jetpack Compose + Material 3** — all at runtime. Push UI changes from your server in seconds, without a Play Store release.

```
Kotlin DSL → JSON → Cloud / Local → Compose UI
```

---

## Features

| | Feature | Description |
|---|---|---|
| 🧩 | **25+ Components** | Column, Row, Box, LazyColumn, Text, Button, Card, Image, Icon, TextField, Scaffold, TopAppBar, BottomAppBar, NavigationBar, NavigationRail, FAB, BottomSheet, and more |
| ✍️ | **Kotlin DSL** | Write UI that reads like Compose — full IDE autocomplete and compile-time safety |
| 🧭 | **Navigation** | Type-safe + string-based routing, JSON-defined nav graphs, 5 navigation styles |
| ⚡ | **Actions & onClick** | 3 formats — string IDs, JSON action objects, action arrays. Register custom functions callable from JSON |
| 🎨 | **Full Modifier Support** | Padding, margin, gradient, shadow, border, cornerRadius, scroll, scale, rotation, alpha, weight |
| ☁️ | **Cloud Sync** | Fetch screen JSON from your API with 5 cache strategies (Network First, Cache First, Optimistic/SWR, Cache Only, Network Only) |
| 🔥 | **Hot Reload** | WebSocket-based live preview — edit DSL, see changes on device in milliseconds |
| ♻️ | **Versioning & Rollback** | Every push is immutable. Roll back to any previous version with one command |
| 🧪 | **Gradual Adoption** | Mix `KetoyContent()` blocks with native Compose in the same screen |
| 🔌 | **Custom Widgets** | Register your own `KetoyWidgetParser` or `KetoyActionParser` for custom components and actions |
| 📦 | **Gradle Plugin** | Push, rollback, list, export — all from the command line |

---

## How It Works

**1. Write UI in the Kotlin DSL**

```kotlin
fun buildHomeScreen() = KColumn(
    modifier = kModifier(fillMaxSize = 1f),
    verticalArrangement = KArrangements.Center,
    horizontalAlignment = KAlignments.CenterHorizontally
) {
    KText("Hello, Ketoy!", fontSize = 24, fontWeight = KFontWeights.Bold)
    KSpacer(height = 16)
    KButton(containerColor = KColors.Blue) {
        KText("Tap Me", color = KColors.White)
    }
}
```

**2. Render in the app**

```kotlin
@Composable
fun HomeScreen() {
    ProvideKetoyScreen(screenName = "home") {
        KetoyContent(nodeBuilder = { buildHomeUI() })
    }
}
```

Or mix SDUI with native Compose:

```kotlin
@Composable
fun DashboardScreen() {
    ProvideKetoyScreen(screenName = "dashboard") {
        KetoyContent(name = "cards", nodeBuilder = { buildCards() })
        Text("Native Compose expenses section")
        KetoyContent(name = "transactions", nodeBuilder = { buildTxns() })
        Button(onClick = {}) { Text("Compose Button") }
    }
}
```

---

## Getting Started

### 1. Add the dependency

```kotlin
// build.gradle.kts
dependencies {
    implementation("dev.ketoy:sdk:0.1-beta")
    
    // Dev tools (debug only)
    debugImplementation("dev.ketoy:devtools:0.1-beta")
}
```

> **Maven Central**: [`dev.ketoy:sdk`](https://central.sonatype.com/artifact/dev.ketoy/sdk)

### 2. Initialize

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Ketoy.initialize(context = this)
    }
}
```

### 3. Add the Gradle plugin (optional, for cloud features)

```kotlin
// build.gradle.kts
plugins {
    id("dev.ketoy.ketoy-dev") version "0.1-beta"
}

ketoyDev {
    apiKey.set("your-api-key")
    packageName.set("com.example.myapp")
}
```

---

## Modules

| Module | Type | Description |
|---|---|---|
| `ketoy-sdk` | Android Library | Core — DSL, renderer, navigation, cloud, actions, theming |
| `ketoy-devtools` | Android Library | WebSocket client, hot-reload wrapper, debug overlay |
| `ketoy-devtools-server` | JVM App | Local dev server — HTTP + WebSocket, file watcher, dashboard |
| `ketoy-gradle-plugin` | Gradle Plugin | Cloud push, rollback, export, screen management CLI |

---

## Gradle Tasks

```bash
# Development
./gradlew ketoyDev              # Start dev server with auto-export + hot-reload
./gradlew ketoyServe            # Start dev server (JSON watching only)
./gradlew ketoyExport           # Export @KScreen DSL → JSON files
./gradlew ketoyExportProd       # Export production JSON + manifests

# Cloud
./gradlew ketoyPush -PscreenName=home -PscreenVersion=1.0.0
./gradlew ketoyPushAll -PscreenVersion=1.0.0
./gradlew ketoyListScreens
./gradlew ketoyScreenVersions -PscreenName=home
./gradlew ketoyRollback -PscreenName=home -PscreenVersion=1.0.0
./gradlew ketoyDeleteScreen -PscreenName=home
```

---

## Screen Resolution (7-Step Priority)

When rendering a screen, Ketoy checks sources in this order:

1. **Dev-server override** — live reload during development
2. **Cloud fetch** — remote JSON from your API
3. **Local JSON string** — embedded via `KetoyScreen.fromJson()`
4. **Asset file** — bundled in `assets/`
5. **Composable lambda** — native `@Composable` fallback
6. **DSL builder** — `nodeBuilder` / `dslBuilder`
7. **Empty placeholder** — last resort

---

## Cloud & Caching

```kotlin
Ketoy.initialize(
    context = this,
    cloudConfig = KetoyCloudConfig(
        apiKey = "your-api-key",
        packageName = "com.example.myapp"
    ),
    cacheConfig = KetoyCacheConfig(
        strategy = KetoyCacheStrategy.NETWORK_FIRST,
        maxAge = 30.days
    )
)
```

5 cache strategies: `NETWORK_FIRST` · `CACHE_FIRST` · `OPTIMISTIC` · `CACHE_ONLY` · `NETWORK_ONLY`

---

## Early Beta — More Coming Soon 🚀

This is just the start. Ketoy is in active development. Upcoming:

- **Cloud Dashboard** — web UI for managing screens and A/B tests
- **Conditional Rendering** — if/else logic in JSON based on user segments
- **Animation Support** — declarative enter/exit transitions from JSON
- **iOS / SwiftUI Renderer** — same JSON, native iOS rendering
- **KMP Support** — Compose Multiplatform for desktop and web
- **Figma-to-Ketoy** — export Figma designs directly to Ketoy JSON

More features, tutorials, and sample projects are on the way. **Star the repo to stay updated.**

---

## Links

- 🌐 **Website**: [ketoy.dev](https://ketoy.dev)
- 📖 **Docs**: [docs.ketoy.dev](https://docs.ketoy.dev)
- 📦 **Maven Central**: [`dev.ketoy:sdk`](https://central.sonatype.com/artifact/dev.ketoy/sdk)
- 🔌 **Gradle Plugin**: [`dev.ketoy.devtools`](https://plugins.gradle.org/plugin/dev.ketoy.devtools)

---

## License

```
MIT License — Copyright (c) 2026 KetoyDev
```

See [LICENSE](LICENSE) for full text.
