package com.developerstring.ketoy.screen

import androidx.compose.runtime.mutableStateMapOf

/**
 * Global, singleton registry for [KetoyScreen] instances.
 *
 * Every screen must be registered here before navigation, [KetoyView], or
 * [KetoyCloudScreen][com.developerstring.ketoy.cloud.KetoyCloudScreen] can
 * resolve it by name. Registration happens automatically via
 * [ProvideKetoyScreen] / [KetoyContent], or can be done explicitly during
 * [Ketoy.initialize][com.developerstring.ketoy.Ketoy.initialize].
 *
 * ## Register a single screen
 * ```kotlin
 * KetoyScreenRegistry.register(
 *     KetoyScreen.fromJson("home", homeJson)
 * )
 * ```
 *
 * ## Register multiple screens
 * ```kotlin
 * KetoyScreenRegistry.registerAll(
 *     KetoyScreen.fromJson("home", homeJson),
 *     KetoyScreen.fromJson("profile", profileJson)
 * )
 * ```
 *
 * ## Bulk-load from JSON map
 * ```kotlin
 * KetoyScreenRegistry.registerFromJsonMap(
 *     mapOf("home" to homeJson, "settings" to settingsJson)
 * )
 * ```
 *
 * ## Bulk-load from assets
 * ```kotlin
 * KetoyScreenRegistry.registerFromAssets(
 *     mapOf("home" to "screens/home.json", "profile" to "screens/profile.json")
 * )
 * ```
 *
 * ## Retrieve a screen
 * ```kotlin
 * val screen: KetoyScreen? = KetoyScreenRegistry.get("home")
 * ```
 *
 * @see KetoyScreen
 * @see ProvideKetoyScreen
 * @see KetoyView
 */
object KetoyScreenRegistry {

    private val screens = mutableStateMapOf<String, KetoyScreen>()

    // ── Registration ────────────────────────────────────────────

    /**
     * Register a [KetoyScreen].
     *
     * Replaces any existing screen with the same [KetoyScreen.screenName].
     *
     * @param screen The screen to register.
     */
    fun register(screen: KetoyScreen) {
        screens[screen.screenName] = screen
    }

    /**
     * Register multiple screens at once.
     *
     * @param screenList Vararg of [KetoyScreen] instances.
     */
    fun registerAll(vararg screenList: KetoyScreen) {
        screenList.forEach { register(it) }
    }

    /**
     * Register multiple screens from a collection.
     *
     * @param screenList List of [KetoyScreen] instances.
     */
    fun registerAll(screenList: List<KetoyScreen>) {
        screenList.forEach { register(it) }
    }

    // ── Retrieval ───────────────────────────────────────────────

    /**
     * Get a screen by its screen name, or `null` if not registered.
     *
     * @param screenName The screen identifier.
     * @return The [KetoyScreen], or `null`.
     */
    fun get(screenName: String): KetoyScreen? = screens[screenName]

    /**
     * Returns the names of all registered screens.
     *
     * @return Immutable [Set] of screen identifiers.
     */
    fun getAllRoutes(): Set<String> = screens.keys.toSet()

    /**
     * Returns all registered screens as an immutable map of name → screen.
     *
     * @return Map of screen name to [KetoyScreen].
     */
    fun getAll(): Map<String, KetoyScreen> = screens.toMap()

    /**
     * Check whether a screen with the given name is registered.
     *
     * @param screenName The screen identifier.
     * @return `true` if registered.
     */
    fun isRegistered(screenName: String): Boolean = screens.containsKey(screenName)

    // ── Count ───────────────────────────────────────────────────

    /**
     * Returns the number of registered screens.
     *
     * @return Screen count.
     */
    fun count(): Int = screens.size

    // ── Removal ─────────────────────────────────────────────────

    /**
     * Remove a screen by name.
     *
     * @param screenName The screen identifier.
     * @return `true` if the screen was found and removed.
     */
    fun remove(screenName: String): Boolean = screens.remove(screenName) != null

    /**
     * Clear all registered screens.
     *
     * Primarily useful in tests or when resetting the SDK.
     */
    fun clear() {
        screens.clear()
    }

    // ── Bulk loading ────────────────────────────────────────────

    /**
     * Register screens from a map of screen name → JSON content.
     *
     * Each entry creates a [KetoyScreen.fromJson] instance and registers
     * it in the registry.
     *
     * ```kotlin
     * KetoyScreenRegistry.registerFromJsonMap(
     *     mapOf("home" to homeJson, "settings" to settingsJson)
     * )
     * ```
     *
     * @param jsonScreens Map of screen name to raw JSON string.
     */
    fun registerFromJsonMap(jsonScreens: Map<String, String>) {
        jsonScreens.forEach { (name, json) ->
            register(KetoyScreen.fromJson(name, json))
        }
    }

    /**
     * Register screens from asset paths.
     *
     * Each entry creates a [KetoyScreen.fromAsset] instance and registers
     * it in the registry.
     *
     * ```kotlin
     * KetoyScreenRegistry.registerFromAssets(
     *     mapOf(
     *         "home"    to "screens/home.json",
     *         "profile" to "screens/profile.json"
     *     )
     * )
     * ```
     *
     * @param assetScreens Map of screen name → asset path (relative to
     *                     Android `assets/` directory).
     */
    fun registerFromAssets(assetScreens: Map<String, String>) {
        assetScreens.forEach { (name, assetPath) ->
            register(KetoyScreen.fromAsset(name, assetPath))
        }
    }
}
