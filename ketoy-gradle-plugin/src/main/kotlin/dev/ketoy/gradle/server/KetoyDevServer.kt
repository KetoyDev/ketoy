package dev.ketoy.gradle.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Ketoy Dev Server — serves SDUI screen JSON over HTTP and pushes live hot-reload
 * updates to connected Android apps via WebSocket.
 *
 * ## HTTP endpoints
 *
 * | Endpoint | Description |
 * |----------|-------------|
 * | `GET /` | Human-readable dashboard with connection info |
 * | `GET /status` | JSON health-check (version, client count) |
 * | `GET /screens` | List available screen names |
 * | `GET /screen?name=X` | Fetch a single screen's JSON |
 * | `GET /navs` | List available navigation graph names |
 * | `GET /nav?name=X` | Fetch a single nav graph's JSON |
 * | `GET /bundle` | Fetch all screens + nav graphs in one payload |
 * | `GET /poll?v=N` | Long-poll; returns immediately if server version > N, else waits up to 30 s |
 * | `WS :port+1` | WebSocket for real-time push updates |
 *
 * @param port The TCP port for the HTTP server. The WebSocket server will bind to `port + 1`.
 * @param screenManager The [ScreenManager] instance that owns the screen and nav-graph caches.
 */
class KetoyDevServer(
    private val port: Int,
    private val screenManager: ScreenManager
) {
    private lateinit var httpServer: HttpServer
    private lateinit var wsServer: KetoyWebSocketServer
    private val executor = Executors.newCachedThreadPool()

    private val pollWaiters = ConcurrentHashMap<Long, HttpExchange>()
    private var pollIdCounter = 0L

    fun start() {
        // Kill any stale process holding the port (e.g. a previous Gradle daemon)
        killProcessOnPort(port)
        killProcessOnPort(port + 1)

        httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer.executor = executor

        // Wrap every handler with request logging so connection issues are visible
        httpServer.createContext("/")       { exchange -> logRequest(exchange) { handleRoot(it) } }
        httpServer.createContext("/status") { exchange -> logRequest(exchange) { handleStatus(it) } }
        httpServer.createContext("/screens") { exchange -> logRequest(exchange) { handleScreens(it) } }
        httpServer.createContext("/screen") { exchange -> logRequest(exchange) { handleScreen(it) } }
        httpServer.createContext("/poll")   { exchange -> logRequest(exchange) { handlePoll(it) } }
        httpServer.createContext("/bundle") { exchange -> logRequest(exchange) { handleBundle(it) } }
        httpServer.createContext("/nav")    { exchange -> logRequest(exchange) { handleNav(it) } }
        httpServer.createContext("/navs")   { exchange -> logRequest(exchange) { handleNavs(it) } }

        httpServer.start()

        wsServer = KetoyWebSocketServer(port + 1)
        wsServer.isReuseAddr = true
        wsServer.start()

        println("🌐 HTTP server started on port $port  (bound to 0.0.0.0 — all interfaces)")
        println("🔌 WebSocket server started on port ${port + 1}  (bound to 0.0.0.0 — all interfaces)")
    }

    /**
     * Logs every incoming HTTP request with its origin address so that
     * developers can confirm their Android app is actually reaching the server.
     */
    private fun logRequest(exchange: HttpExchange, handler: (HttpExchange) -> Unit) {
        val remote = exchange.remoteAddress?.toString() ?: "unknown"
        val path   = exchange.requestURI.toString()
        val method = exchange.requestMethod
        println("📩 $method $path  ← $remote")
        try {
            handler(exchange)
        } catch (e: Exception) {
            System.err.println("⚠️  Handler error for $path: ${e.javaClass.simpleName}: ${e.message}")
            try {
                val msg = """{"error":"Internal server error: ${e.message}"}""".toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(500, msg.size.toLong())
                exchange.responseBody.use { it.write(msg) }
            } catch (_: Exception) { }
        }
    }

    /**
     * Attempts to kill any process currently occupying the given [port].
     * This handles stale Gradle daemon processes from previous runs.
     */
    private fun killProcessOnPort(port: Int) {
        try {
            val os = System.getProperty("os.name")?.lowercase() ?: return
            if (os.contains("mac") || os.contains("linux")) {
                // Find the PID using lsof
                val lsof = ProcessBuilder("lsof", "-ti", "tcp:$port")
                    .redirectErrorStream(true)
                    .start()
                val pids = lsof.inputStream.bufferedReader().readText().trim()
                lsof.waitFor()

                if (pids.isNotEmpty()) {
                    println("⚠️  Port $port is in use — killing stale process (PID: ${pids.replace("\n", ", ")})")
                    for (pid in pids.lines().filter { it.isNotBlank() }) {
                        ProcessBuilder("kill", "-9", pid.trim())
                            .redirectErrorStream(true)
                            .start()
                            .waitFor()
                    }
                    // Brief pause for OS to release the port
                    Thread.sleep(500)
                }
            } else if (os.contains("win")) {
                val netstat = ProcessBuilder("cmd", "/c", "netstat -ano | findstr :$port")
                    .redirectErrorStream(true)
                    .start()
                val output = netstat.inputStream.bufferedReader().readText().trim()
                netstat.waitFor()

                val pidPattern = Regex("\\s(\\d+)\\s*$", RegexOption.MULTILINE)
                val pids = pidPattern.findAll(output).map { it.groupValues[1] }.toSet()
                for (pid in pids) {
                    println("⚠️  Port $port is in use — killing stale process (PID: $pid)")
                    ProcessBuilder("cmd", "/c", "taskkill /F /PID $pid")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor()
                }
                if (pids.isNotEmpty()) Thread.sleep(500)
            }
        } catch (_: Exception) {
            // Best-effort — if it fails, the BindException will still surface
        }
    }

    fun stop() {
        httpServer.stop(1)
        wsServer.stop(1000)
        executor.shutdown()
    }

    fun broadcastUpdate(screenName: String, data: String, isWire: Boolean = false) {
        val version = screenManager.getVersion()
        val dataField = if (isWire) {
            """"data":"$data","format":"ktw""""
        } else {
            """"data":$data"""
        }
        val message = """{"type":"update","screen":"$screenName","version":$version,$dataField}"""

        wsServer.broadcast(message)

        synchronized(pollWaiters) {
            pollWaiters.forEach { (_, exchange) ->
                try {
                    sendJson(exchange, 200, buildBundleJson())
                } catch (_: Exception) { }
            }
            pollWaiters.clear()
        }

        val label = if (isWire) "wire" else "json"
        println("📤 Pushed update: $screenName [$label] (v$version) → ${wsServer.connections.size} client(s)")
    }

    fun broadcastNavUpdate(navName: String, data: String, isWire: Boolean = false) {
        val version = screenManager.getVersion()
        val dataField = if (isWire) {
            """"data":"$data","format":"ktw""""
        } else {
            """"data":$data"""
        }
        val message = """{"type":"nav_update","navHost":"$navName","version":$version,$dataField}"""

        wsServer.broadcast(message)

        synchronized(pollWaiters) {
            pollWaiters.forEach { (_, exchange) ->
                try {
                    sendJson(exchange, 200, buildBundleJson())
                } catch (_: Exception) { }
            }
            pollWaiters.clear()
        }

        val label = if (isWire) "wire" else "json"
        println("📤 Pushed nav update: $navName [$label] (v$version) → ${wsServer.connections.size} client(s)")
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
                }.ifEmpty { "<p>No screens yet. Add .json or .ktw files to the watch directory.</p>" }}
            </div>
            <div class="card">
                <h3>🗺️ Navigation Graphs</h3>
                ${screenManager.listNavGraphs().joinToString("") { name ->
                    val badge = if (screenManager.isWireFormatNav(name)) "ktw" else "nav"
                    "<div class='screen'>• $name <span class='badge'>$badge</span></div>"
                }.ifEmpty { "<p>No nav graphs yet. Add nav_*.json or nav_*.ktw files to the watch directory.</p>" }}
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

    private fun handleStatus(exchange: HttpExchange) {
        val json = """{
            "server": "ketoy-dev-server",
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
        val screenData = screenManager.getScreen(name)
        if (screenData == null) {
            sendJson(exchange, 404, """{"error":"Screen '$name' not found"}""")
            return
        }
        val isWire = screenManager.isWireFormat(name)
        val dataField = if (isWire) {
            """"data":"$screenData","format":"ktw""""
        } else {
            """"data":$screenData"""
        }
        val json = """{"version":${screenManager.getVersion()},"screen":"$name",$dataField}"""
        sendJson(exchange, 200, json)
    }

    private fun handleBundle(exchange: HttpExchange) {
        sendJson(exchange, 200, buildBundleJson())
    }

    private fun handleNavs(exchange: HttpExchange) {
        val navs = screenManager.listNavGraphs()
        val json = """{"version":${screenManager.getVersion()},"navGraphs":[${navs.joinToString(",") { "\"$it\"" }}]}"""
        sendJson(exchange, 200, json)
    }

    private fun handleNav(exchange: HttpExchange) {
        val params = parseQuery(exchange.requestURI.query ?: "")
        val name = params["name"]
        if (name == null) {
            sendJson(exchange, 400, """{"error":"Missing 'name' parameter"}""")
            return
        }
        val navData = screenManager.getNavGraph(name)
        if (navData == null) {
            sendJson(exchange, 404, """{"error":"Nav graph '$name' not found"}""")
            return
        }
        val isWire = screenManager.isWireFormatNav(name)
        val dataField = if (isWire) {
            """"data":"$navData","format":"ktw""""
        } else {
            """"data":$navData"""
        }
        val json = """{"version":${screenManager.getVersion()},"navHost":"$name",$dataField}"""
        sendJson(exchange, 200, json)
    }

    private fun handlePoll(exchange: HttpExchange) {
        val params = parseQuery(exchange.requestURI.query ?: "")
        val clientVersion = params["v"]?.toLongOrNull() ?: 0

        if (screenManager.getVersion() > clientVersion) {
            sendJson(exchange, 200, buildBundleJson())
        } else {
            val pollId = synchronized(this) { pollIdCounter++ }
            pollWaiters[pollId] = exchange

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
        val screensJson = screens.entries.joinToString(",") { (name, data) ->
            if (screenManager.isWireFormat(name)) {
                "\"$name\":{\"format\":\"ktw\",\"data\":\"$data\"}"
            } else {
                "\"$name\":$data"
            }
        }
        val navGraphs = screenManager.getAllNavGraphs()
        val navsJson = navGraphs.entries.joinToString(",") { (name, data) ->
            if (screenManager.isWireFormatNav(name)) {
                "\"$name\":{\"format\":\"ktw\",\"data\":\"$data\"}"
            } else {
                "\"$name\":$data"
            }
        }
        return """{"version":${screenManager.getVersion()},"screens":{$screensJson},"navGraphs":{$navsJson}}"""
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
            (parts.firstOrNull() ?: "") to (parts.getOrNull(1) ?: "")
        }
    }

    // ── WebSocket Server ───────────────────────────────────────────

    private inner class KetoyWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            val addr = conn.remoteSocketAddress
            println("🔌 WebSocket client connected: $addr  (total: ${connections.size})")
            conn.send("""{"type":"connected","version":${screenManager.getVersion()}}""")
            val bundle = buildBundleJson()
            conn.send("""{"type":"bundle","version":${screenManager.getVersion()},"data":$bundle}""")
            println("   ↳ Sent initial bundle to $addr")
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
            val cause = if (remote) "remote closed" else "server closed"
            println("🔌 WebSocket client disconnected: ${conn.remoteSocketAddress}  code=$code reason=${reason?.ifBlank { "none" }} ($cause)  remaining: ${connections.size}")
        }

        override fun onMessage(conn: WebSocket, message: String) {
            when {
                message == "ping" -> conn.send("""{"type":"pong"}""")
                message.startsWith("{") -> {
                    try {
                        if (message.contains("\"type\":\"getScreen\"")) {
                            val nameMatch = Regex("\"name\":\"([^\"]+)\"").find(message)
                            val name = nameMatch?.groupValues?.get(1)
                            if (name != null) {
                                val screenData = screenManager.getScreen(name)
                                if (screenData != null) {
                                    val isWire = screenManager.isWireFormat(name)
                                    val dataField = if (isWire) {
                                        """"data":"$screenData","format":"ktw""""
                                    } else {
                                        """"data":$screenData"""
                                    }
                                    conn.send("""{"type":"screen","screen":"$name",$dataField}""")
                                }
                            }
                        }
                    } catch (_: Exception) { }
                }
            }
        }

        override fun onError(conn: WebSocket?, ex: Exception) {
            if (ex is java.net.BindException) {
                System.err.println("⚠️  WebSocket BIND error on port ${port + 1}: ${ex.message}")
                System.err.println("   → Another process may be using port ${port + 1}. Try a different port.")
            } else {
                System.err.println("⚠️  WebSocket error (${conn?.remoteSocketAddress ?: "no connection"}): ${ex.javaClass.simpleName}: ${ex.message}")
            }
        }

        override fun onStart() {
            // Server started
        }
    }
}
