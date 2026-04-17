package com.developerstring.ketoy.export

import com.developerstring.ketoy.navigation.KetoyNavGraph
import com.developerstring.ketoy.navigation.KetoyNavRegistry
import com.developerstring.ketoy.wire.KetoyWireFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Loads production navigation manifests and individual nav graph JSONs.
 *
 * This is the **production counterpart** to the dev-server's live nav
 * graph loading. Use this to initialize navigation from exported JSON
 * files at app startup.
 *
 * ## Loading from a navigation manifest
 * ```kotlin
 * // In Application.onCreate() or before navigation setup:
 * val manifestJson = assets.open("ketoy-export/navigation_manifest.json")
 *     .bufferedReader().readText()
 * KetoyProductionNavLoader.loadManifest(manifestJson)
 * ```
 *
 * ## Loading individual nav graphs
 * ```kotlin
 * val mainNavJson = assets.open("ketoy-export/nav_main.json")
 *     .bufferedReader().readText()
 * KetoyProductionNavLoader.loadNavGraph(mainNavJson)
 * ```
 *
 * ## Loading all from an asset directory
 * ```kotlin
 * KetoyProductionNavLoader.loadAllFromAssets(context, "ketoy-export")
 * ```
 *
 * After loading, all nav graphs are available via [KetoyNavRegistry]
 * and automatically consumed by [KetoyNavHost].
 */
object KetoyProductionNavLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Load a navigation manifest JSON and register all contained nav graphs.
     *
     * The manifest format is:
     * ```json
     * {
     *   "version": "1.0.0",
     *   "navHosts": ["main", "demo"],
     *   "graphs": {
     *     "main": { ...KetoyNavGraph... },
     *     "demo": { ...KetoyNavGraph... }
     *   }
     * }
     * ```
     *
     * @param manifestJson The raw JSON string of the navigation manifest.
     * @return List of nav host names that were registered.
     */
    fun loadManifest(manifestJson: String): List<String> {
        val root = json.parseToJsonElement(manifestJson).jsonObject
        val graphs = root["graphs"]?.jsonObject ?: return emptyList()

        val registered = mutableListOf<String>()
        graphs.forEach { (name, graphElement) ->
            try {
                val navGraph = KetoyNavGraph.fromJson(graphElement.toString())
                KetoyNavRegistry.register(navGraph)
                registered.add(name)
            } catch (e: Exception) {
                System.err.println("Failed to load nav graph '$name': ${e.message}")
            }
        }
        return registered
    }

    /**
     * Load a single nav graph JSON and register it.
     *
     * @param navGraphJson The raw JSON string of a [KetoyNavGraph].
     * @return The registered nav graph, or null on parse failure.
     */
    fun loadNavGraph(navGraphJson: String): KetoyNavGraph? {
        return try {
            val navGraph = KetoyNavGraph.fromJson(navGraphJson)
            KetoyNavRegistry.register(navGraph)
            navGraph
        } catch (e: Exception) {
            System.err.println("Failed to load nav graph: ${e.message}")
            null
        }
    }

    /**
     * Load a single nav graph from wire format (.ktw) bytes and register it.
     *
     * @param wireBytes The raw .ktw wire format bytes.
     * @return The registered nav graph, or null on decode/parse failure.
     */
    fun loadNavGraphFromWireBytes(wireBytes: ByteArray): KetoyNavGraph? {
        return try {
            val jsonString = KetoyWireFormat.decode(wireBytes)
            val navGraph = KetoyNavGraph.fromJson(jsonString)
            KetoyNavRegistry.register(navGraph)
            navGraph
        } catch (e: Exception) {
            System.err.println("Failed to load nav graph from wire bytes: ${e.message}")
            null
        }
    }

    /**
     * Load all `nav_*.json` and `nav_*.ktw` files and the navigation manifest from an
     * Android asset directory.
     *
     * This method:
     * 1. Looks for `navigation_manifest.json` — if found, loads all graphs from it.
     * 2. Otherwise, loads individual `nav_*.json` files.
     *
     * @param context Android context for asset access.
     * @param assetDirectory The asset directory path (e.g. `"ketoy-export"`).
     * @return List of nav host names that were registered.
     */
    fun loadAllFromAssets(
        context: android.content.Context,
        assetDirectory: String
    ): List<String> {
        val assetManager = context.assets

        // Try manifest first
        try {
            val manifestJson = assetManager.open("$assetDirectory/navigation_manifest.json")
                .bufferedReader().use { it.readText() }
            val result = loadManifest(manifestJson)
            if (result.isNotEmpty()) return result
        } catch (_: Exception) {
            // No manifest, fall through to individual files
        }

        // Load individual nav_*.json files
        val registered = mutableListOf<String>()
        try {
            val files = assetManager.list(assetDirectory) ?: emptyArray()
            files.filter { it.startsWith("nav_") && (it.endsWith(".json") || it.endsWith(".ktw")) }
                .forEach { fileName ->
                    try {
                        if (fileName.endsWith(".ktw")) {
                            val wireBytes = assetManager.open("$assetDirectory/$fileName")
                                .use { it.readBytes() }
                            val navGraph = loadNavGraphFromWireBytes(wireBytes)
                            if (navGraph != null) {
                                registered.add(navGraph.navHostName)
                            }
                        } else {
                            val navJson = assetManager.open("$assetDirectory/$fileName")
                                .bufferedReader().use { it.readText() }
                            val navGraph = loadNavGraph(navJson)
                            if (navGraph != null) {
                                registered.add(navGraph.navHostName)
                            }
                        }
                    } catch (e: Exception) {
                        System.err.println("Failed to load $fileName: ${e.message}")
                    }
                }
        } catch (e: Exception) {
            System.err.println("Failed to list assets in $assetDirectory: ${e.message}")
        }

        return registered
    }

    /**
     * Load all screens and navigation from an asset directory.
     *
     * This is the **single-call production initialiser** for apps that
     * bundle their Ketoy export in the `assets/` folder. It loads:
     *
     * - All `nav_*.json` or `navigation_manifest.json` → [KetoyNavRegistry]
     * - All remaining `*.json` screen files → [com.developerstring.ketoy.screen.KetoyScreenRegistry]
     *
     * ### Usage
     * ```kotlin
     * // In Application.onCreate():
     * val (screens, navGraphs) = KetoyProductionNavLoader
     *     .loadAllFromAssetsComplete(this, "ketoy-export")
     * Log.d("Ketoy", "Loaded $screens screens, $navGraphs nav graphs")
     * ```
     *
     * @param context        Android context for asset access.
     * @param assetDirectory The asset directory path (e.g. `"ketoy-export"`).
     * @return A [Pair] of (screen count, nav graph count) that were loaded.
     */
    fun loadAllFromAssetsComplete(
        context: android.content.Context,
        assetDirectory: String
    ): Pair<Int, Int> {
        val assetManager = context.assets
        var screenCount = 0

        // Load screens
        try {
            val files = assetManager.list(assetDirectory) ?: emptyArray()
            files.filter { (it.endsWith(".json") || it.endsWith(".ktw")) && !it.startsWith("nav_") && it != "navigation_manifest.json" && it != "screen_manifest.json" }
                .forEach { fileName ->
                    try {
                        val screenJson = assetManager.open("$assetDirectory/$fileName")
                            .bufferedReader().use { it.readText() }
                        val screenName = fileName.removeSuffix(".json")
                        com.developerstring.ketoy.screen.KetoyScreenRegistry.register(
                            com.developerstring.ketoy.screen.KetoyScreen.fromJson(screenName, screenJson)
                        )
                        screenCount++
                    } catch (e: Exception) {
                        System.err.println("Failed to load screen $fileName: ${e.message}")
                    }
                }
        } catch (e: Exception) {
            System.err.println("Failed to list assets in $assetDirectory: ${e.message}")
        }

        // Load nav graphs
        val navGraphNames = loadAllFromAssets(context, assetDirectory)

        return screenCount to navGraphNames.size
    }
}
