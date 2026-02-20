package com.developerstring.ketoy.export

import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.model.KNode
import com.developerstring.ketoy.navigation.KetoyNavGraph

/**
 * Base class for declaring production screen and navigation exports.
 *
 * Client apps extend this class to register their **production** screens
 * and navigation graphs. Unlike the dev-server export (`ketoyExport`),
 * the production export:
 *
 * - **No test data** — DSL builders must use real parameter bindings
 * - **No dummy placeholders** — all content comes from declared builders
 * - **No dev tooling** — exports are self-contained JSON for runtime use
 *
 * The exported JSON is consumed by:
 * 1. **Asset-based rendering** — bundle in `assets/` for offline use
 * 2. **Cloud delivery** — push to Ketoy Cloud via `ketoyPushAll`
 * 3. **CDN / custom server** — serve from any HTTP endpoint
 *
 * ## Usage
 *
 * ```kotlin
 * class AppProductionExport : KetoyProductionExport() {
 *
 *     override fun registerScreens() {
 *         screen("home", displayName = "Home") {
 *             content("cards") { buildHomeCards(...) }
 *             content("transactions") { buildHomeTransactions(...) }
 *         }
 *         screen("profile", displayName = "Profile") {
 *             content { buildProfileScreen(...) }
 *         }
 *     }
 *
 *     override fun registerNavGraphs() {
 *         navGraph(AppNavGraphs.main)
 *         navGraph(AppNavGraphs.demo)
 *     }
 * }
 * ```
 *
 * Then in the Gradle task:
 * ```
 * ./gradlew ketoyExportProd
 * ```
 *
 * @see KetoyProductionExport.ScreenBuilder
 * @see KetoyProductionExport.ExportResult
 */
abstract class KetoyProductionExport {

    // ── Internal registries ─────────────────────────────────────

    private val _screens = mutableListOf<ScreenDefinition>()
    private val _navGraphs = mutableListOf<KetoyNavGraph>()

    /** All registered screen definitions (read-only).
     * @see registerScreens
     */
    val screens: List<ScreenDefinition> get() = _screens.toList()

    /** All registered nav graphs (read-only).
     * @see registerNavGraphs
     */
    val navGraphs: List<KetoyNavGraph> get() = _navGraphs.toList()

    // ── Abstract registration hooks ─────────────────────────────

    /**
     * Register all production screens for export.
     *
     * Use [screen] to declare each screen and its content blocks.
     */
    abstract fun registerScreens()

    /**
     * Register all navigation graphs for export.
     *
     * Use [navGraph] to declare each exportable nav graph.
     */
    abstract fun registerNavGraphs()

    // ── Screen registration DSL ─────────────────────────────────

    /**
     * Declare a production screen with one or more content blocks.
     *
     * @param screenName  Unique identifier matching the runtime `ProvideKetoyScreen(screenName = ...)`.
     * @param displayName Human-readable name for dashboards and tooling.
     * @param description Optional description of the screen's purpose.
     * @param version     Semantic version for this screen's UI definition.
     * @param builder     Lambda to declare content blocks via [ScreenBuilder].
     */
    fun screen(
        screenName: String,
        displayName: String = screenName.replace("_", " ")
            .replaceFirstChar { it.uppercaseChar() },
        description: String = "",
        version: String = "1.0.0",
        builder: ScreenBuilder.() -> Unit
    ) {
        val screenBuilder = ScreenBuilder()
        screenBuilder.builder()
        _screens.add(
            ScreenDefinition(
                screenName = screenName,
                displayName = displayName,
                description = description,
                version = version,
                contents = screenBuilder.contents.toList()
            )
        )
    }

    /**
     * Register a navigation graph for production export.
     *
     * The graph is serialized as-is to `nav_{navHostName}.json`.
     * At runtime, the client app loads this JSON and passes it to
     * [KetoyNavRegistry] or reads it from assets for
     * [KetoyNavHost] to consume.
     */
    fun navGraph(graph: KetoyNavGraph) {
        _navGraphs.add(graph)
    }

