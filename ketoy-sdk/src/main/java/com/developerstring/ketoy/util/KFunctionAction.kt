package com.developerstring.ketoy.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject

/**
 * Builds a `callFunction` JSON action object for use in `onClickAction`.
 *
 * The returned [JsonElement] is stored directly in widget props and
 * serialised into the exported JSON. At render time,
 * [OnClickResolver][com.developerstring.ketoy.renderer.OnClickResolver]
 * detects the [JsonObject] and delegates to
 * [CallFunctionActionParser][com.developerstring.ketoy.widget.builtin.CallFunctionActionParser],
 * which calls [KetoyFunctionRegistry][com.developerstring.ketoy.registry.KetoyFunctionRegistry].
 *
 * ### DSL usage
 * ```kotlin
 * KButton(onClickAction = KFunctionAction("showToast", "message" to "Hello!")) {
 *     KText("Show Toast")
 * }
 * ```
 *
 * ### Resulting JSON
 * ```json
 * {
 *   "onClick": {
 *     "actionType": "callFunction",
 *     "functionName": "showToast",
 *     "arguments": { "message": "Hello!" }
 *   }
 * }
 * ```
 *
 * @param functionName The name registered in [KetoyFunctionRegistry].
 * @param arguments    Key-value pairs passed as function arguments.
 * @return A [JsonElement] representing the action.
 */
fun KFunctionAction(
    functionName: String,
    vararg arguments: Pair<String, Any>
): JsonElement = buildJsonObject {
    put("actionType", JsonPrimitive("callFunction"))
    put("functionName", JsonPrimitive(functionName))
    if (arguments.isNotEmpty()) {
        putJsonObject("arguments") {
            arguments.forEach { (k, v) ->
                when (v) {
                    is String -> put(k, JsonPrimitive(v))
                    is Int -> put(k, JsonPrimitive(v))
                    is Long -> put(k, JsonPrimitive(v))
                    is Float -> put(k, JsonPrimitive(v))
                    is Double -> put(k, JsonPrimitive(v))
                    is Boolean -> put(k, JsonPrimitive(v))
                    else -> put(k, JsonPrimitive(v.toString()))
                }
            }
        }
    }
}
