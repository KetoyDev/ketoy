package com.developerstring.ketoy.devserver

import java.io.File
import java.nio.file.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Watches Kotlin source files (`.kt` / `.kts`) for changes and triggers
 * `gradlew ketoyExport` to regenerate the JSON screen definitions in `ketoy-screens/`.
 *
 * Together with [FileWatcher], this class completes the **full live-reload loop**
 * that lets developers edit Kotlin DSL code and see the result on a connected device
 * within seconds — without pressing any button:
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
 * ## Debouncing
 *
 * Rapid successive edits (e.g., saving a file multiple times while typing) are
 * **debounced**: the export is not triggered until [debounceMs] milliseconds have
 * elapsed since the last detected change. A dedicated debounce thread polls every
 * 200 ms and fires the export once the quiet period is over.
 *
 * ## Re-entrancy protection
 *
 * If a Gradle export is already running when a new change arrives, the change is
 * recorded as pending and the export is re-triggered automatically once the current
 * run finishes. This guarantees no update is lost during long builds.
 *
 * ## Threading
 *
 * Like [FileWatcher], `SourceWatcher` implements [Runnable] and should be started on a
 * **daemon thread**:
 *
 * ```kotlin
 * val watcher = SourceWatcher(sourceDirs, projectRoot, debounceMs = 1500)
 * val thread = Thread(watcher, "ketoy-source-watcher")
 * thread.isDaemon = true
 * thread.start()
 * ```
 *
 * @param sourceDirs Absolute paths to the Kotlin source directories to watch recursively.
 * @param projectRoot The root directory of the Gradle project (contains `gradlew`).
 * @param debounceMs Milliseconds of idle time after the last change before triggering an
 *   export. Default `1500`.
 * @see FileWatcher
 * @see KetoyDevServer
 * @see ServerConfig.autoExport
 */
class SourceWatcher(
    private val sourceDirs: List<File>,
    private val projectRoot: File,
    private val debounceMs: Long = 1500
) : Runnable {

    /**
     * Volatile flag that controls the watcher’s main loop.
     * Set to `false` by [stop] to request a graceful shutdown.
     */
    @Volatile
    private var running = true

    /**
     * Guard that prevents overlapping Gradle export processes. Only one export
     * can run at a time; further changes are queued via [pendingExport].
     */
    private val exportRunning = AtomicBoolean(false)
    /**
     * Timestamp (epoch millis) of the most recent Kotlin file change. The debounce
     * thread uses this to determine when the quiet window has elapsed.
     */
    private val lastChangeTime = AtomicLong(0)
    /**
     * Flag indicating that at least one Kotlin file has changed since the last export.
     * Cleared when the debounce timer fires and the export begins.
     */
    private val pendingExport = AtomicBoolean(false)

    /**
     * Main watch loop. Registers every directory under [sourceDirs] with a
     * [WatchService][java.nio.file.WatchService], starts the debounce thread, and
     * continuously polls for `.kt` / `.kts` file-system events until [stop] is called.
     *
     * Newly created subdirectories are automatically registered so that the watcher
     * adapts to evolving project structures without a restart.
     */
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

    /**
     * Launches `gradlew ketoyExport --quiet` in a background thread.
     *
     * If an export is already in progress ([exportRunning] is `true`), the call is
     * skipped and [pendingExport] is re-armed so that the running export’s `finally`
     * block will schedule another attempt.
     *
     * Output is captured and only “important” lines (containing status emojis or
     * error keywords) are printed to keep the console clean.
     */
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

    /**
     * Requests a graceful shutdown of the source watcher.
     *
     * The main loop and debounce thread will exit within ~1 second. Any in-flight
     * Gradle export will run to completion but no further exports will be started.
     * Safe to call from any thread.
     */
    fun stop() {
        running = false
    }
}
