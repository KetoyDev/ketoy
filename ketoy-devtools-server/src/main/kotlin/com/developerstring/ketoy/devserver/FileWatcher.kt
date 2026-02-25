package com.developerstring.ketoy.devserver

import java.io.File
import java.nio.file.*

/**
 * Watches a directory for JSON file changes and pushes live updates to connected Ketoy apps.
 *
 * Uses the Java NIO [WatchService][java.nio.file.WatchService] for efficient, OS-level
 * file-system monitoring. When a `.json` file is created, modified, or deleted inside the
 * watched directory (or any of its subdirectories), the watcher:
 *
 * 1. Delegates to [ScreenManager] to reload (or remove) the cached content.
 * 2. Calls [KetoyDevServer.broadcastUpdate] or [KetoyDevServer.broadcastNavUpdate] so that
 *    every connected device and emulator receives the change in real time.
 *
 * ## File conventions
 *
 * | Pattern | Interpretation |
 * |---------|----------------|
 * | `*.json` (not starting with `nav_`) | Screen definition |
 * | `nav_*.json` | Navigation graph definition |
 *
 * ## Threading
 *
 * `FileWatcher` implements [Runnable] and is intended to be started on a **daemon thread**
 * so that it terminates automatically when the JVM shuts down:
 *
 * ```kotlin
 * val watcher = FileWatcher(watchDir, screenManager, server)
 * val thread = Thread(watcher, "ketoy-file-watcher")
 * thread.isDaemon = true
 * thread.start()
 * ```
 *
 * A short 100 ms debounce sleep is applied after receiving a [WatchKey][java.nio.file.WatchKey]
 * to allow write operations to complete before reading file content.
 *
 * @param watchDir The root directory to monitor for JSON file changes.
 * @param screenManager The [ScreenManager] that owns the in-memory screen and nav-graph caches.
 * @param server The [KetoyDevServer] used to broadcast updates to connected clients.
 * @see ScreenManager
 * @see KetoyDevServer
 * @see SourceWatcher
 */
class FileWatcher(
    private val watchDir: File,
    private val screenManager: ScreenManager,
    private val server: KetoyDevServer
) : Runnable {

    /**
     * Volatile flag that controls the watcher’s main loop.
     * Set to `false` by [stop] to request a graceful shutdown.
     */
    @Volatile
    private var running = true

    /**
     * Main watch loop. Registers the [watchDir] (and all existing subdirectories) with a
     * [WatchService][java.nio.file.WatchService], then continuously polls for file-system
     * events until [stop] is called.
     *
     * For every detected `.json` change the method:
     * - Reloads the file via [ScreenManager.loadScreen] or [ScreenManager.loadNavGraph].
     * - Broadcasts the update through [KetoyDevServer.broadcastUpdate] or
     *   [KetoyDevServer.broadcastNavUpdate].
     * - Handles deletions via [ScreenManager.removeScreen] / [ScreenManager.removeNavGraph].
     */
    override fun run() {
        val path = watchDir.toPath()
        val watchService = FileSystems.getDefault().newWatchService()

        // Watch for create, modify, delete events
        path.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE
        )

        // Also watch subdirectories
        watchDir.walkTopDown()
            .filter { it.isDirectory }
            .forEach { dir ->
                try {
                    dir.toPath().register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE
                    )
                } catch (_: Exception) { }
            }

        println("👀 File watcher started")

        while (running) {
            try {
                val key = watchService.poll(1, java.util.concurrent.TimeUnit.SECONDS)
                if (key == null) continue

                // Debounce: wait a bit for writes to complete
                Thread.sleep(100)

                for (event in key.pollEvents()) {
                    val kind = event.kind()
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue

                    @Suppress("UNCHECKED_CAST")
                    val ev = event as WatchEvent<Path>
                    val filename = ev.context().toString()

                    // Only process JSON files
                    if (!filename.endsWith(".json")) continue

                    val file = watchDir.resolve(filename)
                    val isNavFile = filename.startsWith("nav_")

                    when (kind) {
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                            if (file.exists() && file.length() > 0) {
                                if (isNavFile) {
                                    val json = screenManager.loadNavGraph(file)
                                    if (json != null) {
                                        val navName = file.nameWithoutExtension.removePrefix("nav_")
                                        val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                                        println("[$timestamp] 🗺️  Nav changed: $filename")
                                        server.broadcastNavUpdate(navName, json)
                                    }
                                } else {
                                    val json = screenManager.loadScreen(file)
                                    if (json != null) {
                                        val screenName = file.nameWithoutExtension
                                        val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                                        println("[$timestamp] 📝 Changed: $filename")
                                        server.broadcastUpdate(screenName, json)
                                    }
                                }
                            }
                        }
                        StandardWatchEventKinds.ENTRY_DELETE -> {
                            val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                            if (isNavFile) {
                                val navName = file.nameWithoutExtension.removePrefix("nav_")
                                println("[$timestamp] 🗑️  Nav deleted: $filename")
                                screenManager.removeNavGraph(navName)
                            } else {
                                val screenName = file.nameWithoutExtension
                                println("[$timestamp] 🗑️  Deleted: $filename")
                                screenManager.removeScreen(screenName)
                            }
                        }
                    }
                }

                key.reset()
            } catch (e: InterruptedException) {
                break
            } catch (e: Exception) {
                System.err.println("File watcher error: ${e.message}")
            }
        }

        watchService.close()
        println("👀 File watcher stopped")
    }

    /**
     * Requests a graceful shutdown of the file watcher.
     *
     * The watcher loop will exit within at most ~1 second (the poll timeout) after this
     * method is called. Safe to call from any thread.
     */
    fun stop() {
        running = false
    }
}
