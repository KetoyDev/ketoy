package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import dev.ketoy.gradle.server.DevServerLauncher
import dev.ketoy.gradle.server.ServerConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Starts the Ketoy Dev Server for hot-reload preview.
 *
 * The dev server watches the screens directory (`ketoy-screens/` by default)
 * for JSON file changes and pushes updates to connected Android devices via
 * WebSocket. This enables instant UI preview without recompiling the app.
 *
 * The server runs **embedded** inside the Gradle JVM process — no external
 * server module is required.
 *
 * ## Run
 * ```bash
 * ./gradlew ketoyServe
 * ```
 *
 * @see KetoyDevTask
 */
abstract class KetoyServeTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun serve() {
        val screensDir = extension.screensDir.getOrElse("ketoy-screens")
        val port = extension.serverPort.getOrElse(8484)
        val watchDir = File(project.rootProject.projectDir, screensDir).absolutePath

        logger.lifecycle("🚀 Starting Ketoy Dev Server (port $port, watching $screensDir)...")

        DevServerLauncher.launch(
            ServerConfig(
                port = port,
                watchDir = watchDir,
                autoExport = false,
                projectRoot = project.rootProject.projectDir.absolutePath
            )
        )
    }
}
