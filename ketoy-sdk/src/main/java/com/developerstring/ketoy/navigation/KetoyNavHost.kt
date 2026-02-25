package com.developerstring.ketoy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.developerstring.ketoy.screen.KetoyScreenRegistry

/**
 * [CompositionLocal][androidx.compose.runtime.CompositionLocal] providing the nav-host name
 * to descendant composables within a [KetoyNavHost].
 *
 * The Ketoy dev server uses this value to identify which navigation graph is
 * currently being rendered, enabling targeted live-reload of specific nav-host
 * graphs without affecting others in the same Activity.
 *
 * Returns `null` when accessed outside a [KetoyNavHost] scope.
 *
 * ### Usage
 * ```kotlin
 * @Composable
 * fun DebugNavOverlay() {
 *     val navHostName = LocalKetoyNavHostName.current
 *     Text("Active nav-host: ${navHostName ?: "none"}")
 * }
 * ```
 *
 * @see KetoyNavHost
 * @see KetoyNavDevOverrides
 * @see KetoyCloudNavOverrides
 */
val LocalKetoyNavHostName = staticCompositionLocalOf<String?> { null }

/**
 * [CompositionLocal][androidx.compose.runtime.CompositionLocal] providing the [KetoyNavController]
 * to descendant composables within a [KetoyNavHost].
 *
 * This is the primary mechanism for accessing Ketoy navigation capabilities from
 * any composable inside the navigation hierarchy. The controller supports both
 * type-safe `@Serializable` routes and string-based (JSON-driven) navigation.
 *
 * Returns `null` when accessed outside a [KetoyNavHost] scope.
 *
 * ### Usage
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val nav = LocalKetoyNavController.current
 *
 *     // Type-safe navigation
 *     Button(onClick = { nav?.navigate(Detail(id = "42")) }) {
 *         Text("Go to Detail")
 *     }
 *
 *     // String-based (JSON-driven) navigation
 *     Button(onClick = { nav?.navigateToRoute("favorites") }) {
 *         Text("Go to Favorites")
 *     }
 *
 *     // Pop back
 *     Button(onClick = { nav?.popBackStack() }) {
 *         Text("Go Back")
 *     }
 * }
 * ```
 *
 * @see KetoyNavController
 * @see KetoyNavHost
 * @see LocalKetoyNavGraph
 */
val LocalKetoyNavController = staticCompositionLocalOf<KetoyNavController?> {
    null
}

/**
 * CompositionLocal providing the active [KetoyNavGraph] to descendant composables.
 *
 * Composable screens read this to discover available destinations, build
 * data-driven navigation UI, and resolve route strings — all from JSON.
 *
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val nav = LocalKetoyNavController.current
 *     val navGraph = LocalKetoyNavGraph.current
 *     val destinations = navGraph?.destinations ?: emptyList()
 *
 *     destinations.forEach { dest ->
 *         Button(onClick = { nav?.navigateToRoute(dest.route) }) {
 *             Text(dest.label)
 *         }
 *     }
 * }
 * ```
 */
val LocalKetoyNavGraph = staticCompositionLocalOf<KetoyNavGraph?> { null }

/**
 * A server-driven Navigation host that renders Ketoy screens.
 *
 * Supports **two route styles** – both can be used together in the same graph:
 *
 * ### 1. Type-safe `@Serializable` routes (Navigation2 style)
 * ```kotlin
 * @Serializable data object Home : KetoyRoute
 * @Serializable data class Detail(val id: String) : KetoyRoute
 *
 * KetoyNavHost(
 *     startRoute = Home,
 *     modifier = Modifier.fillMaxSize()
 * ) {
 *     screen<Home> { HomeScreen() }
 *     screen<Detail> { DetailScreen(id = it.id) }
 * }
 * ```
 *
 * ### 2. String-based routes (JSON-driven / dynamic)
 * ```kotlin
 * KetoyNavHost(
 *     startRoute = "home",
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 */

// ═══════════════════════════════════════════════════════════════
//  Overload 1 – Type-safe start route (Navigation2 style)
// ═══════════════════════════════════════════════════════════════

/**
 * Create a Ketoy NavHost with a type-safe `@Serializable` start route.
 *
 * Use the [builder] lambda to register type-safe destinations via
 * [KetoyNavGraphScope.screen] and string destinations via
 * [KetoyNavGraphScope.composable].
 *
 * @param startRoute     The initial route (a `@Serializable` data object / data class).
 * @param modifier       Optional [Modifier] applied to the NavHost.
 * @param navHostName    Name of this nav graph for dev-server live reload (default "main").
 * @param navController  Optional pre-created [NavHostController].
 * @param fallback       Composable rendered for unresolved string routes.
 * @param builder        DSL block for registering destinations.
 */
