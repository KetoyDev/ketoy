package com.developerstring.ketoy.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Describes a single navigation destination inside a [KetoyNavGraph].
 *
 * Each destination maps a stable [id] to an actual Navigation Compose [route],
 * along with metadata for building data-driven navigation UI (bottom bars,
 * drawers, etc.).
 *
 * ### JSON representation
 * ```json
 * {
 *   "id": "home_tab",
 *   "route": "home",
 *   "screenName": "HomeScreen",
 *   "label": "Home",
 *   "icon": "home",
 *   "selectedIcon": "home_filled",
 *   "isStartDestination": true
 * }
 * ```
 *
 * ### Kotlin usage
 * ```kotlin
 * val dest = KetoyNavDestination(
 *     id = "profile_tab",
 *     route = "profile",
 *     screenName = "ProfileScreen",
 *     label = "Profile",
 *     icon = "person",
 *     selectedIcon = "person_filled"
 * )
 * ```
 *
 * @property id             Stable identifier used by the builder DSL and
 *                          [KetoyNavController.navigateToRoute]. When empty, [resolvedId]
 *                          falls back to [route].
 * @property route          The actual route string registered in Navigation Compose.
 * @property screenName     The Ketoy screen name this destination renders (looked up
 *                          in [KetoyScreenRegistry][com.developerstring.ketoy.screen.KetoyScreenRegistry]).
 * @property label          Human-readable label for UI elements (bottom nav, drawer items).
 * @property icon           Icon reference string for the unselected state.
 * @property selectedIcon   Icon reference string for the selected state.
 * @property isStartDestination Whether this destination is the start route of its nav graph.
 * @see KetoyNavGraph
 * @see KetoyNavAction
 */
@Serializable
data class KetoyNavDestination(
    val id: String = "",
    val route: String,
    val screenName: String,
    val label: String = "",
    val icon: String = "",
    val selectedIcon: String = "",
    val isStartDestination: Boolean = false
) {
    /**
     * Resolved identifier for this destination.
     *
     * Returns [id] if it is non-empty; otherwise falls back to [route].
     * Used by [KetoyNavGraph.buildActionRemaps] to create the route remap table.
     */
    val resolvedId: String get() = id.ifEmpty { route }
}

/**
 * A navigation action that maps an action [id] to a target [route].
 *
 * Registered via `navigateToRoute(navId, route)` in code or defined in
 * the `navigations` array of a [KetoyNavGraph] JSON. When the dev server
 * or cloud overrides the nav graph, the [route] can change dynamically—
 * the same action [id] in code will navigate to the new target without
 * any code changes.
 *
 * ### JSON representation
 * ```json
 * {
 *   "id": "go_favorites",
 *   "route": "favorites",
 *   "label": "Open Favorites"
 * }
 * ```
 *
 * Changing `"route": "bookmarks"` in the JSON causes all
 * `navigateToRoute("go_favorites")` calls to navigate to `"bookmarks"` instead.
 *
 * ### Kotlin usage
 * ```kotlin
 * val action = KetoyNavAction(
 *     id = "go_favorites",
 *     route = "favorites",
 *     label = "Open Favorites"
 * )
 * ```
 *
 * @property id    Stable action identifier used in code (e.g., `"go_favorites"`).
 * @property route The default target route string. Overridable via JSON.
 * @property label Optional human-readable label for tooling and debugging.
 * @see KetoyNavGraph
 * @see KetoyNavController.navigateToRoute
 */
@Serializable
data class KetoyNavAction(
    val id: String,
    val route: String,
    val label: String = ""
)

/**
 * Serializable model representing a complete navigation graph for one
 * [KetoyNavHost]. Exported to JSON for the dev server and Ketoy Cloud.
 *
 * A nav graph contains:
 * - **[destinations]** — the screens (nodes) in the graph
 * - **[navigations]** — the `navigateToRoute()` actions (edges) between screens
 *
 * Editing `navigations[*].route` in the JSON and pushing via the dev server
 * or cloud will live-update all matching `navigateToRoute(navId)` calls.
 *
 * ### JSON representation
 * ```json
 * {
 *   "navHostName": "main",
 *   "startRoute": "home",
 *   "destinations": [
 *     { "id": "home", "route": "home", "screenName": "HomeScreen", "label": "Home", "icon": "home" },
 *     { "id": "profile", "route": "profile", "screenName": "ProfileScreen", "label": "Profile", "icon": "person" }
 *   ],
 *   "navigations": [
 *     { "id": "go_profile", "route": "profile", "label": "Open Profile" }
 *   ]
 * }
 * ```
 *
 * ### Kotlin usage
 * ```kotlin
 * val graph = KetoyNavGraph(
 *     navHostName = "main",
 *     startRoute = "home",
 *     destinations = listOf(
 *         KetoyNavDestination(id = "home", route = "home", screenName = "HomeScreen"),
 *         KetoyNavDestination(id = "profile", route = "profile", screenName = "ProfileScreen")
 *     ),
 *     navigations = listOf(
 *         KetoyNavAction(id = "go_profile", route = "profile")
 *     )
 * )
 *
 * // Serialize / deserialize
 * val json = graph.toJson()
 * val parsed = KetoyNavGraph.fromJson(json)
 * ```
 *
 * @property navHostName  Unique name identifying this nav graph (e.g., `"main"`).
 *                         Must match the `navHostName` parameter of [KetoyNavHost].
 * @property startRoute    The default start route string for this nav graph.
 * @property destinations  All navigation destinations (screen nodes) in the graph.
 * @property navigations   All navigation actions (edges) connecting destinations.
 * @see KetoyNavHost
 * @see KetoyNavDestination
 * @see KetoyNavAction
 * @see KetoyNavRegistry
 */
