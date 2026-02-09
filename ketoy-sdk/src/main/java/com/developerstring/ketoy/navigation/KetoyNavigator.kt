package com.developerstring.ketoy.navigation

/**
 * Static helper for building [KNavigateAction]s from Kotlin code.
 *
 * Provides helpers for both **string routes** (JSON-driven) and
 * **type-safe `@Serializable` routes** (Navigation2 style).
 *
 * ## Type-safe examples
 * ```kotlin
 * @Serializable data object Home : KetoyRoute
 * @Serializable data class Detail(val id: String) : KetoyRoute
 *
 * // These execute directly against the nav controller
 * val nav = LocalKetoyNavController.current
 * nav.navigate(Home)
 * nav.navigate(Detail(id = "42"))
 * ```
 *
 * ## String-route examples (JSON actions)
 * ```kotlin
 * val action = KetoyNavigator.navigateToScreen("detail_screen", mapOf("id" to "42"))
 * val popAction = KetoyNavigator.popBackStack(mapOf("saved" to "true"))
 * val resetAction = KetoyNavigator.navigateAndClearBackStackScreen("home")
 * ```
 *
 * > **Note:** Type-safe route navigation is performed directly via
 * > [KetoyNavController.navigate], [KetoyNavController.navigateAndReplace], etc.
 * > The [KNavigateAction]-based helpers below are designed for the
 * > string-based / JSON-driven navigation pipeline.
 */
object KetoyNavigator {

    // ── Ketoy screen routes (string-based) ──────────────────────

    /** Navigate to a registered Ketoy screen by [routeName]. */
    fun navigateToScreen(
        routeName: String,
        arguments: Map<String, String>? = null
    ) = KNavigateAction(
        routeName = routeName,
        navigationStyle = NavigationStyle.Navigate,
        arguments = arguments
    )

    /** Navigate and replace the current screen with another Ketoy screen. */
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

    /** Navigate to a Ketoy screen and clear the entire back stack. */
    fun navigateAndClearBackStackScreen(
        routeName: String,
        arguments: Map<String, String>? = null
    ) = KNavigateAction(
        routeName = routeName,
        navigationStyle = NavigationStyle.NavigateAndClearBackStack,
        arguments = arguments
    )

    // ── Inline JSON screens ─────────────────────────────────────

    /** Navigate to a screen defined by an inline JSON widget tree. */
    fun navigateToJson(widgetJson: String) = KNavigateAction(
        widgetJson = widgetJson,
        navigationStyle = NavigationStyle.Navigate
    )

    /** Navigate and replace the current screen with an inline JSON screen. */
    fun navigateAndReplaceJson(
        widgetJson: String,
        result: Map<String, String>? = null
    ) = KNavigateAction(
        widgetJson = widgetJson,
        navigationStyle = NavigationStyle.NavigateAndReplace,
        result = result
    )

    /** Navigate to an inline JSON screen and clear the entire stack. */
    fun navigateAndClearBackStackJson(widgetJson: String) = KNavigateAction(
        widgetJson = widgetJson,
        navigationStyle = NavigationStyle.NavigateAndClearBackStack
    )

    // ── Asset-based screens ─────────────────────────────────────

    /** Navigate to a screen from a local JSON asset. */
    fun navigateToAsset(assetPath: String) = KNavigateAction(
        assetPath = assetPath,
        navigationStyle = NavigationStyle.Navigate
    )

    /** Navigate and replace current screen with a JSON asset screen. */
    fun navigateAndReplaceAsset(
        assetPath: String,
        result: Map<String, String>? = null
    ) = KNavigateAction(
        assetPath = assetPath,
        navigationStyle = NavigationStyle.NavigateAndReplace,
        result = result
    )

    /** Navigate to a JSON asset screen and clear the entire stack. */
    fun navigateAndClearBackStackAsset(assetPath: String) = KNavigateAction(
        assetPath = assetPath,
        navigationStyle = NavigationStyle.NavigateAndClearBackStack
    )

    // ── Pop ─────────────────────────────────────────────────────

    /** Pop the current screen from the back stack. */
    fun popBackStack(result: Map<String, String>? = null) = KNavigateAction(
        navigationStyle = NavigationStyle.PopBackStack,
        result = result
    )

    /** Pop all screens back to the root. */
    fun popToRoot() = KNavigateAction(
        navigationStyle = NavigationStyle.PopToRoot
    )
}
