package com.developerstring.ketoy.devtools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.renderer.JSONStringToUI
import com.developerstring.ketoy.screen.KetoyScreenRegistry
import kotlinx.coroutines.flow.collectLatest

/**
 * Wraps your entire app to enable Ketoy Dev Tools hot-reload.
 *
 * **The app always runs normally.** In the background the wrapper connects
 * to the dev server and injects updated JSON into any [KetoyScreen] whose
 * `screenName` matches a screen name on the server. This means:
 *
 * - Navigation, scaffold, drawers — everything keeps working.
 * - Only the screens whose JSON changed are hot-reloaded.
 * - Screens without a server match render as usual (cloud → DSL fallback).
 *
 * ## Usage in your Activity
 * ```kotlin
 * setContent {
 *     KetoyDevWrapper {
 *         MyApp()   // ← always rendered
 *     }
 * }
 * ```
 *
 * @param config  Optional configuration for the dev server connection.
 * @param content Your normal app content — always rendered.
 */
@Composable
fun KetoyDevWrapper(
    config: KetoyDevConfig = KetoyDevConfig(),
    content: @Composable () -> Unit
) {
    val client = remember { KetoyDevClient() }
    var isSetupComplete by remember { mutableStateOf(config.autoConnect) }

    val connectionState by client.connectionState
    val screens = client.screens

    // Auto-connect if configured
    LaunchedEffect(config) {
        if (config.autoConnect && config.host.isNotBlank()) {
            client.connect(config.host, config.port)
            isSetupComplete = true
        }
    }

    // ── Core: inject dev-server JSON into registered KetoyScreens ──
    // Uses snapshotFlow to continuously observe the screens map.
    // Any update (including value changes for existing keys) immediately
    // propagates to the matching KetoyScreen's devOverrideJson, which
    // triggers recomposition of the active Content() composable.
    LaunchedEffect(Unit) {
        snapshotFlow { screens.toList() }
            .collectLatest { screenList ->
                val registeredScreens = KetoyScreenRegistry.getAll()
                screenList.forEach { (serverScreenName, json) ->
                    // Match by screen name (case-insensitive)
                    val match = registeredScreens[serverScreenName]
                        ?: registeredScreens.values.firstOrNull {
                            it.screenName.equals(serverScreenName, ignoreCase = true)
                        }
                    match?.let { it.devOverrideJson = json }
                }
            }
    }

    // Clear overrides on disconnect
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Disconnected) {
            KetoyScreenRegistry.getAll().values.forEach { it.devOverrideJson = null }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            // Clear all dev overrides when wrapper is removed
            KetoyScreenRegistry.getAll().values.forEach { it.devOverrideJson = null }
            client.destroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isSetupComplete) {
            // Show connection screen FIRST, then render app after connect
            KetoyDevConnectScreen(
                client = client,
                onConnected = {
                    isSetupComplete = true
                },
                onSkip = {
                    // Skip dev tools — load the app directly without live updates
                    isSetupComplete = true
                }
            )
        } else {
            // ── Always render the full app ──
            content()
        }

        // Dev overlay (always visible when setup is complete)
        if (isSetupComplete && config.showOverlay) {
            KetoyDevOverlay(
                client = client,
                onDisconnect = {
                    // Clear overrides when manually disconnecting
                    KetoyScreenRegistry.getAll().values.forEach { it.devOverrideJson = null }
                    client.disconnect()
                    isSetupComplete = false
                }
            )
        }
    }
}

/**
 * A simpler wrapper that only provides the dev preview screen.
 * Use this when you want to show a specific screen from the dev server
 * without wrapping your entire app.
 *
 * ```kotlin
 * KetoyDevPreviewScreen(
 *     serverUrl = "192.168.1.5:8484",
 *     screenName = "home"
 * )
 * ```
 */
@Composable
fun KetoyDevPreviewScreen(
    serverUrl: String,
    screenName: String? = null,
    modifier: Modifier = Modifier
) {
    val client = remember { KetoyDevClient() }

    LaunchedEffect(serverUrl) {
        client.connect(serverUrl)
    }

    DisposableEffect(Unit) {
        onDispose { client.destroy() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        KetoyDevPreview(client = client, screenName = screenName)

        KetoyDevOverlay(
            client = client,
            onDisconnect = { client.disconnect() }
        )
    }
}

// ── Internal Preview Renderer (for standalone preview mode) ─────

@Composable
internal fun KetoyDevPreview(
    client: KetoyDevClient,
    screenName: String? = null
) {
    val screens = client.screens
    val connectionState by client.connectionState
    val activeScreen by client.activeScreen
    val dataVersion by client.dataVersion

    val targetScreen = screenName ?: activeScreen

    when {
        connectionState is ConnectionState.Connecting ||
        connectionState is ConnectionState.Reconnecting -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D1117)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFF58A6FF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (connectionState is ConnectionState.Reconnecting)
                            "Reconnecting..." else "Connecting to dev server...",
                        color = Color(0xFF8B949E)
                    )
                }
            }
        }

        connectionState is ConnectionState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D1117)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Connection error: ${(connectionState as ConnectionState.Error).message}",
                    color = Color(0xFFF85149)
                )
            }
        }

        screens.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D1117)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Waiting for screens...",
                        color = Color(0xFF8B949E),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add .json files to your ketoy-screens/ directory",
                        color = Color(0xFF484F58),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        targetScreen != null && screens.containsKey(targetScreen) -> {
            val json = screens[targetScreen] ?: return
            key(dataVersion) {
                JSONStringToUI(value = json)
            }
        }

        screens.size == 1 -> {
            val json = screens.values.first()
            key(dataVersion) {
                JSONStringToUI(value = json)
            }
        }

        else -> {
            KetoyDevScreenPicker(
                screens = screens.keys.toList(),
                onScreenSelected = { name ->
                    client.activeScreen.value = name
                }
            )
        }
    }
}

@Composable
private fun KetoyDevScreenPicker(
    screens: List<String>,
    onScreenSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Available Screens",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select a screen to preview",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF8B949E)
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(screens) { name ->
                Card(
                    onClick = { onScreenSelected(name) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF161B22)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC9D1D9)
                        )
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF58A6FF)
                        )
                    }
                }
            }
        }
    }
}
