package com.developerstring.ketoy.export

import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.navigation.KetoyNavGraph
import java.io.File

/**
 * Automatic export runner that reads all registered screen exports
 * from [KetoyExportRegistry] and writes JSON files to disk.
 *
 * Replaces the manual `ExportScreensTest`, `ProductionExportTest`,
 * and `AppProductionExport` pattern with a fully automatic flow.
 *
 * ## How it works
 *
 * 1. Screen files register their export definitions via [ketoyExport]:
 *    ```kotlin
 *    // In ProfileScreen.kt
 *    val profileExport = ketoyExport("profile") {
 *        content { buildProfileScreen(userName = KData.user("name")) }
 *    }
 *    ```
 *
 * 2. Nav graphs are registered via [ketoyNavExport]:
 *    ```kotlin
 *    // In AppNavGraphs.kt
 *    val mainNavExport = ketoyNavExport(AppNavGraphs.main)
 *    ```
 *
 * 3. The runner gathers all registrations and produces JSON:
 *    ```kotlin
 *    val runner = KetoyAutoExportRunner()
 *    runner.exportAll(outputDir = File("ketoy-export"))
 *    ```
 *
 * ## Gradle integration
 *
 * The `dev.ketoy.devtools` plugin provides tasks that invoke this
 * runner automatically:
 *
 * ```bash
 * ./gradlew ketoyExport       # dev server export → ketoy-screens/
 * ./gradlew ketoyExportProd   # production export → ketoy-export/
 * ```
 *
 * @see KetoyExportRegistry
 * @see ketoyExport
 * @see ketoyNavExport
 */
class KetoyAutoExportRunner {

    /**
     * Export all registered screens and nav graphs to the given directory.
     *
     * @param outputDir       Target directory (created if needed).
     * @param clearExisting   Remove existing `.json` files before writing.
     * @param writeManifests  Write navigation_manifest.json and screen_manifest.json.
     * @return [ExportResult] summary of the export operation.
     */
    fun exportAll(
        outputDir: File,
        clearExisting: Boolean = true,
        writeManifests: Boolean = true
    ): ExportResult {
        val screens = KetoyExportRegistry.screens
        val navGraphs = KetoyExportRegistry.navGraphs

        outputDir.mkdirs()

        if (clearExisting) {
            outputDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.forEach { it.delete() }
        }

        // ── Export screens ──────────────────────────────────

        val screenExports = mutableListOf<ScreenExportResult>()

        screens.forEach { definition ->
            val contentsJson = definition.contents.associate { contentDef ->
                val node = contentDef.nodeBuilder()
                val json = node.toJson()
                contentDef.name to json
            }

            if (contentsJson.isEmpty()) return@forEach

            val contentsBlock = contentsJson.entries.joinToString(",\n    ") { (id, json) ->
                "\"$id\": $json"
            }

            val screenJson = buildString {
                appendLine("{")
                appendLine("  \"screenName\": \"${definition.screenName}\",")
                appendLine("  \"displayName\": \"${definition.displayName}\",")
                if (definition.description.isNotBlank()) {
                    appendLine("  \"description\": \"${definition.description}\",")
                }
                appendLine("  \"version\": \"${definition.version}\",")
                appendLine("  \"contents\": {")
                appendLine("    $contentsBlock")
                appendLine("  }")
                append("}")
            }

            val file = File(outputDir, "${definition.screenName}.json")
            file.writeText(screenJson)

            screenExports.add(
                ScreenExportResult(
                    screenName = definition.screenName,
                    fileName = "${definition.screenName}.json",
                    json = screenJson,
                    contentNames = definition.contents.map { it.name }
                )
            )
        }

        // ── Export nav graphs ───────────────────────────────

        val navExports = mutableListOf<NavGraphExportResult>()

        navGraphs.forEach { graph ->
            val json = graph.toJson()
            val file = File(outputDir, "nav_${graph.navHostName}.json")
            file.writeText(json)

            navExports.add(
                NavGraphExportResult(
                    navHostName = graph.navHostName,
                    fileName = "nav_${graph.navHostName}.json",
                    json = json,
                    destinationCount = graph.destinations.size,
                    navigationCount = graph.navigations.size
                )
            )
        }

        // ── Write manifests ─────────────────────────────────

        if (writeManifests && (navExports.isNotEmpty() || screenExports.isNotEmpty())) {
            if (navExports.isNotEmpty()) {
                val navManifest = buildNavigationManifest(navExports)
                File(outputDir, "navigation_manifest.json").writeText(navManifest)
            }

            if (screenExports.isNotEmpty()) {
                val screenManifest = buildScreenManifest(screens, screenExports)
                File(outputDir, "screen_manifest.json").writeText(screenManifest)
            }
        }

        return ExportResult(
            screens = screenExports,
            navGraphs = navExports,
            outputDirectory = outputDir.absolutePath
        )
    }

