package dev.ketoy.gradle.tasks

import dev.ketoy.gradle.KetoyDevExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Exports production-ready Ketoy screens and navigation graphs to JSON.
 *
 * This task triggers the app module's unit test runner, filtered to execute
 * only the `exportForProduction` method of the auto-export test class
 * (`KetoyAutoExportTest` by default). The test class reads all screen
 * export definitions registered via `ketoyExport(...)` and nav graphs
 * registered via `ketoyNavExport(...)`, then writes everything to the
 * production export directory (`ketoy-export/` by default) including
 * navigation and screen manifests.
 *
 * ## Run
 * ```bash
 * ./gradlew ketoyExportProd
 * ```
 *
 * ## Output structure
 * ```
 * ketoy-export/
 * ├── home.json
 * ├── profile.json
 * ├── nav_main.json
 * ├── navigation_manifest.json
 * └── screen_manifest.json
 * ```
 *
 * ## Customisation
 * ```kotlin
 * ketoyDev {
 *     prodExportTestClass.set("MyProdExportTest")
 *     prodExportDir.set("my-export")
 *     appModule.set("my-app-module")
 *     testTaskName.set("testDebugUnitTest")
 * }
 * ```
 *
 * The actual test dependency and filter are wired up by [dev.ketoy.gradle.KetoyDevPlugin].
 */
abstract class KetoyExportProdTask : DefaultTask() {

    @get:Internal
    lateinit var extension: KetoyDevExtension

    @TaskAction
    fun exportProdSummary() {
        val exportDir = project.rootProject.file(
            extension.prodExportDir.getOrElse("ketoy-export")
        )
        if (exportDir.exists()) {
            val jsonFiles = exportDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
            val manifests = jsonFiles.filter { it.name.contains("manifest") }
            val screens = jsonFiles - manifests.toSet()
            logger.lifecycle("✅ Ketoy production export complete: ${screens.size} screen(s), ${manifests.size} manifest(s) in ${exportDir.absolutePath}")
            screens.forEach { file ->
                logger.lifecycle("   📄 ${file.name} (${file.length()} bytes)")
            }
            manifests.forEach { file ->
                logger.lifecycle("   📋 ${file.name} (${file.length()} bytes)")
            }
        } else {
            logger.warn("⚠️  Production export directory not found: ${exportDir.absolutePath}")
        }
    }
}
