package com.developerstring.ketoy.navigation

/**
 * Static factory for building [KNavigateAction] instances from Kotlin code.
 *
 * Provides factory methods for creating navigation actions targeting:
 * - **Ketoy screen routes** — registered screens by route name
 * - **Inline JSON screens** — on-the-fly screens from a JSON widget tree
 * - **Asset-based screens** — screens loaded from local JSON asset files
 * - **Pop operations** — back navigation and root reset
 *
 * These [KNavigateAction] objects are used by the JSON-driven navigation
 * pipeline. For direct navigation, use [KetoyNavController] instead.
 *
 * ### Type-safe navigation (direct, not via KNavigateAction)
 * ```kotlin
 * @Serializable data object Home : KetoyRoute
 * @Serializable data class Detail(val id: String) : KetoyRoute
 *
 * val nav = LocalKetoyNavController.current
 * nav?.navigate(Home)
 * nav?.navigate(Detail(id = "42"))
 * ```
 *
 * ### String-route navigation (via KNavigateAction)
 * ```kotlin
 * val action = KetoyNavigator.navigateToScreen("detail_screen", mapOf("id" to "42"))
 * KetoyNavigationExecutor.execute(navController, action, context)
 *
 * val popAction = KetoyNavigator.popBackStack(mapOf("saved" to "true"))
 * val resetAction = KetoyNavigator.navigateAndClearBackStackScreen("home")
 * ```
 *
 * > **Note:** Type-safe route navigation is performed directly via
 * > [KetoyNavController.navigate], [KetoyNavController.navigateAndReplace], etc.
 * > The [KNavigateAction]-based helpers below are designed for the
 * > string-based / JSON-driven navigation pipeline.
 *
 * @see KNavigateAction
 * @see KetoyNavigationExecutor
 * @see KetoyNavController
 * @see NavigationStyle
 */
object KetoyNavigator {

    // ── Ketoy screen routes (string-based) ──────────────────────

    /**
     * Creates a [KNavigateAction] that navigates to a registered Ketoy screen.
     *
     * The resulting action uses [NavigationStyle.Navigate] (standard forward push).
     *
     * ### Example
     * ```kotlin
     * val action = KetoyNavigator.navigateToScreen(
     *     routeName = "detail_screen",
     *     arguments = mapOf("id" to "42", "source" to "search")
     * )
     * KetoyNavigationExecutor.execute(nav, action, context)
     * ```
     *
     * @param routeName The route name of the target Ketoy screen.
     * @param arguments Optional key-value arguments passed to the target screen.
     * @return A [KNavigateAction] configured for forward navigation.
     * @see KetoyNavigationExecutor.execute
     */
    fun navigateToScreen(
        routeName: String,
        arguments: Map<String, String>? = null
    ) = KNavigateAction(
        routeName = routeName,
        navigationStyle = NavigationStyle.Navigate,
        arguments = arguments
    )

    /**
     * Creates a [KNavigateAction] that replaces the current screen with another Ketoy screen.
     *
     * The current destination is popped before navigating. Optionally passes a
     * [result] back to the previous screen and/or forwards [arguments] to the target.
     *
     * ### Example
     * ```kotlin
     * val action = KetoyNavigator.navigateAndReplaceScreen(
     *     routeName = "checkout",
     *     result = mapOf("status" to "confirmed"),
     *     arguments = mapOf("step" to "payment")
     * )
     * ```
     *
     * @param routeName The route name of the target Ketoy screen.
     * @param result    Optional result map passed back to the previous screen.
     * @param arguments Optional key-value arguments passed to the target screen.
     * @return A [KNavigateAction] configured for replace navigation.
     */
    fun navigateAndReplaceScreen(
        routeName: String,
        result: Map<String, String>? = null,
        arguments: Map<String, String>? = null
    ) = KNavigateAction(
        routeName = routeName,
        navigationStyle = NavigationStyle.NavigateAndReplace,
        result = result,
        arguments = arguments
    )

    /**
     * Creates a [KNavigateAction] that navigates to a Ketoy screen and clears the entire back stack.
     *
     * The target screen becomes the new root of the navigation stack.
     *
     * ### Example
     * ```kotlin
     * // After logout, reset to login screen
     * val action = KetoyNavigator.navigateAndClearBackStackScreen("login")
     * ```
     *
     * @param routeName The route name of the target Ketoy screen.
     * @param arguments Optional key-value arguments passed to the target screen.
     * @return A [KNavigateAction] configured for clear-stack navigation.
     */
    fun navigateAndClearBackStackScreen(
        routeName: String,
        arguments: Map<String, String>? = null
    ) = KNavigateAction(
        routeName = routeName,
        navigationStyle = NavigationStyle.NavigateAndClearBackStack,
        arguments = arguments
    )

