package com.developerstring.ketoy.cloud

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
import com.developerstring.ketoy.renderer.JSONBytesToUI
import com.developerstring.ketoy.renderer.JSONStringToUI
import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy.screen.KetoyScreenRegistry
import com.developerstring.ketoy.theme.KetoyColorScheme

/**
 * Renders a server-driven screen by its name using the unified [KetoyScreen] pipeline.
 *
 * The resolution order is:
 * 1. **Dev-server override** — hot-reload JSON injected via the Ketoy Dev Tools.
 * 2. **Cloud** — fetches from the Ketoy Cloud backend (respects [KetoyCacheStrategy]).
 * 3. **Local JSON** — statically bundled JSON registered in [KetoyScreenRegistry].
 * 4. **DSL fallback** — DSL node tree serialised to JSON at runtime.
 * 5. **Error** — displays [errorContent] with retry.
 *
 * If a [KetoyScreen] with the given [screenName] is already registered in
 * [KetoyScreenRegistry], this composable delegates to `screen.Content()`.
 * Otherwise it falls back to a direct cloud fetch (legacy behaviour for
 * screens that were not registered via [ProvideKetoyScreen]).
 *
 * ## Basic usage
 * ```kotlin
 * @Composable
 * fun App() {
 *     KetoyCloudScreen(screenName = "home_screen")
 * }
 * ```
 *
 * ## With custom loading and error UI
 * ```kotlin
 * KetoyCloudScreen(
 *     screenName     = "profile",
 *     colorScheme    = myKetoyColors,
 *     loadingContent = { ShimmerPlaceholder() },
 *     errorContent   = { error, retry ->
 *         Column {
 *             Text("Oops: $error")
 *             Button(onClick = retry) { Text("Try again") }
 *         }
 *     }
 * )
 * ```
 *
 * @param screenName      The screen identifier. Must match the screen name
 *                        registered on the Ketoy Cloud dashboard or in
 *                        [KetoyScreenRegistry] (e.g. `"home_screen"`).
 * @param modifier        Optional [Modifier] applied to the root [Box] container.
 * @param colorScheme     Optional [KetoyColorScheme] for theming the rendered UI tree.
 * @param loadingContent  Composable shown while the screen JSON is being fetched.
 *                        Defaults to a centred [CircularProgressIndicator].
 * @param errorContent    Composable shown when fetching fails. Receives the error
 *                        message and a `retry` callback. Defaults to
 *                        [DefaultCloudError].
 * @see KetoyCloudScreenFromJson
 * @see KetoyScreen
 * @see KetoyCloudService.fetchScreen
 */
@Composable
fun KetoyCloudScreen(
    screenName: String,
    modifier: Modifier = Modifier,
    colorScheme: KetoyColorScheme? = null,
    loadingContent: @Composable () -> Unit = { DefaultCloudLoading() },
    errorContent: @Composable (error: String, retry: () -> Unit) -> Unit = { error, retry ->
        DefaultCloudError(error, retry)
    }
) {
    Box(modifier = modifier) {
        // Check if a KetoyScreen is registered with this name
        val registered = KetoyScreenRegistry.get(screenName)
        if (registered != null) {
            // Unified path — delegates to KetoyScreen.Content()
            registered.Content(
                colorScheme = colorScheme,
                loadingContent = loadingContent,
                errorContent = errorContent
            )
        } else {
            // Legacy fallback — direct cloud fetch for unregistered screens
            LegacyCloudFetch(screenName, colorScheme, loadingContent, errorContent)
        }
    }
}

/**
 * Renders a pre-fetched Ketoy screen from compressed wire bytes.
 *
 * Use this composable when you have already fetched or constructed the
 * screen data yourself and want Ketoy to render it without any network call.
 * Accepts any format produced by the Ketoy wire pipeline.
 *
 * ```kotlin
 * val wireBytes: ByteArray = node.toWireBytes()
 * KetoyCloudScreenFromWireBytes(data = wireBytes, colorScheme = myKetoyColors)
 * ```
 *
 * @param data        Compressed wire bytes describing the Ketoy UI tree.
 * @param modifier    Optional [Modifier] applied to the root [Box] container.
 * @param colorScheme Optional [KetoyColorScheme] for theming.
 * @see KetoyCloudScreen
 * @see com.developerstring.ketoy.renderer.JSONBytesToUI
 */
@Composable
fun KetoyCloudScreenFromWireBytes(
    data: ByteArray,
    modifier: Modifier = Modifier,
    colorScheme: KetoyColorScheme? = null
) {
    Box(modifier = modifier) {
        JSONBytesToUI(data = data, colorScheme = colorScheme)
    }
}

/**
 * Renders a pre-fetched Ketoy screen from a raw JSON string.
 *
 * @deprecated Use [KetoyCloudScreenFromWireBytes] with compressed wire bytes.
 */
@Deprecated(
    message = "Use KetoyCloudScreenFromWireBytes() with compressed wire bytes. Plain JSON rendering is deprecated.",
    replaceWith = ReplaceWith(
        "KetoyCloudScreenFromWireBytes(data, modifier, colorScheme)",
        "com.developerstring.ketoy.cloud.KetoyCloudScreenFromWireBytes"
    )
)
@Composable
fun KetoyCloudScreenFromJson(
    json: String,
    modifier: Modifier = Modifier,
    colorScheme: KetoyColorScheme? = null
) {
    Box(modifier = modifier) {
        @Suppress("DEPRECATION")
        JSONStringToUI(value = json, colorScheme = colorScheme)
    }
}

// ── Legacy direct cloud fetch (for unregistered screens) ────────

@Composable
private fun LegacyCloudFetch(
    screenName: String,
    colorScheme: KetoyColorScheme?,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (String, () -> Unit) -> Unit
) {
    var fetchState by remember { mutableStateOf<CloudScreenState>(CloudScreenState.Loading) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(screenName, retryTrigger) {
        fetchState = CloudScreenState.Loading
        val result = KetoyCloudService.fetchScreen(screenName)
        fetchState = when (result) {
            is KetoyCloudService.FetchResult.Success -> CloudScreenState.Loaded(
                uiBytes = result.uiBytes,
                version = result.version,
                fromCache = result.fromCache
            )
            is KetoyCloudService.FetchResult.Error -> CloudScreenState.Error(result.message)
        }
    }

    when (val state = fetchState) {
        is CloudScreenState.Loading -> loadingContent()
        is CloudScreenState.Loaded -> {
            JSONBytesToUI(data = state.uiBytes, colorScheme = colorScheme)
        }
        is CloudScreenState.Error -> errorContent(state.message) { retryTrigger++ }
    }
}

// ── Internal state model ────────────────────────────────────────

private sealed class CloudScreenState {
    data object Loading : CloudScreenState()
    data class Loaded(
        val uiBytes: ByteArray,
        val version: String,
        val fromCache: Boolean
    ) : CloudScreenState()
    data class Error(val message: String) : CloudScreenState()
}

// ── Default loading and error composables ───────────────────────

@Composable
private fun DefaultCloudLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun DefaultCloudError(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Failed to load screen",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
