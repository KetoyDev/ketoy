package com.developerstring.ketoy

import android.content.Context
import com.developerstring.ketoy.cloud.KetoyCloudConfig
import com.developerstring.ketoy.cloud.KetoyCloudService
import com.developerstring.ketoy.cloud.cache.KetoyCacheConfig
import com.developerstring.ketoy.cloud.cache.KetoyCacheStore
import com.developerstring.ketoy.cloud.network.KetoyApiClient
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.navigation.KetoyCloudNavOverrides
import com.developerstring.ketoy.registry.KComponentRegistry
import com.developerstring.ketoy.registry.KetoyFunctionRegistry
import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy.screen.KetoyScreenRegistry
import com.developerstring.ketoy.widget.KetoyActionParser
import com.developerstring.ketoy.widget.KetoyActionRegistry
import com.developerstring.ketoy.widget.KetoyWidgetParser
import com.developerstring.ketoy.widget.KetoyWidgetRegistry
import com.developerstring.ketoy.widget.builtin.CallFunctionActionParser
import com.developerstring.ketoy.widget.builtin.NavigateActionParser

/**
 * Main entry point for the Ketoy SDK.
 *
 * Call [Ketoy.initialize] at app startup (e.g. inside your root
 * composable or Application.onCreate) before rendering any
 * server-driven UI.
 *
 * ## Basic usage (local-only)
 * ```kotlin
 * Ketoy.initialize()
 * ```
 *
 * ## Cloud-enabled SDUI
 * ```kotlin
 * Ketoy.initialize(
 *     context = applicationContext,
 *     cloudConfig = KetoyCloudConfig(
 *         apiKey = "your-api-key",
 *         packageName = "com.example.myapp",
 *         baseUrl = "https://api.ketoy.dev"
 *     ),
 *     cacheConfig = KetoyCacheConfig(
 *         strategy = KetoyCacheStrategy.NETWORK_FIRST,
 *         maxAge = 30.days,
 *     )
 * )
 * ```
 *
 * ## Advanced usage (with custom parsers and screens)
 * ```kotlin
 * Ketoy.initialize(
 *     context = applicationContext,
 *     widgetParsers = listOf(MyBadgeParser(), MyRatingParser()),
 *     actionParsers = listOf(ShowToastParser()),
 *     screens = listOf(
 *         KetoyScreen.fromJson("home", homeJson),
 *         KetoyScreen.fromNode("profile") { buildProfileUI() }
 *     ),
 *     cloudConfig = KetoyCloudConfig(
 *         apiKey = "your-api-key",
 *         packageName = "com.example.myapp"
 *     )
 * )
 * ```
 */
object Ketoy {

    private var isInitialized = false
    private var _cloudConfig: KetoyCloudConfig? = null

    /** The current cloud configuration, or null if cloud is not configured. */
    val cloudConfig: KetoyCloudConfig? get() = _cloudConfig

    /**
     * Initialise the Ketoy SDUI engine.
     *
     * Safe to call multiple times – subsequent calls are no-ops unless
     * [force] is set to true.
     *
     * @param context       Android context (required for cloud/caching features).
     *                      Pass `applicationContext` to avoid leaks.
     * @param widgetParsers Custom widget parsers to register.
     * @param actionParsers Custom action parsers to register.
     * @param screens       Screens to register for navigation.
     * @param cloudConfig   Configuration for connecting to the Ketoy server.
     *                      When provided, enables server-driven screen fetching
     *                      via [KetoyCloudScreen] and cache management.
     * @param cacheConfig   Caching strategy for server-driven screens.
     *                      Defaults to [KetoyCacheConfig.DEFAULT] (network-first,
     *                      30-day max age). Only used when [cloudConfig] is set.
     * @param force         If true, reinitialises even if already initialised.
     */
    fun initialize(
        context: Context? = null,
        widgetParsers: List<KetoyWidgetParser<*>> = emptyList(),
        actionParsers: List<KetoyActionParser<*>> = emptyList(),
        screens: List<KetoyScreen> = emptyList(),
        cloudConfig: KetoyCloudConfig? = null,
        cacheConfig: KetoyCacheConfig = KetoyCacheConfig.DEFAULT,
        force: Boolean = false
    ) {
        if (isInitialized && !force) return

        try {
            // Core registries
            KComponentRegistry.initialize()
            ActionRegistry.clear()
            KetoyVariableRegistry.clear()
            KetoyFunctionRegistry.clear()

            // Custom widget parsers
            KetoyWidgetRegistry.registerAll(widgetParsers)

            // Custom action parsers
            KetoyActionRegistry.registerAll(actionParsers)

            // Built-in action parsers
            KetoyActionRegistry.register(NavigateActionParser())
            KetoyActionRegistry.register(CallFunctionActionParser())

            // Screens for navigation
            KetoyScreenRegistry.registerAll(screens)

            // Cloud / server-driven UI setup
            if (cloudConfig != null) {
                _cloudConfig = cloudConfig
                KetoyApiClient.initialize(cloudConfig)
                KetoyCloudService.cacheConfig = cacheConfig

                // Initialize cache store (requires context)
                val ctx = context
                    ?: throw IllegalArgumentException(
                        "Android context is required when cloudConfig is provided. " +
                        "Pass applicationContext to Ketoy.initialize()."
                    )
                KetoyCacheStore.initialize(ctx)
            }

            isInitialized = true
        } catch (e: Exception) {
            // Swallow – let host app decide on crash policy
            e.printStackTrace()
        }
    }

    /** Whether the SDK has been initialised. */
    fun isInitialized(): Boolean = isInitialized

    /** Whether cloud features are configured and available. */
    fun isCloudEnabled(): Boolean = _cloudConfig != null

    /** Reset all internal state – useful for testing. */
    fun reset() {
        KComponentRegistry.reset()
        ActionRegistry.clear()
        KetoyVariableRegistry.clear()
        KetoyFunctionRegistry.clear()
        KetoyWidgetRegistry.clear()
        KetoyActionRegistry.clear()
        KetoyScreenRegistry.clear()
        KetoyCloudNavOverrides.clearAll()
        _cloudConfig = null
        isInitialized = false
    }
}

/**
 * Legacy alias kept for backwards compatibility.
 * Prefer [Ketoy] directly.
 */
@Deprecated("Use Ketoy object directly", ReplaceWith("Ketoy"))
typealias KetoyInitializer = Ketoy
