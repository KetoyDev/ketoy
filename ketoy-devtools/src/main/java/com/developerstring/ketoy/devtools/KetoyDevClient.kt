package com.developerstring.ketoy.devtools

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * Central client that manages the connection to a **Ketoy Dev Server**.
 *
 * `KetoyDevClient` is the networking heart of the devtools module. It:
 *
 * 1. Establishes a **WebSocket** connection for sub-second hot-reload
 *    of SDUI screen JSON and navigation graphs.
 * 2. Falls back to **HTTP long-polling** when WebSocket connectivity is
 *    unavailable (e.g. restrictive proxies).
 * 3. Automatically **reconnects** with exponential back-off when the
 *    server becomes unreachable.
 * 4. Exposes all state as Compose [mutableStateOf] / [mutableStateMapOf]
 *    fields so that UI layers recompose reactively.
 *
 * ## Quick start
 * ```kotlin
 * val client = KetoyDevClient()
 * client.connect("192.168.1.5", port = 8484)
 *
 * // Observe screens in Compose:
 * val screens = client.screens          // Map<String, String>
 * val state   = client.connectionState  // ConnectionState
 * ```
 *
 * ## Protocol overview
 * | Message type  | Direction       | Description                          |
 * |---------------|-----------------|--------------------------------------|
 * | `connected`   | Server → Client | Initial handshake with data version  |
 * | `bundle`      | Server → Client | Full screen + nav graph payload      |
 * | `update`      | Server → Client | Single screen JSON update            |
 * | `nav_update`  | Server → Client | Single navigation graph update       |
 * | `pong`        | Server → Client | Heartbeat acknowledgement            |
 *
 * ## Thread safety
 * All public mutable state is backed by Compose snapshot state objects
 * and can be safely read from the main thread. Network I/O runs on
 * [Dispatchers.IO] via a supervised [CoroutineScope].
 *
 * @see KetoyDevWrapper
 * @see KetoyDevConfig
 * @see ConnectionState
 * @see ServerInfo
 */
class KetoyDevClient {

    // ── Connection State ─────────────────────────────────────────

