package com.developerstring.ketoy.export

import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.model.KNode
import com.developerstring.ketoy.navigation.KetoyNavGraph

/**
 * Central registry for all screen export definitions.
 *
 * Screen files register their export configuration here via [ketoyExport],
 * and the export runner ([KetoyAutoExportRunner]) reads them to produce
 * JSON files — no manual test classes or boilerplate needed.
 *
 * ## How it works
 *
 * Each screen file declares its export inline:
 * ```kotlin
 * // In ProfileScreen.kt
 * val profileExport = ketoyExport("profile") {
 *     content {
 *         buildProfileScreen(
 *             userName = KData.user("name"),
 *             darkModeIcon = KData.user("darkModeIcon"),
 *         )
 *     }
 * }
 * ```
 *
 * Navigation graphs are registered separately:
 * ```kotlin
 * // In DemoNavGraphs.kt or AppNavGraphs.kt
 * val mainNavExport = ketoyNavExport(AppNavGraphs.main)
 * ```
 *
 * At export time, [KetoyAutoExportRunner] iterates all registered
 * definitions and writes the JSON files.
 *
 * @see ketoyExport
 * @see ketoyNavExport
 * @see KetoyAutoExportRunner
 */
object KetoyExportRegistry {

    private val _screens = mutableListOf<ScreenExportDefinition>()
    private val _navGraphs = mutableListOf<KetoyNavGraph>()

    /** All registered screen export definitions (read-only). */
    val screens: List<ScreenExportDefinition> get() = _screens.toList()

    /** All registered nav graph exports (read-only). */
    val navGraphs: List<KetoyNavGraph> get() = _navGraphs.toList()

    /**
     * Register a screen export definition.
     *
     * Called automatically by [ketoyExport]. You don't normally call
     * this directly.
     */
    fun registerScreen(definition: ScreenExportDefinition) {
        // Replace if screen with same name already exists (re-registration)
        _screens.removeAll { it.screenName == definition.screenName }
        _screens.add(definition)
    }

    /**
     * Register a navigation graph for export.
     *
     * Called automatically by [ketoyNavExport].
     */
    fun registerNavGraph(graph: KetoyNavGraph) {
        _navGraphs.removeAll { it.navHostName == graph.navHostName }
        _navGraphs.add(graph)
    }

    /** Clear all registrations (for testing). */
    fun clear() {
        _screens.clear()
        _navGraphs.clear()
    }
}

// ── Data classes ─────────────────────────────────────────────

/**
 * Export definition for a single screen, containing one or more
 * named content blocks.
 *
 * @property screenName  Unique identifier matching `ProvideKetoyScreen(screenName = ...)`.
 * @property displayName Human-readable name for manifests and tooling.
 * @property description Optional description of the screen.
 * @property version     Semantic version for this screen's export.
 * @property contents    Content blocks (name → node builder).
 */
data class ScreenExportDefinition(
    val screenName: String,
    val displayName: String = screenName.replace("_", " ")
        .replaceFirstChar { it.uppercaseChar() },
    val description: String = "",
    val version: String = "1.0.0",
    val contents: List<ContentExportDefinition> = emptyList()
)

/**
 * A single content block within a screen export.
 *
 * @property name        Content name matching `KetoyContent(name = ...)`.
 * @property nodeBuilder Lambda that produces the [KNode] tree for export.
 */
data class ContentExportDefinition(
    val name: String,
    val nodeBuilder: () -> KNode
)

// ── DSL functions ────────────────────────────────────────────

/**
 * Declare and register a screen export definition.
 *
 * Place this alongside your screen composable — it becomes the
 * **single source of truth** for what gets exported.
 *
 * ## Single-content screen
 * ```kotlin
 * val profileExport = ketoyExport("profile") {
 *     content {
 *         buildProfileScreen(userName = KData.user("name"))
 *     }
 * }
 * ```
 *
 * ## Multi-content screen
 * ```kotlin
 * val homeExport = ketoyExport("home", displayName = "Home") {
 *     content("cards") {
 *         buildHomeCards(userName = KData.user("name"))
 *     }
 *     content("transactions") {
 *         buildHomeTransactions()
 *     }
 * }
 * ```
 *
 * @param screenName  Unique screen identifier.
 * @param displayName Human-readable label (auto-derived from screenName if omitted).
 * @param description Optional description.
 * @param version     Semantic version (default `"1.0.0"`).
 * @param builder     Lambda to declare content blocks.
 * @return The registered [ScreenExportDefinition].
 */
fun ketoyExport(
    screenName: String,
    displayName: String = screenName.replace("_", " ")
        .replaceFirstChar { it.uppercaseChar() },
    description: String = "",
    version: String = "1.0.0",
    builder: ExportScreenBuilder.() -> Unit
): ScreenExportDefinition {
    val screenBuilder = ExportScreenBuilder()
    screenBuilder.builder()
    val definition = ScreenExportDefinition(
        screenName = screenName,
        displayName = displayName,
        description = description,
        version = version,
        contents = screenBuilder.contents.toList()
    )
    KetoyExportRegistry.registerScreen(definition)
    return definition
}

/**
 * Register a navigation graph for export.
 *
 * ```kotlin
 * val mainNavExport = ketoyNavExport(AppNavGraphs.main)
 * ```
 *
 * @param graph The [KetoyNavGraph] to export.
 * @return The graph (for chaining or reference).
 */
fun ketoyNavExport(graph: KetoyNavGraph): KetoyNavGraph {
    KetoyExportRegistry.registerNavGraph(graph)
    return graph
}

/**
 * Builder scope for declaring content blocks within a [ketoyExport] call.
 */
class ExportScreenBuilder {
    internal val contents = mutableListOf<ContentExportDefinition>()

    /**
     * Declare a content block for export.
     *
     * @param name        Content name (default `"main"` for single-content screens).
     * @param nodeBuilder Lambda returning the [KNode] tree.
     */
    fun content(
        name: String = "main",
        nodeBuilder: () -> KNode
    ) {
        contents.add(ContentExportDefinition(name = name, nodeBuilder = nodeBuilder))
    }
}
