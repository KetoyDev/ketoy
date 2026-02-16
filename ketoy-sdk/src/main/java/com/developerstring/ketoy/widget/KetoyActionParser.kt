package com.developerstring.ketoy.widget

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.JsonObject

/**
 * Base interface for Ketoy action parsers.
 *
 * An action parser handles a specific type of JSON action (e.g. navigate,
 * showDialog, apiCall).
 *
 * ## Creating a custom action parser
 * ```kotlin
 * class ShowToastActionParser : KetoyActionParser<ShowToastModel> {
 *     override val actionType = "showToast"
 *
 *     override fun getModel(json: JsonObject): ShowToastModel {
 *         return ShowToastModel(
 *             message = json["message"]?.jsonPrimitive?.content ?: ""
 *         )
 *     }
 *
 *     override fun onCall(model: ShowToastModel, context: ActionContext) {
 *         Toast.makeText(context.androidContext, model.message, Toast.LENGTH_SHORT).show()
 *     }
 * }
 * ```
 *
 * @param T The action model type.
 */
interface KetoyActionParser<T> {

    /**
     * Unique action type identifier. Must match `"actionType"` in JSON.
     */
    val actionType: String

    /**
     * Deserialise JSON into the action model.
     */
    fun getModel(json: JsonObject): T

    /**
     * Execute the action.
     *
     * @param model   The parsed action model.
     * @param context Provides access to Android context, nav controller, etc.
     */
    fun onCall(model: T, context: ActionContext)
}

/**
 * Context object passed to action parsers during execution.
 *
 * Provides access to:
 * - Android context (for toasts, system services, etc.)
 * - Ketoy nav controller (for navigation actions)
 * - The current composable scope's recomposition trigger
 */
data class ActionContext(
    val androidContext: android.content.Context,
    val navController: com.developerstring.ketoy.navigation.KetoyNavController? = null
)