    /**
     * The current lifecycle state of this client's connection.
     *
     * Compose code can observe this directly:
     * ```kotlin
     * val state by client.connectionState
     * when (state) {
     *     is ConnectionState.Connected    -> { /* ready */ }
     *     is ConnectionState.Connecting   -> { /* show spinner */ }
     *     is ConnectionState.Reconnecting -> { /* show retry UI */ }
     *     is ConnectionState.Error        -> { /* show error */ }
     *     is ConnectionState.Disconnected -> { /* idle */ }
     * }
     * ```
     *
     * @see ConnectionState
     */
    var connectionState = mutableStateOf<ConnectionState>(ConnectionState.Disconnected)
        private set

    /**
     * Metadata about the connected server, populated after the initial
     * HTTP `/status` handshake succeeds. `null` when disconnected.
     *
     * @see ServerInfo
     */
    var serverInfo = mutableStateOf<ServerInfo?>(null)
        private set

    /**
     * All **screen JSON payloads** received from the dev server,
     * keyed by the screen name (e.g. `"home"`, `"profile"`).
     *
     * This is a Compose [mutableStateMapOf] — reads inside a
     * `@Composable` automatically trigger recomposition when an
     * entry is added, removed, or updated.
     */
    val screens = mutableStateMapOf<String, String>()

    /**
     * All **navigation graph JSON payloads** received from the
     * dev server, keyed by the nav-host name (e.g. `"nav_main"`).
     *
     * Entries are injected into [KetoyNavDevOverrides] by
     * [KetoyDevWrapper] so that live navigation changes take effect.
     */
    val navGraphs = mutableStateMapOf<String, String>()

    /**
     * Monotonically increasing data-version counter broadcast by the
     * server. Each JSON change on the server bumps this value, enabling
     * the client to detect stale data during HTTP polling.
     */
    var dataVersion = mutableStateOf(0L)
        private set

    /**
     * The name of the screen currently being previewed.
     *
     * Set this to focus on a single screen; set to `null` to display
     * the screen picker (when more than one screen is available).
     */
    var activeScreen = mutableStateOf<String?>(null)

    /**
     * Human-readable error message from the most recent failure,
     * or `null` when no error is active.
     */
    var lastError = mutableStateOf<String?>(null)
        private set

    /**
     * The `host:port` string of the currently (or last) connected
     * server. Empty when no connection has been attempted.
     */
    var connectedUrl = mutableStateOf("")
        private set

    // ── Internal ─────────────────────────────────────────────────

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MINUTES) // No timeout for WebSocket
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private var pollingJob: Job? = null
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentHost = ""
    private var currentPort = 8484

    // ── Public API ───────────────────────────────────────────────

    /**
     * Connect to a Ketoy Dev Server at the given [host] and [port].
     *
     * The connection sequence:
     * 1. Any existing connection is [disconnect]ed.
     * 2. An HTTP `GET /status` verifies the server is reachable.
     * 3. A WebSocket is opened on `port + 1` for real-time updates.
     * 4. If the WebSocket handshake fails, the client falls back to
     *    HTTP polling at 1-second intervals.
     *
     * ```kotlin
     * val client = KetoyDevClient()
     * client.connect("192.168.1.5", 8484)
     * ```
     *
     * @param host Hostname or IP address of the Ketoy Dev Server.
     * @param port HTTP port of the server. The WebSocket port is
     *             always `port + 1`. Defaults to `8484`.
     *
     * @see disconnect
     * @see connectionState
     */
    fun connect(host: String, port: Int = 8484) {
        disconnect()

        currentHost = host
        currentPort = port
        connectedUrl.value = "$host:$port"
        connectionState.value = ConnectionState.Connecting

        // First verify server is reachable via HTTP, then open WebSocket
        scope.launch {
            try {
                val status = fetchStatus(host, port)
                if (status != null) {
                    serverInfo.value = status
                    connectWebSocket(host, port)
                } else {
                    connectionState.value = ConnectionState.Error("Server not reachable at $host:$port")
                    lastError.value = "Could not reach the Ketoy Dev Server. Make sure it's running."
                }
            } catch (e: Exception) {
                connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
                lastError.value = e.message
            }
        }
    }

    /**
     * Connect using a raw URL string.
     *
     * Accepted formats:
     * - `"192.168.1.5:8484"`
     * - `"http://192.168.1.5:8484"`
     * - `"192.168.1.5"` (defaults to port 8484)
     *
     * The scheme (`http://` / `https://`) and trailing slashes are
     * stripped automatically before delegating to [connect].
     *
     * ```kotlin
     * client.connect("http://192.168.1.5:8484")
     * ```
     *
     * @param url The server URL to connect to.
     *
     * @see connect
     */
    fun connect(url: String) {
        val cleaned = url
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')

        val parts = cleaned.split(":")
        val host = parts[0]
        val port = parts.getOrNull(1)?.toIntOrNull() ?: 8484

        connect(host, port)
    }

    /**
     * Gracefully disconnect from the dev server.
     *
     * This cancels any active polling or reconnection jobs, closes the
     * WebSocket with code `1000`, and resets [connectionState] to
     * [ConnectionState.Disconnected]. Screen and nav-graph maps are
     * **not** cleared — call [destroy] for a full teardown.
     *
     * @see destroy
     */
    fun disconnect() {
        pollingJob?.cancel()
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        connectionState.value = ConnectionState.Disconnected
        serverInfo.value = null
    }

    /**
     * Returns `true` when the client is in the [ConnectionState.Connected] state.
     *
     * @return Whether the client currently maintains an active connection.
     */
    fun isConnected(): Boolean = connectionState.value is ConnectionState.Connected

    // ── WebSocket ────────────────────────────────────────────────

    private fun connectWebSocket(host: String, port: Int) {
        val wsPort = port + 1
        val request = Request.Builder()
            .url("ws://$host:$wsPort")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                connectionState.value = ConnectionState.Connected
                lastError.value = null
                reconnectJob?.cancel()
                println("🔌 Ketoy Dev: WebSocket connected to $host:$wsPort")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("🔌 Ketoy Dev: WebSocket closed ($code: $reason)")
                connectionState.value = ConnectionState.Disconnected
                startReconnectLoop()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("🔌 Ketoy Dev: WebSocket failed: ${t.message}")
                connectionState.value = ConnectionState.Error(t.message ?: "WebSocket error")
                lastError.value = t.message

                // Fallback to HTTP polling
                startHttpPolling(host, port)
            }
        })
    }

    // ── HTTP Polling (fallback) ──────────────────────────────────

    private fun startHttpPolling(host: String, port: Int) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            println("📡 Ketoy Dev: Falling back to HTTP polling")
            connectionState.value = ConnectionState.Connected
            lastError.value = null

            while (isActive) {
                try {
                    val url = "http://$host:$port/poll?v=${dataVersion.value}"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: continue
                        handleServerMessage("""{"type":"bundle","data":$body}""")
                    }
                } catch (e: Exception) {
                    // Ignore polling errors, will retry
                }

                delay(1000) // Poll every second
            }
        }
    }

    // ── Reconnection ─────────────────────────────────────────────

    private fun startReconnectLoop() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            var retries = 0
            while (isActive && !isConnected()) {
                retries++
                val delayMs = minOf(retries * 2000L, 10_000L)
                delay(delayMs)

                println("🔄 Ketoy Dev: Reconnecting (attempt $retries)...")
                connectionState.value = ConnectionState.Reconnecting(retries)

                try {
                    val status = fetchStatus(currentHost, currentPort)
                    if (status != null) {
                        serverInfo.value = status
                        connectWebSocket(currentHost, currentPort)
                        break
                    }
                } catch (_: Exception) { }
            }
        }
    }

    // ── Message Handling ─────────────────────────────────────────

    private fun handleServerMessage(text: String) {
        try {
            val json = org.json.JSONObject(text)
            when (json.optString("type")) {
                "connected" -> {
                    val version = json.optLong("version", 0)
                    dataVersion.value = version
                }
                "bundle" -> {
                    val data = json.optJSONObject("data") ?: return
                    val version = data.optLong("version", dataVersion.value)
                    val screensObj = data.optJSONObject("screens") ?: return

                    dataVersion.value = version
                    screensObj.keys().forEach { name ->
                        val screenJson = screensObj.get(name).toString()
                        screens[name] = screenJson
                    }

                    // Also load nav graphs if present
                    val navsObj = data.optJSONObject("navGraphs")
                    if (navsObj != null) {
                        navsObj.keys().forEach { name ->
                            val navJson = navsObj.get(name).toString()
                            navGraphs[name] = navJson
                        }
                    }
                }
                "update" -> {
                    val screenName = json.optString("screen")
                    val version = json.optLong("version", dataVersion.value)
                    val data = json.opt("data")?.toString() ?: return

                    dataVersion.value = version
                    screens[screenName] = data
                    println("📱 Ketoy Dev: Updated screen '$screenName' (v$version)")
                }
                "nav_update" -> {
                    val navHost = json.optString("navHost")
                    val version = json.optLong("version", dataVersion.value)
                    val data = json.opt("data")?.toString() ?: return

                    dataVersion.value = version
                    navGraphs[navHost] = data
                    println("📱 Ketoy Dev: Updated nav '$navHost' (v$version)")
                }
                "pong" -> { /* heartbeat ack */ }
            }
        } catch (e: Exception) {
            // Try parsing as raw bundle (from HTTP polling)
            try {
                val json = org.json.JSONObject(text)
                val version = json.optLong("version", 0)
                if (version > dataVersion.value) {
                    dataVersion.value = version
                    val screensObj = json.optJSONObject("screens")
                    screensObj?.keys()?.forEach { name ->
                        val screenJson = screensObj.get(name).toString()
                        screens[name] = screenJson
                    }
                    val navsObj = json.optJSONObject("navGraphs")
                    navsObj?.keys()?.forEach { name ->
                        val navJson = navsObj.get(name).toString()
                        navGraphs[name] = navJson
                    }
                }
            } catch (_: Exception) {
                System.err.println("Ketoy Dev: Failed to parse message: ${e.message}")
            }
        }
    }

    // ── HTTP Helpers ─────────────────────────────────────────────

    private fun fetchStatus(host: String, port: Int): ServerInfo? {
        return try {
            val request = Request.Builder()
                .url("http://$host:$port/status")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = org.json.JSONObject(body)
                ServerInfo(
                    version = json.optString("version", "unknown"),
                    dataVersion = json.optLong("dataVersion", 0),
                    screenCount = json.optInt("screenCount", 0),
                    connectedClients = json.optInt("connectedClients", 0)
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────

    /**
     * Fully tears down this client instance.
     *
     * Calls [disconnect], cancels the internal [CoroutineScope], and
     * shuts down the OkHttp dispatcher thread pool. After calling
     * `destroy()` this instance must **not** be reused.
     *
     * Typically called from a Compose `DisposableEffect`:
     * ```kotlin
     * DisposableEffect(Unit) {
     *     onDispose { client.destroy() }
     * }
     * ```
     */
    fun destroy() {
        disconnect()
        scope.cancel()
        client.dispatcher.executorService.shutdown()
    }
}

// ── Models ──────────────────────────────────────────────────────

/**
 * Represents the lifecycle states of a [KetoyDevClient] connection.
 *
 * Observe this in Compose via `client.connectionState` to drive
 * connection-aware UI (spinners, error banners, status indicators).
 *
 * ```kotlin
 * val state by client.connectionState
 * when (state) {
 *     is ConnectionState.Connected    -> Text("Live")
 *     is ConnectionState.Connecting   -> CircularProgressIndicator()
 *     is ConnectionState.Reconnecting -> Text("Retrying…")
 *     is ConnectionState.Error        -> Text((state as ConnectionState.Error).message)
 *     is ConnectionState.Disconnected -> Text("Offline")
 * }
 * ```
 *
 * @see KetoyDevClient.connectionState
 */
sealed class ConnectionState {
    /** No connection has been established or the client has explicitly disconnected. */
    data object Disconnected : ConnectionState()

    /** A connection attempt is in progress (initial handshake). */
    data object Connecting : ConnectionState()

    /** The client is fully connected and receiving live updates. */
    data object Connected : ConnectionState()

    /**
     * The connection was lost and the client is retrying.
     *
     * @property attempt The 1-based reconnection attempt number.
     */
    data class Reconnecting(val attempt: Int) : ConnectionState()

    /**
     * An unrecoverable error has occurred.
     *
     * @property message Human-readable error description.
     */
    data class Error(val message: String) : ConnectionState()
}

/**
 * Snapshot of metadata returned by the dev server's `/status` endpoint.
 *
 * Populated in [KetoyDevClient.serverInfo] after a successful handshake.
 *
 * @property version          The Ketoy Dev Server version string (e.g. `"1.1.0"`).
 * @property dataVersion      Monotonic counter that increments on every JSON change.
 * @property screenCount      Number of screens currently served.
 * @property connectedClients Number of clients connected to the server.
 *
 * @see KetoyDevClient.serverInfo
 */
data class ServerInfo(
    val version: String,
    val dataVersion: Long,
    val screenCount: Int,
    val connectedClients: Int
)
