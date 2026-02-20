package com.developerstring.ketoy.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Defines the navigation transition styles available in Ketoy's server-driven UI.
 *
 * These styles map directly to Jetpack Navigation operations and are used
 * in [KNavigateAction] to describe how a navigation should be performed.
 * In JSON, values are serialized as lowercase strings (e.g., `"navigate"`,
 * `"popBackStack"`).
 *
 * ### JSON serialization
 * ```json
 * { "navigationStyle": "navigate" }
 * { "navigationStyle": "popBackStack" }
 * { "navigationStyle": "navigateAndReplace" }
 * { "navigationStyle": "navigateAndClearBackStack" }
 * { "navigationStyle": "popToRoot" }
 * ```
 *
 * @see KNavigateAction
 * @see KetoyNavigationExecutor
 * @see KetoyNavController
 */
@Serializable
enum class NavigationStyle {
    /** Standard forward navigation — pushes a new destination onto the back stack. */
    @SerialName("navigate")
    Navigate,

    /** Pops the current destination and returns to the previous one. */
    @SerialName("popBackStack")
    PopBackStack,

    /** Replaces the current destination with the new one (current is popped first). */
    @SerialName("navigateAndReplace")
    NavigateAndReplace,

    /** Clears the entire back stack and sets the new destination as root. */
    @SerialName("navigateAndClearBackStack")
    NavigateAndClearBackStack,

    /** Pops all destinations back to the root of the navigation graph. */
    @SerialName("popToRoot")
    PopToRoot
}

/**
 * Serializable model describing a navigation action from JSON.
 *
 * A navigation action can target one of three sources (mutually exclusive):
 * - **[routeName]** — a registered Ketoy screen route in [KetoyScreenRegistry][com.developerstring.ketoy.screen.KetoyScreenRegistry]
 * - **[widgetJson]** — an inline JSON widget tree rendered on-the-fly
 * - **[assetPath]** — a local asset file containing a JSON screen definition
 *
 * The [navigationStyle] determines how the navigation is performed (push, replace,
 * clear stack, or pop).
 *
 * ### JSON examples
 *
 * Forward navigation to a registered screen:
 * ```json
 * {
 *   "routeName": "detail_screen",
 *   "navigationStyle": "navigate",
 *   "arguments": { "id": "123", "source": "home" }
 * }
 * ```
 *
 * Replace current screen with an inline JSON widget tree:
 * ```json
 * {
 *   "widgetJson": "{\"type\": \"Column\", \"children\": []}",
 *   "navigationStyle": "navigateAndReplace"
 * }
 * ```
 *
 * Pop back with a result:
 * ```json
 * {
 *   "navigationStyle": "popBackStack",
 *   "result": { "selected": "item_42" }
 * }
 * ```
 *
 * ### Kotlin usage
 * ```kotlin
 * // Typically created via KetoyNavigator factory methods:
 * val action = KetoyNavigator.navigateToScreen("detail", mapOf("id" to "42"))
 *
 * // Or deserialized from JSON:
 * val action = Json.decodeFromString<KNavigateAction>(actionJsonString)
 *
 * // Then executed:
 * KetoyNavigationExecutor.execute(navController, action, context)
 * ```
 *
 * @property routeName       Route name of a registered Ketoy screen (mutually exclusive with
 *                           [widgetJson] and [assetPath]).
 * @property widgetJson      Inline JSON widget tree string (mutually exclusive with
 *                           [routeName] and [assetPath]).
 * @property assetPath       Path to a local JSON asset file (mutually exclusive with
 *                           [routeName] and [widgetJson]).
 * @property navigationStyle The navigation transition style. Defaults to [NavigationStyle.Navigate].
 * @property result          Optional key-value map passed back to the previous screen on pop.
 * @property arguments       Optional key-value map forwarded to the target screen.
 * @see KetoyNavigator
 * @see KetoyNavigationExecutor
 * @see NavigationStyle
 */
@Serializable
data class KNavigateAction(
    val routeName: String? = null,
    val widgetJson: String? = null,
    val assetPath: String? = null,
    val navigationStyle: NavigationStyle = NavigationStyle.Navigate,
    val result: Map<String, String>? = null,
    val arguments: Map<String, String>? = null
)
