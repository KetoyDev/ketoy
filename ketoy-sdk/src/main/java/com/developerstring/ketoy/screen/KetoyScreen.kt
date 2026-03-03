package com.developerstring.ketoy.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.cloud.KetoyCloudService
import com.developerstring.ketoy.cloud.cache.KetoyCacheStrategy
import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.model.KNode
import com.developerstring.ketoy.renderer.JSONStringToUI
import com.developerstring.ketoy.theme.KetoyColorScheme
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * [CompositionLocal][androidx.compose.runtime.CompositionLocal] that provides
 * the current [KetoyScreen] to child composables.
 *
 * Set automatically by [ProvideKetoyScreen] so that [KetoyContent] can
 * look up its parent screen without an explicit parameter.
 *
 * ```kotlin
 * @Composable
 * fun MyWidget() {
 *     val screen = LocalKetoyScreen.current
 *         ?: error("Must be inside a ProvideKetoyScreen block")
 *     Text("Screen: ${screen.screenName}")
 * }
 * ```
 *
 * @see ProvideKetoyScreen
 * @see KetoyContent
 */
val LocalKetoyScreen = staticCompositionLocalOf<KetoyScreen?> { null }

/**
 * Composable that creates (or retrieves) a [KetoyScreen] and provides it
 * to child composables via [LocalKetoyScreen].
 *
 * This is the **primary entry point** for every Ketoy screen. Wrapping your
 * screen content with `ProvideKetoyScreen` enables child [KetoyContent]
 * blocks to self-register and resolve their DSL/JSON/Cloud content.
 *
 * If a screen with the given [screenName] already exists in
 * [KetoyScreenRegistry], it is reused; otherwise a new [KetoyScreen] is
 * created and registered automatically.
 *
 * ## Single-content screen
 * ```kotlin
 * @Composable
 * fun HomeScreen() {
 *     ProvideKetoyScreen(screenName = "home") {
 *         KetoyContent(nodeBuilder = { buildHomeUI() })
 *     }
 * }
 * ```
 *
 * ## Multi-content screen (mixed Compose + DSL)
 * ```kotlin
 * @Composable
 * fun DashboardScreen() {
 *     ProvideKetoyScreen(screenName = "dashboard") {
 *         KetoyContent(name = "cards", nodeBuilder = { buildCards() })
 *         Text("Native Compose expenses section")
 *         KetoyContent(name = "transactions", nodeBuilder = { buildTxns() })
 *         Button(onClick = {}) { Text("Compose Button") }
 *     }
 * }
 * ```
 *
 * @param screenName   Unique screen identifier used for cloud lookup,
 *                     dev-server hot reload, and JSON export.
 * @param cloudEnabled Whether Ketoy Cloud fetching is enabled for this screen.
 *                     Defaults to `true`.
 * @param colorScheme  Optional [KetoyColorScheme] override for DSL rendering.
 * @param content      The screen’s composable body. Typically contains one or
 *                     more [KetoyContent] blocks and/or native Compose code.
 * @see KetoyScreen
 * @see KetoyContent
 * @see KetoyScreenRegistry
 * @see LocalKetoyScreen
 */
@Composable
fun ProvideKetoyScreen(
    screenName: String,
    cloudEnabled: Boolean = true,
    colorScheme: KetoyColorScheme? = null,
    content: @Composable () -> Unit
) {
    val screen = remember(screenName) {
        KetoyScreenRegistry.get(screenName)
            ?: KetoyScreen(
                screenName = screenName,
                cloudEnabled = cloudEnabled,
                colorScheme = colorScheme
            ).also { KetoyScreenRegistry.register(it) }
    }
    CompositionLocalProvider(LocalKetoyScreen provides screen) {
        content()
    }
}

