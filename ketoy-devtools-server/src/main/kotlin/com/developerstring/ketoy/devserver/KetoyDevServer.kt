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
 * Ketoy Dev Server — serves screen JSON via HTTP and pushes
 * live updates via WebSocket to connected Ketoy apps.
 */
class KetoyDevServer(
    private val port: Int,
    private val screenManager: ScreenManager
) {
    private lateinit var httpServer: HttpServer
    private lateinit var wsServer: KetoyWebSocketServer
    private val executor = Executors.newCachedThreadPool()

    // Long-poll waiters
    private val pollWaiters = ConcurrentHashMap<Long, HttpExchange>()
    private var pollIdCounter = 0L

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

        httpServer.start()

        // WebSocket server on port + 1
        wsServer = KetoyWebSocketServer(port + 1)
        wsServer.isReuseAddr = true
        wsServer.start()

        println("🌐 HTTP server started on port $port")
        println("🔌 WebSocket server started on port ${port + 1}")
    }

    fun stop() {
        httpServer.stop(1)
        wsServer.stop(1000)
        executor.shutdown()
    }

    /**
     * Called by FileWatcher when screens change.
     * Broadcasts to all WebSocket clients and wakes up long-poll waiters.
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

    // ── HTTP Handlers ──────────────────────────────────────────────

    private fun handleRoot(exchange: HttpExchange) {
        val localIp = NetworkUtils.getLocalIpAddress()
        val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Ketoy Dev Server</title>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; 
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
                <h3>🔗 API Endpoints</h3>
                <pre>
GET /status          → Server status
GET /screens         → List available screens
GET /screen?name=X   → Get screen JSON
GET /bundle          → Get all screens as a bundle
GET /poll?v=N        → Long-poll for changes
WS  :${port + 1}    → WebSocket live updates
                </pre>
            </div>
        </body>
        </html>
        """.trimIndent()
        sendHtml(exchange, 200, html)
    }

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

    private fun handleScreens(exchange: HttpExchange) {
        val screens = screenManager.listScreens()
        val json = """{"version":${screenManager.getVersion()},"screens":[${screens.joinToString(",") { "\"$it\"" }}]}"""
        sendJson(exchange, 200, json)
    }

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

    private fun handleBundle(exchange: HttpExchange) {
        sendJson(exchange, 200, buildBundleJson())
    }

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

    private fun buildBundleJson(): String {
        val screens = screenManager.getAllScreens()
        val screensJson = screens.entries.joinToString(",") { (name, json) ->
            "\"$name\":$json"
        }
        return """{"version":${screenManager.getVersion()},"screens":{$screensJson}}"""
    }

    // ── Helpers ────────────────────────────────────────────────────

    private fun sendJson(exchange: HttpExchange, code: Int, json: String) {
        val bytes = json.toByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun sendHtml(exchange: HttpExchange, code: Int, html: String) {
        val bytes = html.toByteArray()
        exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split("&").associate { param ->
            val parts = param.split("=", limit = 2)
            parts[0] to (parts.getOrNull(1) ?: "")
        }
    }

    // ── WebSocket Server ───────────────────────────────────────────

    private inner class KetoyWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {
        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            println("🔌 Client connected: ${conn.remoteSocketAddress}")
            // Send current bundle on connect
            conn.send("""{"type":"connected","version":${screenManager.getVersion()}}""")
            val bundle = buildBundleJson()
            conn.send("""{"type":"bundle","version":${screenManager.getVersion()},"data":$bundle}""")
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
            println("🔌 Client disconnected: ${conn.remoteSocketAddress}")
        }

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

        override fun onError(conn: WebSocket?, ex: Exception) {
            if (ex !is java.net.BindException) {
                System.err.println("WebSocket error: ${ex.message}")
            }
        }

        override fun onStart() {
            // Server started
        }
    }
}