    // ── Manifest builders ────────────────────────────────────

    private fun buildNavigationManifest(navExports: List<NavGraphExportResult>): String {
        val graphsBlock = navExports.joinToString(",\n    ") { export ->
            "\"${export.navHostName}\": ${export.json}"
        }
        val hostNames = navExports.joinToString(", ") { "\"${it.navHostName}\"" }

        return buildString {
            appendLine("{")
            appendLine("  \"version\": \"1.0.0\",")
            appendLine("  \"navHosts\": [$hostNames],")
            appendLine("  \"graphs\": {")
            appendLine("    $graphsBlock")
            appendLine("  }")
            append("}")
        }
    }

    private fun buildScreenManifest(
        definitions: List<ScreenExportDefinition>,
        exports: List<ScreenExportResult>
    ): String {
        val screensBlock = definitions
            .filter { def -> exports.any { it.screenName == def.screenName } }
            .joinToString(",\n    ") { def ->
                val contentNames = def.contents.joinToString(", ") { "\"${it.name}\"" }
                buildString {
                    append("{")
                    append("\"screenName\": \"${def.screenName}\", ")
                    append("\"displayName\": \"${def.displayName}\", ")
                    append("\"version\": \"${def.version}\", ")
                    append("\"fileName\": \"${def.screenName}.json\", ")
                    append("\"contents\": [$contentNames]")
                    append("}")
                }
            }

        return buildString {
            appendLine("{")
            appendLine("  \"version\": \"1.0.0\",")
            appendLine("  \"screens\": [")
            appendLine("    $screensBlock")
            appendLine("  ]")
            append("}")
        }
    }

    // ── Result data classes ──────────────────────────────────

    data class ScreenExportResult(
        val screenName: String,
        val fileName: String,
        val json: String,
        val contentNames: List<String>
    )

    data class NavGraphExportResult(
        val navHostName: String,
        val fileName: String,
        val json: String,
        val destinationCount: Int,
        val navigationCount: Int
    )

    data class ExportResult(
        val screens: List<ScreenExportResult>,
        val navGraphs: List<NavGraphExportResult>,
        val outputDirectory: String
    ) {
        val totalCount: Int get() = screens.size + navGraphs.size

        override fun toString(): String = buildString {
            appendLine()
            appendLine("╔════════════════════════════════════════════╗")
            appendLine("║     Ketoy Auto Export Complete             ║")
            appendLine("╚════════════════════════════════════════════╝")
            appendLine()
            screens.forEach { export ->
                appendLine("  📄 ${export.screenName} → ${export.fileName} (${export.json.length} bytes, contents: ${export.contentNames})")
            }
            navGraphs.forEach { export ->
                appendLine("  🗺️  ${export.navHostName} → ${export.fileName} (${export.destinationCount} destinations, ${export.navigationCount} navigations)")
            }
            appendLine()
            appendLine("  ✅ Exported ${screens.size} screen(s) + ${navGraphs.size} nav graph(s) to $outputDirectory")
        }
    }
}
