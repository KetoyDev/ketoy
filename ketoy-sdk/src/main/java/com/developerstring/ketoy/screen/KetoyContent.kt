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
import com.developerstring.ketoy.theme.KetoyColorScheme

/**
 * The composable entry point for rendering a content block within a [KetoyScreen].
 *
 * Screen metadata (name, displayName, description, version) lives on the
 * parent [KetoyScreen], not here. Each `KetoyContent` is identified by a
 * [contentId] (default `"main"`), so a single `@KScreen` can contain
 * **multiple** content blocks.
 *
 * `KetoyContent` **self-registers** its content entry with the parent
 * [KetoyScreen] (obtained from [LocalKetoyScreen]) and delegates rendering
 * to the screen's resolution chain.
 *
 * ## Resolution order
 * 1. Dev-server override (hot-reload JSON)
 * 2. Ketoy Cloud (if cloud is configured)
 * 3. Local JSON
 * 4. Asset file
 * 5. Composable fallback
 * 6. DSL fallback ([dslBuilder] or [nodeBuilder])
 * 7. "Empty content" placeholder
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
 *     KetoyContent(contentId = "body")   { KText("Hello") }
 * }
 * ```
 *
 * ## Standalone usage (without LocalKetoyScreen)
 *
 * If no parent [KetoyScreen] is available via [LocalKetoyScreen],
 * pass a [screenName] to auto-create and register one:
 * ```kotlin
 * KetoyContent(screenName = "history", nodeBuilder = { buildHistory() })
 * ```
 *
 * @param contentId    Identifies this content block within the screen (default `"main"`).
 * @param screenName   Optional screen name — only needed when there is no parent
 *                     [KetoyScreen] in the composition (standalone usage).
 * @param cloudEnabled Whether to attempt fetching UI from Ketoy Cloud.
 * @param colorScheme  Optional color scheme override for the renderer.
 * @param nodeBuilder  A lambda returning a [KNode] tree.
 * @param dslBuilder   Inline DSL builder. If both [nodeBuilder] and [dslBuilder]
 *                     are provided, [nodeBuilder] takes priority.
 */
@Composable
fun KetoyContent(
    contentId: String = "main",
    screenName: String? = null,
    cloudEnabled: Boolean = true,
    colorScheme: KetoyColorScheme? = null,
    nodeBuilder: (() -> KNode)? = null,
    dslBuilder: (KUniversalScope.() -> Unit)? = null
) {
    // Resolve or create the parent screen
    val parentScreen = LocalKetoyScreen.current
        ?: resolveOrCreateScreen(screenName, cloudEnabled)

    // Register this content entry with the screen
    remember(contentId, nodeBuilder, dslBuilder) {
        parentScreen.addContent(
            contentId = contentId,
            nodeBuilder = nodeBuilder,
            dslBuilder = dslBuilder
        )
    }

    // Render using the screen's resolution chain
    parentScreen.ContentInternal(
        contentId = contentId,
        colorScheme = colorScheme,
        loadingContent = { DefaultContentLoading() },
        errorContent = { msg, retry -> DefaultContentError(msg, retry) }
    )
}

/**
 * Overload with trailing-lambda for inline DSL:
 * ```kotlin
 * KetoyContent("body") {
 *     KText("Hello")
 * }
 * ```
 */
@Composable
fun KetoyContent(
    contentId: String = "main",
    screenName: String? = null,
    cloudEnabled: Boolean = true,
    colorScheme: KetoyColorScheme? = null,
    dslBuilder: KUniversalScope.() -> Unit
) {
    KetoyContent(
        contentId = contentId,
        screenName = screenName,
        cloudEnabled = cloudEnabled,
        colorScheme = colorScheme,
        nodeBuilder = null,
        dslBuilder = dslBuilder
    )
}

/**
 * Resolves an existing screen from the registry or creates & registers one.
 * Used when `KetoyContent` is called without a parent `LocalKetoyScreen`.
 */
private fun resolveOrCreateScreen(
    screenName: String?,
    cloudEnabled: Boolean
): KetoyScreen {
    val name = screenName
        ?: error("KetoyContent requires either a parent KetoyScreen (via LocalKetoyScreen) or an explicit screenName parameter.")

    return KetoyScreenRegistry.get(name)
        ?: KetoyScreen(
            screenName = name,
            cloudEnabled = cloudEnabled
        ).also { KetoyScreenRegistry.register(it) }
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
