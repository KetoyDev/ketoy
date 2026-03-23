package com.developerstring.ketoy.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import kotlinx.serialization.json.Json

/**
 * Executes [KNavigateAction] instances against a [KetoyNavController].
 *
 * This is the bridge between JSON-driven navigation actions and the actual
 * Jetpack Compose Navigation runtime. The executor handles all target types:
 *
 * | Target         | Description                                               |
 * |----------------|-----------------------------------------------------------|
 * | `routeName`    | Navigates to a registered Ketoy screen by route name      |
 * | `widgetJson`   | Registers a temporary inline JSON screen and navigates    |
 * | `assetPath`    | Loads a JSON asset, registers it, and navigates           |
 * | *(pop)*        | Pops the back stack or resets to root                     |
 *
 * ### Usage from a JSON action handler
 * ```kotlin
 * // Decode and execute a navigation action from JSON
 * val actionJson = """{ "routeName": "detail", "navigationStyle": "navigate" }"""
 * KetoyNavigationExecutor.executeFromJson(navController, actionJson, context)
 *
 * // Or execute a pre-built KNavigateAction
 * val action = KetoyNavigator.navigateToScreen("detail", mapOf("id" to "42"))
 * KetoyNavigationExecutor.execute(navController, action, context)
 * ```
 *
 * @see KNavigateAction
 * @see KetoyNavigator
 * @see KetoyNavController
 * @see NavigationStyle
 */
object KetoyNavigationExecutor {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Execute a [KNavigateAction] using the given [navController].
     *
     * Dispatches to the appropriate navigation method based on the action's
     * [NavigationStyle] and target type ([KNavigateAction.routeName],
     * [KNavigateAction.widgetJson], or [KNavigateAction.assetPath]).
     *
     * For inline JSON and asset-based screens, a temporary screen is registered
     * in [KetoyScreenRegistry][com.developerstring.ketoy.screen.KetoyScreenRegistry]
     * with a unique timestamped route before navigating.
     *
     * @param navController The [KetoyNavController] to perform navigation on.
     * @param action        The [KNavigateAction] describing what navigation to perform.
     * @param context       Android [Context] (required for asset loading; can be `null`
     *                      if the action does not use [KNavigateAction.assetPath]).
     * @see executeFromJson
     * @see KNavigateAction
     */
    fun execute(
        navController: KetoyNavController,
        action: KNavigateAction,
        context: Context? = null
    ) {
        when (action.navigationStyle) {
            NavigationStyle.PopBackStack -> {
                @Suppress("UNCHECKED_CAST")
                navController.popBackStack(action.result as? Map<String, Any>)
            }

            NavigationStyle.PopToRoot -> {
                navController.popToRoot()
            }

            NavigationStyle.Navigate -> {
                when {
                    action.routeName != null -> {
                        @Suppress("UNCHECKED_CAST")
                        navController.navigateToRoute(
                            action.routeName,
                            action.arguments as? Map<String, Any>
                        )
                    }
                    action.widgetJson != null -> {
                        // Register a temporary inline-JSON screen and navigate to it
                        val tempRoute = "ketoy_inline_${System.currentTimeMillis()}"
                        registerInlineJsonScreen(tempRoute, action.widgetJson)
                        navController.navigateToRoute(tempRoute)
                    }
                    action.assetPath != null && context != null -> {
                        val assetJson = loadAsset(context, action.assetPath)
                        if (assetJson != null) {
                            val tempRoute = "ketoy_asset_${System.currentTimeMillis()}"
                            registerInlineJsonScreen(tempRoute, assetJson)
                            navController.navigateToRoute(tempRoute)
                        }
                    }
                }
            }

            NavigationStyle.NavigateAndReplace -> {
                when {
                    action.routeName != null -> {
                        @Suppress("UNCHECKED_CAST")
                        navController.navigateAndReplaceRoute(
                            action.routeName,
                            action.result as? Map<String, Any>,
                            action.arguments as? Map<String, Any>
                        )
                    }
                    action.widgetJson != null -> {
                        val tempRoute = "ketoy_inline_${System.currentTimeMillis()}"
                        registerInlineJsonScreen(tempRoute, action.widgetJson)
                        @Suppress("UNCHECKED_CAST")
                        navController.navigateAndReplaceRoute(
                            tempRoute,
                            action.result as? Map<String, Any>
                        )
                    }
                    action.assetPath != null && context != null -> {
                        val assetJson = loadAsset(context, action.assetPath)
                        if (assetJson != null) {
                            val tempRoute = "ketoy_asset_${System.currentTimeMillis()}"
                            registerInlineJsonScreen(tempRoute, assetJson)
                            @Suppress("UNCHECKED_CAST")
                            navController.navigateAndReplaceRoute(
                                tempRoute,
                                action.result as? Map<String, Any>
                            )
                        }
                    }
                }
            }

            NavigationStyle.NavigateAndClearBackStack -> {
                when {
                    action.routeName != null -> {
                        @Suppress("UNCHECKED_CAST")
                        navController.navigateAndClearBackStackRoute(
                            action.routeName,
                            action.arguments as? Map<String, Any>
                        )
                    }
                    action.widgetJson != null -> {
                        val tempRoute = "ketoy_inline_${System.currentTimeMillis()}"
                        registerInlineJsonScreen(tempRoute, action.widgetJson)
                        navController.navigateAndClearBackStackRoute(tempRoute)
                    }
                    action.assetPath != null && context != null -> {
                        val assetJson = loadAsset(context, action.assetPath)
                        if (assetJson != null) {
                            val tempRoute = "ketoy_asset_${System.currentTimeMillis()}"
                            registerInlineJsonScreen(tempRoute, assetJson)
                            navController.navigateAndClearBackStackRoute(tempRoute)
                        }
                    }
                }
            }
        }
    }

    /**
     * Deserialize a JSON string into a [KNavigateAction] and execute it.
     *
     * This is a convenience method that combines JSON parsing and execution
     * in a single call. Unknown JSON keys are silently ignored.
     *
     * ### Example
     * ```kotlin
     * val json = """{
     *   "routeName": "profile",
     *   "navigationStyle": "navigate",
     *   "arguments": { "userId": "abc123" }
     * }"""
     * KetoyNavigationExecutor.executeFromJson(navController, json, context)
     * ```
     *
     * @param navController The [KetoyNavController] to perform navigation on.
     * @param actionJson    A JSON string conforming to the [KNavigateAction] schema.
     * @param context       Android [Context] (required for asset loading; can be `null`).
     * @see execute
     */
    fun executeFromJson(
        navController: KetoyNavController,
        actionJson: String,
        context: Context? = null
    ) {
        val action = json.decodeFromString<KNavigateAction>(actionJson)
        execute(navController, action, context)
    }

    // ── Private helpers ─────────────────────────────────────────

    private fun registerInlineJsonScreen(route: String, jsonContent: String) {
        com.developerstring.ketoy.screen.KetoyScreenRegistry.register(
            com.developerstring.ketoy.screen.KetoyScreen.fromJson(
                screenName = route,
                json = jsonContent
            )
        )
    }

    private fun loadAsset(context: Context, assetPath: String): String? {
        return try {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
