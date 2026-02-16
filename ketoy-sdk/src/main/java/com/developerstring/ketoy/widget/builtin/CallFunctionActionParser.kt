package com.developerstring.ketoy.widget.builtin

import com.developerstring.ketoy.registry.KetoyFunctionRegistry
import com.developerstring.ketoy.widget.ActionContext
import com.developerstring.ketoy.widget.KetoyActionParser
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Built-in action parser for the `"callFunction"` action type.
 *
 * Bridges JSON actions to [KetoyFunctionRegistry] — enabling
 * server-driven UI to invoke app-side Kotlin functions by name.
 *
 * ## JSON format
 * ```json
 * {
 *     "actionType": "callFunction",
 *     "functionName": "addToCart",
 *     "arguments": {
 *         "productId": "SKU-12345",
 *         "quantity": 2
 *     }
 * }
 * ```
 *
 * Arguments are automatically converted from JSON primitives to their
 * Kotlin types: `String`, `Boolean`, `Int`, `Float`, `Double`.
 */
class CallFunctionActionParser : KetoyActionParser<CallFunctionAction> {

    override val actionType: String = "callFunction"

    override fun getModel(json: JsonObject): CallFunctionAction {
        val functionName = json["functionName"]?.jsonPrimitive?.content ?: ""
        val argsJson = json["arguments"]?.jsonObject
        val arguments = mutableMapOf<String, Any>()

        argsJson?.forEach { (key, value) ->
            if (value is JsonPrimitive) {
                when {
                    value.isString -> arguments[key] = value.content
                    value.booleanOrNull != null -> arguments[key] = value.booleanOrNull!!
                    value.intOrNull != null -> arguments[key] = value.intOrNull!!
                    value.floatOrNull != null -> arguments[key] = value.floatOrNull!!
                    value.doubleOrNull != null -> arguments[key] = value.doubleOrNull!!
                    else -> arguments[key] = value.content
                }
            } else {
                arguments[key] = value.toString()
            }
        }

        return CallFunctionAction(
            functionName = functionName,
            arguments = arguments
        )
    }

    override fun onCall(model: CallFunctionAction, context: ActionContext) {
        KetoyFunctionRegistry.call(model.functionName, model.arguments)
    }
}

/**
 * Model for a `callFunction` action.
 */
data class CallFunctionAction(
    val functionName: String,
    val arguments: Map<String, Any> = emptyMap()
)