@Composable
fun <T : Any> KetoyNavHost(
    startRoute: T,
    modifier: Modifier = Modifier,
    navHostName: String = "main",
    navController: NavHostController = rememberNavController(),
    fallback: @Composable (route: String) -> Unit = { DefaultFallbackScreen(it) },
    builder: KetoyNavGraphScope.() -> Unit = {}
) {
    val ketoyNavController = remember(navController) { KetoyNavController(navController) }

    // Priority: dev-server override → cloud override → local registry → null
    val navGraph = KetoyNavDevOverrides.overrides[navHostName]
        ?: KetoyCloudNavOverrides.overrides[navHostName]
        ?: KetoyNavRegistry.get(navHostName)

    // Build remap table from both destinations (id→route) AND navigations (actionId→route).
    val routeRemaps: Map<String, String> = navGraph?.buildActionRemaps() ?: emptyMap()
    ketoyNavController.routeRemaps = routeRemaps

    CompositionLocalProvider(
        LocalKetoyNavController provides ketoyNavController,
        LocalKetoyNavHostName provides navHostName,
        LocalKetoyNavGraph provides navGraph
    ) {
        // key() forces NavHost rebuild when override routes change (live editing)
        key(navGraph?.startRoute, navGraph?.destinations?.map { it.route }) {
            NavHost(
                navController = navController,
                startDestination = startRoute,
                modifier = modifier
            ) {
                val scope = KetoyNavGraphScope(this)
                scope.builder()

                // Register collected string destinations with remapped routes
                val registeredRoutes = mutableSetOf<String>()
                scope.stringDestinations.forEach { (id, content) ->
                    val actualRoute = routeRemaps[id] ?: id
                    registeredRoutes.add(actualRoute)
                    composable(actualRoute) { content() }
                }

                registerStringScreens(fallback, registeredRoutes)

                if (navGraph != null) {
                    registerNavOverrideDestinations(navGraph, fallback, registeredRoutes)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Overload 2 – String-based start route (dynamic / JSON)
// ═══════════════════════════════════════════════════════════════

/**
 * Create a Ketoy NavHost with a string-based start route.
 *
 * When a nav override is active (from the dev server), the start route
 * and all builder routes resolve through the override's remap table.
 * This means changing a route in `nav_*.json` **automatically updates
 * navigation** — including `navigateToRoute("id")` calls in code.
 *
 * @param startRoute   The initial screen route name (or destination ID).
 * @param modifier     Optional [Modifier] applied to the NavHost.
 * @param navHostName  Name of this nav graph for dev-server live reload (default "main").
 * @param navController Optional pre-created [NavHostController].
 * @param fallback     Composable rendered when a route has no registered screen.
 * @param builder      DSL block for registering destinations.
 */
@Composable
fun KetoyNavHost(
    startRoute: String,
    modifier: Modifier = Modifier,
    navHostName: String = "main",
    navController: NavHostController = rememberNavController(),
    fallback: @Composable (route: String) -> Unit = { DefaultFallbackScreen(it) },
    builder: KetoyNavGraphScope.() -> Unit = {}
) {
    val ketoyNavController = remember(navController) { KetoyNavController(navController) }

    // Priority: dev-server override → cloud override → local registry → null
    val navGraph = KetoyNavDevOverrides.overrides[navHostName]
        ?: KetoyCloudNavOverrides.overrides[navHostName]
        ?: KetoyNavRegistry.get(navHostName)

    // Build remap table from both destinations (id→route) AND navigations (actionId→route).
    val routeRemaps: Map<String, String> = navGraph?.buildActionRemaps() ?: emptyMap()
    ketoyNavController.routeRemaps = routeRemaps

    // Resolve start route through the remap table so JSON can override it
    val resolvedStartRoute = routeRemaps[startRoute] ?: startRoute

    CompositionLocalProvider(
        LocalKetoyNavController provides ketoyNavController,
        LocalKetoyNavHostName provides navHostName,
        LocalKetoyNavGraph provides navGraph
    ) {
        // key() forces NavHost rebuild when override routes change (live editing)
        key(navGraph?.startRoute, navGraph?.destinations?.map { it.route }) {
            NavHost(
                navController = navController,
                startDestination = resolvedStartRoute,
                modifier = modifier
            ) {
                val scope = KetoyNavGraphScope(this)
                scope.builder()

                // Register collected string destinations with remapped routes
                val registeredRoutes = mutableSetOf<String>()
                scope.stringDestinations.forEach { (id, content) ->
                    val actualRoute = routeRemaps[id] ?: id
                    registeredRoutes.add(actualRoute)
                    composable(actualRoute) { content() }
                }

                registerStringScreens(fallback, registeredRoutes)

                if (navGraph != null) {
                    registerNavOverrideDestinations(navGraph, fallback, registeredRoutes)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  KetoyNavGraphScope – DSL for type-safe composable<T>
// ═══════════════════════════════════════════════════════════════

/**
 * DSL scope for declaring Ketoy destinations inside a [KetoyNavHost]
 * builder — works like Jetpack Navigation 2.
 *
 * ### Type-safe routes
 * ```kotlin
 * KetoyNavHost(startRoute = Home) {
 *     screen<Home>   { HomeScreen() }
 *     screen<Detail> { DetailScreen(id = it.id) }
 * }
 * ```
 *
 * ### String routes (with live-remap from JSON)
 * ```kotlin
 * KetoyNavHost(startRoute = "explore", navHostName = "demo") {
 *     composable("explore")       { ExploreScreen() }
 *     composable("favorites")     { FavoritesScreen() }
 *     composable("notifications") { NotificationsScreen() }
 * }
 * ```
 *
 * String routes act as **destination IDs**. If the dev server overrides
 * the nav graph (via `nav_demo.json`), the ID can be remapped to a
 * different route. `navigateToRoute("favorites")` resolves through the
 * remap table automatically — no code changes needed.
 */
class KetoyNavGraphScope(
    @PublishedApi internal val navGraphBuilder: NavGraphBuilder
) {

    /**
     * Collected string-based destinations as (id, content) pairs.
     *
     * [KetoyNavHost] reads this list during graph construction and registers
     * each entry through the route remap table from the active nav override
     * (dev server or cloud). If no override is active, the id is used as the
     * literal route string.
     *
     * @see KetoyNavHost
     * @see KetoyNavGraphScope.composable
     */
    internal val stringDestinations = mutableListOf<Pair<String, @Composable () -> Unit>>()

    /**
     * Register a type-safe destination using a `@Serializable` route class.
     *
     * The destination is registered immediately in the underlying [NavGraphBuilder].
     * The [content] lambda receives the deserialized route object, giving you
     * direct access to type-safe parameters.
     *
     * ### Example
     * ```kotlin
     * @Serializable data class Detail(val id: String) : KetoyRoute
     *
     * KetoyNavHost(startRoute = Home) {
     *     screen<Detail> { route ->
     *         DetailScreen(id = route.id)
     *     }
     * }
     * ```
     *
     * @param T       The `@Serializable` route type implementing [KetoyRoute] or any type.
     * @param content Composable lambda that receives the deserialized route instance
     *                extracted from the current [NavBackStackEntry].
     * @see KetoyNavGraphScope.composable
     * @see KetoyNavHost
     */
    inline fun <reified T : Any> screen(
        noinline content: @Composable (route: T) -> Unit
    ) {
        navGraphBuilder.composable<T> { backStackEntry ->
            val route = backStackEntry.toRoute<T>()
            content(route)
        }
    }

    /**
     * Declare a string-based destination — Nav2-style API.
     *
     * The [route] string is the **destination ID**. It's also the default
     * navigation route. When a nav override is active, the ID can be
     * remapped to a different actual route. `navigateToRoute("id")`
     * resolves through the remap automatically.
     *
     * ```kotlin
     * composable("favorites") { FavoritesScreen() }
     * // then anywhere: nav?.navigateToRoute("favorites")
     * ```
     *
     * @param route   Destination ID / default route string.
     * @param content Composable rendered when this destination is active.
     */
    fun composable(
        route: String,
        content: @Composable () -> Unit
    ) {
        stringDestinations.add(route to content)
    }
}

// ═══════════════════════════════════════════════════════════════
//  Internal helpers
// ═══════════════════════════════════════════════════════════════

/**
 * Registers all string-based screens from [KetoyScreenRegistry] and
 * [KetoyComposableRegistry] into the navigation graph.
 *
 * Routes already declared in the builder DSL via [KetoyNavGraphScope.composable]
 * are excluded to prevent duplicate registrations. Also registers a wildcard
 * fallback route (`ketoy_fallback/{route}`) for dynamically resolved screens.
 *
 * Resolution order for each route:
 * 1. [KetoyScreenRegistry] — JSON-rendered Ketoy screens
 * 2. [KetoyComposableRegistry] — native `@Composable` screen functions
 * 3. [fallback] composable for unresolved routes
 *
 * @param fallback     Composable rendered when a route has no registered screen.
 * @param builderRoutes Set of route strings already registered via the DSL builder.
 * @see registerNavOverrideDestinations
 * @see KetoyScreenRegistry
 * @see KetoyComposableRegistry
 */
private fun NavGraphBuilder.registerStringScreens(
    fallback: @Composable (route: String) -> Unit,
    builderRoutes: Set<String> = emptySet()
) {
    val allRoutes = KetoyScreenRegistry.getAllRoutes() - builderRoutes

    allRoutes.forEach { route ->
        composable(route) {
            val screen = KetoyScreenRegistry.get(route)
            if (screen != null) {
                screen.Content()
            } else {
                fallback(route)
            }
        }
    }

    // Also register composable destinations not already covered
    val composableRoutes = KetoyComposableRegistry.getAllRoutes() -
            KetoyScreenRegistry.getAllRoutes() - builderRoutes
    composableRoutes.forEach { route ->
        composable(route) {
            val content = KetoyComposableRegistry.get(route)
            if (content != null) {
                content()
            } else {
                fallback(route)
            }
        }
    }

    // Fallback route for dynamic / unknown routes
    composable("ketoy_fallback/{route}") { backStackEntry ->
        val route = backStackEntry.arguments?.getString("route") ?: "unknown"
        val screen = KetoyScreenRegistry.get(route)
        if (screen != null) {
            screen.Content()
        } else {
            fallback(route)
        }
    }
}

/**
 * Registers destinations from a dev-server or cloud [KetoyNavGraph] override
 * into the navigation graph.
 *
 * Only adds destinations whose routes are **not already registered** via the builder
 * DSL, [KetoyScreenRegistry], or [KetoyComposableRegistry]. This ensures that
 * explicitly declared destinations always take priority over override-defined ones.
 *
 * Resolution order for each destination:
 * 1. [KetoyComposableRegistry] — native `@Composable` screen functions (by route or screenName)
 * 2. [KetoyScreenRegistry] (by `screenName`) — JSON-rendered Ketoy screens
 * 3. [fallback] composable for unresolved routes
 *
 * @param navGraph     The override navigation graph containing destinations to register.
 * @param fallback     Composable rendered when a destination has no registered screen.
 * @param builderRoutes Set of route strings already registered via the DSL builder.
 * @see registerStringScreens
 * @see KetoyNavDevOverrides
 * @see KetoyCloudNavOverrides
 */
private fun NavGraphBuilder.registerNavOverrideDestinations(
    navGraph: KetoyNavGraph,
    fallback: @Composable (route: String) -> Unit,
    builderRoutes: Set<String> = emptySet()
) {
    // Combine all already-registered routes to avoid duplicates
    val existingRoutes = KetoyScreenRegistry.getAllRoutes() +
            KetoyComposableRegistry.getAllRoutes() +
            builderRoutes

    navGraph.destinations.forEach { dest ->
        // Skip if already registered via builder, KetoyScreenRegistry, or KetoyComposableRegistry
        if (dest.route in existingRoutes) return@forEach

        composable(dest.route) {
            // 1. Check KetoyComposableRegistry for a real Compose destination
            val composable = KetoyComposableRegistry.get(dest.route)
                ?: KetoyComposableRegistry.get(dest.screenName)
            if (composable != null) {
                composable()
                return@composable
            }

            // 2. Fall back to KetoyScreenRegistry (JSON rendering)
            val screen = KetoyScreenRegistry.get(dest.screenName)
            if (screen != null) {
                screen.Content()
            } else {
                fallback(dest.route)
            }
        }
    }
}

/**
 * Default fallback screen displayed when a route cannot be resolved to any
 * registered screen in [KetoyScreenRegistry], [KetoyComposableRegistry], or
 * the [KetoyNavGraphScope] builder.
 *
 * Shows an error-styled text message indicating the unresolved route name.
 * Override this by passing a custom `fallback` lambda to [KetoyNavHost].
 *
 * @param route The route string that could not be resolved.
 * @see KetoyNavHost
 */
@Composable
private fun DefaultFallbackScreen(route: String) {
    androidx.compose.material3.Text(
        text = "Screen not found: $route",
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.error
    )
}
