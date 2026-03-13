package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.server.DevServerLauncher
import dev.ketoy.gradle.server.ServerConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Starts the Ketoy Dev Server with **auto-export** enabled.
 *
 * This is the recommended single-command workflow for live development:
 * 1. The server watches Kotlin source files for changes
 * 2. On change, it automatically runs `ketoyExport` to regenerate JSON
 * 3. The regenerated JSON is pushed to connected devices via WebSocket
 * 4. The Android app recomposes the UI instantly
 *
 * The server runs **embedded** inside the Gradle JVM process — no external
 * server module is required.
 *
 * ## Run
 * ```bash
 * ./gradlew ketoyDev
 * ```
 *
 * @see KetoyServeTask
 * @see KetoyExportTask
 */
abstract class KetoyDevTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun dev() {
        val screensDir = extension.screensDir.getOrElse("ketoy-screens")
        val port = extension.serverPort.getOrElse(8484)
        val watchDir = File(project.rootProject.projectDir, screensDir).absolutePath
        val projectRoot = project.rootProject.projectDir.absolutePath

        logger.lifecycle("🚀 Starting Ketoy Dev Server with auto-export (port $port)...")

        DevServerLauncher.launch(
            ServerConfig(
                port = port,
                watchDir = watchDir,
                autoExport = true,
                projectRoot = projectRoot,
                sourceDirs = listOf("app/src/main/java", "app/src/main/kotlin"),
                debounceMs = 1500
            )
        )
    }
}
