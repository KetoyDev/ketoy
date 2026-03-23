# Ketoy Wire Format — 10-15x SDUI Compression Pipeline

> **What this document covers:** Why plain JSON fails at scale for Server-Driven UI, how the Ketoy Wire Format (`.ktw`) compresses payloads 10-15x through four stacked layers, how each layer works internally, how the file format is built and served, and how the client decodes bytes back into a live Compose UI tree.

---

## 1. The Problem with Plain JSON for SDUI

A typical Ketoy screen — a profile screen with cards, text, icons, and a lazy list — serializes to roughly **8–15 KB of JSON**. At first glance that seems fine. But SDUI at scale multiplies the problem:

- **Repeated keys.** Every node in the tree carries `"type"`, `"props"`, `"children"`, `"modifier"`, `"backgroundColor"`, `"verticalArrangement"`, `"horizontalAlignment"`, etc. In a 100-node tree, the string `"backgroundColor"` alone can appear 30–50 times, spending **15 bytes per occurrence just on the key**.
- **Verbose type strings.** Every node broadcasts its component type as a full string: `"FloatingActionButton"` (20 chars), `"NavigationDrawerItem"` (20 chars), `"LazyColumn"` (10 chars). These strings repeat throughout the tree.
- **No binary representation.** JSON must travel as UTF-8 text. Numbers, booleans, and even small integers like `4` or `16` are encoded as multi-byte character sequences instead of single-byte values.
- **Cumulative payload.** A dashboard screen with 5 content blocks can easily exceed **40 KB uncompressed**. Over a slow mobile connection that is a noticeable delay. Over WebSocket hot-reload that is stutter on every save.

The Ketoy Wire Format addresses all of these with a four-layer pipeline that achieves **10-15x overall compression** with full backward compatibility and zero schema changes on the server.

---

## 2. The Four Layers at a Glance

```
Raw JSON string
  │
  ▼  Layer 2a — Key Aliasing           (1.5–2x reduction)
  │  "backgroundColor" → "bg"
  │  "verticalArrangement" → "va"
  │  "children" → "c"
  │
  ▼  Layer 2c — Component Type IDs     (1.2x additional reduction)
  │  "type": "Column" → "t": 1
  │  "type": "FloatingActionButton" → "t": 35
  │
  ▼  Layer 3 — MessagePack Encoding    (2–3x additional reduction)
  │  JSON text → compact binary
  │  numbers become 1–4 bytes
  │  short strings become length+bytes
  │
  ▼  Layer 1 — Gzip Compression        (~4x additional reduction)
  │  standard DEFLATE on the binary blob
  │
  ▼  Wire bytes (.ktw)
     10–15x smaller than the original JSON
```

The layers are applied in this order during encoding (and reversed during decoding). The numbering reflects priority of impact, not order of execution: key aliasing and type IDs fire first because they reduce the input to the binary encoder, making MessagePack and gzip more effective.

---

## 3. Layer 2a — Key Aliasing

**File:** `KetoyKeyAlias.kt`

### The Problem

SDUI JSON is *key-heavy*. Every `JsonObject` node in the tree carries property names that are long, human-readable strings. These strings:
- Repeat thousands of times across a typical screen tree
- Add no information value after the first occurrence (the schema is fixed)
- Cost `(key_length + 4)` bytes per field for the quotes and colon

### The Solution

A compile-time bidirectional dictionary maps every known Ketoy property name to a **1–3 character alias**:

```kotlin
// Structural (single char — highest frequency)
"type"     → "t"
"props"    → "p"
"children" → "c"

// Modifier keys (2 chars)
"modifier"            → "md"
"backgroundColor"     → "bg"
"verticalArrangement" → "va"
"horizontalAlignment" → "ha"
"padding"             → "pd"
"fillMaxSize"         → "fs"

// Text (2 chars)
"text"       → "tx"
"fontSize"   → "fz"
"fontWeight" → "fW"

// Colors (prefix pattern: f=focused, u=unfocused, d=disabled, e=error)
"focusedTextColor"   → "fTC"
"unfocusedTextColor" → "uFTC"
"disabledTextColor"  → "dTCl"
```

The full dictionary covers **130+ property names** across layouts, widgets, modifiers, scaffold, text fields, navigation, and data constructs.

### Encoding / Decoding

```kotlin
// Server side — replace full names with aliases
val aliased = KetoyKeyAlias.aliasKeys(originalJsonElement)

// Client side — restore full names from aliases
val expanded = KetoyKeyAlias.expandKeys(aliasedJsonElement)
```

