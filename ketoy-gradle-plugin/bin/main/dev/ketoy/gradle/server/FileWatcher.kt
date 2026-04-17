package dev.ketoy.gradle.server

import java.io.File
import java.nio.file.*
import java.util.concurrent.TimeUnit

/**
 * Watches a directory for screen file changes and pushes live updates to connected Ketoy apps.
 *
 * Uses the Java NIO [WatchService] for efficient, OS-level file-system monitoring. When a
 * `.json` or `.ktw` (wire format) file is created, modified, or deleted inside the watched
 * directory, the watcher delegates to [ScreenManager] and broadcasts via [KetoyDevServer].
 *
 * @param watchDir The root directory to monitor for screen file changes.
 * @param screenManager The [ScreenManager] that owns the in-memory screen and nav-graph caches.
 * @param server The [KetoyDevServer] used to broadcast updates to connected clients.
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

        path.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE
        )

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
                val key = watchService.poll(1, TimeUnit.SECONDS)
                if (key == null) continue

                // Debounce: wait a bit for writes to complete
                Thread.sleep(100)

                for (event in key.pollEvents()) {
                    val kind = event.kind()
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue

                    @Suppress("UNCHECKED_CAST")
                    val ev = event as WatchEvent<Path>
                    val filename = ev.context().toString()

                    if (!filename.endsWith(".json") && !filename.endsWith(".ktw")) continue

                    val file = watchDir.resolve(filename)
                    val isNavFile = filename.startsWith("nav_")

                    when (kind) {
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                            if (file.exists() && file.length() > 0) {
                                if (isNavFile) {
                                    val content = screenManager.loadNavGraph(file)
                                    if (content != null) {
                                        val navName = file.nameWithoutExtension.removePrefix("nav_")
                                        val isWire = file.extension == "ktw"
                                        val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                                        val label = if (isWire) "📦" else "🗺️ "
                                        println("[$timestamp] $label Nav changed: $filename")
                                        server.broadcastNavUpdate(navName, content, isWire)
                                    }
                                } else {
                                    val content = screenManager.loadScreen(file)
                                    if (content != null) {
                                        val screenName = file.nameWithoutExtension
                                        val isWire = file.extension == "ktw"
                                        val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                                        val label = if (isWire) "📦" else "📝"
                                        println("[$timestamp] $label Changed: $filename")
                                        server.broadcastUpdate(screenName, content, isWire)
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

    fun stop() {
        running = false
    }
}
