/**
 * Entry point for the Ketoy Dev Server — a JVM-based development server that enables
 * **hot-reload preview** of Ketoy Server-Driven UI (SDUI) screens on connected Android devices
 * and emulators.
 *
 * This module is the backbone of the Ketoy developer experience: it watches JSON screen
 * definitions (and optionally Kotlin DSL sources), serves them over HTTP/WebSocket, and
 * pushes live updates to every connected Ketoy-powered app in real time.
 *
 * ## Architecture overview
 *
 * ```
 * Developer edits .json / .kt DSL
 *          ↓
 *   SourceWatcher (optional)  →  gradlew ketoyExport  →  regenerated JSON
 *          ↓                                                    ↓
 *   FileWatcher detects change  ←──────────────────────────────┘
 *          ↓
 *   ScreenManager caches + versions
 *          ↓
 *   KetoyDevServer pushes via WebSocket / long-poll
 *          ↓
 *   Android app recomposes UI instantly
 * ```
 *
 * ## Quick start
 *
 * ```bash
 * # JSON-only watching (edit JSON → instant preview)
 * ./gradlew ketoyServe
 *
 * # Full live-reload (edit Kotlin DSL → auto-export JSON → instant preview)
 * ./gradlew ketoyDev
 * ```
 *
 * @see KetoyDevServer
 * @see FileWatcher
 * @see SourceWatcher
 * @see ScreenManager
 * @see ServerConfig
 */
package com.developerstring.ketoy.devserver

import java.io.File

/**
 * Application entry point that bootstraps the entire Ketoy Dev Server pipeline.
 *
 * Performs the following steps in order:
 * 1. Parses command-line arguments into a [ServerConfig].
 * 2. Ensures the watch directory exists.
 * 3. Creates a [ScreenManager] to load and cache screen/nav-graph JSON files.
 * 4. Starts the [KetoyDevServer] (HTTP + WebSocket).
 * 5. Starts the [FileWatcher] to monitor the JSON watch directory.
 * 6. Optionally starts a [SourceWatcher] for Kotlin DSL auto-export.
 * 7. Registers a JVM shutdown hook for graceful cleanup.
 *
 * ### Usage
 *
 * ```bash
 * # Minimal — watches ./ketoy-screens on port 8484
 * java -jar ketoy-devtools-server.jar
 *
 * # Custom port + auto-export
 * java -jar ketoy-devtools-server.jar --port 9090 --auto-export
 *
 * # Multiple source directories
 * java -jar ketoy-devtools-server.jar -a -s app/src/main/java -s feature/src/main/kotlin
 * ```
 *
 * @param args Command-line arguments. Run with `--help` for the full list.
 * @see parseArgs
 * @see ServerConfig
 */
fun main(args: Array<String>) {
    val config = parseArgs(args)

    println("""
    ┌─────────────────────────────────────────────────┐
    │           Ketoy Dev Server v0.1-beta            │
    │       Hot-reload for Ketoy UI Framework         │
    └─────────────────────────────────────────────────┘
    """.trimIndent())

    val watchDir = File(config.watchDir).absoluteFile
    if (!watchDir.exists()) {
        watchDir.mkdirs()
        println("📁 Created watch directory: ${watchDir.absolutePath}")
    }

    val screenManager = ScreenManager(watchDir)
    val server = KetoyDevServer(config.port, screenManager)
    val fileWatcher = FileWatcher(watchDir, screenManager, server)

    // Print connection info
    val localIp = NetworkUtils.getLocalIpAddress()
    val connectionUrl = "http://$localIp:${config.port}"

    println()
    println("📡 Server running at:")
    println("   Local:     http://localhost:${config.port}")
    println("   Network:   $connectionUrl")
    println("   Emulator:  http://10.0.2.2:${config.port}  (use this if running in Android Emulator)")
    println()
    println("📱 Enter this URL in your app's Ketoy Dev connection screen:")
    println("   Physical device → $localIp")
    println("   Android Emulator → 10.0.2.2")
    println()
    println("👀 Watching: ${watchDir.absolutePath}")
    println("   Supported files: *.json")
    println()
    println("   Available screens:")
    val screens = screenManager.listScreens()
    if (screens.isEmpty()) {
        println("   (none yet — add .json files to ${watchDir.absolutePath})")
    } else {
        screens.forEach { println("   • $it") }
    }

    val navGraphs = screenManager.listNavGraphs()
    if (navGraphs.isNotEmpty()) {
        println()
        println("   Navigation graphs:")
        navGraphs.forEach { println("   🗺️  $it") }
    }
    println()

    // ── Auto-export: watch Kotlin source for DSL changes ──────
    var sourceWatcher: SourceWatcher? = null
    if (config.autoExport) {
        val projectRoot = File(config.projectRoot).absoluteFile
        val sourceDirs = config.sourceDirs.map { File(projectRoot, it).absoluteFile }

        println("─".repeat(50))
        println()
        println("🔁 Auto-export enabled!")
        println("   Edit Kotlin DSL → auto-rebuild JSON → live push to app")
        println()

        // Run initial export to ensure JSON files are up to date
        println("🔄 Running initial export...")
        runInitialExport(projectRoot)

        // Reload screens after export so the server has the latest JSON
        screenManager.loadAllScreens()

        val sw = SourceWatcher(sourceDirs, projectRoot, config.debounceMs)
        sourceWatcher = sw
        val sourceWatcherThread = Thread(sw, "ketoy-source-watcher")
        sourceWatcherThread.isDaemon = true
        sourceWatcherThread.start()

        println()
    } else {
        println("💡 Tip: Run with --auto-export to watch Kotlin sources and auto-rebuild JSON:")
        println("   ./gradlew ketoyDev")
        println("   (or: ./gradlew ketoyServe --args=\"--auto-export\")")
        println()
    }

    println("Press Ctrl+C to stop the server.")
    println("─".repeat(50))
    println()

    // Start server
    server.start()

    // Start file watcher in background
    val watcherThread = Thread(fileWatcher, "ketoy-file-watcher")
    watcherThread.isDaemon = true
    watcherThread.start()

    // Handle shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        println("\n🛑 Shutting down Ketoy Dev Server...")
        server.stop()
        fileWatcher.stop()
        sourceWatcher?.stop()
    })

    // Keep main thread alive
    Thread.currentThread().join()
}