Both functions recurse the entire tree. Unknown keys (custom widget properties not in the dictionary) are passed through unchanged, preserving forward compatibility.

### Effect

| Original key             | Aliased | Bytes saved |
|--------------------------|---------|-------------|
| `"backgroundColor"`      | `"bg"`  | 13 bytes    |
| `"verticalArrangement"`  | `"va"`  | 17 bytes    |
| `"horizontalAlignment"`  | `"ha"`  | 18 bytes    |
| `"contentDescription"`   | `"cd"`  | 17 bytes    |
| `"FloatingActionButton"` | (type layer) | 17 bytes |

Over a 100-node tree, key aliasing alone saves **2–5 KB**, a 1.5–2x reduction.

---

## 4. Layer 2c — Component Type IDs

**File:** `KetoyComponentTypeId.kt`

### The Problem

Every node in the SDUI tree carries a `"type"` field. This is the single most-repeated *value* in the tree (every node has exactly one). Type strings range from 3 to 20 characters:

```json
"type": "Column"                  // 6 chars
"type": "FloatingActionButton"    // 20 chars
"type": "NavigationDrawerItem"    // 20 chars
```

### The Solution

A dense integer ID table maps each component type to a 1-or-2-digit integer. IDs are assigned by component family and are stable — once assigned, an ID never changes:

```kotlin
// Layout containers: 1–10
"Column"  → 1
"Row"     → 2
"Box"     → 3

// Leaf widgets: 11–30
"Text"    → 11
"Button"  → 12
"Card"    → 14

// Scaffold: 31–60
"Scaffold"              → 31
"FloatingActionButton"  → 35
"NavigationDrawerItem"  → 38

// Data constructs: 61–70
"DataClass" → 61
"DataList"  → 63
```

After this layer, `"type": "FloatingActionButton"` (28 chars) becomes `"t": 35` (6 chars including the aliased key). When subsequently MessagePack-encoded, `35` encodes as a single byte.

### Effect

Combined with key aliasing: the field `"type": "FloatingActionButton"` goes from **28 bytes in JSON text** to **2 bytes in MessagePack** (1-byte key alias + 1-byte integer). That is a **14x reduction per occurrence** on this one field.

---

## 5. Layer 3 — MessagePack Binary Encoding

**File:** `KetoyMessagePack.kt`

### The Problem

After aliasing and type IDs, the data is still stored as JSON text. JSON text wastes bytes:
- The number `4` is 1 byte as a number but is encoded as the ASCII character `'4'` (1 byte only for single digits — but `"16"` is 2 bytes, `"255"` is 3 bytes)
- Booleans `true`/`false` are 4–5 bytes each
- Every string value needs surrounding `"..."` quotes
- Object fields need `:` separators and `,` between items

### The Solution

MessagePack is a binary serialization format that maps JSON's type system directly to compact byte representations:

| JSON value         | JSON bytes      | MessagePack bytes |
|--------------------|-----------------|-------------------|
| `null`             | 4               | 1 (nil byte)      |
| `true`             | 4               | 1                 |
| `false`            | 5               | 1                 |
| integer `0–127`    | 1–3             | 1 (positive fixint)|
| integer `128–255`  | 3               | 2 (uint8)         |
| small string `"hi"`| 4               | 3 (fixstr)        |
| map with 4 fields  | `{...}` overhead| 1 byte (fixmap)   |
| array of 5 items   | `[...]` overhead| 1 byte (fixarray) |

Ketoy's MessagePack implementation is **zero-dependency** — no Jackson, no `msgpack-core`. It implements exactly the subset needed for SDUI payloads: nil, bool, int (fixint through int64), float64, str (fixstr/str8/str16/str32), array (fixarray/array16/array32), and map (fixmap/map16/map32).

```kotlin
val json = Json.parseToJsonElement("""{"type":"Text","props":{"text":"Hello"}}""")
val packed: ByteArray = KetoyMessagePack.encode(json)
val decoded: JsonElement = KetoyMessagePack.decode(packed)
// decoded == json  ✓
```

### Effect

Over the aliased+type-compressed JSON, MessagePack encoding adds a **2–3x reduction** by eliminating all text-encoding overhead for numbers, booleans, and structural markers.

---

## 6. Layer 1 — Gzip Compression

**File:** `KetoyCompression.kt`

### Why Gzip Runs Last

Gzip (DEFLATE) finds and collapses repeating byte sequences. Applying it to the raw JSON first would already achieve 3–5x. But applying it *after* key aliasing, type IDs, and MessagePack is more effective because:

