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
 * 1. Establishes a **WebSocket** connection for sub-second hot-reload.
 * 2. Falls back to **HTTP long-polling** when WebSocket is unavailable.
 * 3. Automatically **reconnects** with exponential back-off.
 * 4. Exposes all state as Compose [mutableStateOf] / [mutableStateMapOf]
 *    fields so that UI layers recompose reactively.
 *
 * @see KetoyDevWrapper
 * @see KetoyDevConfig
 * @see ConnectionState
 * @see ServerInfo
 */
class KetoyDevClient {

    // ── Connection State ─────────────────────────────────────────

    var connectionState = mutableStateOf<ConnectionState>(ConnectionState.Disconnected)
        private set

    var serverInfo = mutableStateOf<ServerInfo?>(null)
        private set

    val screens = mutableStateMapOf<String, String>()

    val navGraphs = mutableStateMapOf<String, String>()

    var dataVersion = mutableStateOf(0L)
        private set

    var activeScreen = mutableStateOf<String?>(null)

    var lastError = mutableStateOf<String?>(null)
        private set

    var connectedUrl = mutableStateOf("")
        private set

    // ── Internal ─────────────────────────────────────────────────

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MINUTES)
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
     */
    fun connect(host: String, port: Int = 8484) {
        disconnect()

        currentHost = host
        currentPort = port
        connectedUrl.value = "$host:$port"
        connectionState.value = ConnectionState.Connecting

        scope.launch {
            try {
                val status = fetchStatus(host, port)
                if (status != null) {
                    serverInfo.value = status
                    connectWebSocket(host, port)
                } else {
                    val hint = when {
                        host == "localhost" || host == "127.0.0.1" ->
                            "Tip: make sure ADB reverse is active, or try 10.0.2.2:$port instead"
                        host == "10.0.2.2" ->
                            "Tip: confirm ketoyDev is running and the port is correct"
                        else ->
                            "Tip: for emulators use 10.0.2.2:$port or localhost:$port"
                    }
                    connectionState.value = ConnectionState.Error("No response from $host:$port")
                    lastError.value = "Server not reachable at $host:$port\n$hint"
                }
            } catch (e: Exception) {
                connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
                lastError.value = "${e.javaClass.simpleName}: ${e.message}"
            }
        }
    }

    /**
     * Connect using a raw URL string.
     *
     * Accepted formats: `"192.168.1.5:8484"`, `"http://192.168.1.5:8484"`, `"192.168.1.5"`.
     */
    fun connect(url: String) {
        val cleaned = url
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')

        val parts = cleaned.split(":")
        val host = parts.firstOrNull()?.ifEmpty { null } ?: return
        val port = parts.getOrNull(1)?.toIntOrNull() ?: 8484

        connect(host, port)
    }

    /**
     * Connects to the Ketoy Dev Server, automatically selecting the correct host
     * for the current device type.
     *
     * - On an **Android Emulator**: connects to `10.0.2.2` (the emulator's alias
     *   for the host machine's loopback interface).
     * - On a **physical device**: connects to [lanIp] (your machine's LAN IP,
     *   e.g. `"192.168.1.5"` as printed by the dev server on startup).
     *
     * @param lanIp The LAN IP of the host machine running the dev server.
     * @param port  The HTTP port of the dev server. Defaults to `8484`.
     */
    fun connectAuto(lanIp: String, port: Int = 8484) {
        val host = if (EmulatorUtils.isEmulator()) EmulatorUtils.HOST_LOOPBACK else lanIp
        connect(host, port)
    }

    /**
     * Gracefully disconnect from the dev server.
     */
    fun disconnect() {
        pollingJob?.cancel()
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        connectionState.value = ConnectionState.Disconnected
        serverInfo.value = null
    }

    fun isConnected(): Boolean = connectionState.value is ConnectionState.Connected

    // ── WebSocket ────────────────────────────────────────────────

    private fun connectWebSocket(host: String, port: Int) {
        val wsPort = port + 1
        println("🔌 Ketoy Dev: Opening WebSocket at ws://$host:$wsPort ...")
        val request = Request.Builder()
            .url("ws://$host:$wsPort")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                scope.launch(Dispatchers.Main.immediate) {
                    connectionState.value = ConnectionState.Connected
                    lastError.value = null
                    reconnectJob?.cancel()
                }
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
                scope.launch(Dispatchers.Main.immediate) {
                    connectionState.value = ConnectionState.Disconnected
                }
                startReconnectLoop()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val detail = "${t.javaClass.simpleName}: ${t.message}"
                println("🔌 Ketoy Dev: WebSocket failed at ws://$host:$wsPort — $detail")
                println("   → Falling back to HTTP long-polling on http://$host:$port")
                scope.launch(Dispatchers.Main.immediate) {
                    connectionState.value = ConnectionState.Error(detail)
                    lastError.value = "WebSocket failed ($detail) — using HTTP poll fallback"
                }
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

                delay(1000)
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
        // Parse on the calling thread (OkHttp/IO), then dispatch
        // all Compose state mutations to the Main thread so that
        // snapshot apply notifications are processed synchronously.
        try {
            val json = org.json.JSONObject(text)
            when (json.optString("type")) {
                "connected" -> {
                    val version = json.optLong("version", 0)
                    scope.launch(Dispatchers.Main.immediate) {
                        dataVersion.value = version
                    }
                }
                "bundle" -> {
                    val data = json.optJSONObject("data") ?: return
                    val version = data.optLong("version", dataVersion.value)
                    val screensObj = data.optJSONObject("screens") ?: return
                    val screenUpdates = buildMap {
                        screensObj.keys().forEach { name ->
                            put(name, screensObj.get(name).toString())
                        }
                    }
                    val navsObj = data.optJSONObject("navGraphs")
                    val navUpdates = if (navsObj != null) buildMap {
                        navsObj.keys().forEach { name ->
                            put(name, navsObj.get(name).toString())
                        }
                    } else emptyMap()

                    scope.launch(Dispatchers.Main.immediate) {
                        dataVersion.value = version
                        screenUpdates.forEach { (name, screenJson) -> screens[name] = screenJson }
                        navUpdates.forEach { (name, navJson) -> navGraphs[name] = navJson }
                    }
                }
                "update" -> {
                    val screenName = json.optString("screen")
                    val version = json.optLong("version", dataVersion.value)
                    val data = json.opt("data")?.toString() ?: return

                    scope.launch(Dispatchers.Main.immediate) {
                        dataVersion.value = version
                        screens[screenName] = data
                        println("📱 Ketoy Dev: Updated screen '$screenName' (v$version)")
                    }
                }
                "nav_update" -> {
                    val navHost = json.optString("navHost")
                    val version = json.optLong("version", dataVersion.value)
                    val data = json.opt("data")?.toString() ?: return

                    scope.launch(Dispatchers.Main.immediate) {
                        dataVersion.value = version
                        navGraphs[navHost] = data
                        println("📱 Ketoy Dev: Updated nav '$navHost' (v$version)")
                    }
                }
                "pong" -> { /* heartbeat ack */ }
            }
        } catch (e: Exception) {
            try {
                val json = org.json.JSONObject(text)
                val version = json.optLong("version", 0)
                if (version > dataVersion.value) {
                    val screensObj = json.optJSONObject("screens")
                    val screenUpdates = if (screensObj != null) buildMap {
                        screensObj.keys().forEach { name ->
                            put(name, screensObj.get(name).toString())
                        }
                    } else emptyMap()
                    val navsObj = json.optJSONObject("navGraphs")
                    val navUpdates = if (navsObj != null) buildMap {
                        navsObj.keys().forEach { name ->
                            put(name, navsObj.get(name).toString())
                        }
                    } else emptyMap()

                    scope.launch(Dispatchers.Main.immediate) {
                        dataVersion.value = version
                        screenUpdates.forEach { (name, screenJson) -> screens[name] = screenJson }
                        navUpdates.forEach { (name, navJson) -> navGraphs[name] = navJson }
                    }
                }
            } catch (_: Exception) {
                System.err.println("Ketoy Dev: Failed to parse message: ${e.message}")
            }
        }
    }

    // ── HTTP Helpers ─────────────────────────────────────────────

    private fun fetchStatus(host: String, port: Int): ServerInfo? {
        val url = "http://$host:$port/status"
        return try {
            println("🔍 Ketoy Dev: Checking server at $url ...")
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            println("🔍 Ketoy Dev: Server responded HTTP ${response.code}")
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = org.json.JSONObject(body)
                ServerInfo(
                    version = json.optString("version", "unknown"),
                    dataVersion = json.optLong("dataVersion", 0),
                    screenCount = json.optInt("screenCount", 0),
                    connectedClients = json.optInt("connectedClients", 0)
                )
            } else {
                println("🔍 Ketoy Dev: Server returned error HTTP ${response.code}")
                null
            }
        } catch (e: java.net.ConnectException) {
            println("🔍 Ketoy Dev: Connection refused at $url — is ketoyDev running?")
            null
        } catch (e: java.net.SocketTimeoutException) {
            println("🔍 Ketoy Dev: Timeout reaching $url — check the address and port")
            null
        } catch (e: Exception) {
            println("🔍 Ketoy Dev: Failed to reach $url — ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────

    /**
     * Fully tears down this client instance.
     * After calling `destroy()` this instance must **not** be reused.
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
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Snapshot of metadata returned by the dev server's `/status` endpoint.
 */
data class ServerInfo(
    val version: String,
    val dataVersion: Long,
    val screenCount: Int,
    val connectedClients: Int
)

// ── Emulator Detection ──────────────────────────────────────────

/**
 * Utilities for detecting whether the app is running on an Android Emulator
 * and for resolving the correct dev server host address.
 *
 * The Android Emulator runs in a virtual network where the host machine's
 * loopback (`127.0.0.1`) is reachable via the special alias `10.0.2.2`.
 * Physical devices must use the host machine's LAN IP instead.
 *
 * @see KetoyDevClient.connectAuto
 */
object EmulatorUtils {

    /** The special IP alias used by Android Emulators to reach the host machine's loopback. */
    const val HOST_LOOPBACK: String = "10.0.2.2"

    /**
     * Returns `true` when the current process is running inside an Android Emulator
     * (AVD, Genymotion, or similar virtual device).
     *
     * Uses multiple [android.os.Build] signals (requires ≥ 2 hits) to avoid
     * false positives on real devices. Covers goldfish/ranchu AVDs and Genymotion.
     * Works from API 1 — does NOT require [android.os.Build.IS_EMULATOR] (API 35+).
     */
    fun isEmulator(): Boolean {
        val signals = listOf(
            android.os.Build.FINGERPRINT.startsWith("generic"),
            android.os.Build.FINGERPRINT.contains("generic"),
            android.os.Build.FINGERPRINT == "unknown",
            android.os.Build.MODEL.contains("google_sdk"),
            android.os.Build.MODEL.contains("Emulator"),
            android.os.Build.MODEL.contains("Android SDK built for"),
            android.os.Build.MANUFACTURER.contains("Genymotion"),
            android.os.Build.PRODUCT.contains("sdk_gphone"),
            android.os.Build.PRODUCT.contains("vbox86p"),
            android.os.Build.PRODUCT.contains("emulator"),
            android.os.Build.PRODUCT.contains("simulator"),
            android.os.Build.BRAND.startsWith("generic"),
            android.os.Build.BRAND == "google",
            android.os.Build.DEVICE.contains("generic"),
            android.os.Build.HARDWARE == "goldfish",
            android.os.Build.HARDWARE == "ranchu",
        )
        return signals.count { it } >= 2
    }
}
