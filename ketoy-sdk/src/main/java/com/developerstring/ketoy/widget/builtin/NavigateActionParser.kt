package com.developerstring.ketoy.widget.builtin

import com.developerstring.ketoy.navigation.KNavigateAction
import com.developerstring.ketoy.navigation.KetoyNavigationExecutor
import com.developerstring.ketoy.navigation.NavigationStyle
import com.developerstring.ketoy.widget.ActionContext
import com.developerstring.ketoy.widget.KetoyActionParser
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Built-in action parser for the `"navigate"` action type.
 *
 * Handles JSON actions like:
 * ```json
 * {
 *   "actionType": "navigate",
 *   "routeName": "detail_screen",
 *   "navigationStyle": "push",
 *   "arguments": { "id": "123" }
 * }
 * ```
 *
 * This parser bridges the JSON action system with the
 * [KetoyNavigationExecutor], which performs the actual navigation.
 */
class NavigateActionParser : KetoyActionParser<KNavigateAction> {

    /**
     * The action type identifier: `"navigate"`.
     */
    override val actionType: String = "navigate"

    /**
     * Parse a JSON action object into a [KNavigateAction] model.
     *
     * Supported JSON fields:
     * | Field             | Type     | Description                                         |
     * |-------------------|----------|-----------------------------------------------------|
     * | `routeName`       | `String` | Target route name (e.g. `"detail_screen"`)           |
     * | `widgetJson`      | `String` | Inline widget JSON to render at the destination      |
     * | `assetPath`       | `String` | Asset path for the destination’s JSON file           |
     * | `navigationStyle` | `String` | One of `push`, `pop`, `pushReplacement`, etc.        |
     * | `result`          | `Object` | Key-value pairs returned to the previous screen      |
     * | `arguments`       | `Object` | Key-value arguments passed to the destination screen |
     *
     * @param json The JSON object representing a `"navigate"` action.
     * @return A fully populated [KNavigateAction].
     */
    override fun getModel(json: JsonObject): KNavigateAction {
        return KNavigateAction(
            routeName = json["routeName"]?.jsonPrimitive?.content,
            widgetJson = json["widgetJson"]?.jsonPrimitive?.content,
            assetPath = json["assetPath"]?.jsonPrimitive?.content,
            navigationStyle = json["navigationStyle"]?.jsonPrimitive?.content
                ?.let { parseNavigationStyle(it) }
                ?: NavigationStyle.Navigate,
            result = json["result"]?.jsonObject?.mapValues { (_, v) ->
                v.jsonPrimitive.content
            },
            arguments = json["arguments"]?.jsonObject?.mapValues { (_, v) ->
                v.jsonPrimitive.content
            }
        )
    }

    /**
     * Execute the navigation action via [KetoyNavigationExecutor].
     *
     * Requires a non-null [ActionContext.navController]; if the nav
     * controller is absent the action is silently ignored.
     *
     * @param model   The [KNavigateAction] containing route and arguments.
     * @param context The runtime [ActionContext] with the active nav controller.
     */
    override fun onCall(model: KNavigateAction, context: ActionContext) {
        val navController = context.navController ?: return
        KetoyNavigationExecutor.execute(
            navController = navController,
            action = model,
            context = context.androidContext
        )
    }

    /**
     * Convert a raw JSON string into a [NavigationStyle] enum value.
     *
     * Recognised values (case-insensitive):
     * - `"navigate"` / `"push"` → [NavigationStyle.Navigate]
     * - `"popbackstack"` / `"pop"` → [NavigationStyle.PopBackStack]
     * - `"navigateandreplace"` / `"pushreplacement"` → [NavigationStyle.NavigateAndReplace]
     * - `"navigateandclearbackstack"` / `"pushandremoveall"` → [NavigationStyle.NavigateAndClearBackStack]
     * - `"poptoroot"` / `"popall"` → [NavigationStyle.PopToRoot]
     *
     * Unrecognised values default to [NavigationStyle.Navigate].
     *
     * @param value The raw navigation style string from JSON.
     * @return The corresponding [NavigationStyle].
     */
    private fun parseNavigationStyle(value: String): NavigationStyle {
        return when (value.lowercase()) {
            "navigate", "push" -> NavigationStyle.Navigate
            "popbackstack", "pop" -> NavigationStyle.PopBackStack
            "navigateandreplace", "pushreplacement" -> NavigationStyle.NavigateAndReplace
            "navigateandclearbackstack", "pushandremoveall" -> NavigationStyle.NavigateAndClearBackStack
            "poptoroot", "popall" -> NavigationStyle.PopToRoot
            else -> NavigationStyle.Navigate
        }
    }
}
