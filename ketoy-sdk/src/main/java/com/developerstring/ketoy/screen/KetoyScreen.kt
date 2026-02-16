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
import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.model.KNode
import com.developerstring.ketoy.renderer.JSONStringToUI
import com.developerstring.ketoy.theme.KetoyColorScheme

/**
 * Provides the current [KetoyScreen] to child composables so that
 * [KetoyContent] can find its parent screen without an explicit parameter.
 */
val LocalKetoyScreen = staticCompositionLocalOf<KetoyScreen?> { null }

/**
 * Represents a server-driven screen in the Ketoy framework.
 *
 * A single `KetoyScreen` can hold **multiple content entries** identified
 * by a `contentId` (default `"main"`). This allows a `@KScreen` composable
 * to contain several [KetoyContent] blocks that are exported together
 * into a single JSON file.
 *
 * Screen metadata ([screenName], [displayName], [description], [version])
 * lives here, not in individual content blocks.
 *
 * ## Content resolution order (inside [Content]):
 * 1. **Dev-server override** — hot-reload JSON injected via [setDevOverride]
 * 2. **Cloud** — fetches from Ketoy Cloud (when [cloudEnabled])
 * 3. **Local JSON** — from the content entry's `jsonContent`
 * 4. **Asset** — loads JSON from a local asset file
 * 5. **Composable** — renders a `@Composable` lambda directly
 * 6. **DSL fallback** — `nodeBuilder` or `dslBuilder` → serialised to JSON
 * 7. **Empty** — placeholder text
 *
 * ## Usage — single content
 * ```kotlin
 * @KScreen(name = "home")
 * @Composable
 * fun HomeScreen() {
 *     KetoyContent(nodeBuilder = { buildHomeUI() })
 * }
 * ```
 *
 * ## Usage — multiple contents
 * ```kotlin
 * @KScreen(name = "dashboard")
 * @Composable
 * fun DashboardScreen() {
 *     KetoyContent(contentId = "header", nodeBuilder = { buildHeader() })
 *     KetoyContent(contentId = "body",   nodeBuilder = { buildBody() })
 * }
 * ```
 */
