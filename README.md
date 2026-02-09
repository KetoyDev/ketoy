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
- [DSL Reference](#dsl-reference)
  - [Layout Components](#layout-components)
  - [Widget Components](#widget-components)
  - [Scaffold Components](#scaffold-components)
  - [Modifier System](#modifier-system)
  - [Constants & Utilities](#constants--utilities)
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
| **A/B Testing** | Serve different JSON payloads to different user segments |
| **Lightweight** | Only `kotlinx-serialization` and `coil-compose` as transitive deps |

```
┌──────────────┐      JSON       ┌──────────────┐      Compose       ┌──────────┐
│  K-DSL Code  │ ──────────────▶ │  Ketoy SDK   │ ──────────────────▶ │  Screen  │
│  (or Server) │                 │  Renderer    │                     │  Pixels  │
└──────────────┘                 └──────────────┘                     └──────────┘
```

---

## Architecture

```
ketoy-sdk/
├── Ketoy.kt                       # Entry point — initialize(), reset()
├── annotation/
│   └── KComponent.kt              # @KComponent annotation
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
├── model/
│   ├── KNode.kt                   # Sealed class — every UI element is a KNode
│   ├── KModifier.kt               # Modifier data class (padding, size, scroll…)
│   ├── KProps.kt                   # Layout & widget property classes
│   ├── KGradient.kt               # Gradient model
│   ├── KImageSource.kt            # Image source sealed class
│   ├── KTextFieldProps.kt         # TextField property classes
│   ├── KScaffoldProps.kt          # Scaffold property classes
│   ├── KComponentModels.kt        # Custom component metadata
│   ├── KetoyVariable.kt           # Variable system
│   └── AnyValueSerializer.kt      # Polymorphic serializer
├── parser/
│   ├── ModifierParser.kt          # JSON → Compose Modifier
│   ├── ColorParser.kt             # Hex / named → Compose Color
│   ├── ShapeParser.kt             # String → Compose Shape
│   ├── ArrangementParser.kt       # String → Arrangement / Alignment
│   ├── GradientParser.kt          # JSON → Brush
│   ├── TextFieldParser.kt         # TextField JSON parsing
│   └── ScaffoldParser.kt          # Scaffold JSON parsing
├── registry/
│   └── KComponentRegistry.kt      # Custom component registry
├── renderer/
│   ├── KetoyRenderer.kt           # JSONStringToUI entry-point composable
│   ├── LayoutRenderer.kt          # Column, Row, Box, LazyColumn, LazyRow
│   ├── WidgetRenderer.kt          # Text, Button, Spacer, Card, Image
│   ├── TextFieldRenderer.kt       # TextField renderer
│   ├── ScaffoldRenderer.kt        # Scaffold + all scaffold sub-components
│   └── ComponentRenderer.kt       # Custom/registered component renderer
└── util/
    ├── KConstants.kt              # KArrangements, KAlignments, KFontWeights…
    ├── KColors.kt                 # Predefined hex colour constants
    ├── KShapes.kt                 # Shape descriptor constants
    └── Helpers.kt                 # kModifier(), kPadding(), kBorder()…
```

---

## Getting Started

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
    KText("Hello, Ketoy! 🎉", fontSize = 24, fontWeight = KFontWeights.Bold)
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
<summary><b>KLazyColumn</b> — Vertically scrolling list</summary>

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

<details>
<summary><b>KLazyRow</b> — Horizontally scrolling list</summary>

```kotlin
KLazyRow(
    modifier = kModifier(fillMaxWidth = 1f),
    horizontalArrangement = "spacedBy_12"
) {
    items(categories) { cat ->
        KButton(containerColor = "#EEEDFF", shape = KShapes.Rounded16) {
            KText(cat.name)
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
    elevation = 4
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
    border = kBorder(1, "#E0E0E0")
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

// From resources
KImage(source = kImageRes("ic_logo"))
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
    keyboardOptions = KKeyboardOptions(
        keyboardType = "email",
        imeAction = "done"
    )
) {
    label { KText("Email") }
    placeholder { KText("you@example.com") }
    leadingIcon { KText("📧") }
}
```
</details>

<details>
<summary><b>KSpacer</b> — Empty space</summary>

```kotlin
KSpacer(height = 24)          // vertical space
KSpacer(width = 16)           // horizontal space
KSpacer(modifier = kModifier(weight = 1f))  // flexible space
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
                KIconButton(onClick = { /* nav back */ }) {
                    KText("←", fontSize = 20)
                }
            },
            actions = {
                KAppBarAction(onClick = { /* settings */ }) {
                    KText("⚙️", fontSize = 18)
                }
            }
        )
    },
    bottomBar = {
        KNavigationBar {
            KNavigationDrawerItem(selected = true, onClick = { }) {
                KText("Home")
            }
            KNavigationDrawerItem(selected = false, onClick = { }) {
                KText("Profile")
            }
        }
    },
    floatingActionButton = {
        KFloatingActionButton(onClick = { /* add */ }) {
            KText("+", fontSize = 24, color = KColors.White)
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
    fillMaxSize = 1f,           // Float 0..1
    fillMaxWidth = 1f,
    fillMaxHeight = 1f,
    weight = 0.5f,              // For Row/Column children
    size = 100,                 // dp
    width = 200,                // dp
    height = 150,               // dp

    // Spacing
    padding = kPadding(all = 16),
    margin = kMargin(horizontal = 8, vertical = 4),

    // Appearance
    background = "#FF5722",
    gradient = KGradients.linear(listOf("#FF5722", "#E91E63")),
    border = kBorder(width = 2, color = "#000000"),
    shape = KShapes.Rounded16,  // or "circle", "rounded_24", etc.
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
<summary><b>kPadding() / kMargin()</b> — Spacing helpers</summary>

```kotlin
kPadding(all = 16)
kPadding(horizontal = 20, vertical = 12)
kPadding(top = 8, bottom = 16, start = 12, end = 12)

kMargin(all = 8)
kMargin(horizontal = 16)
```
</details>

<details>
<summary><b>Gradient helpers</b></summary>

```kotlin
KGradients.linear(listOf("#FF5722", "#E91E63"))
KGradients.linear(listOf("#4ECDC4", "#556270"), KGradients.Directions.Horizontal)
KGradients.linearAngle(listOf("#FF0000", "#0000FF"), angleDegrees = 135f)
KGradients.radial(listOf("#FFFFFF", "#000000"), radius = 200f)
KGradients.sweep(listOf("#FF0000", "#00FF00", "#0000FF"))
```
</details>

### Constants & Utilities

<details>
<summary><b>Arrangement & Alignment</b></summary>

```kotlin
// Arrangements (for verticalArrangement / horizontalArrangement)
KArrangements.Top           // "top"
KArrangements.Bottom        // "bottom"
KArrangements.Start         // "start"
KArrangements.End           // "end"
KArrangements.Center        // "center"
KArrangements.SpaceBetween  // "spaceBetween"
KArrangements.SpaceEvenly   // "spaceEvenly"
KArrangements.SpaceAround   // "spaceAround"
// Custom spacing:  "spacedBy_8", "spacedBy_16"

// Alignments (for horizontalAlignment / verticalAlignment / contentAlignment)
KAlignments.Start               KAlignments.TopStart
KAlignments.Center              KAlignments.TopCenter
KAlignments.End                 KAlignments.TopEnd
KAlignments.Top                 KAlignments.CenterStart
KAlignments.Bottom              KAlignments.CenterEnd
KAlignments.CenterHorizontally  KAlignments.BottomStart
KAlignments.CenterVertically    KAlignments.BottomCenter
                                KAlignments.BottomEnd
```
</details>

<details>
<summary><b>Colours, Shapes & Fonts</b></summary>

```kotlin
// Predefined colours
KColors.Blue   KColors.Red     KColors.Green   KColors.Orange
KColors.Purple KColors.Teal    KColors.Gray    KColors.Black
KColors.White  KColors.Transparent
KColors.hex("FF5722")                 // normalise to #FFFF5722
KColors.withAlpha(KColors.Blue, 0.5f) // 50% alpha

// Shapes
KShapes.Circle      KShapes.Rectangle
KShapes.Rounded4    KShapes.Rounded8    KShapes.Rounded12
KShapes.Rounded16   KShapes.Rounded20   KShapes.Rounded24
KShapes.rounded(18)                   // custom radius
KShapes.rounded(topStart = 24, topEnd = 24, bottomEnd = 0, bottomStart = 0)

// Font weights
KFontWeights.Light   KFontWeights.Normal  KFontWeights.Medium
KFontWeights.SemiBold KFontWeights.Bold

// Text align
KTextAlign.Start  KTextAlign.Center  KTextAlign.End  KTextAlign.Justify
```
</details>

---

## JSON Wire Format

The DSL serialises to a standard JSON tree. You can also build this JSON on the server and render it client-side.

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
                "fontWeight": "bold",
                "color": "#1A1A2E"
            }
        },
        {
            "type": "Spacer",
            "props": { "height": 16 }
        },
        {
            "type": "Button",
            "props": {
                "containerColor": "#FF2196F3",
                "shape": "rounded_12"
            },
            "children": [
                {
                    "type": "Text",
                    "props": { "text": "Tap Me", "color": "#FFFFFFFF" }
                }
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
| `Image` | Async image (URL, res, base64) |
| `TextField` | Text input |
| `Scaffold` | Full-screen scaffold |
| `TopAppBar` | Top app bar |
| `BottomAppBar` | Bottom app bar |
| `NavigationBar` | Bottom navigation |
| `FloatingActionButton` | FAB |
| `SnackBar` | Snackbar |
| `Component` | Custom registered component |
</details>

---

## UI Samples

<details>
<summary><b>🏦 Bank Wallet Card</b></summary>

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
<summary><b>📋 Transaction List Item</b></summary>

```kotlin
KCard(
    modifier = kModifier(fillMaxWidth = 1f),
    containerColor = "#FFFFFF",
    shape = KShapes.Rounded16,
    elevation = 2
) {
    KRow(
        modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)),
        horizontalArrangement = KArrangements.SpaceBetween,
        verticalAlignment = KAlignments.CenterVertically
    ) {
        KRow(
            horizontalArrangement = "spacedBy_12",
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KBox(
                modifier = kModifier(size = 44, background = "#F0F0F0", shape = KShapes.Circle),
                contentAlignment = KAlignments.Center
            ) {
                KText("🛒", fontSize = 20)
            }
            KColumn(verticalArrangement = "spacedBy_2") {
                KText("Grocery Store", fontSize = 15, fontWeight = KFontWeights.Bold)
                KText("Today, 2:30 PM", fontSize = 12, color = "#999999")
            }
        }
        KText("- $45.20", fontSize = 15, fontWeight = KFontWeights.Bold, color = "#FF6B6B")
    }
}
```
</details>

<details>
<summary><b>🔘 Quick Action Buttons</b></summary>

```kotlin
KRow(
    modifier = kModifier(fillMaxWidth = 1f),
    horizontalArrangement = KArrangements.SpaceEvenly
) {
    listOf("💸" to "Send", "📥" to "Receive", "📄" to "Bills", "⋯" to "More")
        .forEach { (icon, label) ->
            KColumn(
                verticalArrangement = "spacedBy_8",
                horizontalAlignment = KAlignments.CenterHorizontally
            ) {
                KButton(containerColor = "#EEEDFF", shape = KShapes.Rounded16) {
                    KText(icon, fontSize = 24)
                }
                KText(label, fontSize = 12, color = "#555555")
            }
        }
}
```
</details>

<details>
<summary><b>📝 Login Form</b></summary>

```kotlin
KColumn(
    modifier = kModifier(
        fillMaxSize = 1f,
        padding = kPadding(all = 24),
        verticalScroll = true
    ),
    verticalArrangement = "spacedBy_16",
    horizontalAlignment = KAlignments.CenterHorizontally
) {
    KText("Welcome Back", fontSize = 28, fontWeight = KFontWeights.Bold)
    KSpacer(height = 8)
    KTextField(
        value = "",
        onValueChange = { },
        modifier = kModifier(fillMaxWidth = 1f),
        singleLine = true,
        keyboardOptions = KKeyboardOptions(keyboardType = "email")
    ) {
        label { KText("Email") }
        placeholder { KText("you@example.com") }
    }
    KTextField(
        value = "",
        onValueChange = { },
        modifier = kModifier(fillMaxWidth = 1f),
        singleLine = true,
        visualTransformation = KVisualTransformation(type = "password")
    ) {
        label { KText("Password") }
    }
    KSpacer(height = 8)
    KButton(
        modifier = kModifier(fillMaxWidth = 1f),
        containerColor = KColors.Blue,
        shape = KShapes.Rounded12
    ) {
        KText("Sign In", color = KColors.White, fontWeight = KFontWeights.Bold)
    }
}
```
</details>

<details>
<summary><b>🎨 Gradient Header</b></summary>

```kotlin
KBox(
    modifier = kModifier(
        fillMaxWidth = 1f,
        height = 200,
        gradient = KGradients.linear(
            colors = listOf("#6C63FF", "#3F3D9E"),
            direction = KGradients.Directions.Horizontal
        ),
        shape = KShapes.rounded(bottomStart = 32, bottomEnd = 32)
    ),
    contentAlignment = KAlignments.Center
) {
    KText(
        "Dashboard",
        fontSize = 28,
        fontWeight = KFontWeights.Bold,
        color = KColors.White
    )
}
```
</details>

---

## Contributing

We welcome contributions! Please follow these guidelines:

### Checklist

- [ ] Fork the repo and create your branch from `master`
- [ ] Follow the existing package structure (`model/`, `dsl/`, `parser/`, `renderer/`, etc.)
- [ ] Each new widget must include:
  - A `KNode` subclass in `model/KNode.kt`
  - A Props data class in `model/KProps.kt` (or its own file for complex components)
  - A DSL builder function in `dsl/KUniversalScope.kt`
  - A top-level builder in `dsl/TopLevelBuilders.kt` (if it can be a root node)
  - A renderer `@Composable` in `renderer/`
  - A parser function in `parser/` (if it has custom JSON parsing)
  - Unit tests in `src/test/`
  - UI tests in `src/androidTest/`
- [ ] All public API must have KDoc comments
- [ ] Run `./gradlew build` and confirm zero errors before submitting a PR
- [ ] Ensure min SDK 26 compatibility — no APIs that require higher versions without fallbacks

### Code Style

- Use Kotlin idioms (data classes, sealed classes, extension functions)
- Prefer `const val` for string constants
- Keep DSL function signatures consistent: `modifier` first, `content` last
- Use `internal` visibility for renderer/parser functions

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