1. **MessagePack output has shorter strings** — the deduplication opportunities for the aliased keys (now `"t"`, `"p"`, `"c"`) are even denser since the same 1–2 byte sequences repeat throughout the binary blob.
2. **Integer representations are uniform** — component type IDs are now consistent 1-byte integers, making the DEFLATE sliding window more effective.
3. **Fewer unique byte sequences** — with 130+ property names collapsed to short aliases and 25+ type strings to integers, the byte vocabulary is dramatically narrowed, improving DEFLATE's LZ77 compression.

### Implementation

Standard Java `GZIPOutputStream` / `GZIPInputStream`. Detection via magic bytes `0x1F 0x8B`:

```kotlin
fun isGzipped(data: ByteArray): Boolean =
    data.size >= 2 && data[0] == 0x1F.toByte() && data[1] == 0x8B.toByte()

fun autoDecompress(data: ByteArray): ByteArray =
    if (isGzipped(data)) gunzip(data) else data
```

### Effect

Over the MessagePack-encoded aliased data: **3–5x additional reduction**. Combined total: **10–15x** over the original JSON.

---

## 7. Cumulative Compression Numbers

For a typical mid-complexity profile screen (100 nodes, mix of text, cards, icons, lazy list):

| Stage                        | Typical size  | Reduction from previous |
|------------------------------|---------------|-------------------------|
| Raw pretty-print JSON        | ~14,000 bytes | —                       |
| Minified JSON                | ~9,500 bytes  | 1.5x                    |
| + Key aliasing               | ~5,200 bytes  | 1.8x                    |
| + Type IDs                   | ~4,100 bytes  | 1.3x                    |
| + MessagePack                | ~1,900 bytes  | 2.2x                    |
| + Gzip                       | **~750 bytes**| 2.5x                    |
| **Total (raw → .ktw)**       | **~750 bytes**| **~12-13x**             |

For larger, deeply nested screens the ratio pushes toward **15x**. For very small screens (few nodes) it may be closer to **8x** because gzip has less to work with.

---

## 8. The `.ktw` File Format

`.ktw` stands for **Ketoy Wire** — the on-disk/on-wire binary container produced by the export pipeline.

### Why a Custom Extension

- **Unambiguous content detection.** The file watcher, ScreenManager, and CI pipelines all need to distinguish wire format blobs from plain JSON. A dedicated extension makes this O(1) — no sniffing required.
- **Tooling ergonomics.** IDE plugins, git attributes, and server MIME type mappings can be scoped precisely without pattern-matching heuristics.
- **Format versioning.** Future wire format versions can introduce new extensions (`.ktw2`) while the server remains backward compatible with old clients that only know `.ktw`.
- **Separation of concerns.** `.json` files remain human-readable source artifacts. `.ktw` files are build outputs — committed only if the project chooses to check in generated assets.

### File Structure

A `.ktw` file is a flat byte sequence (no header, no framing):

```
[ gzip magic: 0x1F 0x8B | compressed MessagePack payload ]
```

The `autoDecode()` function reconstructs the original `JsonElement` without needing to know the configuration used at encode time:

```kotlin
fun autoDecode(data: ByteArray): JsonElement {
    val decompressed = KetoyCompression.autoDecompress(data)   // gzip if magic bytes present
    val element = if (isMessagePack(decompressed)) {
        KetoyMessagePack.decode(decompressed)                  // binary → JsonElement
    } else {
        Json.parseToJsonElement(decompressed.toString(Charsets.UTF_8))  // fallback: plain JSON
    }
    val typeExpanded = KetoyComponentTypeId.expandTypes(element)  // int IDs → type strings
    return KetoyKeyAlias.expandKeys(typeExpanded)                  // aliases → full keys
}
```

**MessagePack detection heuristic:** MessagePack maps/arrays start with bytes outside the ranges used by JSON (`{` = 0x7B, `[` = 0x5B, whitespace). Any first byte that is not one of `{`, `[`, space, tab, `\n`, `\r` is treated as MessagePack.

### Naming Convention

Files are named `${screenName}_${contentName}.ktw`:

- `profile_main.ktw` — the `"main"` content block of the `"profile"` screen
- `home_cards.ktw` — the `"cards"` content block of the `"home"` screen
- `dashboard_header.ktw` — the `"header"` content block of the `"dashboard"` screen

Multi-content screens produce one `.ktw` file per content block. The `_` separator between screen name and content name is what allows the dev server to reconstruct the `screenName → contentName` routing when broadcasting updates to the app.

---

