package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Exports Ketoy DSL screens to JSON files for the dev server.
 *
 * This task triggers the app module's unit test runner, filtered to
 * execute only the `exportForDevServer` method of the auto-export test
 * class (`KetoyAutoExportTest` by default). The test class reads all
 * screen export definitions registered via `ketoyExport(...)` and writes
 * the resulting JSON files to the screens directory (`ketoy-screens/`
 * by default).
 *
 * ## Run
 * ```bash
 * ./gradlew ketoyExport
 * ```
 *
 * ## Customisation
 * ```kotlin
 * ketoyDev {
 *     exportTestClass.set("MyCustomExportTest")
 *     screensDir.set("my-screens")
 *     appModule.set("my-app-module")
 *     testTaskName.set("testDebugUnitTest")
 * }
 * ```
 *
 * The actual test dependency and filter are wired up by [dev.ketoy.gradle.KetoyDevPlugin].
 */
abstract class KetoyExportTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun exportSummary() {
        val screensDir = project.rootProject.file(
            extension.screensDir.getOrElse("ketoy-screens")
        )
        if (screensDir.exists()) {
            val screenFiles = screensDir.listFiles()?.filter {
                it.extension == "json" || it.extension == "ktw"
            } ?: emptyList()
            logger.lifecycle("✅ Ketoy export complete: ${screenFiles.size} screen(s) in ${screensDir.absolutePath}")
            screenFiles.forEach { file ->
                val label = if (file.extension == "ktw") "📦" else "📄"
                logger.lifecycle("   $label ${file.name} (${file.length()} bytes)")
            }
        } else {
            logger.warn("⚠️  Screens directory not found: ${screensDir.absolutePath}")
        }
    }
}