/**
 * Represents a server-driven screen in the Ketoy SDUI framework.
 *
 * A single `KetoyScreen` can hold **multiple content entries** identified
 * by a unique `name` (default `"main"`). This allows a `@KScreen` composable
 * to contain several [KetoyContent] blocks that are exported together
 * into a single JSON file.
 *
 * Screen-wide metadata ([screenName], [displayName], [description],
 * [version]) lives here, not in individual content blocks.
 *
 * ## Content resolution order (inside [Content]):
 * 1. **Dev-server override** — hot-reload JSON injected via [setDevOverride].
 * 2. **Cloud** — fetches from Ketoy Cloud (when [cloudEnabled] and SDK cloud is configured).
 * 3. **Local JSON** — from the content entry’s [ContentEntry.jsonContent].
 * 4. **Asset** — loads JSON from a local asset file ([ContentEntry.assetPath]).
 * 5. **Composable** — renders a `@Composable` lambda directly.
 * 6. **DSL fallback** — `nodeBuilder` or `dslBuilder` → serialised to JSON.
 * 7. **Empty** — placeholder text.
 *
 * ## Factory methods
 * | Method            | Source                        |
 * |-------------------|-------------------------------|
 * | [create]          | Inline DSL builder            |
 * | [fromNode]        | `() -> KNode` function        |
 * | [fromJson]        | Raw JSON string               |
 * | [fromComposable]  | `@Composable` lambda          |
 * | [fromAsset]       | Local asset file path         |
 *
 * ## Usage — single content
 * ```kotlin
 * @Composable
 * fun HomeScreen() {
 *     ProvideKetoyScreen(screenName = "home") {
 *         KetoyContent(nodeBuilder = { buildHomeUI() })
 *     }
 * }
 * ```
 *
 * ## Usage — mixed Compose + DSL
 * ```kotlin
 * @Composable
 * fun DashboardScreen() {
 *     ProvideKetoyScreen(screenName = "dashboard") {
 *         KetoyContent(name = "cards", nodeBuilder = { buildCards() })
 *         Text("Native Compose expenses section")
 *         KetoyContent(name = "transactions", nodeBuilder = { buildTxns() })
 *         Button(onClick = {}) { Text("Compose Button") }
 *     }
 * }
 * ```
 *
 * ## Export JSON format (multi-content)
 * ```json
 * {
 *   "screenName": "dashboard",
 *   "displayName": "Dashboard",
 *   "version": "1.0.0",
 *   "contents": {
 *     "cards":        { "type": "Column", ... },
 *     "transactions": { "type": "LazyColumn", ... }
 *   }
 * }
 * ```
 *
 * @property screenName  Unique identifier used for cloud fetching, dev-server
 *                       hot reload, and JSON export (e.g. `"home_screen"`).
 * @property displayName Human-readable name derived from [screenName] by
 *                       default (underscores replaced, first char capitalised).
 * @property description Optional description of the screen’s purpose.
 * @property version     Semantic version string (e.g. `"1.0.0"`).
 * @property cloudEnabled Whether cloud fetching is enabled for this screen.
 * @property colorScheme Optional [KetoyColorScheme] override applied when
 *                       rendering the DSL or JSON UI tree.
 * @see ProvideKetoyScreen
 * @see KetoyContent
 * @see KetoyScreenRegistry
 * @see KetoyCloudService
 */
