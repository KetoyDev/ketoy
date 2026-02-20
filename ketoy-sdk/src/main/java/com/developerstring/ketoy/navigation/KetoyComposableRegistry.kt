package com.developerstring.ketoy.navigation

import androidx.compose.runtime.Composable

/**
 * Global registry mapping route strings to native `@Composable` screen functions.
 *
 * This bridges **JSON-defined nav graphs** (e.g., `nav_demo.json`) with **real
 * Compose destinations**. When [KetoyNavHost] encounters a destination from a
 * nav override, it checks this registry first—if a composable is registered
 * for that route, it renders the native Compose screen instead of falling back
 * to JSON rendering via [KetoyScreenRegistry][com.developerstring.ketoy.screen.KetoyScreenRegistry].
 *
 * This enables the key Ketoy nav feature: **define the navigation graph in JSON**
 * (routes, order, labels, icons) while the **actual screen content is native Compose**.
 *
 * ### Registration
 * ```kotlin
 * // Register in Application.onCreate or Activity.onCreate
 * KetoyComposableRegistry.register("explore") { ExploreScreen() }
 * KetoyComposableRegistry.register("favorites") { FavoritesScreen() }
 *
 * // Or register multiple at once
 * KetoyComposableRegistry.registerAll(
 *     "explore" to { ExploreScreen() },
 *     "favorites" to { FavoritesScreen() },
 *     "settings" to { SettingsScreen() }
 * )
 * ```
 *
 * ### How it works with nav overrides
 * When `nav_demo.json` references route `"explore"`, [KetoyNavHost] resolves it
 * through this registry and renders `ExploreScreen()` natively.
 *
 * ### Resolution priority in KetoyNavHost
 * 1. Builder DSL destinations ([KetoyNavGraphScope.screen] / [KetoyNavGraphScope.composable])
 * 2. **[KetoyComposableRegistry]** — native composable screens
 * 3. [KetoyScreenRegistry][com.developerstring.ketoy.screen.KetoyScreenRegistry] — JSON-rendered screens
 * 4. Fallback composable
 *
 * @see KetoyNavHost
 * @see KetoyNavGraph
 * @see KetoyNavDevOverrides
 */
object KetoyComposableRegistry {

    private val composables = mutableMapOf<String, @Composable () -> Unit>()

    /**
     * Register a native `@Composable` screen function for a route string.
     *
     * If a composable is already registered for this route, it is silently replaced.
     *
     * ### Example
     * ```kotlin
     * KetoyComposableRegistry.register("explore") {
     *     ExploreScreen()
     * }
     * ```
     *
     * @param route   The route string to associate with the composable.
     * @param content The `@Composable` function to render when this route is active.
     */
    fun register(route: String, content: @Composable () -> Unit) {
        composables[route] = content
    }

    /**
     * Register multiple composable screens at once using vararg pairs.
     *
     * ### Example
     * ```kotlin
     * KetoyComposableRegistry.registerAll(
     *     "explore" to { ExploreScreen() },
     *     "favorites" to { FavoritesScreen() }
     * )
     * ```
     *
     * @param entries Vararg of `(route, composable)` pairs to register.
     */
    fun registerAll(vararg entries: Pair<String, @Composable () -> Unit>) {
        entries.forEach { (route, content) -> composables[route] = content }
    }

    /**
     * Retrieve the composable screen function for a given route.
     *
     * @param route The route string to look up.
     * @return The registered `@Composable` function, or `null` if no composable is registered.
     */
    fun get(route: String): (@Composable () -> Unit)? = composables[route]

    /**
     * Check whether a native composable screen is registered for the given route.
     *
     * @param route The route string to check.
     * @return `true` if a composable is registered for this route.
     */
    fun isRegistered(route: String): Boolean = composables.containsKey(route)

    /**
     * Returns the set of all route strings with registered composable screens.
     *
     * @return An immutable set of route strings.
     */
    fun getAllRoutes(): Set<String> = composables.keys.toSet()

    /**
     * Remove a composable registration for the given route.
     *
     * @param route The route string to unregister.
     */
    fun remove(route: String) {
        composables.remove(route)
    }

    /**
     * Clear all composable registrations.
     *
     * Useful for test teardown or full re-initialization.
     */
    fun clear() {
        composables.clear()
    }
}
