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
import com.developerstring.ketoy.navigation.KetoyNavDevOverrides
import com.developerstring.ketoy.navigation.KetoyNavGraph
import com.developerstring.ketoy.screen.KetoyScreenRegistry
import kotlinx.coroutines.flow.collectLatest

/**
 * Top-level composable that wraps your **entire app** to enable
 * Ketoy Dev Tools hot-reload.
 *
 * **Your app always runs normally.** Behind the scenes the wrapper:
 *
 * 1. Connects to the Ketoy Dev Server (via [KetoyDevClient]).
 * 2. Listens for screen JSON updates pushed over WebSocket / HTTP.
 * 3. Matches each incoming screen name against the screens registered
 *    in [KetoyScreenRegistry] and injects the new JSON as a
 *    dev-override (`setScreenDevOverride`).
 * 4. Parses incoming `nav_*.json` payloads and feeds them to
 *    [KetoyNavDevOverrides] so navigation graphs are hot-reloaded too.
 * 5. Clears all overrides when the connection drops, restoring the
 *    app to its original state.
 *
 * This means:
 * - Navigation, scaffold, drawers — everything keeps working.
 * - Only the screens whose JSON changed are hot-reloaded.
 * - Screens without a server match render as usual (cloud → DSL fallback).
 *
 * ## Usage
 * ```kotlin
 * // In your Activity’s setContent:
 * setContent {
 *     KetoyDevWrapper {
 *         MyApp()   // ← always rendered, never blocked
 *     }
 * }
 *
 * // Or with auto-connect to a known server:
 * setContent {
 *     KetoyDevWrapper(
 *         config = KetoyDevConfig(
 *             host = "192.168.1.5",
 *             port = 8484,
 *             autoConnect = true
 *         )
 *     ) {
 *         MyApp()
 *     }
 * }
 * ```
 *
 * ## Lifecycle
 * - If [KetoyDevConfig.autoConnect] is `false` (the default), the
 *   [KetoyDevConnectScreen] is displayed first. Once the user connects
 *   (or taps “Skip”), [content] is rendered.
 * - On `DisposableEffect` disposal, all dev-overrides are cleared and
 *   the [KetoyDevClient] is destroyed.
 *
 * @param config  Connection configuration. Defaults to an empty
 *                [KetoyDevConfig] which shows the connection screen.
 * @param content Your normal application content — always rendered
 *                after the setup phase completes.
 *
 * @see KetoyDevConfig
 * @see KetoyDevClient
 * @see KetoyDevConnectScreen
 * @see KetoyDevPreviewScreen
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
    // Observe each screen entry individually via derivedStateOf so that
    // recomposition only happens for the screen that actually changed,
    // instead of re-processing every screen on any map mutation.
    LaunchedEffect(Unit) {
        snapshotFlow { screens.keys.toSet() }
            .collectLatest { screenNames ->
                screenNames.forEach { serverScreenName ->
                    val json = screens[serverScreenName] ?: return@forEach
                    val registeredScreens = KetoyScreenRegistry.getAll()
                    val match = registeredScreens[serverScreenName]
                        ?: registeredScreens.values.firstOrNull {
                            it.screenName.equals(serverScreenName, ignoreCase = true)
                        }
                    match?.setScreenDevOverride(json)
                }
            }
    }

    // Per-screen observer: reacts only when a specific screen's JSON changes
    val screenKeys = screens.keys.toSet()
    for (name in screenKeys) {
        key(name) {
            val json = screens[name]
            LaunchedEffect(json) {
                if (json == null) return@LaunchedEffect
                val registeredScreens = KetoyScreenRegistry.getAll()
                val match = registeredScreens[name]
                    ?: registeredScreens.values.firstOrNull {
                        it.screenName.equals(name, ignoreCase = true)
                    }
                match?.setScreenDevOverride(json)
            }
        }
    }

    // ── Nav graph injection: parse nav_*.json and update KetoyNavDevOverrides ──
    val navGraphJsons = client.navGraphs
    val navKeys = navGraphJsons.keys.toSet()
    for (navName in navKeys) {
        key("nav_$navName") {
            val navJson = navGraphJsons[navName]
            LaunchedEffect(navJson) {
                if (navJson == null) return@LaunchedEffect
                try {
                    val graph = KetoyNavGraph.fromJson(navJson)
                    KetoyNavDevOverrides.set(navName, graph)
                    println("📱 Ketoy Dev: Nav graph '$navName' injected (${graph.destinations.size} destinations)")
                } catch (e: Exception) {
                    System.err.println("Ketoy Dev: Failed to parse nav graph '$navName': ${e.message}")
                }
            }
        }
    }

    // Clear overrides on disconnect
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Disconnected) {
            KetoyScreenRegistry.getAll().values.forEach { it.setScreenDevOverride(null) }
            KetoyNavDevOverrides.clearAll()
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            // Clear all dev overrides when wrapper is removed
            KetoyScreenRegistry.getAll().values.forEach { it.setScreenDevOverride(null) }
            KetoyNavDevOverrides.clearAll()
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
        // Removed: the status dot / circle overlay is intentionally
        // omitted to keep the UI clean during normal app usage.
    }
}

/**
 * A self-contained composable that connects to a dev server and renders
 * a specific screen (or a screen picker) in an isolated preview surface.
 *
 * Unlike [KetoyDevWrapper] — which wraps your full app and injects
 * overrides into already-registered screens — `KetoyDevPreviewScreen`
 * creates its **own** [KetoyDevClient] and renders the raw JSON through
 * [JSONStringToUI]. Use it for quick, standalone previews.
 *
 * ## Usage
 * ```kotlin
 * KetoyDevPreviewScreen(
 *     serverUrl  = "192.168.1.5:8484",
 *     screenName = "home"     // null = show screen picker
 * )
 * ```
 *
 * The composable also shows a [KetoyDevOverlay] in the corner for
 * connection status and a disconnect action.
 *
 * @param serverUrl  The `host:port` (or full `http://…`) URL of the
 *                    Ketoy Dev Server.
 * @param screenName Optional screen to preview immediately. When
 *                    `null`, the user sees a picker listing all
 *                    available screens.
 * @param modifier   Optional [Modifier] applied to the root [Box].
 *
 * @see KetoyDevWrapper
 * @see KetoyDevClient
 * @see KetoyDevOverlay
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

/**
 * Internal composable that renders the live SDUI preview for a
 * connected [KetoyDevClient].
 *
 * Depending on the current [ConnectionState] and available screens,
 * it shows one of:
 * - A **connecting** spinner while the handshake is in progress.
 * - An **error** message when the connection fails.
 * - A **“Waiting for screens…”** placeholder when no JSON has arrived.
 * - The **single screen** rendered via [JSONStringToUI] when exactly
 *   one screen is available (or [screenName] is specified).
 * - A **[KetoyDevScreenPicker]** list when multiple screens exist and
 *   no specific screen is targeted.
 *
 * @param client     The active [KetoyDevClient].
 * @param screenName Optional name of the screen to render. `null`
 *                    means “show all / show picker”.
 *
 * @see KetoyDevPreviewScreen
 * @see KetoyDevScreenPicker
 */
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

/**
 * A dark-themed list of available screen names received from the dev
 * server. Tapping an item calls [onScreenSelected] which sets
 * [KetoyDevClient.activeScreen] and switches the preview to that
 * screen.
 *
 * Shown inside [KetoyDevPreview] when multiple screens are available
 * and no [KetoyDevClient.activeScreen] has been chosen yet.
 *
 * @param screens         Sorted list of screen names.
 * @param onScreenSelected Callback invoked with the selected screen name.
 */
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
