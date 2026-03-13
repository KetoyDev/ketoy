package dev.ketoy.gradle.server

import java.io.File
import java.nio.file.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Watches Kotlin source files (`.kt` / `.kts`) for changes and triggers
 * `gradlew ketoyExport` to regenerate the JSON screen definitions.
 *
 * Together with [FileWatcher], this class completes the **full live-reload loop**:
 *
 * ```
 * Developer edits .kt DSL
 *          ↓
 * SourceWatcher detects the change
 *          ↓
 * Runs `gradlew ketoyExport --quiet`
 *          ↓
 * ketoy-screens/ JSON files are regenerated
 *          ↓
 * FileWatcher detects the new JSON
 *          ↓
 * KetoyDevServer pushes the update via WebSocket
 *          ↓
 * Android app recomposes the UI instantly
 * ```
 *
 * @param sourceDirs Absolute paths to the Kotlin source directories to watch recursively.
 * @param projectRoot The root directory of the Gradle project (contains `gradlew`).
 * @param debounceMs Milliseconds of idle time after the last change before triggering an export.
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

                    if (filename.endsWith(".kt") || filename.endsWith(".kts")) {
                        hasKotlinChange = true
                        val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                        println("[$timestamp] ✏️  Source changed: $filename")
                    }

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
            pendingExport.set(true)
            return
        }

        Thread({
            try {
                val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
                println("[$timestamp] 🔄 Re-exporting screens from DSL...")

                val gradlew = if (System.getProperty("os.name")?.lowercase()?.contains("win") == true) {
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

                val output = StringBuilder()
                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        output.appendLine(line)
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
                    val errorLines = output.lines().takeLast(5).filter { it.isNotBlank() }
                    errorLines.forEach { println("   $it") }
                }
            } catch (e: Exception) {
                System.err.println("⚠️  Export error: ${e.message}")
            } finally {
                exportRunning.set(false)
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
