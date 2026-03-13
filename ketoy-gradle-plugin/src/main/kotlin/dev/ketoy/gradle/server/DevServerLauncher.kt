package dev.ketoy.gradle.server

import java.io.File

/**
 * Launches the Ketoy Dev Server pipeline — used by `ketoyServe` and `ketoyDev` Gradle tasks.
 *
 * This replaces the standalone `main()` entry point that previously lived in the
 * `ketoy-devtools-server` module. The server now runs directly inside the Gradle JVM
 * process, removing the need for a separate application module.
 */
object DevServerLauncher {

    /**
     * Boots the full Ketoy Dev Server from the given [config].
     *
     * This method **blocks** the calling thread (via `Thread.currentThread().join()`)
     * until the JVM is shut down (e.g. Ctrl+C).
     */
    fun launch(config: ServerConfig) {
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

        val localIp = NetworkUtils.getLocalIpAddress()

        println()
        println("📡 Server running at:")
        println("   Local:     http://localhost:${config.port}")
        println("   Network:   http://$localIp:${config.port}")
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

            println("🔄 Running initial export...")
            runInitialExport(projectRoot)

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
            println()
        }

        println("Press Ctrl+C to stop the server.")
        println("─".repeat(50))
        println()

        server.start()

        val watcherThread = Thread(fileWatcher, "ketoy-file-watcher")
        watcherThread.isDaemon = true
        watcherThread.start()

        Runtime.getRuntime().addShutdownHook(Thread {
            println("\n🛑 Shutting down Ketoy Dev Server...")
            server.stop()
            fileWatcher.stop()
            sourceWatcher?.stop()
        })

        // Keep alive until terminated
        Thread.currentThread().join()
    }

    private fun runInitialExport(projectRoot: File) {
        try {
            val gradlew = if (System.getProperty("os.name")?.lowercase()?.contains("win") == true) {
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
}
