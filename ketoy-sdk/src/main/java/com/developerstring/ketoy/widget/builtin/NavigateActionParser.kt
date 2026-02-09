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

    override val actionType: String = "navigate"

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

    override fun onCall(model: KNavigateAction, context: ActionContext) {
        val navController = context.navController ?: return
        KetoyNavigationExecutor.execute(
            navController = navController,
            action = model,
            context = context.androidContext
        )
    }

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
