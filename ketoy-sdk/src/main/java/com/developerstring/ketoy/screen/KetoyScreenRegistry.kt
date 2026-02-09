package com.developerstring.ketoy.screen

import com.developerstring.ketoy.navigation.KetoyRoute
import kotlin.reflect.KClass

/**
 * Global registry for [KetoyScreen] instances.
 *
 * All screens must be registered here before navigation can resolve them.
 * Registration happens during [Ketoy.initialize] or at any point before
 * the first navigation to that route.
 *
 * Supports both **string-based** and **type-safe** route lookups:
 *
 * ```kotlin
 * // String-based
 * KetoyScreenRegistry.register(KetoyScreen.fromJson("home", homeJson))
 * val screen = KetoyScreenRegistry.get("home")
 *
 * // Type-safe
 * @Serializable data object Home : KetoyRoute
 * KetoyScreenRegistry.register(KetoyScreen.fromComposable(Home::class) { HomeScreen() })
 * val screen = KetoyScreenRegistry.get(Home::class)
 * ```
 */
object KetoyScreenRegistry {

    private val screens = mutableMapOf<String, KetoyScreen>()
    private val routeClassIndex = mutableMapOf<KClass<*>, KetoyScreen>()

    // ── Registration ────────────────────────────────────────────

    /**
     * Register a [KetoyScreen]. If a screen with the same route name
     * already exists, it will be replaced.
     *
     * When the screen has a [KetoyScreen.routeClass], it is also indexed
     * by class for type-safe lookups.
     */
    fun register(screen: KetoyScreen) {
        screens[screen.routeName] = screen
        screen.routeClass?.let { routeClassIndex[it] = screen }
    }

    /**
     * Register multiple screens at once.
     */
    fun registerAll(vararg screenList: KetoyScreen) {
        screenList.forEach { register(it) }
    }

    /**
     * Register multiple screens from a collection.
     */
    fun registerAll(screenList: List<KetoyScreen>) {
        screenList.forEach { register(it) }
    }

    // ── Retrieval (string) ──────────────────────────────────────

    /**
     * Get a screen by its route name, or null if not registered.
     */
    fun get(routeName: String): KetoyScreen? = screens[routeName]

    /**
     * Returns all registered string route names.
     */
    fun getAllRoutes(): Set<String> = screens.keys.toSet()

    /**
     * Returns all registered screens as a map of route → screen.
     */
    fun getAll(): Map<String, KetoyScreen> = screens.toMap()

    /**
     * Check whether a screen with the given route name is registered.
     */
    fun isRegistered(routeName: String): Boolean = screens.containsKey(routeName)

    // ── Retrieval (type-safe) ───────────────────────────────────

    /**
     * Get a screen by its `@Serializable` route class.
     *
     * ```kotlin
     * val screen = KetoyScreenRegistry.get(Home::class)
     * ```
     */
    fun <T : KetoyRoute> get(routeClass: KClass<T>): KetoyScreen? =
        routeClassIndex[routeClass]

    /**
     * Check whether a screen with the given route class is registered.
     */
    fun <T : KetoyRoute> isRegistered(routeClass: KClass<T>): Boolean =
        routeClassIndex.containsKey(routeClass)

    /**
     * Returns all registered route classes.
     */
    fun getAllRouteClasses(): Set<KClass<*>> = routeClassIndex.keys.toSet()

    // ── Count ───────────────────────────────────────────────────

    /**
     * Returns the number of registered screens.
     */
    fun count(): Int = screens.size

    // ── Removal ─────────────────────────────────────────────────

    /**
     * Remove a screen by route name.
     *
     * @return true if the screen was found and removed.
     */
    fun remove(routeName: String): Boolean {
        val screen = screens.remove(routeName) ?: return false
        screen.routeClass?.let { routeClassIndex.remove(it) }
        return true
    }

    /**
     * Remove a screen by route class.
     *
     * @return true if the screen was found and removed.
     */
    fun <T : KetoyRoute> remove(routeClass: KClass<T>): Boolean {
        val screen = routeClassIndex.remove(routeClass) ?: return false
        screens.remove(screen.routeName)
        return true
    }

    /**
     * Clear all registered screens.
     */
    fun clear() {
        screens.clear()
        routeClassIndex.clear()
    }

    // ── Bulk loading ────────────────────────────────────────────

    /**
     * Register screens from a map of route name → JSON content.
     * Convenience method for loading multiple JSON screens at once.
     */
    fun registerFromJsonMap(jsonScreens: Map<String, String>) {
        jsonScreens.forEach { (route, json) ->
            register(KetoyScreen.fromJson(route, json))
        }
    }

    /**
     * Register screens from asset paths.
     * Each entry maps route name → asset path.
     */
    fun registerFromAssets(assetScreens: Map<String, String>) {
        assetScreens.forEach { (route, assetPath) ->
            register(KetoyScreen.fromAsset(route, assetPath))
        }
    }
}