    // ── Export execution ─────────────────────────────────────────

    /**
     * Execute all registrations and build the export result.
     *
     * Call this from the export test or Gradle task runner.
     *
     * @return [ExportResult] containing all screen JSONs and nav graph JSONs,
     *         ready to be written to disk.
     */
    fun buildExport(): ExportResult {
        // Clear previous state (allows re-running)
        _screens.clear()
        _navGraphs.clear()

        // Let the subclass register everything
        registerScreens()
        registerNavGraphs()

        // Build screen JSONs
        val screenExports = _screens.map { definition ->
            val contentsJson = definition.contents
                .mapNotNull { contentDef ->
                    val node = contentDef.nodeBuilder()
                    val json = node.toJson()
                    contentDef.name to json
                }
                .toMap()

            if (contentsJson.isEmpty()) return@map null

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

            ScreenExport(
                screenName = definition.screenName,
                fileName = "${definition.screenName}.json",
                json = screenJson
            )
        }.filterNotNull()

        // Build nav graph JSONs
        val navExports = _navGraphs.map { graph ->
            NavGraphExport(
                navHostName = graph.navHostName,
                fileName = "nav_${graph.navHostName}.json",
                json = graph.toJson(),
                destinationCount = graph.destinations.size,
                navigationCount = graph.navigations.size
            )
        }

        return ExportResult(
            screens = screenExports,
            navGraphs = navExports
        )
    }

    // ── Builder DSL ─────────────────────────────────────────────

    /**
     * Builder scope for declaring content blocks within a screen.
     *
     * Each `content(...)` call adds a named content block backed
     * by a [KNode] builder — matching the `KetoyContent(name = ..., nodeBuilder = ...)`
     * declarations in the composable screen.
     */
    class ScreenBuilder {
        internal val contents = mutableListOf<ContentDefinition>()

        /**
         * Declare a named content block.
         *
         * @param name        Content name matching the runtime `KetoyContent(name = ...)`.
         *                    Defaults to `"main"` for screens with a single content block.
         * @param nodeBuilder Lambda that returns the [KNode] tree for this content.
         */
        fun content(
            name: String = "main",
            nodeBuilder: () -> KNode
        ) {
            contents.add(ContentDefinition(name = name, nodeBuilder = nodeBuilder))
        }
    }

    // ── Data classes ─────────────────────────────────────────────

    /**
     * Internal definition of a screen before export.
     *
     * @property screenName  Unique identifier matching the runtime screen name.
     * @property displayName Human-readable label for tooling.
     * @property description Optional description of the screen’s purpose.
     * @property version     Semantic version of this screen’s UI definition.
     * @property contents    Ordered list of content blocks within the screen.
     */
    data class ScreenDefinition(
        val screenName: String,
        val displayName: String,
        val description: String,
        val version: String,
        val contents: List<ContentDefinition>
    )

    /**
     * Internal definition of a content block before export.
     *
     * @property name        Content block identifier (e.g. `"main"`, `"cards"`).
     * @property nodeBuilder Lambda that constructs the [KNode] tree for this block.
     */
    data class ContentDefinition(
        val name: String,
        val nodeBuilder: () -> KNode
    )

    /**
     * A single exported screen ready to be written to disk.
     *
     * @property screenName The unique screen identifier.
     * @property fileName   The target file name (e.g. `"home.json"`).
     * @property json       The serialised JSON string of the screen.
     */
    data class ScreenExport(
        val screenName: String,
        val fileName: String,
        val json: String
    )

    /**
     * A single exported navigation graph ready to be written to disk.
     *
     * @property navHostName      The nav-host identifier (e.g. `"main"`).
     * @property fileName          The target file name (e.g. `"nav_main.json"`).
     * @property json              The serialised JSON string of the nav graph.
     * @property destinationCount  Number of destinations in the graph.
     * @property navigationCount   Number of navigation edges in the graph.
     */
    data class NavGraphExport(
        val navHostName: String,
        val fileName: String,
        val json: String,
        val destinationCount: Int,
        val navigationCount: Int
    )

