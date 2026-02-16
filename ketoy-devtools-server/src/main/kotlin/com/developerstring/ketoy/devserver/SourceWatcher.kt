package com.developerstring.ketoy.devserver

import java.io.File
import java.nio.file.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Watches Kotlin source files for changes and triggers a Gradle export
 * to regenerate the JSON files in `ketoy-screens/`.
 *
 * The existing [FileWatcher] picks up the regenerated JSON and pushes
 * it to connected clients via WebSocket — completing the live-reload loop.
 *
 * Flow:
 *  Edit .kt DSL → SourceWatcher detects → runs gradlew ketoyExport
 *                                           ↓
 *                                      ketoy-screens JSON files updated
 *                                           ↓
 *                                      FileWatcher detects → WebSocket push → App recomposes
 */
class SourceWatcher(
    private val sourceDirs: List<File>,
    private val projectRoot: File,
    private val debounceMs: Long = 1500
) : Runnable {

    @Volatile
    private var running = true

    private val exportRunning = AtomicBoolean(false)
    private val lastChangeTime = AtomicLong(0)
    private val pendingExport = AtomicBoolean(false)

    override fun run() {
        val watchService = FileSystems.getDefault().newWatchService()
        val watchedPaths = mutableSetOf<Path>()

        // Register all source directories and their subdirectories
        for (sourceDir in sourceDirs) {
            if (!sourceDir.exists()) {
                println("⚠️  Source directory not found: ${sourceDir.absolutePath}")
                continue
            }
            sourceDir.walkTopDown()
                .filter { it.isDirectory }
                .forEach { dir ->
                    try {
                        val path = dir.toPath()
                        if (watchedPaths.add(path)) {
                            path.register(
                                watchService,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE
                            )
                        }
                    } catch (_: Exception) { }
                }
        }

        if (watchedPaths.isEmpty()) {
            println("⚠️  No source directories to watch — auto-export disabled")
            return
        }

        println("🔍 Source watcher started — watching ${watchedPaths.size} directories for .kt changes")
        sourceDirs.filter { it.exists() }.forEach {
            println("   📂 ${it.absolutePath}")
        }

        // Debounce thread: triggers export after debounceMs of quiet time
        val debounceThread = Thread({
            while (running) {
                try {
                    Thread.sleep(200)
                    if (pendingExport.get()) {
                        val elapsed = System.currentTimeMillis() - lastChangeTime.get()
                        if (elapsed >= debounceMs) {
                            pendingExport.set(false)
                            triggerExport()
                        }
                    }
                } catch (_: InterruptedException) {
                    break
                }
            }
        }, "ketoy-source-debounce")
        debounceThread.isDaemon = true
        debounceThread.start()

        while (running) {
            try {
                val key = watchService.poll(1, TimeUnit.SECONDS) ?: continue

                var hasKotlinChange = false

                for (event in key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue

                    @Suppress("UNCHECKED_CAST")
                    val ev = event as WatchEvent<Path>
                    val filename = ev.context().toString()

                    // Only care about Kotlin files
                    if (filename.endsWith(".kt") || filename.endsWith(".kts")) {
                        hasKotlinChange = true
                        val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                        println("[$timestamp] ✏️  Source changed: $filename")
                    }

                    // Watch newly created directories
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        val watchedDir = (key.watchable() as? Path)?.resolve(ev.context())
                        if (watchedDir != null && watchedDir.toFile().isDirectory) {
                            try {
                                if (watchedPaths.add(watchedDir)) {
                                    watchedDir.register(
                                        watchService,
                                        StandardWatchEventKinds.ENTRY_CREATE,
                                        StandardWatchEventKinds.ENTRY_MODIFY,
                                        StandardWatchEventKinds.ENTRY_DELETE
                                    )
                                }
                            } catch (_: Exception) { }
                        }
                    }
                }

                if (hasKotlinChange) {
                    lastChangeTime.set(System.currentTimeMillis())
                    pendingExport.set(true)
                }

                key.reset()
            } catch (_: InterruptedException) {
                break
            } catch (e: Exception) {
                System.err.println("Source watcher error: ${e.message}")
            }
        }

        watchService.close()
        println("🔍 Source watcher stopped")
    }

    private fun triggerExport() {
        if (!exportRunning.compareAndSet(false, true)) {
            // Already running — mark pending so we re-run after current finishes
            pendingExport.set(true)
            return
        }

        Thread({
            try {
                val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                println("[$timestamp] 🔄 Re-exporting screens from DSL...")

                val gradlew = if (System.getProperty("os.name").lowercase().contains("win")) {
                    File(projectRoot, "gradlew.bat")
                } else {
                    File(projectRoot, "gradlew")
                }

                if (!gradlew.exists()) {
                    System.err.println("⚠️  Gradle wrapper not found at: ${gradlew.absolutePath}")
                    return@Thread
                }

                val process = ProcessBuilder(
                    gradlew.absolutePath,
                    "ketoyExport",
                    "--quiet"
                )
                    .directory(projectRoot)
                    .redirectErrorStream(true)
                    .start()

                // Stream output for live feedback
                val output = StringBuilder()
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        output.appendLine(line)
                        // Only show important lines
                        if (line.contains("📄") || line.contains("✅") || line.contains("⚠️") ||
                            line.contains("FAIL") || line.contains("ERROR")) {
                            println("   $line")
                        }
                    }
                }

                val exitCode = process.waitFor()
                val endTimestamp = java.time.LocalTime.now().toString().substring(0, 8)

                if (exitCode == 0) {
                    println("[$endTimestamp] ✅ Export complete — JSON files updated")
                } else {
                    println("[$endTimestamp] ❌ Export failed (exit code $exitCode)")
                    // Show last few lines of output for debugging
                    val errorLines = output.lines().takeLast(5).filter { it.isNotBlank() }
                    errorLines.forEach { println("   $it") }
                }
            } catch (e: Exception) {
                System.err.println("⚠️  Export error: ${e.message}")
            } finally {
                exportRunning.set(false)
                // If another change came in while we were exporting, re-trigger
                if (pendingExport.get()) {
                    lastChangeTime.set(System.currentTimeMillis())
                }
            }
        }, "ketoy-export").start()
    }

    fun stop() {
        running = false
    }
}
