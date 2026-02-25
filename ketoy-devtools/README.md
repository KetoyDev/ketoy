# Ketoy Dev Tools — Hot-Reload for Ketoy UI

> **Live hot-reload for Ketoy Server-Driven Android UI.**
> Edit your UI JSON → See changes instantly on your emulator/device.

## Architecture

```
┌─────────────────┐     WebSocket/HTTP     ┌──────────────────────┐
│  Dev Machine    │ ◄──────────────────────► │  Android App         │
│                 │                          │                      │
│  JSON Files     │     File Watcher        │  KetoyDevWrapper     │
│  (ketoy-screens)│ ────────────────►       │    ├─ ConnectScreen  │
│                 │     Dev Server          │    ├─ DevPreview     │
│  DSL Code ──►   │     (port 8484)        │    └─ StatusOverlay  │
│  JSON Export    │                          │                      │
└─────────────────┘                          └──────────────────────┘
```

## Quick Start

### 1. Add the dependency (debug only)

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation(project(":ketoy-devtools"))
}
```

### 2. Wrap your app

```kotlin
// In your MainActivity
setContent {
    KetoyDevWrapper {
        // Your normal app code
        MyApp()
    }
}
```

### 3. Start the dev server

```bash
./gradlew ketoyServe
```

### 4. Connect and iterate!

The app will show a connection screen. Enter the IP address shown in the terminal.
Drop `.json` files in the `ketoy-screens/` directory — changes appear instantly!

## How It Works

1. **Dev Server** watches the `ketoy-screens/` directory for `.json` file changes
2. When a JSON file changes, the server pushes the update via **WebSocket** to all connected apps
3. The app receives the new JSON and re-renders it through Ketoy's `JSONStringToUI()` renderer
4. A **status overlay** shows connection state and version info

## Components

### `ketoy-devtools-server/` — Dev Server (JVM)

A lightweight HTTP + WebSocket server that:
- Watches a directory for JSON file changes using Java's `WatchService`
- Broadcasts updates to all connected apps via WebSocket
- Provides REST endpoints for screen data
- Shows connection URL + QR code info in terminal

**Run:**
```bash
./gradlew ketoyServe

# With options:
./gradlew :ketoy-devtools-server:run --args="--port 9090 --watch ./my-screens"
```

**API Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Server info dashboard |
| GET | `/status` | Server status JSON |
| GET | `/screens` | List available screens |
| GET | `/screen?name=X` | Get specific screen JSON |
| GET | `/bundle` | Get all screens |
| GET | `/poll?v=N` | Long-poll for updates |
| WS | `:8485` | WebSocket live updates |

### `ketoy-devtools/` — Android Library

Provides:

#### `KetoyDevWrapper`
Wraps your entire app. Shows connection screen → live preview → your normal app.

```kotlin
KetoyDevWrapper(
    config = KetoyDevConfig(
        host = "192.168.1.5",  // optional: auto-connect
        port = 8484,
        autoConnect = true,
        showOverlay = true
    ),
    screenName = "home"  // optional: show specific screen
) {
    MyApp()
}
```

#### `KetoyDevPreviewScreen`
Simpler widget for showing a specific screen from the dev server.

```kotlin
KetoyDevPreviewScreen(
    serverUrl = "192.168.1.5:8484",
    screenName = "home"
)
```

#### `KetoyDevActivity`
A standalone activity you can launch without modifying your app:

```kotlin
KetoyDevActivity.launch(context)
KetoyDevActivity.launch(context, host = "192.168.1.5", port = 8484)
```

Or via deep link: `ketoy://dev`

#### `KetoyDevExporter`
Export DSL screens to JSON files for the dev server:

```kotlin
class MyExporter : KetoyDevExporter() {
    override fun registerScreens() {
        screen("home") {
            KColumn(modifier = KMod(fillMaxSize = true)) {
                KText(text = "Hello from DSL!")
            }
        }
    }
}

// Then call:
MyExporter().exportTo(File("./ketoy-screens"))
```

## Workflow

### Fast iteration (edit JSON directly)
1. Start dev server: `./gradlew ketoyServe`
2. Connect app to server
3. Edit JSON files in `ketoy-screens/` → instant updates

### DSL → JSON → Hot Reload
1. Write your screen DSL
2. Export to JSON: use `KetoyDevExporter` or the `TimedKetoyScreen` JSON export button
3. Drop JSON in `ketoy-screens/`
4. Edit and iterate on the JSON
5. When happy, convert back to DSL or keep as server-driven UI

### Release mode
In release builds, `ketoy-devtools` is not included (it's a `debugImplementation`).
Your app runs normally without any dev tools overhead.

## Network Requirements

- Dev machine and Android device must be on the **same Wi-Fi network**
- Port 8484 (HTTP) and 8485 (WebSocket) must be accessible
- The debug build includes a network security config for cleartext HTTP on local IPs

## Configuration

### Server

| Flag | Default | Description |
|------|---------|-------------|
| `--port`, `-p` | 8484 | HTTP server port |
| `--watch`, `-w` | `./ketoy-screens` | Directory to watch |

### Client

```kotlin
KetoyDevConfig(
    host = "",          // Server IP/hostname
    port = 8484,        // Server port
    autoConnect = false, // Auto-connect on launch
    showOverlay = true,  // Show connection status overlay
    shakeToDisconnect = true // Shake to disconnect
)
```
