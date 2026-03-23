<div align="center">

<img src="https://ketoy.dev/logo.png" alt="Ketoy" width="80" />

# Ketoy

**Server-Driven UI for Android — ship UI changes without a Play Store update.**

[![Maven Central](https://img.shields.io/maven-central/v/dev.ketoy/sdk?label=SDK&color=5C6BC0)](https://central.sonatype.com/artifact/dev.ketoy/sdk)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/dev.ketoy.devtools?label=Gradle%20Plugin&color=5C6BC0)](https://plugins.gradle.org/plugin/dev.ketoy.devtools)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2025.01.01-4285F4?logo=jetpackcompose)](https://developer.android.com/develop/ui/compose/bom)
[![Min SDK](https://img.shields.io/badge/minSdk-26-brightgreen)](https://developer.android.com)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)

[Documentation](https://docs.ketoy.dev) · [Quick Start](https://docs.ketoy.dev/docs/quick-start) · [Console](https://console.ketoy.dev) · [Security](https://docs.ketoy.dev/security)

</div>

---

## What is Ketoy?

Ketoy is an open-source **Server-Driven UI (SDUI) framework** for Android. You define screens in a Kotlin DSL, export them as optimized binary payloads, and render them as native **Jetpack Compose + Material 3** at runtime. When a screen changes, you push the update — the app reflects it instantly without a Play Store release.

```kotlin
// Define a screen in Kotlin
val home = ketoyExport("home") {
    content {
        KColumn {
            KText("Welcome back", fontSize = 24f, fontWeight = "Bold")
            KButton("Explore") { navigate("feed") }
        }
    }
}
```

That DSL compiles to a `.ktw` wire binary — **10-15x smaller than equivalent JSON** — and ships to your app over WebSocket, HTTP, or cloud. The app decodes it in under 2 ms and renders native Compose.

→ **[Quick Start guide](https://docs.ketoy.dev/docs/quick-start)** to go from zero to hot-reload in minutes.

---

## Why Ketoy?

| Problem | Ketoy's answer |
|---|---|
| UI updates require Play Store releases (days to weeks) | Push screen changes instantly via cloud or dev server |
| SDUI JSON payloads are large and slow to parse | [Wire format (.ktw)](#wire-format--10-15x-compression) compresses 10-15x with 4-layer pipeline |
| Custom JSON renderers are brittle | Type-safe Kotlin DSL + sealed class model, zero stringly-typed rendering |
| Hot-reload dev loops are slow | Embedded WebSocket server — save-to-screen in ~100-300 ms |
| Offline / poor connectivity | 5 cache strategies: Network First, Cache First, Optimistic, Cache Only, Network Only |
| Hard to extend with custom widgets | Full extension system: custom widget parsers, action parsers, component registry |

---

## Installation

### SDK — Maven Central

```kotlin
// build.gradle.kts (app module)
dependencies {
    implementation("dev.ketoy:sdk:0.1.3-beta.2")
}
```

[![Maven Central](https://img.shields.io/maven-central/v/dev.ketoy/sdk)](https://central.sonatype.com/artifact/dev.ketoy/sdk)

### Gradle Plugin — Plugin Portal

```kotlin
// build.gradle.kts (app module)
plugins {
    id("dev.ketoy.devtools") version "0.1.5-beta.10"
}
```

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/dev.ketoy.devtools)](https://plugins.gradle.org/plugin/dev.ketoy.devtools)

The Gradle plugin adds the dev server, hot-reload, export tasks, and cloud push/pull. The SDK works standalone without it.

---

## Wire Format — 10-15x Compression

Ketoy ships a custom binary container format (`.ktw` — Ketoy Wire) that compresses SDUI payloads through a **four-layer pipeline**:

```
Raw JSON
  │
  ├─ Layer 2a: Key Aliasing       "backgroundColor" → "bg"     (~1.5-2x)
  ├─ Layer 2c: Type ID Encoding   "FloatingActionButton" → 35  (~1.2x)
  ├─ Layer 3:  MessagePack        JSON text → binary            (~2-3x)
  └─ Layer 1:  Gzip               DEFLATE on the binary blob    (~4x)
  │
  └─ .ktw wire bytes   (10-15x smaller than original JSON)
```

**What this means in practice:**

| Screen complexity | Raw JSON | `.ktw` | Saved |
|---|---|---|---|
| Simple (30 nodes) | ~3.5 KB | ~300 B | 3.2 KB |
| Medium (100 nodes) | ~14 KB | ~1 KB | 13 KB |
| Complex (300 nodes) | ~42 KB | ~3 KB | 39 KB |
| Full app bundle (10 screens) | ~140 KB | ~10 KB | 130 KB |

Decoding is **auto-detecting** — the client inspects magic bytes and automatically applies the reverse pipeline (gzip → MessagePack → type expansion → key expansion). No configuration needed on the client, full backward compatibility guaranteed.

The zero-dependency MessagePack implementation (~200 LOC), gzip layer, key alias dictionary (130+ property names), and type ID table are all built into the SDK. No external binary dependencies.

---

## How It Works

```
┌─ Developer Machine ──────────────────────────────────────┐
│                                                          │
│  Kotlin DSL  →  ketoyExport task  →  .ktw files          │
│                        │                                 │
│               FileWatcher + Dev Server                   │
│               (embedded HTTP + WebSocket)                │
└─────────────────────────┬────────────────────────────────┘
                          │  WebSocket push (Base64 .ktw)
                          ▼
┌─ Android App ────────────────────────────────────────────┐
│                                                          │
│  KetoyDevClient  →  autoDecode()  →  JSONBytesToUI()     │
│                                           │              │
│                              KetoyRenderer (Compose)     │
│                              ↓                           │
│                              Native Material 3 UI        │
└──────────────────────────────────────────────────────────┘
```

**In production (cloud mode):**

```
Ketoy Console  →  ketoyPush  →  Ketoy Cloud API
                                      │
                               KetoyCloudScreen
                               (fetches + renders)
                                      │
                               5 cache strategies
```

---

## Core Capabilities

### Kotlin DSL + Native Compose Rendering

Screens are defined in pure Kotlin using a composable-style DSL. The DSL produces a typed `KNode` tree that serializes to the wire format and renders as real Jetpack Compose: `Column`, `Row`, `Box`, `LazyColumn`, `LazyRow`, `Text`, `Button`, `Card`, `Image`, `Icon`, `Scaffold`, `TopAppBar`, `NavigationBar`, `ModalBottomSheet`, and more.

### Material 3 Theme System

`KetoyThemeProvider` exposes 35+ Material 3 color tokens (primary, secondary, tertiary, surfaces, containers, error, outline, custom success) via CompositionLocal — available to all rendered components without explicit passing.

### Navigation

Type-safe navigation built on `navigation-compose`. `KetoyNavHost`, `KetoyNavGraph`, and `KetoyNavController` support deep links, back stack management, and runtime nav graph replacement via dev or cloud overrides — without redeployment.

### Variable System

Runtime variable injection via `KetoyVariableRegistry`. Templates like `{{data:user:name}}` and `{{enum:status:value}}` resolve at render time — change a variable and all screens using it recompose automatically.

### Extension Points

- **Custom widget parsers** — handle any JSON type your server emits
- **Custom action parsers** — wire JSON actions to your own Kotlin lambdas
- **Component registry** — register named Composables available to SDUI trees
- **Function registry** — register callable functions from JSON action payloads

### Cloud + 5 Cache Strategies

`KetoyCloudScreen` fetches screens from the Ketoy cloud API with your choice of cache strategy:

| Strategy | Behavior |
|---|---|
| `NETWORK_FIRST` | Try network, fall back to cache |
| `CACHE_FIRST` | Try cache, fall back to network |
| `OPTIMISTIC` | Return cache immediately, refresh in background |
| `CACHE_ONLY` | Offline-only, never network |
| `NETWORK_ONLY` | Always fresh, never cache |

### Dev Hot-Reload

The Gradle plugin embeds an HTTP + WebSocket server. Run `./gradlew ketoyDev` and every `.kt` DSL change re-exports and pushes to connected devices in ~100-300 ms. The `KetoyDevWrapper` composable receives wire bytes and updates the UI live — no rebuild, no re-install.

### Gradle Tasks

The `dev.ketoy.devtools` plugin adds 11 tasks:

| Task | What it does |
|---|---|
| `ketoyDev` | Start hot-reload dev server |
| `ketoyExport` | Export all DSL screens to `.ktw` files |
| `ketoyPush` | Push a screen to Ketoy cloud |
| `ketoyPushAll` | Push all screens to cloud |
| `ketoyListScreens` | List cloud screens |
| `ketoyScreenVersions` | Show version history |
| `ketoyRollback` | Rollback to a previous version |
| `ketoyDeleteScreen` | Remove a screen from cloud |

---

## Artifacts

| Artifact | Coordinates | Registry |
|---|---|---|
| Android SDK | `dev.ketoy:sdk:0.1.3-beta.2` | [Maven Central](https://central.sonatype.com/artifact/dev.ketoy/sdk) |
| Gradle Plugin | `dev.ketoy.devtools:0.1.5-beta.10` | [Gradle Plugin Portal](https://plugins.gradle.org/plugin/dev.ketoy.devtools) |

---

## Requirements

| | Minimum |
|---|---|
| Android minSdk | 26 (Android 8.0) |
| Android targetSdk | 36 |
| Kotlin | 2.0.21 |
| Compose BOM | 2025.01.01 |
| Java | 17 |

---

## Security

Ketoy Dev Server uses cleartext HTTP/WebSocket — restrict it to **debug builds only**. Production traffic goes through the Ketoy cloud API over HTTPS.

For responsible disclosure, vulnerability reports, and our security policy: [docs.ketoy.dev/security](https://docs.ketoy.dev/security)

---

## Resources

| | |
|---|---|
| Documentation | [docs.ketoy.dev](https://docs.ketoy.dev) |
| Quick Start | [docs.ketoy.dev/docs/quick-start](https://docs.ketoy.dev/docs/quick-start) |
| Console | [console.ketoy.dev](https://console.ketoy.dev) |
| Gradle Plugin | [plugins.gradle.org/plugin/dev.ketoy.devtools](https://plugins.gradle.org/plugin/dev.ketoy.devtools) |
| Maven Central | [central.sonatype.com/artifact/dev.ketoy/sdk](https://central.sonatype.com/artifact/dev.ketoy/sdk) |
| Security | [docs.ketoy.dev/security](https://docs.ketoy.dev/security) |

---

## License

```
Copyright 2026 KetoyDev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