class KetoyScreen(
    val screenName: String,
    val displayName: String = screenName.replace("_", " ")
        .replaceFirstChar { it.uppercaseChar() },
    val description: String = "",
    val version: String = "1.0.0",
    val cloudEnabled: Boolean = true
) {
    // ── Content entries ─────────────────────────────────────────

    /**
     * A single content block within this screen.
     */
    data class ContentEntry(
        val contentId: String,
        val jsonContent: String? = null,
        val dslBuilder: (KUniversalScope.() -> Unit)? = null,
        val nodeBuilder: (() -> KNode)? = null,
        val composableBuilder: (@Composable () -> Unit)? = null,
        val assetPath: String? = null
    ) {
        /** Build the DSL to a [KNode] tree. Prefers [nodeBuilder] over [dslBuilder]. */
        fun buildNode(): KNode? {
            nodeBuilder?.let { return it() }
            val builder = dslBuilder ?: return null
            val scope = KUniversalScope()
            scope.builder()
            return scope.children.firstOrNull()
        }

        /** Build the DSL to a JSON string. */
        fun buildJson(): String? = buildNode()?.toJson()
    }

    private val _contents = mutableMapOf<String, ContentEntry>()

    /** All content entries keyed by contentId. */
    val contents: Map<String, ContentEntry> get() = _contents.toMap()

    /** Dev-server override JSONs, one per contentId. */
    private val _devOverrides = mutableStateMapOf<String, String>()

    // ── Legacy single-content compat ────────────────────────────

    /** Legacy: get/set the "main" content's dev override. */
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
     * Add a content entry to this screen. If a content with the same
     * [contentId] already exists it is replaced.
     *
     * @return this screen (for chaining)
     */
    fun addContent(
        contentId: String = "main",
        jsonContent: String? = null,
        dslBuilder: (KUniversalScope.() -> Unit)? = null,
        nodeBuilder: (() -> KNode)? = null,
        composableBuilder: (@Composable () -> Unit)? = null,
        assetPath: String? = null
    ): KetoyScreen {
        _contents[contentId] = ContentEntry(
            contentId = contentId,
            jsonContent = jsonContent,
            dslBuilder = dslBuilder,
            nodeBuilder = nodeBuilder,
            composableBuilder = composableBuilder,
            assetPath = assetPath
        )
        return this
    }

    /** Get a content entry by its ID. */
    fun getContent(contentId: String = "main"): ContentEntry? = _contents[contentId]

    /** All content IDs in this screen. */
    fun contentIds(): Set<String> = _contents.keys.toSet()

    /** Set a dev-server override for a specific content. */
    fun setDevOverride(contentId: String = "main", json: String?) {
        if (json != null) _devOverrides[contentId] = json
        else _devOverrides.remove(contentId)
    }

    /** Get a dev-server override for a specific content. */
    fun getDevOverride(contentId: String = "main"): String? = _devOverrides[contentId]

    // ── JSON building ───────────────────────────────────────────

    /**
     * Build the "main" content's DSL to a JSON string.
     * Backward-compatible with the old single-content API.
     */
    fun buildDslJson(): String? = _contents["main"]?.buildJson()

    /**
     * Build the "main" content's DSL to a [KNode] tree.
     */
    fun buildDslNode(): KNode? = _contents["main"]?.buildNode()

    /**
     * Build all contents' DSL to a JSON map (contentId → json).
     */
    fun buildAllJson(): Map<String, String?> =
        _contents.mapValues { (_, entry) -> entry.buildJson() }

    /**
     * Builds the export JSON for this screen.
     *
     * - **Single content ("main" only):** returns just the content JSON
     *   (backward-compatible with existing format).
     * - **Multiple contents:** returns a wrapper object:
     *   ```json
     *   {
     *     "screenName": "dashboard",
     *     "displayName": "Dashboard",
     *     "description": "...",
     *     "version": "1.0.0",
     *     "contents": {
     *       "header": { ... },
     *       "body": { ... }
     *     }
     *   }
     *   ```
     */
    fun buildExportJson(): String? {
        val allJson = buildAllJson().filterValues { it != null }
        if (allJson.isEmpty()) return null

        // Single "main" content — backward-compatible flat format
        if (allJson.size == 1 && allJson.containsKey("main")) {
            return allJson["main"]
        }

        // Multiple contents — wrap with metadata
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
     * Renders a specific content entry using the resolution order:
     * devOverride → cloud → local JSON → asset → composable → DSL → empty.
     *
     * @param contentId Which content block to render (default `"main"`).
     */
    @Composable
    fun Content(
        contentId: String = "main",
        colorScheme: KetoyColorScheme? = null,
        loadingContent: @Composable () -> Unit = { DefaultLoading() },
        errorContent: @Composable (error: String, retry: () -> Unit) -> Unit = { msg, retry ->
            DefaultError(msg, retry)
        }
    ) {
        // Provide this screen to child composables via LocalKetoyScreen
        CompositionLocalProvider(LocalKetoyScreen provides this) {
            ContentInternal(contentId, colorScheme, loadingContent, errorContent)
        }
    }

    @Composable
    internal fun ContentInternal(
        contentId: String,
        colorScheme: KetoyColorScheme?,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (error: String, retry: () -> Unit) -> Unit
    ) {
        val entry = _contents[contentId]

        // 1. Dev-server override (hot-reload)
        val devJson = _devOverrides[contentId]
        if (devJson != null) {
            key(devJson) {
                JSONStringToUI(value = devJson, colorScheme = colorScheme)
            }
            return
        }

        // 2. Cloud fetch
        if (cloudEnabled && com.developerstring.ketoy.Ketoy.isCloudEnabled()) {
            val fallbackJson = entry?.jsonContent ?: entry?.buildJson()
            CloudContent(screenName, colorScheme, loadingContent, errorContent, fallbackJson)
            return
        }

        if (entry == null) {
            Text("Empty content: $screenName/$contentId")
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
        Text("Empty content: $screenName/$contentId")
    }

    companion object {

        /**
         * Create a screen with a single "main" DSL builder content.
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
        ).addContent(contentId = "main", dslBuilder = dslBuilder)

        /**
         * Create a screen from a function that returns a [KNode] directly.
         * The node builder is added as the "main" content.
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
        ).addContent(contentId = "main", nodeBuilder = nodeBuilder)

        /**
         * Create a screen from a raw JSON string (as the "main" content).
         */
        fun fromJson(
            screenName: String,
            json: String,
            displayName: String = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() },
            description: String = "",
            version: String = "1.0.0"
        ) = KetoyScreen(
            screenName = screenName,
            displayName = displayName,
            description = description,
            version = version
        ).addContent(contentId = "main", jsonContent = json)

        /**
         * Create a screen backed by a `@Composable` lambda (as the "main" content).
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
        ).addContent(contentId = "main", composableBuilder = composable)

        /**
         * Create a screen that loads its JSON from a local asset file (as "main" content).
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
        ).addContent(contentId = "main", assetPath = assetPath)
    }
}

// ── Internal composables ────────────────────────────────────────

@Composable
private fun CloudContent(
    screenName: String,
    colorScheme: KetoyColorScheme?,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (String, () -> Unit) -> Unit,
    fallbackJson: String?
) {
    var fetchState by remember { mutableStateOf<CloudState>(CloudState.Loading) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(screenName, retryTrigger) {
        fetchState = CloudState.Loading
        val result = KetoyCloudService.fetchScreen(screenName)
        fetchState = when (result) {
            is KetoyCloudService.FetchResult.Success -> CloudState.Loaded(result.uiJson)
            is KetoyCloudService.FetchResult.Error -> CloudState.Error(result.message)
        }
    }

    when (val state = fetchState) {
        is CloudState.Loading -> loadingContent()
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
