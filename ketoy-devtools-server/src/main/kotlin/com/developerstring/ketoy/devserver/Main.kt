package com.developerstring.ketoy.devserver

import java.io.File

fun main(args: Array<String>) {
    val config = parseArgs(args)

    println("""
    ┌─────────────────────────────────────────────────┐
    │          🚀 Ketoy Dev Server v1.1.0             │
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

    // Print QR code
    QRCodeGenerator.printQR(connectionUrl)

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

data class ServerConfig(
    val port: Int = 8484,
    val watchDir: String = "./ketoy-screens",
    val autoExport: Boolean = false,
    val projectRoot: String = ".",
    val sourceDirs: List<String> = listOf("app/src/main/java", "app/src/main/kotlin"),
    val debounceMs: Long = 1500
)

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
