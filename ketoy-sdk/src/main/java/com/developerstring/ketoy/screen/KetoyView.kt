package com.developerstring.ketoy.screen

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.developerstring.ketoy.renderer.JSONStringToUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Primary composable for rendering a Ketoy screen by its registered name.
 *
 * Looks up a [KetoyScreen] in [KetoyScreenRegistry] and renders its content
 * using the full resolution chain. Falls back to [errorContent] if the
 * screen has not been registered.
 *
 * ## From a registered screen
 * ```kotlin
 * // Ensure the screen is registered first:
 * KetoyScreenRegistry.register(
 *     KetoyScreen.fromJson("home", homeJson)
 * )
 *
 * @Composable
 * fun App() {
 *     KetoyView(screenName = "home")
 * }
 * ```
 *
 * ## With custom error UI
 * ```kotlin
 * KetoyView(
 *     screenName   = "settings",
 *     errorContent = { msg -> Text("Error: $msg") }
 * )
 * ```
 *
 * @param screenName     The name identifying the screen in
 *                       [KetoyScreenRegistry].
 * @param modifier       Optional [Modifier] applied to the root container.
 * @param loadingContent Composable shown while loading async screens.
 *                       Defaults to a centred [CircularProgressIndicator].
 * @param errorContent   Composable shown when the screen cannot be found
 *                       or loaded. Receives a human-readable error message.
 * @see KetoyViewFromJson
 * @see KetoyViewFromAsset
 * @see KetoyViewFromNetwork
 * @see KetoyScreenRegistry
 */
@Composable
fun KetoyView(
    screenName: String,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (String) -> Unit = { msg -> DefaultErrorContent(msg) }
) {
    Box(modifier = modifier) {
        val screen = KetoyScreenRegistry.get(screenName)
        if (screen != null) {
            screen.Content()
        } else {
            errorContent("Screen not found: $screenName")
        }
    }
}

/**
 * Render a Ketoy screen from a raw JSON string.
 *
 * Directly invokes the
 * [JSONStringToUI][com.developerstring.ketoy.renderer.JSONStringToUI]
 * renderer — no registry lookup or caching is involved.
 *
 * ## Expected JSON format
 * ```json
 * {
 *   "type": "Column",
 *   "children": [
 *     { "type": "Text", "text": "Hello, World!" }
 *   ]
 * }
 * ```
 *
 * ```kotlin
 * val json = """{ "type": "Text", "text": "Hello" }"""
 * KetoyViewFromJson(json = json)
 * ```
 *
 * @param json     The raw JSON string describing the Ketoy UI tree.
 * @param modifier Optional [Modifier] applied to the root container.
 * @see KetoyView
 * @see com.developerstring.ketoy.renderer.JSONStringToUI
 */
@Composable
fun KetoyViewFromJson(
    json: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        JSONStringToUI(json)
    }
}

/**
 * Render a Ketoy screen from a local Android asset JSON file.
 *
 * Loads the file asynchronously on [Dispatchers.IO] and renders
 * the UI tree once the content is available.
 *
 * ```kotlin
 * KetoyViewFromAsset(assetPath = "screens/onboarding.json")
 * ```
 *
 * @param assetPath      Relative path inside the Android `assets/` directory
 *                       (e.g. `"screens/home.json"`).
 * @param modifier       Optional [Modifier] applied to the root container.
 * @param loadingContent Composable shown while reading the file.
 * @param errorContent   Composable shown when reading fails.
 * @see KetoyView
 * @see KetoyViewFromJson
 */
@Composable
fun KetoyViewFromAsset(
    assetPath: String,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (String) -> Unit = { msg -> DefaultErrorContent(msg) }
) {
    val context = LocalContext.current
    var jsonContent by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(assetPath) {
        try {
            val content = withContext(Dispatchers.IO) {
                context.assets.open(assetPath).bufferedReader().use { it.readText() }
            }
            jsonContent = content
        } catch (e: Exception) {
            error = "Failed to load asset: $assetPath\n${e.message}"
        }
    }

    Box(modifier = modifier) {
        when {
            error != null -> errorContent(error!!)
            jsonContent != null -> JSONStringToUI(jsonContent!!)
            else -> loadingContent()
        }
    }
}

/**
 * Render a Ketoy screen from a network URL.
 *
 * Fetches the JSON asynchronously via [HttpURLConnection] and renders
 * the UI tree once the download completes. Custom HTTP headers can be
 * provided for authentication or caching.
 *
 * ```kotlin
 * KetoyViewFromNetwork(
 *     url     = "https://api.example.com/screens/promo",
 *     headers = mapOf("Authorization" to "Bearer token123")
 * )
 * ```
 *
 * @param url            Fully-qualified URL returning a Ketoy JSON payload.
 * @param modifier       Optional [Modifier] applied to the root container.
 * @param headers        Optional HTTP headers appended to the request.
 * @param loadingContent Composable shown while downloading.
 * @param errorContent   Composable shown on network or parse failure.
 * @see KetoyView
 * @see KetoyViewFromJson
 * @see com.developerstring.ketoy.cloud.KetoyCloudScreen
 */
@Composable
fun KetoyViewFromNetwork(
    url: String,
    modifier: Modifier = Modifier,
    headers: Map<String, String> = emptyMap(),
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    errorContent: @Composable (String) -> Unit = { msg -> DefaultErrorContent(msg) }
) {
    var jsonContent by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        try {
            val content = withContext(Dispatchers.IO) {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
                connection.inputStream.bufferedReader().use { it.readText() }
            }
            jsonContent = content
        } catch (e: Exception) {
            error = "Failed to load from network: $url\n${e.message}"
        }
    }

    Box(modifier = modifier) {
        when {
            error != null -> errorContent(error!!)
            jsonContent != null -> JSONStringToUI(jsonContent!!)
            else -> loadingContent()
        }
    }
}

// ── Default loading and error content ───────────────────────────

@Composable
private fun DefaultLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DefaultErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
