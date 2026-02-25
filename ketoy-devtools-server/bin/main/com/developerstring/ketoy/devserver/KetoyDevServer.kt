package com.developerstring.ketoy.devserver

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Ketoy Dev Server — the core networking component that serves SDUI screen JSON over HTTP
 * and pushes live hot-reload updates to connected Ketoy-powered Android apps via WebSocket.
 *
 * ## Responsibilities
 *
 * - **HTTP API**: Exposes a lightweight REST-like API for clients to fetch screens, navigation
 *   graphs, and full bundles. Includes a long-poll endpoint for environments where WebSocket
 *   is unavailable (e.g., certain corporate proxies).
 * - **WebSocket server**: Runs on `port + 1` and broadcasts JSON diffs to every connected
 *   app the instant a file changes, enabling sub-second hot-reload.
 * - **CORS**: All responses include `Access-Control-Allow-Origin: *` so that browser-based
 *   tooling (e.g., the Ketoy web inspector) can query the server freely.
 *
 * ## HTTP endpoints
 *
 * | Endpoint | Description |
 * |----------|-------------|
 * | `GET /` | Human-readable dashboard with connection info |
 * | `GET /status` | JSON health-check (version, client count) |
 * | `GET /screens` | List available screen names |
 * | `GET /screen?name=X` | Fetch a single screen’s JSON |
 * | `GET /navs` | List available navigation graph names |
 * | `GET /nav?name=X` | Fetch a single nav graph’s JSON |
 * | `GET /bundle` | Fetch all screens + nav graphs in one payload |
 * | `GET /poll?v=N` | Long-poll; returns immediately if server version > N, else waits up to 30 s |
 * | `WS :port+1` | WebSocket for real-time push updates |
 *
 * ## Integration in the devtools pipeline
 *
 * ```
 * FileWatcher / SourceWatcher
 *          │
 *          └── broadcastUpdate() / broadcastNavUpdate()
 *                     │
 *                     ├── WebSocket push to all clients
 *                     └── Wake long-poll waiters
 * ```
 *
 * ### Usage
 *
 * ```kotlin
 * val screenManager = ScreenManager(watchDir)
 * val server = KetoyDevServer(port = 8484, screenManager)
 * server.start()
 * // … later …
 * server.stop()
 * ```
 *
 * @param port The TCP port for the HTTP server. The WebSocket server will bind to `port + 1`.
 * @param screenManager The [ScreenManager] instance that owns the screen and nav-graph caches.
 * @see ScreenManager
 * @see FileWatcher
 * @see SourceWatcher
 */