/**
 * Runs a one-time `gradlew ketoyExport` so that the JSON files in the watch directory
 * are up to date before the server starts serving content.
 *
 * This is invoked only when `--auto-export` is enabled. If the Gradle wrapper is missing
 * or the export task fails, a warning is printed but the server continues — the
 * [SourceWatcher] will retry on the next source change.
 *
 * @param projectRoot The root directory of the Gradle project (contains `gradlew` / `gradlew.bat`).
 * @see SourceWatcher
 */
private fun runInitialExport(projectRoot: File) {
    try {
        val gradlew = if (System.getProperty("os.name").lowercase().contains("win")) {
            File(projectRoot, "gradlew.bat")
        } else {
            File(projectRoot, "gradlew")
        }
        if (!gradlew.exists()) return

        val process = ProcessBuilder(gradlew.absolutePath, "ketoyExport", "--quiet")
            .directory(projectRoot)
            .redirectErrorStream(true)
            .start()

        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.contains("📄") || line.contains("✅")) println("   $line")
            }
        }
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            println("   ✅ Initial export complete")
        } else {
            println("   ⚠️  Initial export had errors (exit $exitCode) — will retry on next source change")
        }
    } catch (e: Exception) {
        println("   ⚠️  Could not run initial export: ${e.message}")
    }
}

/**
 * Immutable configuration for the Ketoy Dev Server, typically produced by [parseArgs].
 *
 * Encapsulates every tunable parameter for the server, file-watching, and auto-export
 * pipelines. The defaults are chosen to work out-of-the-box for a standard Ketoy project
 * layout generated by the Ketoy Gradle plugin.
 *
 * ### Example — programmatic construction
 *
 * ```kotlin
 * val config = ServerConfig(
 *     port = 9090,
 *     watchDir = "./my-screens",
 *     autoExport = true,
 *     sourceDirs = listOf("feature/src/main/kotlin")
 * )
 * ```
 *
 * @property port The TCP port for the HTTP server. The WebSocket server uses `port + 1`. Default `8484`.
 * @property watchDir Path to the directory that contains the screen JSON files to serve. Default `./ketoy-screens`.
 * @property autoExport When `true`, a [SourceWatcher] monitors Kotlin sources and automatically
 *   runs `gradlew ketoyExport` to regenerate JSON on every change. Default `false`.
 * @property projectRoot Root directory of the Gradle project. Used to locate the Gradle wrapper. Default `.`.
 * @property sourceDirs List of source directories (relative to [projectRoot]) to watch when [autoExport] is enabled.
 *   Default `["app/src/main/java", "app/src/main/kotlin"]`.
 * @property debounceMs Milliseconds to wait after the last source change before triggering an export,
 *   preventing redundant builds during rapid edits. Default `1500`.
 * @see parseArgs
 */
data class ServerConfig(
    val port: Int = 8484,
    val watchDir: String = "./ketoy-screens",
    val autoExport: Boolean = false,
    val projectRoot: String = ".",
    val sourceDirs: List<String> = listOf("app/src/main/java", "app/src/main/kotlin"),
    val debounceMs: Long = 1500
)

