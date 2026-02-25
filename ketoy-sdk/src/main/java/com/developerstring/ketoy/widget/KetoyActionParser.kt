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
     *
     * Implementations should extract the action-specific fields from [json]
     * and construct the typed model. The surrounding `"actionType"` key is
     * already consumed by the framework before this method is called.
     *
     * ### Example JSON input
     * ```json
     * { "actionType": "showToast", "message": "Item added!" }
     * ```
     *
     * @param json The JSON object containing action-specific properties.
     * @return A fully initialised action model of type [T].
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
 * Context object passed to [KetoyActionParser] implementations during
 * action execution.
 *
 * [ActionContext] centralises the runtime dependencies that actions may
 * need — Android system services, the active navigation controller, etc.
 * The framework constructs this object automatically; custom parsers
 * receive it in [KetoyActionParser.onCall].
 *
 * ### Example — accessing context in a custom action
 * ```kotlin
 * override fun onCall(model: MyModel, context: ActionContext) {
 *     Toast.makeText(context.androidContext, model.message, Toast.LENGTH_SHORT).show()
 *     context.navController?.navigate("result_screen")
 * }
 * ```
 *
 * @property androidContext The Android [android.content.Context] of the host Activity or Fragment.
 *                          Use it for toasts, starting activities, accessing system services, etc.
 * @property navController  The active [KetoyNavController][com.developerstring.ketoy.navigation.KetoyNavController]
 *                          for navigation actions. May be `null` if the action is triggered
 *                          outside a navigation-enabled scope.
 * @see KetoyActionParser
 * @see KetoyActionRegistry
 */
data class ActionContext(
    val androidContext: android.content.Context,
    val navController: com.developerstring.ketoy.navigation.KetoyNavController? = null
)
