package com.developerstring.ketoy.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.model.KNode

/**
 * A **child** composable that represents one DSL content block inside
 * a [ProvideKetoyScreen]-wrapped parent.
 *
 * Screen-level settings (`screenName`, `cloudEnabled`, `colorScheme`)
 * are **not** set here — they belong on the parent [KetoyScreen]
 * (provided via [LocalKetoyScreen]). Each `KetoyContent` is identified
 * by a unique [name] (default `"main"`), so a single screen can
 * contain **multiple** DSL content blocks freely interleaved with
 * native Jetpack Compose code.
 *
 * `KetoyContent` **self-registers** its content entry with the parent
 * [KetoyScreen] and renders the DSL-driven UI in-place using the
 * screen’s full resolution chain.
 *
 * ## Resolution order (inherited from parent [KetoyScreen])
 * 1. Dev-server override (hot-reload JSON)
 * 2. Ketoy Cloud (if cloud is configured on the parent)
 * 3. Local JSON ([KetoyScreen.ContentEntry.jsonContent])
 * 4. Asset file ([KetoyScreen.ContentEntry.assetPath])
 * 5. Composable fallback
 * 6. DSL fallback ([dslBuilder] or [nodeBuilder])
 * 7. “Empty content” placeholder
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
 *         Text("Native Compose section")
 *         KetoyContent(name = "transactions", nodeBuilder = { buildTxns() })
 *         Button(onClick = {}) { Text("Compose Button") }
 *     }
 * }
 * ```
 *
 * ## Usage — trailing-lambda DSL
 * ```kotlin
 * KetoyContent("body") {
 *     KText("Hello from DSL")
 * }
 * ```
 *
 * @param name        Identifies this content block within the screen
 *                    (default `"main"`). Must be unique among siblings.
 * @param nodeBuilder A lambda returning a [KNode] tree. Takes precedence
 *                    over [dslBuilder].
 * @param dslBuilder  Inline DSL builder using [KUniversalScope]. If both
 *                    [nodeBuilder] and [dslBuilder] are provided, [nodeBuilder]
 *                    is used.
 * @throws IllegalStateException if no parent [KetoyScreen] is found via
 *         [LocalKetoyScreen] (i.e. not inside [ProvideKetoyScreen]).
 * @see ProvideKetoyScreen
 * @see KetoyScreen
 * @see LocalKetoyScreen
 */
@Composable
fun KetoyContent(
    name: String = "main",
    nodeBuilder: (() -> KNode)? = null,
    dslBuilder: (KUniversalScope.() -> Unit)? = null
) {
    // Resolve the parent screen from LocalKetoyScreen
    val parentScreen = LocalKetoyScreen.current
        ?: error("KetoyContent must be used inside a @KScreen-annotated composable (no parent KetoyScreen found via LocalKetoyScreen).")

    // Register this content entry with the screen
    remember(name, nodeBuilder, dslBuilder) {
        parentScreen.addContent(
            name = name,
            nodeBuilder = nodeBuilder,
            dslBuilder = dslBuilder
        )
    }

    // Render using the screen's resolution chain
    parentScreen.ContentInternal(
        name = name,
        colorScheme = parentScreen.colorScheme,
        loadingContent = { DefaultContentLoading() },
        errorContent = { msg, retry -> DefaultContentError(msg, retry) }
    )
}

/**
 * Convenience overload with a trailing-lambda for inline DSL.
 *
 * ```kotlin
 * KetoyContent("body") {
 *     KText("Hello")
 *     KButton(text = "Click me", onClick = "navigate://settings")
 * }
 * ```
 *
 * @param name       Content block identifier (default `"main"`).
 * @param dslBuilder Inline DSL builder using [KUniversalScope].
 * @see KetoyContent
 */
@Composable
fun KetoyContent(
    name: String = "main",
    dslBuilder: KUniversalScope.() -> Unit
) {
    KetoyContent(
        name = name,
        nodeBuilder = null,
        dslBuilder = dslBuilder
    )
}

@Composable
private fun DefaultContentLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun DefaultContentError(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Failed to load content",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
