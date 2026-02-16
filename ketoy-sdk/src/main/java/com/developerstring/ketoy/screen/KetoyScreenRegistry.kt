package com.developerstring.ketoy.screen

/**
 * Global registry for [KetoyScreen] instances.
 *
 * All screens must be registered here before navigation can resolve them.
 * Registration happens automatically via [KetoyContent] or during
 * [Ketoy.initialize].
 *
 * ```kotlin
 * KetoyScreenRegistry.register(KetoyScreen.fromJson("home", homeJson))
 * val screen = KetoyScreenRegistry.get("home")
 * ```
 */
object KetoyScreenRegistry {

    private val screens = mutableMapOf<String, KetoyScreen>()

    // ── Registration ────────────────────────────────────────────

    /** Register a [KetoyScreen]. Replaces any existing screen with the same screen name. */
    fun register(screen: KetoyScreen) {
        screens[screen.screenName] = screen
    }

    /** Register multiple screens at once. */
    fun registerAll(vararg screenList: KetoyScreen) {
        screenList.forEach { register(it) }
    }

    /** Register multiple screens from a collection. */
    fun registerAll(screenList: List<KetoyScreen>) {
        screenList.forEach { register(it) }
    }

    // ── Retrieval ───────────────────────────────────────────────

    /** Get a screen by its screen name, or null if not registered. */
    fun get(screenName: String): KetoyScreen? = screens[screenName]

    /** Returns all registered screen names. */
    fun getAllRoutes(): Set<String> = screens.keys.toSet()

    /** Returns all registered screens as a map of name → screen. */
    fun getAll(): Map<String, KetoyScreen> = screens.toMap()

    /** Check whether a screen with the given name is registered. */
    fun isRegistered(screenName: String): Boolean = screens.containsKey(screenName)

    // ── Count ───────────────────────────────────────────────────

    /** Returns the number of registered screens. */
    fun count(): Int = screens.size

    // ── Removal ─────────────────────────────────────────────────

    /**
     * Remove a screen by name.
     * @return true if the screen was found and removed.
     */
    fun remove(screenName: String): Boolean = screens.remove(screenName) != null

    /** Clear all registered screens. */
    fun clear() {
        screens.clear()
    }

    // ── Bulk loading ────────────────────────────────────────────

    /** Register screens from a map of screen name → JSON content. */
    fun registerFromJsonMap(jsonScreens: Map<String, String>) {
        jsonScreens.forEach { (name, json) ->
            register(KetoyScreen.fromJson(name, json))
        }
    }

    /**
     * Register screens from asset paths.
     * Each entry maps screen name → asset path.
     */
    fun registerFromAssets(assetScreens: Map<String, String>) {
        assetScreens.forEach { (name, assetPath) ->
            register(KetoyScreen.fromAsset(name, assetPath))
        }
    }
}