class KetoyScreen(
    val screenName: String,
    val displayName: String = screenName.replace("_", " ")
        .replaceFirstChar { it.uppercaseChar() },
    val description: String = "",
    val version: String = "1.0.0",
    val cloudEnabled: Boolean = true,
    val colorScheme: KetoyColorScheme? = null
) {
    // ── Content entries ─────────────────────────────────────────

    /**
     * A single content block within this screen.
     *
     * Each entry represents one [KetoyContent] region that can be
     * independently rendered or exported.
     *
     * @property name              Unique identifier within the parent screen
     *                             (default `"main"`).
     * @property jsonContent       Static JSON string for the UI tree; `null`
     *                             when content is provided via DSL or composable.
     * @property dslBuilder        Inline DSL builder using [KUniversalScope].
     * @property nodeBuilder       Lambda returning a [KNode] tree directly.
     *                             Takes precedence over [dslBuilder].
     * @property composableBuilder A `@Composable` lambda rendered as-is.
     * @property assetPath         Path to a local JSON asset file.
     * @see KetoyScreen.addContent
     */
    data class ContentEntry(
        val name: String,
        val jsonContent: String? = null,
        val dslBuilder: (KUniversalScope.() -> Unit)? = null,
        val nodeBuilder: (() -> KNode)? = null,
        val composableBuilder: (@Composable () -> Unit)? = null,
        val assetPath: String? = null
    ) {
        /**
         * Build the DSL to a [KNode] tree.
         *
         * Prefers [nodeBuilder] over [dslBuilder]. Returns `null` if
         * neither builder is set.
         *
         * @return The root [KNode], or `null`.
         */
        fun buildNode(): KNode? {
            nodeBuilder?.let { return it() }
            val builder = dslBuilder ?: return null
            val scope = KUniversalScope()
            scope.builder()
            return scope.children.firstOrNull()
        }

        /**
         * Build the DSL to a JSON string.
         *
         * Delegates to [buildNode] and serialises the resulting [KNode]
         * via [KNode.toJson][com.developerstring.ketoy.core.toJson].
         *
         * @return JSON string, or `null` if no builder is set.
         */
        fun buildJson(): String? = buildNode()?.toJson()
    }

    private val _contents = mutableMapOf<String, ContentEntry>()

    /** Read-only view of all content entries keyed by name. */
    val contents: Map<String, ContentEntry> get() = _contents.toMap()

    /** Dev-server override JSONs, one per content name. */
    private val _devOverrides = mutableStateMapOf<String, String>()

    // ── Legacy single-content compat ────────────────────────────

    /**
     * Legacy: get/set the `"main"` content’s dev-server override JSON.
     *
     * Prefer [setDevOverride] for the multi-content API.
     */
    var devOverrideJson: String?
        get() = _devOverrides["main"]
        set(value) {
            if (value != null) _devOverrides["main"] = value
            else _devOverrides.remove("main")
        }

    /** Legacy: routeName alias for [screenName]. */
    val routeName: String get() = screenName

    /** Legacy: main content's dslBuilder. */
    val dslBuilder: (KUniversalScope.() -> Unit)?
        get() = _contents["main"]?.dslBuilder

    /** Legacy: main content's nodeBuilder. */
    val nodeBuilder: (() -> KNode)?
        get() = _contents["main"]?.nodeBuilder

    // ── Content management ──────────────────────────────────────

    /**
     * Add (or replace) a content entry in this screen.
     *
     * ```kotlin
     * screen.addContent(
     *     name = "header",
     *     nodeBuilder = { buildHeaderNode() }
     * )
     * ```
     *
     * @param name              Unique content identifier (default `"main"`).
     * @param jsonContent       Static JSON string for the UI tree.
     * @param dslBuilder        Inline DSL builder.
     * @param nodeBuilder       Lambda returning a [KNode] tree.
     * @param composableBuilder A `@Composable` lambda rendered as-is.
     * @param assetPath         Path to a local JSON asset file.
     * @return This [KetoyScreen] (for chaining).
     */
    fun addContent(
        name: String = "main",
        jsonContent: String? = null,
        dslBuilder: (KUniversalScope.() -> Unit)? = null,
        nodeBuilder: (() -> KNode)? = null,
        composableBuilder: (@Composable () -> Unit)? = null,
        assetPath: String? = null
    ): KetoyScreen {
        _contents[name] = ContentEntry(
            name = name,
            jsonContent = jsonContent,
            dslBuilder = dslBuilder,
            nodeBuilder = nodeBuilder,
            composableBuilder = composableBuilder,
            assetPath = assetPath
        )
        return this
    }

    /**
     * Get a content entry by its ID.
     *
     * @param name Content identifier (default `"main"`).
     * @return The [ContentEntry], or `null` if not found.
     */
    fun getContent(name: String = "main"): ContentEntry? = _contents[name]

    /**
     * All content names registered in this screen.
     *
     * @return Immutable [Set] of content identifiers.
     */
    fun contentNames(): Set<String> = _contents.keys.toSet()

    /**
     * Set a dev-server override for a specific content block.
     *
     * Passing `null` removes the override.
     *
     * @param name Content identifier (default `"main"`).
     * @param json Raw JSON string, or `null` to clear.
     */
    fun setDevOverride(name: String = "main", json: String?) {
        if (json != null) _devOverrides[name] = json
        else _devOverrides.remove(name)
    }

    /**
     * Set a screen-level dev-server override from JSON that uses the
     * `{ "contents": { "name": {...} } }` wrapper format.
     *
     * Automatically distributes overrides to individual content entries
     * when the wrapper format is detected. Falls back to storing as a
     * screen-level override for legacy flat JSON.
     *
     * @param json The screen-level JSON override string, or `null` to clear.
     */
    fun setScreenDevOverride(json: String?) {
        if (json == null) {
            _devOverrides.remove("__screen__")
            return
        }
        try {
            val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(json)
            val contents = jsonElement.jsonObject["contents"]?.jsonObject
            if (contents != null) {
                // Distribute per-content overrides
                contents.forEach { (contentName, contentJson) ->
                    _devOverrides[contentName] = contentJson.toString()
                }
            } else {
                // Legacy flat format — store as screen-level
                _devOverrides["__screen__"] = json
            }
        } catch (_: Exception) {
            _devOverrides["__screen__"] = json
        }
    }

    /**
     * Get the dev-server override JSON for a specific content block.
     *
     * @param name Content identifier (default `"main"`).
     * @return The override JSON string, or `null` if none is set.
     */
    fun getDevOverride(name: String = "main"): String? = _devOverrides[name]

    // ── JSON building ───────────────────────────────────────────

    /**
     * Build the `"main"` content’s DSL to a JSON string.
     *
     * Backward-compatible with the old single-content API.
     *
     * @return JSON string, or `null` if no builder is set.
     */
    fun buildDslJson(): String? = _contents["main"]?.buildJson()

    /**
     * Build the `"main"` content’s DSL to a [KNode] tree.
     *
     * @return Root [KNode], or `null` if no builder is set.
     */
    fun buildDslNode(): KNode? = _contents["main"]?.buildNode()

    /**
     * Build all contents’ DSL to a JSON map (name → json).
     *
     * @return Map of content name to its JSON string (values may be `null`).
     */
    fun buildAllJson(): Map<String, String?> =
        _contents.mapValues { (_, entry) -> entry.buildJson() }

    /**
     * Builds the export JSON for this screen.
     *
     * Always uses the multi-content wrapper format:
     * ```json
     * {
     *   "screenName": "dashboard",
     *   "displayName": "Dashboard",
     *   "description": "Main app dashboard",
     *   "version": "1.0.0",
     *   "contents": {
     *     "header": { "type": "Row", ... },
     *     "body":   { "type": "Column", ... }
     *   }
     * }
     * ```
     *
     * @return The export JSON string, or `null` if no content has a builder.
     */
    fun buildExportJson(): String? {
        val allJson = buildAllJson().filterValues { it != null }
        if (allJson.isEmpty()) return null

        // Always use the contents wrapper — each KetoyContent is a named block
        val contentsBlock = allJson.entries.joinToString(",\n    ") { (id, json) ->
            "\"$id\": $json"
        }
        return buildString {
            appendLine("{")
            appendLine("  \"screenName\": \"$screenName\",")
            appendLine("  \"displayName\": \"$displayName\",")
            if (description.isNotBlank()) appendLine("  \"description\": \"$description\",")
            appendLine("  \"version\": \"$version\",")
            appendLine("  \"contents\": {")
            appendLine("    $contentsBlock")
            appendLine("  }")
            append("}")
        }
    }

    // ── Rendering ───────────────────────────────────────────────

    /**
     * Renders a specific content entry using the full resolution order:
     * devOverride → cloud → local JSON → asset → composable → DSL → empty.
     *
     * Provides this screen via [LocalKetoyScreen] so child composables
     * can access screen context.
     *
     * @param name           Which content block to render (default `"main"`).
     * @param colorScheme    Optional [KetoyColorScheme] override.
     * @param loadingContent Composable shown while fetching cloud data.
     * @param errorContent   Composable shown on error; receives a message
     *                       and a `retry` callback.
     */
    @Composable
    fun Content(
        name: String = "main",
        colorScheme: KetoyColorScheme? = null,
        loadingContent: @Composable () -> Unit = { DefaultLoading() },
        errorContent: @Composable (error: String, retry: () -> Unit) -> Unit = { msg, retry ->
            DefaultError(msg, retry)
        }
    ) {
        // Provide this screen to child composables via LocalKetoyScreen
        CompositionLocalProvider(LocalKetoyScreen provides this) {
            ContentInternal(name, colorScheme, loadingContent, errorContent)
        }
    }

    @Composable
    internal fun ContentInternal(
        name: String,
        colorScheme: KetoyColorScheme?,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (error: String, retry: () -> Unit) -> Unit
    ) {
        val entry = _contents[name]

        // 1. Dev-server override (hot-reload)
        val devJson = _devOverrides[name]
        if (devJson != null) {
            key(devJson) {
                JSONStringToUI(value = devJson, colorScheme = colorScheme)
            }
            return
        }

        // Also check if we have a screen-level dev override with contents wrapper
        val screenDevJson = _devOverrides["__screen__"]
        if (screenDevJson != null) {
            val contentJson = extractContentFromScreenJson(screenDevJson, name)
            if (contentJson != null) {
                key(contentJson) {
                    JSONStringToUI(value = contentJson, colorScheme = colorScheme)
                }
                return
            }
        }

        // 2. Cloud fetch
        if (cloudEnabled && com.developerstring.ketoy.Ketoy.isCloudEnabled()) {
            val fallbackJson = entry?.jsonContent ?: entry?.buildJson()
            CloudContent(screenName, name, colorScheme, loadingContent, errorContent, fallbackJson)
            return
        }

        if (entry == null) {
            Text("Empty content: $screenName/$name")
            return
        }

        // 3. Local JSON
        if (entry.jsonContent != null) {
            JSONStringToUI(value = entry.jsonContent, colorScheme = colorScheme)
            return
        }

        // 4. Asset fallback
        if (entry.assetPath != null) {
            AssetScreenContent(assetPath = entry.assetPath, colorScheme = colorScheme)
            return
        }

        // 5. Composable fallback
        val composable = entry.composableBuilder
        if (composable != null) {
            composable()
            return
        }

        // 6. DSL fallback
        val dslJson = remember { entry.buildJson() }
        if (dslJson != null) {
            JSONStringToUI(value = dslJson, colorScheme = colorScheme)
            return
        }

        // 7. Empty
        Text("Empty content: $screenName/$name")
    }

    /**
     * Extracts a named content block from a screen-level JSON wrapper.
     *
     * Supports the `{ "contents": { "name": {...} } }` format.
     *
     * @param screenJson   The full screen-level JSON string.
     * @param contentName  The content block to extract.
     * @return The content JSON string, or `null` if not found.
     */
    private fun extractContentFromScreenJson(screenJson: String, contentName: String): String? {
        return try {
            val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(screenJson)
            val contents = jsonElement.jsonObject["contents"]?.jsonObject ?: return null
            contents[contentName]?.toString()
        } catch (_: Exception) {
            null
        }
    }

    companion object {

        /**
         * Create a screen with a single `"main"` content from an inline DSL builder.
         *
         * ```kotlin
         * val screen = KetoyScreen.create("home") {
         *     KColumn {
         *         KText("Hello World")
         *     }
         * }
         * ```
         *
         * @param screenName   Unique screen identifier.
         * @param cloudEnabled Whether cloud fetching is enabled.
         * @param displayName  Human-readable display name.
         * @param description  Optional description.
         * @param version      Semantic version string.
         * @param dslBuilder   Inline DSL builder using [KUniversalScope].
         * @return A new [KetoyScreen] with a single `"main"` content entry.
         */
        fun create(
            screenName: String,
            cloudEnabled: Boolean = true,
            displayName: String = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() },
            description: String = "",
            version: String = "1.0.0",
            dslBuilder: KUniversalScope.() -> Unit
        ) = KetoyScreen(
            screenName = screenName,
            displayName = displayName,
            description = description,
            version = version,
            cloudEnabled = cloudEnabled
        ).addContent(name = "main", dslBuilder = dslBuilder)

        /**
         * Create a screen from a function that returns a [KNode] directly.
         *
         * The node builder is added as the `"main"` content entry.
         *
         * ```kotlin
         * val screen = KetoyScreen.fromNode("profile") {
         *     buildProfileNode()
         * }
         * ```
         *
         * @param screenName   Unique screen identifier.
         * @param cloudEnabled Whether cloud fetching is enabled (default `false`).
         * @param displayName  Human-readable display name.
         * @param description  Optional description.
         * @param version      Semantic version string.
         * @param nodeBuilder  Lambda returning the root [KNode].
         * @return A new [KetoyScreen] with a single `"main"` content entry.
         */
        fun fromNode(
            screenName: String,
            cloudEnabled: Boolean = false,
            displayName: String = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() },
            description: String = "",
            version: String = "1.0.0",
            nodeBuilder: () -> KNode
        ) = KetoyScreen(
            screenName = screenName,
            displayName = displayName,
            description = description,
            version = version,
            cloudEnabled = cloudEnabled
        ).addContent(name = "main", nodeBuilder = nodeBuilder)

        /**
         * Create a screen from a raw JSON string.
         *
         * Supports both formats:
         * - **Multi-content wrapper:**
         *   ```json
         *   { "screenName": "...", "contents": { "header": {...}, "body": {...} } }
         *   ```
         * - **Legacy single-content:** `{ "type": "Column", ... }` — added as `"main"`.
         *
         * ```kotlin
         * val screen = KetoyScreen.fromJson("home", homeJsonString)
         * ```
         *
         * @param screenName   Unique screen identifier.
         * @param json         Raw JSON string.
         * @param displayName  Human-readable display name.
         * @param description  Optional description.
         * @param version      Semantic version string.
         * @return A new [KetoyScreen] with one or more content entries
         *         parsed from the JSON.
         */
        fun fromJson(
            screenName: String,
            json: String,
            displayName: String = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() },
            description: String = "",
            version: String = "1.0.0"
        ): KetoyScreen {
            // Try to parse as the multi-content wrapper format
            try {
                val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(json)
                val obj = jsonElement.jsonObject
                val contents = obj["contents"]?.jsonObject
                if (contents != null) {
                    val resolvedName = obj["screenName"]?.jsonPrimitive?.contentOrNull ?: screenName
                    val resolvedDisplay = obj["displayName"]?.jsonPrimitive?.contentOrNull ?: displayName
                    val resolvedDesc = obj["description"]?.jsonPrimitive?.contentOrNull ?: description
                    val resolvedVersion = obj["version"]?.jsonPrimitive?.contentOrNull ?: version
                    val screen = KetoyScreen(
                        screenName = resolvedName,
                        displayName = resolvedDisplay,
                        description = resolvedDesc,
                        version = resolvedVersion
                    )
                    contents.forEach { (contentName, contentJson) ->
                        screen.addContent(name = contentName, jsonContent = contentJson.toString())
                    }
                    return screen
                }
            } catch (_: Exception) {
                // Fall through to legacy single-content
            }

            // Legacy single-content format
            return KetoyScreen(
                screenName = screenName,
                displayName = displayName,
                description = description,
                version = version
            ).addContent(name = "main", jsonContent = json)
        }

        /**
         * Create a screen backed by a `@Composable` lambda (as the `"main"` content).
         *
         * ```kotlin
         * val screen = KetoyScreen.fromComposable("settings") {
         *     SettingsPageComposable()
         * }
         * ```
         *
         * @param screenName   Unique screen identifier.
         * @param cloudEnabled Whether cloud fetching is enabled.
         * @param displayName  Human-readable display name.
         * @param description  Optional description.
         * @param version      Semantic version string.
         * @param composable   The `@Composable` lambda to render.
         * @return A new [KetoyScreen] with a single `"main"` content entry.
         */
        fun fromComposable(
            screenName: String,
            cloudEnabled: Boolean = true,
            displayName: String = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() },
            description: String = "",
            version: String = "1.0.0",
            composable: @Composable () -> Unit
        ) = KetoyScreen(
            screenName = screenName,
            displayName = displayName,
            description = description,
            version = version,
            cloudEnabled = cloudEnabled
        ).addContent(name = "main", composableBuilder = composable)

        /**
         * Create a screen that loads its JSON from a local asset file (as `"main"` content).
         *
         * ```kotlin
         * val screen = KetoyScreen.fromAsset(
         *     screenName = "onboarding",
         *     assetPath  = "screens/onboarding.json"
         * )
         * ```
         *
         * @param screenName   Unique screen identifier.
         * @param assetPath    Relative path inside the Android `assets/` directory.
         * @param cloudEnabled Whether cloud fetching is enabled (default `false`).
         * @param displayName  Human-readable display name.
         * @param description  Optional description.
         * @param version      Semantic version string.
         * @return A new [KetoyScreen] with a single `"main"` content entry.
         */
        fun fromAsset(
            screenName: String,
            assetPath: String,
            cloudEnabled: Boolean = false,
            displayName: String = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() },
            description: String = "",
            version: String = "1.0.0"
        ) = KetoyScreen(
            screenName = screenName,
            displayName = displayName,
            description = description,
            version = version,
            cloudEnabled = cloudEnabled
        ).addContent(name = "main", assetPath = assetPath)
    }
}