    // ── Inline JSON screens ─────────────────────────────────────

    /**
     * Creates a [KNavigateAction] that navigates to a screen defined by an inline JSON widget tree.
     *
     * The JSON string is registered as a temporary [KetoyScreen][com.developerstring.ketoy.screen.KetoyScreen]
     * and rendered on-the-fly. Useful for fully dynamic screens pushed from a server.
     *
     * ### Example
     * ```kotlin
     * val json = """{ "type": "Column", "children": [{"type": "Text", "text": "Hello!"}] }"""
     * val action = KetoyNavigator.navigateToJson(json)
     * ```
     *
     * @param widgetJson A JSON string representing a Ketoy widget tree.
     * @return A [KNavigateAction] configured for inline JSON navigation.
     */
    fun navigateToJson(widgetJson: String) = KNavigateAction(
        widgetJson = widgetJson,
        navigationStyle = NavigationStyle.Navigate
    )

    /**
     * Creates a [KNavigateAction] that replaces the current screen with an inline JSON screen.
     *
     * @param widgetJson A JSON string representing a Ketoy widget tree.
     * @param result     Optional result map passed back to the previous screen.
     * @return A [KNavigateAction] configured for replace navigation with inline JSON.
     */
    fun navigateAndReplaceJson(
        widgetJson: String,
        result: Map<String, String>? = null
    ) = KNavigateAction(
        widgetJson = widgetJson,
        navigationStyle = NavigationStyle.NavigateAndReplace,
        result = result
    )

    /**
     * Creates a [KNavigateAction] that navigates to an inline JSON screen and clears the stack.
     *
     * @param widgetJson A JSON string representing a Ketoy widget tree.
     * @return A [KNavigateAction] configured for clear-stack navigation with inline JSON.
     */
    fun navigateAndClearBackStackJson(widgetJson: String) = KNavigateAction(
        widgetJson = widgetJson,
        navigationStyle = NavigationStyle.NavigateAndClearBackStack
    )

    // ── Asset-based screens ─────────────────────────────────────

    /**
     * Creates a [KNavigateAction] that navigates to a screen defined in a local JSON asset file.
     *
     * The asset is loaded at navigation time using the Android [Context].
     *
     * ### Example
     * ```kotlin
     * val action = KetoyNavigator.navigateToAsset("screens/promo_banner.json")
     * KetoyNavigationExecutor.execute(nav, action, context)
     * ```
     *
     * @param assetPath Relative path to the JSON asset file (e.g., `"screens/home.json"`).
     * @return A [KNavigateAction] configured for asset-based navigation.
     */
    fun navigateToAsset(assetPath: String) = KNavigateAction(
        assetPath = assetPath,
        navigationStyle = NavigationStyle.Navigate
    )

    /**
     * Creates a [KNavigateAction] that replaces the current screen with a JSON asset screen.
     *
     * @param assetPath Relative path to the JSON asset file.
     * @param result    Optional result map passed back to the previous screen.
     * @return A [KNavigateAction] configured for replace navigation with an asset screen.
     */
    fun navigateAndReplaceAsset(
        assetPath: String,
        result: Map<String, String>? = null
    ) = KNavigateAction(
        assetPath = assetPath,
        navigationStyle = NavigationStyle.NavigateAndReplace,
        result = result
    )

    /**
     * Creates a [KNavigateAction] that navigates to a JSON asset screen and clears the stack.
     *
     * @param assetPath Relative path to the JSON asset file.
     * @return A [KNavigateAction] configured for clear-stack navigation with an asset screen.
     */
    fun navigateAndClearBackStackAsset(assetPath: String) = KNavigateAction(
        assetPath = assetPath,
        navigationStyle = NavigationStyle.NavigateAndClearBackStack
    )

    // ── Pop ─────────────────────────────────────────────────────

    /**
     * Creates a [KNavigateAction] that pops the current screen from the back stack.
     *
     * Optionally passes a [result] map back to the previous screen.
     *
     * ### Example
     * ```kotlin
     * // Simple pop
     * val action = KetoyNavigator.popBackStack()
     *
     * // Pop with result
     * val action = KetoyNavigator.popBackStack(mapOf("saved" to "true"))
     * ```
     *
     * @param result Optional result map passed back to the previous screen via [SavedStateHandle].
     * @return A [KNavigateAction] configured for pop-back-stack.
     */
    fun popBackStack(result: Map<String, String>? = null) = KNavigateAction(
        navigationStyle = NavigationStyle.PopBackStack,
        result = result
    )

    /**
     * Creates a [KNavigateAction] that pops all screens back to the root.
     *
     * @return A [KNavigateAction] configured for pop-to-root.
     */
    fun popToRoot() = KNavigateAction(
        navigationStyle = NavigationStyle.PopToRoot
    )
}
