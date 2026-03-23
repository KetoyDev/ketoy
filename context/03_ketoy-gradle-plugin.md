# Ketoy Gradle Plugin

Module path: `Ketoy/ketoy-gradle-plugin/`
Published: Gradle Plugin Portal + Maven Central
Plugin ID: `dev.ketoy.devtools`
Version: `0.1.5-beta.10`
Applied in: `app/build.gradle.kts` via `id("dev.ketoy.devtools")`

The plugin owns: dev hot-reload server, JSON export tasks, cloud push/pull tasks.
It does NOT own: SDK rendering, navigation, or model definitions.

---

## Build Strategy

Uses **Shadow JAR** to bundle `Java-WebSocket:1.5.7` (the embedded dev server) while excluding Gradle API.
WebSocket lib is relocated to `dev.ketoy.embedded.java_websocket` to avoid classpath conflicts with host project.

```
ketoy-gradle-plugin/build.gradle.kts
├── Produces: single fat JAR as primary artifact
├── Bundles: org.java-websocket:Java-WebSocket:1.5.7
└── Relocates to: dev.ketoy.embedded.java_websocket
```

---

## Configuration DSL

Applied in the host app's `build.gradle.kts`:

```kotlin
ketoy {
    apiKey = "your-api-key"          // Ketoy cloud API key
    packageName = "com.example.app"  // Android app package name
    baseUrl = "https://api.ketoy.dev" // Cloud API base URL
    serverPort = 8080                 // Local dev server port (default: 8080)
    exportDir = "ketoy-export"        // Dir for exported JSON (default: ketoy-export)
}
```

Extension class: `KetoyDevExtension.kt`

---

## All 11 Gradle Tasks

### Cloud Tasks (require `apiKey`, `packageName`, `baseUrl`)

| Task | Purpose |
|---|---|
| `ketoyPush` | Push a specific screen's JSON to cloud |
| `ketoyPushAll` | Push all exported screens to cloud |
| `ketoyListScreens` | List all screens stored in cloud |
| `ketoyScreenVersions` | Show version history for a screen |
| `ketoyScreenDetails` | Detailed info for a specific screen |
| `ketoyRollback` | Rollback a screen to a previous version |
| `ketoyDeleteScreen` | Delete a screen from cloud |

### Export Tasks

| Task | Purpose |
|---|---|
| `ketoyExport` | Export DSL screens to JSON in `exportDir` |
| `ketoyExportProd` | Export with `screen_manifest.json` + `navigation_manifest.json` |

### Dev Tasks

| Task | Purpose |
|---|---|
| `ketoyServe` | Start local HTTP + WebSocket server only |
| `ketoyDev` | Start server + watch Kotlin source for changes (triggers auto-export) |

---

## Dev Server Architecture

`server/` package inside the plugin:

- **`KetoyServeTask` / `KetoyDevTask`** — Gradle tasks that start the embedded server
- **`FileWatcher.kt`** — Monitors `exportDir` JSON files; debounce: **100ms**
- **`SourceWatcher.kt`** — Monitors `.kt`/`.kts` source files; debounce: **1500ms** (triggers `ketoyExport` then broadcasts)
- **`WebSocketHandler.kt`** — Broadcasts updated JSON to all connected `KetoyDevWrapper` clients
- **`ScreenManager.kt`** — Tracks active screens, versions, metadata during dev session
- **`KetoyHttpClient.kt`** — Internal HTTP client used by cloud tasks

Communication protocol:
- App connects via WebSocket to `ws://localhost:{serverPort}`
- On JSON file change → server reads new file → broadcasts to all connected clients
- `KetoyDevClient` (SDK side) receives payload → updates `KetoyDevWrapper` → recomposition

Long-poll HTTP endpoint also available for pull-based sync (fallback).

---

## Source Structure

```
ketoy-gradle-plugin/src/main/kotlin/dev/ketoy/gradle/
├── KetoyDevPlugin.kt           ← Plugin entry point, registers all tasks
├── KetoyDevExtension.kt        ← @gradle.dsl configuration class
├── tasks/
│   ├── KetoyPushTask.kt
│   ├── KetoyPushAllTask.kt
│   ├── KetoyListScreensTask.kt
│   ├── KetoyScreenVersionsTask.kt
│   ├── KetoyScreenDetailsTask.kt
│   ├── KetoyRollbackTask.kt
│   ├── KetoyDeleteScreenTask.kt
│   ├── KetoyExportTask.kt
│   ├── KetoyExportProdTask.kt
│   ├── KetoyServeTask.kt
│   └── KetoyDevTask.kt
├── server/
│   ├── ScreenManager.kt
│   ├── FileWatcher.kt
│   ├── SourceWatcher.kt
│   └── WebSocketHandler.kt
└── internal/
    └── KetoyHttpClient.kt
```

---

## Dev Workflow

```
Developer runs: ./gradlew ketoyDev
        ↓
Plugin starts HTTP + WebSocket server on localhost:8080
SourceWatcher monitors .kt files (1500ms debounce)
        ↓
Developer edits Kotlin DSL screen
        ↓
SourceWatcher detects change → triggers ketoyExport
        ↓
FileWatcher detects new JSON (100ms debounce)
        ↓
WebSocketHandler broadcasts JSON to app
        ↓
KetoyDevClient receives → KetoyDevWrapper updates → instant recompose
```

---

## Network Security (Android Dev Mode)

`app/src/debug/res/xml/network_security_config.xml` allows cleartext for `localhost` only in debug builds.
Production builds use HTTPS exclusively. See `NETWORK_SECURITY.md` at project root for details.

---

## Non-Obvious Details

- `ketoyDev` = `ketoyServe` + source watching combined; use `ketoyServe` if you only want to serve existing JSON without recompiling
- The source watcher has a 1500ms debounce (longer than file watcher) to wait for Kotlin compilation to stabilize
- Plugin requires the Shadow JAR artifact — regular JAR will be missing WebSocket dependency
- Plugin tasks fail silently if `apiKey` is not set for cloud tasks (prints warning, does not throw)
