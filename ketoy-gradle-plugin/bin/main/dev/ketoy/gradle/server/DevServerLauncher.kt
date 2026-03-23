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
    │           Ketoy Dev Server v0.1.2-beta            │
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
        println()
        println("📱 Enter this URL in your app's Ketoy Dev connection screen:")
        println("   Physical device  → $localIp:${config.port}")
        println("   Android Emulator → localhost:${config.port}       ← requires ADB reverse (auto-configured below)")
        println("   Android Emulator → 10.0.2.2:${config.port}       ← always works, no ADB needed")
        println()
        println("👀 Watching: ${watchDir.absolutePath}")
        println("   Supported files: *.json, *.ktw (wire format)")
        println()
        println("   Available screens:")
        val screens = screenManager.listScreens()
        if (screens.isEmpty()) {
            println("   (none yet — add .json or .ktw files to ${watchDir.absolutePath})")
        } else {
            val wireNames = screenManager.getWireScreenNames()
            screens.forEach { name ->
                val label = if (wireNames.contains(name)) "[ktw]" else "[json]"
                println("   • $name $label")
            }
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

        // ── Emulator: set up adb reverse port forwarding ────────
        val reversePorts = listOf(config.port, config.port + 1)
        val adbResults = NetworkUtils.setupAdbReverse(reversePorts, config.androidSdkDir)
        if (adbResults.isNotEmpty()) {
            println("🔌 Emulator port forwarding:")
            adbResults.forEach { println("   $it") }
            println()
        }

        // Retry ADB reverse every 30 s to pick up emulators that start after the server.
        // Without this, `localhost:port` silently fails on late-starting emulators.
        val adbRetryThread = Thread({
            // Track which (serial, port) combos already have reverse configured so
            // we only print a log line when something genuinely new is set up.
            val alreadyForwarded = mutableSetOf<String>()
            // Seed with what was set up at startup
            adbResults.filter { it.startsWith("✅") }.forEach { alreadyForwarded.add(it) }

            while (!Thread.currentThread().isInterrupted) {
                try { Thread.sleep(30_000) } catch (_: InterruptedException) { break }
                val retryResults = NetworkUtils.setupAdbReverse(reversePorts, config.androidSdkDir)
                retryResults
                    .filter { it.startsWith("✅") && !alreadyForwarded.contains(it) }
                    .forEach { msg ->
                        println("🔄 ADB: new emulator detected — $msg")
                        alreadyForwarded.add(msg)
                    }
            }
        }, "ketoy-adb-retry")
        adbRetryThread.isDaemon = true
        adbRetryThread.start()

        val watcherThread = Thread(fileWatcher, "ketoy-file-watcher")
        watcherThread.isDaemon = true
        watcherThread.start()

        Runtime.getRuntime().addShutdownHook(Thread {
            println("\n🛑 Shutting down Ketoy Dev Server...")
            adbRetryThread.interrupt()
            NetworkUtils.removeAdbReverse(reversePorts, config.androidSdkDir)
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