/**
 * Parses command-line arguments into a [ServerConfig].
 *
 * Supports both `--flag value` and `--flag=value` syntax. Unrecognised flags are
 * silently ignored so that Gradle-injected JVM arguments do not cause errors.
 *
 * ### Supported flags
 *
 * | Flag | Short | Description | Default |
 * |------|-------|-------------|---------|
 * | `--port` | `-p` | HTTP server port | `8484` |
 * | `--watch` | `-w` | JSON watch directory | `./ketoy-screens` |
 * | `--auto-export` | `-a` | Enable Kotlin DSL auto-export | `false` |
 * | `--project` | — | Gradle project root | `.` |
 * | `--source` | `-s` | Source dir to watch (repeatable) | `app/src/main/java`, `app/src/main/kotlin` |
 * | `--debounce` | — | Export debounce in ms | `1500` |
 * | `--help` | `-h` | Print help and exit | — |
 *
 * ### Example
 *
 * ```kotlin
 * val config = parseArgs(arrayOf("--port", "9090", "--auto-export"))
 * // config.port == 9090
 * // config.autoExport == true
 * ```
 *
 * @param args The raw command-line arguments passed to [main].
 * @return A fully resolved [ServerConfig] with defaults applied for any omitted flags.
 * @see ServerConfig
 */
fun parseArgs(args: Array<String>): ServerConfig {
    var port = 8484
    var watchDir = "./ketoy-screens"
    var autoExport = false
    var projectRoot = "."
    var sourceDirs = mutableListOf<String>()
    var debounceMs = 1500L

    var i = 0
    while (i < args.size) {
        when {
            args[i] == "--port" || args[i] == "-p" -> {
                port = args.getOrNull(i + 1)?.toIntOrNull() ?: 8484
                i += 2
            }
            args[i] == "--watch" || args[i] == "-w" -> {
                watchDir = args.getOrNull(i + 1) ?: "./ketoy-screens"
                i += 2
            }
            args[i] == "--auto-export" || args[i] == "-a" -> {
                autoExport = true
                i++
            }
            args[i] == "--project" -> {
                projectRoot = args.getOrNull(i + 1) ?: "."
                i += 2
            }
            args[i] == "--source" || args[i] == "-s" -> {
                sourceDirs.add(args.getOrNull(i + 1) ?: "app/src/main/java")
                i += 2
            }
            args[i] == "--debounce" -> {
                debounceMs = args.getOrNull(i + 1)?.toLongOrNull() ?: 1500L
                i += 2
            }
            args[i].startsWith("--port=") -> {
                port = args[i].substringAfter("=").toIntOrNull() ?: 8484
                i++
            }
            args[i].startsWith("--watch=") -> {
                watchDir = args[i].substringAfter("=")
                i++
            }
            args[i].startsWith("--project=") -> {
                projectRoot = args[i].substringAfter("=")
                i++
            }
            args[i].startsWith("--source=") -> {
                sourceDirs.add(args[i].substringAfter("="))
                i++
            }
            args[i].startsWith("--debounce=") -> {
                debounceMs = args[i].substringAfter("=").toLongOrNull() ?: 1500L
                i++
            }
            args[i] == "--help" || args[i] == "-h" -> {
                println("""
                    Ketoy Dev Server - Hot-reload server for Ketoy UI
                    
                    Usage: ketoy-serve [options]
                    
                    Options:
                      --port, -p <port>        Server port (default: 8484)
                      --watch, -w <dir>        Directory to watch for JSON files (default: ./ketoy-screens)
                      --auto-export, -a        Watch Kotlin sources and auto-export DSL to JSON
                      --project <dir>          Project root for Gradle (default: current dir)
                      --source, -s <dir>       Source directory to watch (repeatable, default: app/src/main/java)
                      --debounce <ms>          Wait time before triggering export (default: 1500ms)
                      --help, -h               Show this help message
                    
                    Examples:
                      ketoy-serve                             # JSON watching only
                      ketoy-serve --auto-export               # Full live-reload (DSL → JSON → app)
                      ketoy-serve -a -s app/src/main/java -s ketoy-sdk/src/main/java
                """.trimIndent())
                System.exit(0)
                i++
            }
            else -> i++
        }
    }

    return ServerConfig(
        port = port,
        watchDir = watchDir,
        autoExport = autoExport,
        projectRoot = projectRoot,
        sourceDirs = sourceDirs.ifEmpty { listOf("app/src/main/java", "app/src/main/kotlin") },
        debounceMs = debounceMs
    )
}
