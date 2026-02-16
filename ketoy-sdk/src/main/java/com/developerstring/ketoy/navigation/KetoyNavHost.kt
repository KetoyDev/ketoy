package com.developerstring.ketoy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy.screen.KetoyScreenRegistry

/**
 * CompositionLocal providing the [KetoyNavController] to descendant composables.
 *
 * Access via `LocalKetoyNavController.current` anywhere inside a [KetoyNavHost].
 */
val LocalKetoyNavController = staticCompositionLocalOf<KetoyNavController?> {
    null
}

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
 * [KetoyNavGraphScope.screen].
 *
 * @param startRoute     The initial route (a `@Serializable` data object / data class).
 * @param modifier       Optional [Modifier] applied to the NavHost.
 * @param navController  Optional pre-created [NavHostController].
 * @param fallback       Composable rendered for unresolved string routes.
 * @param builder        DSL block for registering type-safe destinations.
 */
@Composable
fun <T : Any> KetoyNavHost(
    startRoute: T,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    fallback: @Composable (route: String) -> Unit = { DefaultFallbackScreen(it) },
    builder: KetoyNavGraphScope.() -> Unit = {}
) {
    val ketoyNavController = KetoyNavController(navController)

    CompositionLocalProvider(LocalKetoyNavController provides ketoyNavController) {
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = modifier
        ) {
            // 1. Type-safe destinations declared via the DSL
            val scope = KetoyNavGraphScope(this)
            scope.builder()

            // 2. String-based screens from KetoyScreenRegistry
            registerStringScreens(fallback)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Overload 2 – String-based start route (dynamic / JSON)
// ═══════════════════════════════════════════════════════════════

/**
 * Create a Ketoy NavHost with a string-based start route.
 *
 * All screens registered in [KetoyScreenRegistry] are automatically
 * added as navigation destinations.
 *
 * @param startRoute   The initial screen route name (must be registered).
 * @param modifier     Optional [Modifier] applied to the NavHost.
 * @param navController Optional pre-created [NavHostController].
 * @param fallback     Composable rendered when a route has no registered screen.
 * @param builder      Optional DSL block for adding type-safe destinations too.
 */
@Composable
fun KetoyNavHost(
    startRoute: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    fallback: @Composable (route: String) -> Unit = { DefaultFallbackScreen(it) },
    builder: KetoyNavGraphScope.() -> Unit = {}
) {
    val ketoyNavController = KetoyNavController(navController)

    CompositionLocalProvider(LocalKetoyNavController provides ketoyNavController) {
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = modifier
        ) {
            // 1. Type-safe destinations declared via the DSL
            val scope = KetoyNavGraphScope(this)
            scope.builder()

            // 2. String-based screens from KetoyScreenRegistry
            registerStringScreens(fallback)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  KetoyNavGraphScope – DSL for type-safe composable<T>
// ═══════════════════════════════════════════════════════════════

/**
 * DSL scope for registering type-safe Ketoy destinations inside
 * a [KetoyNavHost] builder lambda.
 *
 * ```kotlin
 * KetoyNavHost(startRoute = Home) {
 *     screen<Home>       { HomeScreen() }
 *     screen<Detail>     { DetailScreen(id = it.id) }
 *     screen<Settings>   { SettingsScreen() }
 * }
 * ```
 */
class KetoyNavGraphScope(
    @PublishedApi internal val navGraphBuilder: NavGraphBuilder
) {

    /**
     * Register a type-safe destination.
     *
     * @param T       The `@Serializable` route type.
     * @param content Composable that receives the route instance.
     */
    inline fun <reified T : Any> screen(
        noinline content: @Composable (route: T) -> Unit
    ) {
        navGraphBuilder.composable<T> { backStackEntry ->
            val route = backStackEntry.toRoute<T>()
            content(route)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Internal helpers
// ═══════════════════════════════════════════════════════════════

/**
 * Register all string-based screens from [KetoyScreenRegistry].
 */
private fun NavGraphBuilder.registerStringScreens(
    fallback: @Composable (route: String) -> Unit
) {
    val allRoutes = KetoyScreenRegistry.getAllRoutes()

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

@Composable
private fun DefaultFallbackScreen(route: String) {
    androidx.compose.material3.Text(
        text = "Screen not found: $route",
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.error
    )
}
