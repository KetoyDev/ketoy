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

    /**
     * Route remap table populated by [KetoyNavHost] from the active nav override.
     *
     * Maps destination **IDs** (the strings used in [navigateToRoute] calls)
     * to **actual routes** (from the JSON nav graph override).
     *
     * Example: builder declares `composable("favorites") { … }`. The dev
     * server pushes `nav_demo.json` with `{"id": "favorites", "route": "bookmarks"}`.
     * The remap table becomes `{"favorites" → "bookmarks"}`.
     * Now `navigateToRoute("favorites")` resolves to `"bookmarks"` automatically.
     *
     * When empty (no dev server / no override), routes are used as-is.
     */
    internal var routeRemaps: Map<String, String> = emptyMap()

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
     * Navigate to a string route **or** a navigation action ID.
     *
     * The [route] is resolved through the remap table built from the
     * active nav graph (local [KetoyNavRegistry] or dev-server override).
     *
     * ### Direct route
     * ```kotlin
     * nav?.navigateToRoute("favorites")
     * ```
     *
     * ### Navigation action ID (from [KetoyNavAction])
     * ```kotlin
     * // DemoNavGraphs: KetoyNavAction(id = "go_favorites", route = "favorites")
     * nav?.navigateToRoute("go_favorites") // → resolves to "favorites"
     *
     * // Change route in JSON → navigation updates live, no code change
     * ```
     *
     * @param route     A route string or a navigation action ID.
     * @param arguments Optional arguments (stored via SavedStateHandle).
     */
    fun navigateToRoute(route: String, arguments: Map<String, Any>? = null) {
        val resolvedRoute = routeRemaps[route] ?: route
        if (arguments != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.apply {
                    arguments.forEach { (key, value) ->
                        set(key, value.toString())
                    }
                }
        }
        navController.navigate(resolvedRoute)
    }

    /**
     * Navigate to a string route, replacing the current screen in the back stack.
     *
     * The current destination is popped (inclusive) before navigating to the
     * resolved route. Optionally passes a [result] back to the previous screen
     * and/or forwards [arguments] to the target screen.
     *
     * The [route] is resolved through the remap table built from the active
     * nav graph override (if any).
     *
     * ### Example
     * ```kotlin
     * // Replace current screen with "settings"
     * nav?.navigateAndReplaceRoute("settings")
     *
     * // Replace and pass a result back + arguments forward
     * nav?.navigateAndReplaceRoute(
     *     route = "checkout",
     *     result = mapOf("selected" to "item_42"),
     *     arguments = mapOf("step" to "payment")
     * )
     * ```
     *
     * @param route     A route string or navigation action ID, resolved via the remap table.
     * @param result    Optional result map passed back to the previous screen via [SavedStateHandle].
     * @param arguments Optional arguments forwarded to the target screen via [SavedStateHandle].
     * @see navigateToRoute
     * @see navigateAndClearBackStackRoute
     */
    fun navigateAndReplaceRoute(
        route: String,
        result: Map<String, Any>? = null,
        arguments: Map<String, Any>? = null
    ) {
        val resolvedRoute = routeRemaps[route] ?: route
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
        navController.navigate(resolvedRoute) {
            if (currentRoute != null) {
                popUpTo(currentRoute) { inclusive = true }
            }
        }
    }

    /**
     * Navigate to a string route and clear the entire back stack.
     *
     * After this call, the target route becomes the new root of the
     * navigation stack. All previous back stack entries are removed.
     * The [route] is resolved through the remap table.
     *
     * ### Example
     * ```kotlin
     * // Reset to home after logout
     * nav?.navigateAndClearBackStackRoute("login")
     *
     * // With arguments
     * nav?.navigateAndClearBackStackRoute(
     *     route = "home",
     *     arguments = mapOf("refresh" to "true")
     * )
     * ```
     *
     * @param route     A route string or navigation action ID, resolved via the remap table.
     * @param arguments Optional arguments forwarded to the target screen via [SavedStateHandle].
     * @see navigateToRoute
     * @see navigateAndReplaceRoute
     */
    fun navigateAndClearBackStackRoute(route: String, arguments: Map<String, Any>? = null) {
        val resolvedRoute = routeRemaps[route] ?: route
        if (arguments != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.apply {
                    arguments.forEach { (key, value) ->
                        set(key, value.toString())
                    }
                }
        }
        navController.navigate(resolvedRoute) {
            popUpTo(0) { inclusive = true }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Pop (works with both route styles)
    // ═══════════════════════════════════════════════════════════

    /**
     * Pop the current screen from the back stack, optionally passing a result
     * to the previous screen.
     *
     * The result map is written to the previous back stack entry's
     * [SavedStateHandle][androidx.lifecycle.SavedStateHandle] before popping,
     * allowing the destination screen to read values via [getResult].
     *
     * ### Example
     * ```kotlin
     * // Simple pop
     * nav?.popBackStack()
     *
     * // Pop with result
     * nav?.popBackStack(mapOf("selectedItem" to "item_42"))
     * ```
     *
     * @param result Optional result map passed back to the previous screen.
     *               Each value is converted to a [String] via `toString()`.
     * @see popToRoot
     * @see getResult
     */
    fun popBackStack(result: Map<String, Any>? = null) {
        setResult(result)
        navController.popBackStack()
    }

    /**
     * Pop all screens back to the root of the navigation stack.
     *
     * The root destination (start destination of the nav graph) is retained;
     * all destinations above it are removed. If no start destination is
     * available, this is a no-op.
     *
     * ### Example
     * ```kotlin
     * // Return to the root screen from deep in the navigation hierarchy
     * nav?.popToRoot()
     * ```
     *
     * @see popBackStack
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

    /**
     * The current destination's route string, or `null` if no destination is active.
     *
     * ### Example
     * ```kotlin
     * val currentScreen = nav?.currentRoute
     * Log.d("Nav", "Currently on: $currentScreen")
     * ```
     */
    val currentRoute: String?
        get() = navController.currentBackStackEntry?.destination?.route

    /**
     * Whether the navigation back stack has entries to pop.
     *
     * Returns `true` if there is a previous back stack entry, meaning
     * [popBackStack] will succeed.
     *
     * ### Example
     * ```kotlin
     * if (nav?.canGoBack == true) {
     *     BackButton(onClick = { nav.popBackStack() })
     * }
     * ```
     */
    val canGoBack: Boolean
        get() = navController.previousBackStackEntry != null

    /**
     * Retrieve a navigation argument by [key] from the current back stack entry's
     * [SavedStateHandle][androidx.lifecycle.SavedStateHandle].
     *
     * Arguments are set by the sender via the `arguments` parameter of
     * [navigateToRoute], [navigateAndReplaceRoute], or [navigateAndClearBackStackRoute].
     *
     * ### Example
     * ```kotlin
     * val itemId: String? = nav?.getArgument("id")
     * ```
     *
     * @param T   The expected type of the argument value.
     * @param key The argument key.
     * @return The argument value cast to [T], or `null` if not found.
     */
    fun <T> getArgument(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return navController.currentBackStackEntry
            ?.savedStateHandle?.get<T>(key)
    }

    /**
     * Retrieve a result value passed back from a popped screen.
     *
     * When a screen calls [popBackStack] with a `result` map, the previous
     * screen can read those values via this method.
     *
     * ### Example
     * ```kotlin
     * // In the destination screen (after a child pops with a result):
     * val selectedItem: String? = nav?.getResult("selected")
     * ```
     *
     * @param T   The expected type of the result value.
     * @param key The result key.
     * @return The result value cast to [T], or `null` if not found.
     */
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
