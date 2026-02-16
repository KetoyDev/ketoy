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
 * Primary composable for rendering a Ketoy screen by screen name.
 *
 * Looks up a registered [KetoyScreen] and renders its content, with
 * support for loading and error states.
 *
 * ## From a registered screen
 * ```kotlin
 * KetoyView(screenName = "home")
 * ```
 *
 * ## From JSON string
 * ```kotlin
 * KetoyView.fromJson(jsonString)
 * ```
 *
 * ## From an asset
 * ```kotlin
 * KetoyView.fromAsset("screens/home.json")
 * ```
 *
 * @param screenName   The name identifying the screen in [KetoyScreenRegistry].
 * @param modifier     Optional Modifier.
 * @param loadingContent Composable shown while loading (e.g. async screens).
 * @param errorContent   Composable shown when the screen cannot be found or loaded.
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
 * Render a Ketoy screen from a local asset JSON file.
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