@Serializable
data class KetoyNavGraph(
    val navHostName: String,
    val startRoute: String,
    val destinations: List<KetoyNavDestination> = emptyList(),
    val navigations: List<KetoyNavAction> = emptyList()
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        /**
         * Deserialize a [KetoyNavGraph] from a JSON string.
         *
         * Unknown keys in the JSON are silently ignored, making this
         * forward-compatible with future schema additions.
         *
         * ### Example
         * ```kotlin
         * val json = """
         * {
         *   "navHostName": "main",
         *   "startRoute": "home",
         *   "destinations": []
         * }
         * """
         * val graph = KetoyNavGraph.fromJson(json)
         * ```
         *
         * @param jsonString A valid JSON string conforming to the [KetoyNavGraph] schema.
         * @return The deserialized [KetoyNavGraph] instance.
         * @see toJson
         */
        fun fromJson(jsonString: String): KetoyNavGraph =
            json.decodeFromString<KetoyNavGraph>(jsonString)

        /**
         * Serialize a [KetoyNavGraph] to a pretty-printed JSON string.
         *
         * Default values are included in the output for completeness,
         * making the JSON self-documenting.
         *
         * @param graph The [KetoyNavGraph] to serialize.
         * @return A pretty-printed JSON string.
         * @see fromJson
         */
        fun toJson(graph: KetoyNavGraph): String = json.encodeToString(graph)
    }

    /**
     * Serialize this nav graph instance to a pretty-printed JSON string.
     *
     * Convenience wrapper around [Companion.toJson].
     *
     * @return A pretty-printed JSON string representing this nav graph.
     */
    fun toJson(): String = Companion.toJson(this)

    /**
     * Builds the action remap table mapping action/destination IDs to resolved
     * target routes.
     *
     * [KetoyNavController] uses this table so that `navigateToRoute("go_favorites")`
     * resolves to whatever route the JSON currently defines. Destination remaps
     * are applied first, then navigation action remaps (which take precedence).
     *
     * @return A map of `id → route` for all destinations and navigation actions.
     * @see KetoyNavController.routeRemaps
     */
    fun buildActionRemaps(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        // Destination id → route remaps
        for (dest in destinations) {
            map[dest.resolvedId] = dest.route
        }
        // Navigation action id → route remaps (overrides destination remaps)
        for (action in navigations) {
            map[action.id] = action.route
        }
        return map
    }
}

/**
 * Global registry for exportable [KetoyNavGraph] definitions.
 *
 * Stores navigation graphs that can be exported as JSON for the Ketoy dev server,
 * or looked up at runtime by [KetoyNavHost] when no dev-server or cloud override
 * is active.
 *
 * ### Usage
 * ```kotlin
 * // Register a nav graph (typically in Application.onCreate or test setup)
 * KetoyNavRegistry.register(
 *     KetoyNavGraph(
 *         navHostName = "main",
 *         startRoute = "home",
 *         destinations = listOf(
 *             KetoyNavDestination("home", "home", "HomeScreen", "Home", "home"),
 *             KetoyNavDestination("profile", "profile", "ProfileScreen", "Profile", "person")
 *         )
 *     )
 * )
 *
 * // Retrieve by name
 * val graph = KetoyNavRegistry.get("main")
 *
 * // Export all for the dev server
 * val allGraphs = KetoyNavRegistry.getAll()
 * ```
 *
 * ### Override priority
 * [KetoyNavHost] resolves nav graphs in this order (highest priority first):
 * 1. [KetoyNavDevOverrides] — live dev-server reload
 * 2. [KetoyCloudNavOverrides] — cloud-fetched graphs
 * 3. **[KetoyNavRegistry]** — compile-time / locally registered
 *
 * @see KetoyNavGraph
 * @see KetoyNavHost
 * @see KetoyNavDevOverrides
 * @see KetoyCloudNavOverrides
 */
object KetoyNavRegistry {

    private val graphs = mutableMapOf<String, KetoyNavGraph>()

    /**
     * Register or replace a navigation graph by its [KetoyNavGraph.navHostName].
     *
     * If a graph with the same name already exists, it is silently replaced.
     *
     * @param graph The [KetoyNavGraph] to register.
     * @see get
     * @see getAll
     */
    fun register(graph: KetoyNavGraph) {
        graphs[graph.navHostName] = graph
    }

    /**
     * Retrieve a registered nav graph by its [navHostName].
     *
     * @param name The `navHostName` of the graph to retrieve.
     * @return The matching [KetoyNavGraph], or `null` if not registered.
     * @see register
     */
    fun get(name: String): KetoyNavGraph? = graphs[name]

    /**
     * Returns a snapshot of all registered nav graphs as an immutable map.
     *
     * @return Map of `navHostName → KetoyNavGraph`.
     */
    fun getAll(): Map<String, KetoyNavGraph> = graphs.toMap()

    /**
     * Returns the set of all registered nav graph names.
     *
     * @return Set of `navHostName` strings.
     */
    fun getAllNames(): Set<String> = graphs.keys.toSet()

    /**
     * Removes all registered nav graphs.
     *
     * Useful for test teardown or full re-initialization.
     */
    fun clear() {
        graphs.clear()
    }
}
