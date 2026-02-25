/**
 * Unified onClick / action resolution for the Ketoy SDUI library.
 *
 * Every interactive component in the rendering pipeline (buttons, cards,
 * navigation items, etc.) delegates its `onClick` prop resolution to
 * [OnClickResolver]. This file contains the single [OnClickResolver] object
 * that converts raw JSON elements into executable `() -> Unit` lambdas.
 *
 * @see OnClickResolver
 * @see ActionRegistry
 * @see KetoyActionRegistry
 */
package com.developerstring.ketoy.renderer

import android.content.Context
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.navigation.KetoyNavController
import com.developerstring.ketoy.widget.ActionContext
import com.developerstring.ketoy.widget.KetoyActionRegistry
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Unified action resolution for `onClick` handlers in server-driven UI.
 *
 * All renderers call [resolve] to convert a raw JSON element into a
 * `() -> Unit` callback. Three formats are supported:
 *
 * 1. **Legacy string ID** — from DSL-built UI (e.g. `"action_42"`).
 *    Resolved via [ActionRegistry].
 *
 * 2. **JSON action object** — from the server
 *    (e.g. `{"actionType": "navigate", "route": "/home"}`).
 *    Resolved via [KetoyActionRegistry].
 *
 * 3. **JSON array** — multiple actions executed in sequence.
 *    Each element is resolved individually.
 *
 * ### Thread safety
 * Resolution and execution happen on the calling thread (typically the
 * main/UI thread). Heavy work should be offloaded by the registered
 * action handlers themselves.
 *
 * @see ActionRegistry
 * @see KetoyActionRegistry
 * @see ActionContext
 */
internal object OnClickResolver {

    /**
     * Resolves an `onClick` JSON element into a callback lambda.
     *
     * @param element       The raw `"onClick"` [JsonElement] from the component’s props.
     *                      May be a [JsonPrimitive] (string ID), a [JsonObject]
     *                      (single action), a [JsonArray] (multiple actions), or `null`.
     * @param context       Android [Context] forwarded to the action handler.
     * @param navController Optional [KetoyNavController] for navigation actions.
     *                      When provided, JSON `"navigate"` actions work correctly.
     * @return A lambda that executes the resolved action(s), or `null` if
     *         [element] is `null` or no handler could be found.
     */
    fun resolve(
        element: JsonElement?,
        context: Context,
        navController: KetoyNavController? = null
    ): (() -> Unit)? {
        if (element == null) return null

        return when (element) {
            is JsonPrimitive -> {
                val id = element.content
                // Legacy: look up the registered lambda by ID
                val action: () -> Unit = { ActionRegistry.get(id)?.invoke() }
                action
            }

            is JsonObject -> {
                resolveJsonAction(element, context, navController)
            }

            is JsonArray -> {
                val callbacks = element.mapNotNull { el ->
                    if (el is JsonObject) resolveJsonAction(el, context, navController) else null
                }
                if (callbacks.isNotEmpty()) {
                    val action: () -> Unit = { callbacks.forEach { it.invoke() } }
                    action
                } else {
                    null
                }
            }

            else -> null
        }
    }

    /**
     * Resolves a single JSON action object into a callback lambda.
     *
     * Reads the `actionType` key, looks up the corresponding parser in
     * [KetoyActionRegistry], deserialises the model, and wraps the
     * [KetoyActionParser.onCall] invocation in a try/catch so that a
     * failing action does not crash the host application.
     *
     * @param json          The action JSON object.
     * @param context       Android [Context] for execution.
     * @param navController Optional [KetoyNavController] for navigation.
     * @return A callback, or `null` if `actionType` is missing or unregistered.
     */
    @Suppress("UNCHECKED_CAST")
    private fun resolveJsonAction(
        json: JsonObject,
        context: Context,
        navController: KetoyNavController?
    ): (() -> Unit)? {
        val actionType = json["actionType"]?.jsonPrimitive?.content ?: return null
        val parser = KetoyActionRegistry.get<Any>(actionType) ?: return null

        return {
            try {
                val model = parser.getModel(json)
                parser.onCall(
                    model,
                    ActionContext(
                        androidContext = context,
                        navController = navController
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("OnClickResolver", "Error in '$actionType': ${e.message}", e)
            }
        }
    }
}