    /**
     * Complete export result containing all screens and nav graphs.
     *
     * Use [writeTo] to write all files to a directory, or access
     * individual exports for custom handling.
     */
    data class ExportResult(
        val screens: List<ScreenExport>,
        val navGraphs: List<NavGraphExport>
    ) {
        /**
         * Total number of exported items (screens + nav graphs).
         */
        val totalCount: Int get() = screens.size + navGraphs.size

        /**
         * Write all exported JSONs to the given directory.
         *
         * @param directory Target directory (created if it doesn't exist).
         * @param clearExisting If true, removes existing `.json` files in the directory
         *                      before writing. Defaults to true for clean exports.
         * @return Summary of the export operation.
         */
        fun writeTo(
            directory: java.io.File,
            clearExisting: Boolean = true
        ): ExportSummary {
            directory.mkdirs()

            if (clearExisting) {
                directory.listFiles()
                    ?.filter { it.extension == "json" }
                    ?.forEach { it.delete() }
            }

            var screenBytes = 0L
            var navBytes = 0L

            screens.forEach { export ->
                val file = java.io.File(directory, export.fileName)
                file.writeText(export.json)
                screenBytes += export.json.length
            }

            navGraphs.forEach { export ->
                val file = java.io.File(directory, export.fileName)
                file.writeText(export.json)
                navBytes += export.json.length
            }

            return ExportSummary(
                screenCount = screens.size,
                navGraphCount = navGraphs.size,
                totalBytes = screenBytes + navBytes,
                outputDirectory = directory.absolutePath
            )
        }

        /**
         * Build a combined navigation manifest containing all nav graphs.
         *
         * This is useful for production apps that want to load all
         * navigation definitions from a single JSON file instead of
         * individual `nav_*.json` files.
         *
         * The manifest includes:
         * - All nav graphs with their full definitions
         * - A top-level index of available nav host names
         * - Metadata about the export
         *
         * ```json
         * {
         *   "version": "1.0.0",
         *   "navHosts": ["main", "demo"],
         *   "graphs": {
         *     "main": { ... full KetoyNavGraph ... },
         *     "demo": { ... full KetoyNavGraph ... }
         *   }
         * }
         * ```
         */
        fun buildNavigationManifest(): String {
            val graphsBlock = navGraphs.joinToString(",\n    ") { export ->
                "\"${export.navHostName}\": ${export.json}"
            }
            val hostNames = navGraphs.joinToString(", ") { "\"${it.navHostName}\"" }

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

        /**
         * Build a screen manifest listing all exported screens.
         *
         * Useful for production apps to discover available screens
         * without parsing individual JSON files.
         *
         * ```json
         * {
         *   "version": "1.0.0",
         *   "screens": [
         *     { "screenName": "home", "displayName": "Home", "fileName": "home.json", "contents": ["cards", "transactions"] },
         *     ...
         *   ]
         * }
         * ```
         */
        fun buildScreenManifest(allScreenDefs: List<ScreenDefinition>): String {
            val screensBlock = allScreenDefs
                .filter { def -> screens.any { it.screenName == def.screenName } }
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
    }

    /**
     * Summary of a completed export operation.
     *
     * @property screenCount     Number of screens exported.
     * @property navGraphCount   Number of nav graphs exported.
     * @property totalBytes      Combined byte size of all exported JSON files.
     * @property outputDirectory Absolute path of the export output directory.
     */
    data class ExportSummary(
        val screenCount: Int,
        val navGraphCount: Int,
        val totalBytes: Long,
        val outputDirectory: String
    ) {
        override fun toString(): String = buildString {
            appendLine("╔════════════════════════════════════════════╗")
            appendLine("║     Ketoy Production Export Complete       ║")
            appendLine("╚════════════════════════════════════════════╝")
            appendLine("  Screens:     $screenCount")
            appendLine("  Nav graphs:  $navGraphCount")
            appendLine("  Total size:  ${formatBytes(totalBytes)}")
            appendLine("  Output:      $outputDirectory")
        }

        private fun formatBytes(bytes: Long): String = when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        }
    }
}
