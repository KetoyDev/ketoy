package com.developerstring.ketoy.devtools

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * Manages the connection to the Ketoy Dev Server.
 * Handles WebSocket communication for real-time JSON updates,
 * with HTTP polling as fallback.
 */
class KetoyDevClient {

    // ── Connection State ─────────────────────────────────────────

    /** Current connection status */
    var connectionState = mutableStateOf<ConnectionState>(ConnectionState.Disconnected)
        private set

    /** Server info (populated after successful connection) */
    var serverInfo = mutableStateOf<ServerInfo?>(null)
        private set

    /** All screen JSONs received from the server, keyed by screen name */
    val screens = mutableStateMapOf<String, String>()

    /** Data version from server (increments on every change) */
    var dataVersion = mutableStateOf(0L)
        private set

    /** The active screen name being previewed (null = show all) */
    var activeScreen = mutableStateOf<String?>(null)

    /** Error message, if any */
    var lastError = mutableStateOf<String?>(null)
        private set

    /** Connected host:port */
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
     * Connect to a Ketoy Dev Server.
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
                    startReconnectLoop()
                }
            } catch (e: Exception) {
                connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
                lastError.value = e.message
                startReconnectLoop()
            }
        }
    }

    /**
     * Connect using a URL string like "http://192.168.1.5:8484" or "192.168.1.5:8484"
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
     * Disconnect from the dev server.
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
     * Check if connected.
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
                }
                "update" -> {
                    val screenName = json.optString("screen")
                    val version = json.optLong("version", dataVersion.value)
                    val data = json.opt("data")?.toString() ?: return

                    dataVersion.value = version
                    screens[screenName] = data
                    println("📱 Ketoy Dev: Updated screen '$screenName' (v$version)")
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

    fun destroy() {
        disconnect()
        scope.cancel()
        client.dispatcher.executorService.shutdown()
    }
}

// ── Models ──────────────────────────────────────────────────────

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class ServerInfo(
    val version: String,
    val dataVersion: Long,
    val screenCount: Int,
    val connectedClients: Int
)