class KetoyDevServer(
    private val port: Int,
    private val screenManager: ScreenManager
) {
    /** Backing HTTP server (JDK built-in `com.sun.net.httpserver`). */
    private lateinit var httpServer: HttpServer
    /** Backing WebSocket server (java-websocket library). */
    private lateinit var wsServer: KetoyWebSocketServer
    /** Shared thread pool for HTTP handler execution and long-poll timeouts. */
    private val executor = Executors.newCachedThreadPool()

    /**
     * Map of active long-poll waiters keyed by a monotonically increasing poll ID.
     * Each entry holds the [HttpExchange] whose response has been deferred until a
     * data change occurs or the 30-second timeout expires.
     */
    private val pollWaiters = ConcurrentHashMap<Long, HttpExchange>()
    /** Counter used to generate unique long-poll IDs. */
    private var pollIdCounter = 0L

    /**
     * Starts both the HTTP server on [port] and the WebSocket server on `port + 1`.
     *
     * Registers all HTTP context handlers and begins accepting connections. This method
     * returns immediately — the servers run on background threads managed by [executor].
     *
     * @see stop
     */
    fun start() {
        // HTTP Server
        httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer.executor = executor

        httpServer.createContext("/") { exchange -> handleRoot(exchange) }
        httpServer.createContext("/status") { exchange -> handleStatus(exchange) }
        httpServer.createContext("/screens") { exchange -> handleScreens(exchange) }
        httpServer.createContext("/screen") { exchange -> handleScreen(exchange) }
        httpServer.createContext("/poll") { exchange -> handlePoll(exchange) }
        httpServer.createContext("/bundle") { exchange -> handleBundle(exchange) }
        httpServer.createContext("/nav") { exchange -> handleNav(exchange) }
        httpServer.createContext("/navs") { exchange -> handleNavs(exchange) }

        httpServer.start()

        // WebSocket server on port + 1
        wsServer = KetoyWebSocketServer(port + 1)
        wsServer.isReuseAddr = true
        wsServer.start()

        println("🌐 HTTP server started on port $port")
        println("🔌 WebSocket server started on port ${port + 1}")
    }

    /**
     * Gracefully stops both the HTTP and WebSocket servers and shuts down the thread pool.
     *
     * Outstanding long-poll waiters are abandoned. Already-connected WebSocket clients
     * receive a close frame.
     *
     * @see start
     */
    fun stop() {
        httpServer.stop(1)
        wsServer.stop(1000)
        executor.shutdown()
    }

    /**
     * Called by [FileWatcher] when a screen JSON file is created or modified.
     *
     * Performs two actions:
     * 1. **WebSocket broadcast** — sends a `{"type":"update"}` message containing the
     *    updated screen name, the current data version, and the full JSON payload.
     * 2. **Long-poll wake-up** — responds to all waiting `/poll` requests with the latest
     *    full bundle so they can return to their callers.
     *
     * @param screenName The logical name of the screen (file name without `.json` extension).
     * @param json The raw JSON content of the updated screen file.
     * @see broadcastNavUpdate
     */
    fun broadcastUpdate(screenName: String, json: String) {
        val version = screenManager.getVersion()
        val message = """{"type":"update","screen":"$screenName","version":$version,"data":${json}}"""

        // WebSocket broadcast
        wsServer.broadcast(message)

        // Wake up long-poll waiters
        synchronized(pollWaiters) {
            pollWaiters.forEach { (_, exchange) ->
                try {
                    sendJson(exchange, 200, buildBundleJson())
                } catch (_: Exception) { }
            }
            pollWaiters.clear()
        }

        println("📤 Pushed update: $screenName (v$version) → ${wsServer.connections.size} client(s)")
    }

    /**
     * Called by [FileWatcher] when a navigation graph JSON file (`nav_*.json`) changes.
     *
     * Behaves identically to [broadcastUpdate] but sends a `{"type":"nav_update"}` message
     * so that the client SDK can distinguish screen updates from navigation-graph updates and
     * re-resolve routes without a full screen reload.
     *
     * @param navName The logical name of the navigation graph (e.g., `"main"` for `nav_main.json`).
     * @param json The raw JSON content of the updated nav graph file.
     * @see broadcastUpdate
     */
    fun broadcastNavUpdate(navName: String, json: String) {
        val version = screenManager.getVersion()
        val message = """{"type":"nav_update","navHost":"$navName","version":$version,"data":${json}}"""

        // WebSocket broadcast
        wsServer.broadcast(message)

        // Wake up long-poll waiters
        synchronized(pollWaiters) {
            pollWaiters.forEach { (_, exchange) ->
                try {
                    sendJson(exchange, 200, buildBundleJson())
                } catch (_: Exception) { }
            }
            pollWaiters.clear()
        }

        println("📤 Pushed nav update: $navName (v$version) → ${wsServer.connections.size} client(s)")
    }

    // ── HTTP Handlers ──────────────────────────────────────────────

    /**
     * Serves the root (`/`) endpoint — a human-readable HTML dashboard that displays
     * the server status, connection URLs, available screens and nav graphs, and a list
     * of API endpoints. Useful for quick browser-based verification during development.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handleRoot(exchange: HttpExchange) {
        val localIp = NetworkUtils.getLocalIpAddress()
        val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Ketoy Dev Server</title>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
                body { font-family: 'Segoe UI', Roboto, 'Noto Sans', 'Liberation Sans', sans-serif; 
                       max-width: 800px; margin: 0 auto; padding: 2rem; background: #0d1117; color: #c9d1d9; }
                h1 { color: #58a6ff; }
                .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 1.5rem; margin: 1rem 0; }
                .url { font-family: monospace; font-size: 1.2em; color: #7ee787; background: #0d1117; 
                       padding: 0.5rem 1rem; border-radius: 4px; display: inline-block; }
                .screen { padding: 0.5rem; border-bottom: 1px solid #21262d; }
                .screen:last-child { border-bottom: none; }
                .badge { background: #238636; color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.8em; }
                pre { background: #0d1117; padding: 1rem; border-radius: 4px; overflow-x: auto; }
                .status { display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #3fb950; margin-right: 8px; }
            </style>
        </head>
        <body>
            <h1>🚀 Ketoy Dev Server</h1>
            <div class="card">
                <p><span class="status"></span> Server running</p>
                <p>Connect your app using:</p>
                <p class="url">http://$localIp:$port</p>
            </div>
            <div class="card">
                <h3>📱 Available Screens</h3>
                ${screenManager.listScreens().joinToString("") { 
                    "<div class='screen'>• $it <span class='badge'>v${screenManager.getVersion()}</span></div>" 
                }.ifEmpty { "<p>No screens yet. Add .json files to the watch directory.</p>" }}
            </div>
            <div class="card">
                <h3>�️ Navigation Graphs</h3>
                ${screenManager.listNavGraphs().joinToString("") { 
                    "<div class='screen'>• $it <span class='badge'>nav</span></div>" 
                }.ifEmpty { "<p>No nav graphs yet. Add nav_*.json files to the watch directory.</p>" }}
            </div>
            <div class="card">
                <h3>🔗 API Endpoints</h3>
                <pre>
GET /status          → Server status
GET /screens         → List available screens
GET /screen?name=X   → Get screen JSON
GET /navs            → List navigation graphs
GET /nav?name=X      → Get nav graph JSON
GET /bundle          → Get all screens + nav graphs
GET /poll?v=N        → Long-poll for changes
WS  :${port + 1}    → WebSocket live updates
                </pre>
            </div>
        </body>
        </html>
        """.trimIndent()
        sendHtml(exchange, 200, html)
    }

    /**
     * Serves `/status` — a lightweight JSON health-check that returns the server name,
     * version, current data version, screen count, connected WebSocket client count,
     * and the WebSocket port.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handleStatus(exchange: HttpExchange) {
        val json = """{
            "server": "ketoy-devtools-server",
            "version": "1.0.0",
            "status": "running",
            "dataVersion": ${screenManager.getVersion()},
            "screenCount": ${screenManager.listScreens().size},
            "connectedClients": ${wsServer.connections.size},
            "wsPort": ${port + 1}
        }"""
        sendJson(exchange, 200, json)
    }

    /**
     * Serves `/screens` — returns a JSON object containing the current data version and
     * an array of all registered screen names.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handleScreens(exchange: HttpExchange) {
        val screens = screenManager.listScreens()
        val json = """{"version":${screenManager.getVersion()},"screens":[${screens.joinToString(",") { "\"$it\"" }}]}"""
        sendJson(exchange, 200, json)
    }

    /**
     * Serves `/screen?name=X` — returns the full JSON for the requested screen wrapped
     * in metadata (version, screen name). Returns `400` if the `name` query parameter is
     * missing, or `404` if the screen does not exist.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handleScreen(exchange: HttpExchange) {
        val params = parseQuery(exchange.requestURI.query ?: "")
        val name = params["name"]
        if (name == null) {
            sendJson(exchange, 400, """{"error":"Missing 'name' parameter"}""")
            return
        }
        val screenJson = screenManager.getScreen(name)
        if (screenJson == null) {
            sendJson(exchange, 404, """{"error":"Screen '$name' not found"}""")
            return
        }
        val json = """{"version":${screenManager.getVersion()},"screen":"$name","data":$screenJson}"""
        sendJson(exchange, 200, json)
    }

    /**
     * Serves `/bundle` — returns the full bundle containing every screen and every
     * navigation graph in a single JSON payload. This is the endpoint the Ketoy client SDK
     * calls on initial connection to hydrate its local cache.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handleBundle(exchange: HttpExchange) {
        sendJson(exchange, 200, buildBundleJson())
    }

    /**
     * Serves `/navs` — returns a JSON object containing the current data version and
     * an array of all registered navigation graph names.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handleNavs(exchange: HttpExchange) {
        val navs = screenManager.listNavGraphs()
        val json = """{"version":${screenManager.getVersion()},"navGraphs":[${navs.joinToString(",") { "\"$it\"" }}]}"""
        sendJson(exchange, 200, json)
    }

    /**
     * Serves `/nav?name=X` — returns the full JSON for the requested navigation graph.
     * Returns `400` if `name` is missing, or `404` if the nav graph does not exist.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handleNav(exchange: HttpExchange) {
        val params = parseQuery(exchange.requestURI.query ?: "")
        val name = params["name"]
        if (name == null) {
            sendJson(exchange, 400, """{"error":"Missing 'name' parameter"}""")
            return
        }
        val navJson = screenManager.getNavGraph(name)
        if (navJson == null) {
            sendJson(exchange, 404, """{"error":"Nav graph '$name' not found"}""")
            return
        }
        val json = """{"version":${screenManager.getVersion()},"navHost":"$name","data":$navJson}"""
        sendJson(exchange, 200, json)
    }

    /**
     * Serves `/poll?v=N` — a long-poll endpoint for clients that cannot use WebSocket.
     *
     * - If the current server version is greater than `N`, responds immediately with the
     *   full bundle.
     * - Otherwise, parks the request for up to **30 seconds**. If a data change arrives
     *   within that window, the parked request is woken and answered with the latest bundle.
     *   If no change arrives, a timeout response is sent.
     *
     * @param exchange The incoming HTTP exchange.
     */
    private fun handlePoll(exchange: HttpExchange) {
        val params = parseQuery(exchange.requestURI.query ?: "")
        val clientVersion = params["v"]?.toLongOrNull() ?: 0

        if (screenManager.getVersion() > clientVersion) {
            // Client is behind — send immediately
            sendJson(exchange, 200, buildBundleJson())
        } else {
            // Client is up-to-date — long poll (wait up to 30s)
            val pollId = synchronized(this) { pollIdCounter++ }
            pollWaiters[pollId] = exchange

            // Timeout after 30 seconds
            executor.submit {
                Thread.sleep(30_000)
                val removed = pollWaiters.remove(pollId)
                if (removed != null) {
                    try {
                        sendJson(removed, 200, """{"version":${screenManager.getVersion()},"timeout":true}""")
                    } catch (_: Exception) { }
                }
            }
        }
    }

    /**
     * Builds a JSON payload that bundles **all** screens and **all** navigation graphs
     * together with the current data version.
     *
     * The resulting shape:
     * ```json
     * {
     *   "version": 42,
     *   "screens": { "home": { … }, "profile": { … } },
     *   "navGraphs": { "main": { … } }
     * }
     * ```
     *
     * @return The complete bundle JSON string.
     */
    private fun buildBundleJson(): String {
        val screens = screenManager.getAllScreens()
        val screensJson = screens.entries.joinToString(",") { (name, json) ->
            "\"$name\":$json"
        }
        val navGraphs = screenManager.getAllNavGraphs()
        val navsJson = navGraphs.entries.joinToString(",") { (name, json) ->
            "\"$name\":$json"
        }
        return """{"version":${screenManager.getVersion()},"screens":{$screensJson},"navGraphs":{$navsJson}}"""
    }

    // ── Helpers ────────────────────────────────────────────────────

    /**
     * Writes a JSON response with the given HTTP status code.
     * Adds CORS and `Content-Type: application/json` headers automatically.
     *
     * @param exchange The HTTP exchange to respond to.
     * @param code The HTTP status code (e.g., `200`, `400`, `404`).
     * @param json The raw JSON string to send as the response body.
     */
    private fun sendJson(exchange: HttpExchange, code: Int, json: String) {
        val bytes = json.toByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    /**
     * Writes an HTML response with the given HTTP status code.
     * Adds CORS and `Content-Type: text/html; charset=utf-8` headers automatically.
     *
     * @param exchange The HTTP exchange to respond to.
     * @param code The HTTP status code.
     * @param html The HTML string to send as the response body.
     */
    private fun sendHtml(exchange: HttpExchange, code: Int, html: String) {
        val bytes = html.toByteArray()
        exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    /**
     * Parses a URL query string (`key=value&key2=value2`) into a `Map<String, String>`.
     * If a key appears more than once, the last value wins.
     *
     * @param query The raw query string (without the leading `?`).
     * @return A map of key-value pairs; empty map if [query] is blank.
     */
    private fun parseQuery(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split("&").associate { param ->
            val parts = param.split("=", limit = 2)
            parts[0] to (parts.getOrNull(1) ?: "")
        }
    }

    // ── WebSocket Server ───────────────────────────────────────────

    /**
     * Inner WebSocket server that handles real-time bidirectional communication with
     * connected Ketoy client apps.
     *
     * ## Protocol
     *
     * **Server → Client messages:**
     *
     * | `type` | When | Payload |
     * |--------|------|---------|
     * | `connected` | On WebSocket open | current version |
     * | `bundle` | Immediately after `connected` | full screen + nav bundle |
     * | `update` | Screen file changed | screen name + JSON data |
     * | `nav_update` | Nav graph file changed | nav host name + JSON data |
     * | `pong` | In response to `"ping"` | — |
     * | `screen` | In response to `getScreen` request | screen name + JSON data |
     *
     * **Client → Server messages:**
     *
     * - `"ping"` — keep-alive; server replies with `{"type":"pong"}`.
     * - `{"type":"getScreen","name":"X"}` — on-demand screen fetch over the WebSocket channel.
     *
     * @param port The TCP port to bind the WebSocket server to (typically `httpPort + 1`).
     */
    private inner class KetoyWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {
        /**
         * Called when a new WebSocket client connects.
         *
         * Immediately sends a `connected` message with the current data version,
         * followed by a full `bundle` message so the client can hydrate its local cache.
         *
         * @param conn The newly opened WebSocket connection.
         * @param handshake The client handshake data.
         */
        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            println("🔌 Client connected: ${conn.remoteSocketAddress}")
            // Send current bundle on connect
            conn.send("""{"type":"connected","version":${screenManager.getVersion()}}""")
            val bundle = buildBundleJson()
            conn.send("""{"type":"bundle","version":${screenManager.getVersion()},"data":$bundle}""")
        }

        /**
         * Called when a WebSocket client disconnects. Logs the remote address for diagnostics.
         *
         * @param conn The closed connection.
         * @param code The WebSocket close code.
         * @param reason An optional human-readable close reason.
         * @param remote `true` if the close was initiated by the remote peer.
         */
        override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
            println("🔌 Client disconnected: ${conn.remoteSocketAddress}")
        }

        /**
         * Handles incoming messages from a WebSocket client.
         *
         * Supported messages:
         * - `"ping"` — replies with `{"type":"pong"}`.
         * - `{"type":"getScreen","name":"X"}` — replies with the requested screen JSON.
         *
         * Unknown messages are silently ignored.
         *
         * @param conn The connection that sent the message.
         * @param message The raw text message from the client.
         */
        override fun onMessage(conn: WebSocket, message: String) {
            // Handle client messages (e.g., ping, screen requests)
            when {
                message == "ping" -> conn.send("""{"type":"pong"}""")
                message.startsWith("{") -> {
                    // Try to parse as JSON command
                    try {
                        if (message.contains("\"type\":\"getScreen\"")) {
                            val nameMatch = Regex("\"name\":\"([^\"]+)\"").find(message)
                            val name = nameMatch?.groupValues?.get(1)
                            if (name != null) {
                                val screenJson = screenManager.getScreen(name)
                                if (screenJson != null) {
                                    conn.send("""{"type":"screen","screen":"$name","data":$screenJson}""")
                                }
                            }
                        }
                    } catch (_: Exception) { }
                }
            }
        }

        /**
         * Called when a WebSocket error occurs. Logs the error unless it is a
         * [java.net.BindException] (port already in use), which is handled at startup.
         *
         * @param conn The connection on which the error occurred, or `null` for server-level errors.
         * @param ex The exception describing the error.
         */
        override fun onError(conn: WebSocket?, ex: Exception) {
            if (ex !is java.net.BindException) {
                System.err.println("WebSocket error: ${ex.message}")
            }
        }

        /**
         * Called when the WebSocket server has started and is ready to accept connections.
         */
        override fun onStart() {
            // Server started
        }
    }
}