// ── Internal composables ────────────────────────────────────────

@Composable
private fun CloudContent(
    screenName: String,
    contentName: String,
    colorScheme: KetoyColorScheme?,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (String, () -> Unit) -> Unit,
    fallbackJson: String?
) {
    val strategy = KetoyCloudService.cacheConfig.strategy
    val showCacheFirst = strategy == KetoyCacheStrategy.CACHE_FIRST ||
            strategy == KetoyCacheStrategy.OPTIMISTIC ||
            strategy == KetoyCacheStrategy.CACHE_ONLY

    var fetchState by remember { mutableStateOf<CloudState>(CloudState.Loading) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(screenName, retryTrigger) {
        // Don't flash loading if we already have content (e.g. from a previous fetch)
        if (fetchState !is CloudState.Loaded) {
            fetchState = CloudState.Loading
        }
        val result = KetoyCloudService.fetchScreen(screenName)
        fetchState = when (result) {
            is KetoyCloudService.FetchResult.Success -> {
                // The cloud JSON may be a screen-level wrapper with "contents".
                // Extract the right content block by name.
                val resolvedJson = extractContentJsonFromCloud(result.uiJson, contentName)
                CloudState.Loaded(resolvedJson)
            }
            is KetoyCloudService.FetchResult.Error -> {
                // Keep showing content if already loaded (don't regress to error)
                val current = fetchState
                if (current is CloudState.Loaded) current
                else CloudState.Error(result.message)
            }
        }
    }

    when (val state = fetchState) {
        is CloudState.Loading -> {
            // For cache-first strategies, show fallback content immediately
            // instead of a loading spinner — this is the "cache first" UX.
            if (showCacheFirst && fallbackJson != null) {
                JSONStringToUI(value = fallbackJson, colorScheme = colorScheme)
            } else {
                loadingContent()
            }
        }
        is CloudState.Loaded -> JSONStringToUI(value = state.json, colorScheme = colorScheme)
        is CloudState.Error -> {
            if (fallbackJson != null) {
                JSONStringToUI(value = fallbackJson, colorScheme = colorScheme)
            } else {
                errorContent(state.message) { retryTrigger++ }
            }
        }
    }
}

/**
 * Extracts a named content block from cloud JSON.
 *
 * If the JSON is a screen-level wrapper (`{ "contents": { ... } }`),
 * pulls out the specific content by [contentName]. Otherwise returns
 * the raw JSON (backward-compatible with legacy single-content format).
 *
 * @param json        The raw cloud JSON string.
 * @param contentName The content block to extract.
 * @return The extracted content JSON, or the original JSON if no
 *         wrapper is detected.
 */
private fun extractContentJsonFromCloud(json: String, contentName: String): String {
    return try {
        val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(json)
        val obj = jsonElement.jsonObject
        val contents = obj["contents"]?.jsonObject
        if (contents != null) {
            contents[contentName]?.toString() ?: json
        } else {
            // Legacy flat format — return as-is
            json
        }
    } catch (_: Exception) {
        json
    }
}

private sealed class CloudState {
    data object Loading : CloudState()
    data class Loaded(val json: String) : CloudState()
    data class Error(val message: String) : CloudState()
}

@Composable
private fun AssetScreenContent(
    assetPath: String,
    colorScheme: KetoyColorScheme? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var json by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(assetPath) {
        json = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        }
    }

    if (json != null) {
        JSONStringToUI(value = json!!, colorScheme = colorScheme)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun DefaultLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun DefaultError(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Failed to load screen", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
