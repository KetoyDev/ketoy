package com.developerstring.ketoy.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the navigation styles available in Ketoy's server-driven UI.
 *
 * Uses Android-friendly naming aligned with Jetpack Navigation conventions:
 * - [Navigate]                 – standard forward navigation
 * - [PopBackStack]             – go back one screen
 * - [NavigateAndReplace]       – swap the current screen
 * - [NavigateAndClearBackStack] – new root, clear stack
 * - [PopToRoot]                – pop back to root
 */
@Serializable
enum class NavigationStyle {
    @SerialName("navigate")
    Navigate,

    @SerialName("popBackStack")
    PopBackStack,

    @SerialName("navigateAndReplace")
    NavigateAndReplace,

    @SerialName("navigateAndClearBackStack")
    NavigateAndClearBackStack,

    @SerialName("popToRoot")
    PopToRoot
}

/**
 * Serializable model describing a navigation action from JSON.
 *
 * A navigation action can target:
 * - **routeName** – a registered Ketoy screen route
 * - **widgetJson** – an inline JSON widget tree (rendered on-the-fly)
 * - **assetPath**  – a local asset file containing a JSON screen
 *
 * ## JSON example
 * ```json
 * {
 *   "actionType": "navigate",
 *   "routeName": "detail_screen",
 *   "navigationStyle": "navigate",
 *   "arguments": { "id": "123" }
 * }
 * ```
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
