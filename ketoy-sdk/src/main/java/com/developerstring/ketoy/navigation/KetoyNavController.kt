package com.developerstring.ketoy.navigation

import androidx.navigation.NavHostController
import androidx.navigation.toRoute

/**
 * Wrapper around Jetpack Navigation's [NavHostController] that provides
 * a server-driven navigation API for Ketoy.
 *
 * Uses Android-friendly naming aligned with Jetpack Navigation conventions.
 * Supports **two route styles**, both fully interoperable:
 *
 * ### 1. Type-safe routes (Navigation2 `@Serializable` classes)
 * ```kotlin
 * @Serializable data object Home : KetoyRoute
 * @Serializable data class Detail(val id: String) : KetoyRoute
 *
 * nav.navigate(Home)
 * nav.navigate(Detail(id = "42"))
 * nav.navigateAndReplace(Home)
 * nav.navigateAndClearBackStack(Home)
 * ```
 *
 * ### 2. String routes (for JSON-driven / dynamic navigation)
 * ```kotlin
 * nav.navigateToRoute("home")
 * nav.navigateToRoute("detail", mapOf("id" to "42"))
 * ```
 */
class KetoyNavController(
    @PublishedApi internal val navController: NavHostController
) {

    // ═══════════════════════════════════════════════════════════
    //  Type-safe navigation (Navigation2 @Serializable routes)
    // ═══════════════════════════════════════════════════════════

    /**
     * Navigate to a type-safe route.
     *
     * ```kotlin
     * nav.navigate(Detail(id = "42"))
     * ```
     */
    fun <T : Any> navigate(route: T) {
        navController.navigate(route)
    }

    /**
     * Navigate to a type-safe route, replacing the current screen.
     *
     * ```kotlin
     * nav.navigateAndReplace(Home)
     * ```
     */
    fun <T : Any> navigateAndReplace(route: T) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        navController.navigate(route) {
            if (currentRoute != null) {
                popUpTo(currentRoute) { inclusive = true }
            }
        }
    }

    /**
     * Navigate to a type-safe route and clear the entire back stack.
     * The new screen becomes the root.
     *
     * ```kotlin
     * nav.navigateAndClearBackStack(Home)
     * ```
     */
    fun <T : Any> navigateAndClearBackStack(route: T) {
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }

    /**
     * Extract the type-safe route object from the current back stack entry.
     *
     * ```kotlin
     * val detail: Detail = nav.currentRouteAs<Detail>()
     * ```
     */
    inline fun <reified T : KetoyRoute> currentRouteAs(): T? {
        return try {
            navController.currentBackStackEntry?.toRoute<T>()
        } catch (_: Exception) {
            null
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  String-based navigation (for JSON / dynamic screens)
    // ═══════════════════════════════════════════════════════════

    /**
     * Navigate to a string route.
     *
     * @param route     The registered screen route string.
     * @param arguments Optional arguments (stored via SavedStateHandle).
     */
    fun navigateToRoute(route: String, arguments: Map<String, Any>? = null) {
        if (arguments != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.apply {
                    arguments.forEach { (key, value) ->
                        set(key, value.toString())
                    }
                }
        }
        navController.navigate(route)
    }

    /**
     * Navigate to a string route, replacing the current screen.
     */
    fun navigateAndReplaceRoute(
        route: String,
        result: Map<String, Any>? = null,
        arguments: Map<String, Any>? = null
    ) {
        setResult(result)

        if (arguments != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.apply {
                    arguments.forEach { (key, value) ->
                        set(key, value.toString())
                    }
                }
        }

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        navController.navigate(route) {
            if (currentRoute != null) {
                popUpTo(currentRoute) { inclusive = true }
            }
        }
    }

    /**
     * Navigate to a string route and clear the entire back stack.
     */
    fun navigateAndClearBackStackRoute(route: String, arguments: Map<String, Any>? = null) {
        if (arguments != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.apply {
                    arguments.forEach { (key, value) ->
                        set(key, value.toString())
                    }
                }
        }
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Pop (works with both route styles)
    // ═══════════════════════════════════════════════════════════

    /**
     * Pop the current screen from the back stack.
     *
     * @param result Optional result map passed back to the previous screen.
     */
    fun popBackStack(result: Map<String, Any>? = null) {
        setResult(result)
        navController.popBackStack()
    }

    /**
     * Pop all screens back to the root of the navigation stack.
     */
    fun popToRoot() {
        val startRoute = navController.graph.startDestinationRoute
        if (startRoute != null) {
            navController.popBackStack(startRoute, inclusive = false)
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Query helpers
    // ═══════════════════════════════════════════════════════════

    /** The current route string, or null if no destination is active. */
    val currentRoute: String?
        get() = navController.currentBackStackEntry?.destination?.route

    /** Whether the back stack has entries to pop. */
    val canGoBack: Boolean
        get() = navController.previousBackStackEntry != null

    /** Retrieve a navigation argument by key from the current SavedStateHandle. */
    fun <T> getArgument(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return navController.currentBackStackEntry
            ?.savedStateHandle?.get<T>(key)
    }

    /** Retrieve a result value passed from a popped screen. */
    fun <T> getResult(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return navController.currentBackStackEntry
            ?.savedStateHandle?.get<T>(key)
    }

    // ── Private helpers ─────────────────────────────────────────

    private fun setResult(result: Map<String, Any>?) {
        if (result != null) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.apply {
                    result.forEach { (key, value) ->
                        set(key, value.toString())
                    }
                }
        }
    }
}
