package com.developerstring.ketoy.devserver

import java.io.File
import java.nio.file.*

/**
 * Watches a directory for JSON file changes and notifies the server.
 * Uses Java NIO WatchService for efficient file system monitoring.
 */
class FileWatcher(
    private val watchDir: File,
    private val screenManager: ScreenManager,
    private val server: KetoyDevServer
) : Runnable {

    @Volatile
    private var running = true

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
                    val screenName = file.nameWithoutExtension

                    when (kind) {
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                            if (file.exists() && file.length() > 0) {
                                val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                                println("[$timestamp] 📝 Changed: $filename")

                                val json = screenManager.loadScreen(file)
                                if (json != null) {
                                    server.broadcastUpdate(screenName, json)
                                }
                            }
                        }
                        StandardWatchEventKinds.ENTRY_DELETE -> {
                            val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                            println("[$timestamp] 🗑️  Deleted: $filename")
                            screenManager.removeScreen(screenName)
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

    fun stop() {
        running = false
    }
}
