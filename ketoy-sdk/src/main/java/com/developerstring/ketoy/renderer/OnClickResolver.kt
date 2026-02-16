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
 * Unified action resolution for onClick handlers in server-driven UI.
 *
 * Supports three formats:
 *
 * 1. **Legacy string ID** – from DSL-built UI (e.g. `"action_42"`)
 *    → resolved via [ActionRegistry]
 *
 * 2. **JSON action object** – from server (e.g. `{"actionType": "navigate", ...}`)
 *    → resolved via [KetoyActionRegistry]
 *
 * 3. **JSON array** – multiple actions in sequence
 *    → each resolved individually
 *
 * Called by all renderers (WidgetRenderer, ScaffoldRenderer, etc.)
 * to create a unified `() -> Unit` callback.
 */
internal object OnClickResolver {

    /**
     * Resolve an `onClick` JSON element into a callback lambda.
     *
     * @param element       The raw `"onClick"` JSON element from component props.
     * @param context       Android context for action execution.
     * @param navController Optional [KetoyNavController] for navigation actions.
     *                      When provided, JSON `"navigate"` actions will work correctly.
     * @return A lambda that executes the action(s), or null if element is null.
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