## 9. The Encoding Pipeline (Export / Dev Server Side)

```
KScreen DSL (Kotlin code)
  │  val profileExport = ketoyExport("profile") {
  │      content { buildProfileScreen() }
  │  }
  │
  ▼  KetoyAutoExportRunner.exportAllWire()
  │  1. Calls content.nodeBuilder() → KNode tree
  │  2. Calls KNode.toWireBytes(WireFormatConfig.OPTIMIZED)
  │     └─ KNode.toMinifiedJson()        → compact JSON string
  │        └─ KetoyWireFormat.encode()
  │           ├─ Layer 2a: KetoyKeyAlias.aliasKeys()
  │           ├─ Layer 2c: KetoyComponentTypeId.compressTypes()
  │           ├─ Layer 3:  KetoyMessagePack.encode()
  │           └─ Layer 1:  KetoyCompression.gzip()
  │
  ▼  profile_main.ktw written to ketoy-screens/
  │
  ▼  ScreenManager.loadScreen(file)
  │  Reads bytes, Base64-encodes for in-memory storage
  │  Tracks name as "profile_main" (filename without extension)
  │
  ▼  FileWatcher detects change → KetoyDevServer.broadcastUpdate()
     Sends over WebSocket:
     {
       "type": "update",
       "screen": "profile_main",
       "version": 12,
       "format": "ktw",
       "data": "<base64-encoded .ktw bytes>"
     }
```

### WireFormatConfig Presets

| Preset        | Layers active                              | Reduction  |
|---------------|--------------------------------------------|------------|
| `NONE`        | none — raw JSON                            | 1x         |
| `GZIP_ONLY`   | gzip only                                  | 3–5x       |
| `ALIASED`     | gzip + key aliasing + type IDs             | 7–8x       |
| `OPTIMIZED`   | all four layers (default for production)   | 10–15x     |

---

## 10. The Decoding Pipeline (Client / App Side)

```
WebSocket message arrives (Base64 string)
  │
  ▼  KetoyDevClient.handleServerMessage()
  │  Detects "format":"ktw"
  │  Base64.decode(b64) → raw .ktw ByteArray
  │  Stores: screenBytes["profile_main"] = bytes
  │          screens["profile_main"] = "__wire__" (sentinel)
  │  Increments: dataVersion.value++
  │
  ▼  KetoyDevWrapper recomposes (dv key changed)
  │  LaunchedEffect(dv, screens.keys, registeredRoutes) fires
  │  Resolves "profile_main" → screen="profile", content="main"
  │  Calls: screen.setDevOverrideBytes("main", bytes)
  │  Stores in: _devOverrideBytes["main"] = bytes
  │
  ▼  KetoyScreen.ContentInternal("main") recomposes
  │  Reads: _devOverrideBytes["main"] ← non-null
  │  Calls: JSONBytesToUI(data = bytes)
  │
  ▼  JSONBytesToUI()
  │  val element = KetoyWireFormat.autoDecode(data)
  │  ├─ KetoyCompression.autoDecompress()   [Layer 1 reverse]
  │  ├─ KetoyMessagePack.decode()            [Layer 3 reverse]
  │  ├─ KetoyComponentTypeId.expandTypes()   [Layer 2c reverse]
  │  └─ KetoyKeyAlias.expandKeys()           [Layer 2a reverse]
  │  val component = Json.decodeFromJsonElement<UIComponent>(element)
  │
  ▼  KetoyThemeProvider { RenderComponent(component) }
     Live Compose UI updated instantly
```

### Auto-Detection — Why It Matters

`autoDecode()` never requires the client to know which `WireFormatConfig` was used at encode time. It inspects the bytes:

1. **Gzip?** Check magic bytes `0x1F 0x8B` → decompress first.
2. **MessagePack?** Check if first byte is outside JSON start characters → binary decode.
3. **JSON?** Parse as UTF-8 text.
4. **Always** attempt type expansion and key expansion — safe because unknown IDs/aliases are passed through unchanged.

This means old clients receiving `.ktw` files encoded with future configurations still degrade gracefully (they will see unexpanded IDs or aliases rather than crashing).

---

## 11. Impact on Large-Scale SDUI

### Bandwidth

| Screen complexity | JSON (uncompressed) | `.ktw`  | Saving         |
|-------------------|---------------------|---------|----------------|
| Simple (30 nodes) | ~3.5 KB             | ~300 B  | ~3.2 KB / push |
| Medium (100 nodes)| ~14 KB              | ~1 KB   | ~13 KB / push  |
| Complex (300 nodes)| ~42 KB             | ~3 KB   | ~39 KB / push  |
| Full app bundle (10 screens) | ~140 KB | ~10 KB | ~130 KB saved  |

