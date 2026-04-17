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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.renderer.JSONBytesToUI
import com.developerstring.ketoy.navigation.KetoyNavDevOverrides
import com.developerstring.ketoy.navigation.KetoyNavGraph
import com.developerstring.ketoy.screen.KetoyScreenRegistry

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

    // ── Core: inject dev-server data into registered KetoyScreens ──
    // dataVersion is a MutableState<Long> that increments on every server push.
    // Reading it with `by` here causes recomposition on each push, which
    // restarts the LaunchedEffect below (keyed on dv + registeredRoutes).
    val dv by client.dataVersion
    val registeredRoutes = KetoyScreenRegistry.getAllRoutes()

    LaunchedEffect(dv, screens.keys.toSet(), registeredRoutes) {
        val registeredScreens = KetoyScreenRegistry.getAll()
        println("📱 Ketoy Dev [inject] dv=$dv | server screens=${screens.keys} | registered=${registeredScreens.keys} | wireBytes keys=${client.screenBytes.keys}")

        screens.forEach { (serverScreenName, serverValue) ->
            // ── Step 1: Try direct match (exact → case-insensitive) ──
            var match = registeredScreens[serverScreenName]
                ?: registeredScreens.values.firstOrNull {
                    it.screenName.equals(serverScreenName, ignoreCase = true)
                }
            var contentName = "main" // default content slot

            // ── Step 2: If no direct match, try splitting "screenName_contentName" ──
            // Export creates files like "profile_main.ktw" → server key "profile_main"
            // App registers screen as "profile". Split and try matching the prefix.
            // For screen names with underscores (e.g. "history_screen_main"), try
            // splitting at each underscore from right to left until a match is found.
            if (match == null && serverScreenName.contains("_")) {
                var idx = serverScreenName.length
                while (idx > 0) {
                    idx = serverScreenName.lastIndexOf('_', idx - 1)
                    if (idx <= 0) break
                    val candidateScreen = serverScreenName.substring(0, idx)
                    val candidateContent = serverScreenName.substring(idx + 1)

                    val found = registeredScreens[candidateScreen]
                        ?: registeredScreens.values.firstOrNull {
                            it.screenName.equals(candidateScreen, ignoreCase = true)
                        }
                    if (found != null) {
                        match = found
                        contentName = candidateContent
                        println("📱 Ketoy Dev [inject] Split '$serverScreenName' → screen='${found.screenName}' content='$contentName'")
                        break
                    }
                }
            }

            if (match == null) {
                println("📱 Ketoy Dev [inject] ⚠️ NO MATCH for server screen '$serverScreenName'. Registered: ${registeredScreens.keys}")
                return@forEach
            }

            // ── Step 3: Inject wire bytes or JSON override ──
            val bytes = client.screenBytes[serverScreenName]
            if (bytes != null) {
                println("📱 Ketoy Dev [inject] ✅ Injecting ${bytes.size} wire bytes → ${match.screenName}/$contentName")
                match.setDevOverrideBytes(contentName, bytes)
            } else if (serverValue != "__wire__") {
                println("📱 Ketoy Dev [inject] ✅ Injecting JSON override → ${match.screenName}/$contentName (${serverValue.length} chars)")
                match.setDevOverride(contentName, serverValue)
            } else {
                println("📱 Ketoy Dev [inject] ⚠️ Wire sentinel '__wire__' but no bytes for '$serverScreenName'")
            }
        }
    }

    // ── Nav graph injection ──
    val navGraphJsons = client.navGraphs
    val navKeys = navGraphJsons.keys.toSet()
    for (navName in navKeys) {
        key("nav_$navName") {
            val navJson = navGraphJsons[navName]
            val navBytes = client.navGraphBytes[navName]
            LaunchedEffect(navJson, navBytes) {
                try {
                    val graph = if (navBytes != null) {
                        val decoded = com.developerstring.ketoy.wire.KetoyWireFormat.decode(navBytes)
                        KetoyNavGraph.fromJson(decoded)
                    } else if (navJson != null && navJson != "__wire__") {
                        KetoyNavGraph.fromJson(navJson)
                    } else {
                        return@LaunchedEffect
                    }
                    KetoyNavDevOverrides.set(navName, graph)
                    println("📱 Ketoy Dev: Nav graph '$navName' injected (${graph.destinations.size} destinations) [${if (navBytes != null) "wire" else "json"}]")
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
            KetoyScreenRegistry.getAll().values.forEach { it.setScreenDevOverride(null) }
            KetoyNavDevOverrides.clearAll()
            client.destroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isSetupComplete) {
            KetoyDevConnectScreen(
                client = client,
                onConnected = {
                    isSetupComplete = true
                },
                onSkip = {
                    isSetupComplete = true
                }
            )
        } else {
            content()
        }
    }
}

/**
 * A self-contained composable that connects to a dev server and renders
 * a specific screen (or a screen picker) in an isolated preview surface.
 *
 * Unlike [KetoyDevWrapper] — which wraps your full app and injects
 * overrides into already-registered screens — `KetoyDevPreviewScreen`
 * creates its **own** [KetoyDevClient] and renders the raw JSON through
 * [JSONStringToUI].
 *
 * @param serverUrl  The `host:port` (or full `http://…`) URL of the
 *                    Ketoy Dev Server.
 * @param screenName Optional screen to preview immediately. When
 *                    `null`, the user sees a picker listing all
 *                    available screens.
 * @param modifier   Optional [Modifier] applied to the root [Box].
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

// ── Internal Preview Renderer ───────────────────────────────────

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
                        text = "Add .json or .ktw files to your ketoy-screens/ directory",
                        color = Color(0xFF484F58),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        targetScreen != null && screens.containsKey(targetScreen) -> {
            val wireBytes = client.screenBytes[targetScreen]
            val renderBytes = remember(wireBytes, screens[targetScreen], dataVersion) {
                wireBytes ?: screens[targetScreen]?.toByteArray(Charsets.UTF_8)
            }
            if (renderBytes != null) {
                key(dataVersion) {
                    JSONBytesToUI(data = renderBytes)
                }
            }
        }

        screens.size == 1 -> {
            val name = screens.keys.first()
            val wireBytes = client.screenBytes[name]
            val renderBytes = remember(wireBytes, screens[name], dataVersion) {
                wireBytes ?: screens[name]?.toByteArray(Charsets.UTF_8)
            }
            if (renderBytes != null) {
                key(dataVersion) {
                    JSONBytesToUI(data = renderBytes)
                }
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
