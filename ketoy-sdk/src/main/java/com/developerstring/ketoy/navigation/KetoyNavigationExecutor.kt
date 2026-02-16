package com.developerstring.ketoy.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import com.developerstring.ketoy.renderer.JSONStringToUI
import kotlinx.serialization.json.Json

/**
 * Executes [KNavigateAction]s against a [KetoyNavController].
 *
 * This is the bridge between JSON-driven navigation actions and the
 * actual Compose Navigation runtime. It handles:
 * - Route-based navigation (Ketoy screens)
 * - Inline JSON screen rendering (push a JSON widget tree)
 * - Asset-based screen rendering (push from a local JSON asset)
 * - Pop and popAll
 *
 * ## Usage (from a JSON action handler)
 * ```kotlin
 * val action = Json.decodeFromString<KNavigateAction>(actionJson)
 * KetoyNavigationExecutor.execute(navController, action, context)
 * ```
 */
object KetoyNavigationExecutor {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Execute a [KNavigateAction] using the given [navController].
     *
     * @param navController The Ketoy nav controller to perform navigation on.
     * @param action        The navigation action to execute.
     * @param context       Android context (needed for asset loading).
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
     * Parse a JSON string into a [KNavigateAction] and execute it.
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