On a typical LTE connection (10 Mbps down), a 140 KB JSON bundle takes ~112 ms. The same content as `.ktw` (~10 KB) takes ~8 ms. That is over **100 ms of perceived launch latency removed** — before any rendering starts.

### Hot-Reload Latency

With `.ktw` + WebSocket, a save-to-visible-update cycle is:

1. Gradle file watcher detects `.kt` change → runs `ketoyExport` (~50–200 ms for DSL compilation)
2. FileWatcher detects `.ktw` change → broadcasts immediately
3. WebSocket message received on device → bytes decoded in ~1 ms
4. Compose recomposition → frame update in next vsync (~16 ms)

Total: **~100–300 ms from save to screen update**, constrained by the Kotlin compile step, not by transfer or decoding.

### Memory

`.ktw` files stored in the on-device cache occupy **10–15x less space** than equivalent JSON. A 10-screen app that previously cached ~1.4 MB of JSON now caches ~100 KB — well within typical Android app data budgets.

### CPU

Decoding cost is minimal:
- Gzip decompression: O(n) on the compressed size — negligible for <10 KB blobs
- MessagePack decode: single-pass binary traversal, ~2–5x faster than JSON parsing
- Key expansion: one-pass map lookup per node property
- Total decode time for a 100-node screen: **< 2 ms** on mid-range hardware

This is important for on-device rendering where frame budget is 16 ms. JSON parsing a 14 KB string can take 8–12 ms; `.ktw` decoding for the same content takes < 2 ms, leaving 10+ ms more for layout and draw.

---

## 12. Backward Compatibility

The pipeline is designed to be append-only and backward-safe:

- **Key alias dictionary:** New aliases are appended. Old aliases never change. Clients with older alias tables will see unrecognized short keys passed through unchanged (Kotlin's `ignoreUnknownKeys = true` handles this in the renderer).
- **Type ID table:** IDs are appended at the end of each range. Existing IDs never change. Unknown integer type IDs expand to their integer string representation, which the renderer will log as `"Unknown component: 35"` rather than crashing.
- **Format detection:** `autoDecode()` handles all known past and present format combinations. A client that only knows gzip+JSON will still work if the server sends gzip+JSON (`ALIASED` preset without MessagePack).
- **Sentinel value:** When a `.ktw` file is in use, `screens["profile_main"] = "__wire__"` ensures the dev client's screen map correctly represents that the screen exists, without accidentally treating the base64 blob as JSON.

---

## 13. File Map

| File | Role |
|------|------|
| `wire/KetoyWireFormat.kt` | Pipeline orchestrator — `encode()`, `decode()`, `autoDecode()`, `measureCompression()` |
| `wire/KetoyKeyAlias.kt` | Layer 2a — 130+ key alias dictionary, `aliasKeys()` / `expandKeys()` |
| `wire/KetoyComponentTypeId.kt` | Layer 2c — 25+ component type→int table, `compressTypes()` / `expandTypes()` |
| `wire/KetoyMessagePack.kt` | Layer 3 — zero-dependency MessagePack encoder/decoder |
| `wire/KetoyCompression.kt` | Layer 1 — gzip/gunzip, magic byte detection, `autoDecompress()` |
| `wire/KetoyPatch.kt` | Layer 4 — JSON Patch delta updates (incremental hot-reload) |
| `core/KetoyJsonUtils.kt` | `KNode.toWireBytes()`, `parseKetoyWireBytes()` — top-level DSL API |
| `renderer/KetoyRenderer.kt` | `JSONBytesToUI()` — entry point for wire bytes → Compose UI |
| `screen/KetoyScreen.kt` | `setDevOverrideBytes()` — injects wire bytes into per-content slots |
| `devtools/KetoyDevWrapper.kt` | Bridges client→screen: splits `profile_main` → screen+content, injects bytes |
| `devtools/KetoyDevClient.kt` | WebSocket client, Base64 decode, `screenBytes` map |
| `export/KetoyAutoExportRunner.kt` | `exportAllWire()` — writes `${screen}_${content}.ktw` to disk |
| `gradle/server/ScreenManager.kt` | Loads `.ktw` files, tracks wire screen names |
| `gradle/server/FileWatcher.kt` | Detects `.ktw` changes, triggers `broadcastUpdate()` |
| `gradle/server/KetoyDevServer.kt` | WebSocket broadcast with `"format":"ktw"` and Base64 payload |
