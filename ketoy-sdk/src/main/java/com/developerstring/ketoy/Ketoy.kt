package com.developerstring.ketoy

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.registry.KComponentRegistry
import com.developerstring.ketoy.screen.KetoyScreen
import com.developerstring.ketoy.screen.KetoyScreenRegistry
import com.developerstring.ketoy.widget.KetoyActionParser
import com.developerstring.ketoy.widget.KetoyActionRegistry
import com.developerstring.ketoy.widget.KetoyWidgetParser
import com.developerstring.ketoy.widget.KetoyWidgetRegistry
import com.developerstring.ketoy.widget.builtin.NavigateActionParser

/**
 * Main entry point for the Ketoy SDK.
 *
 * Call [Ketoy.initialize] at app startup (e.g. inside your root
 * composable or Application.onCreate) before rendering any
 * server-driven UI.
 *
 * ## Basic usage
 * ```kotlin
 * Ketoy.initialize()
 * ```
 *
 * ## Advanced usage (with custom parsers and screens)
 * ```kotlin
 * Ketoy.initialize(
 *     widgetParsers = listOf(MyBadgeParser(), MyRatingParser()),
 *     actionParsers = listOf(ShowToastParser()),
 *     screens = listOf(
 *         KetoyScreen.fromJson("home", homeJson),
 *         KetoyScreen.fromComposable("profile") { ProfileScreen() }
 *     )
 * )
 * ```
 */
object Ketoy {

    private var isInitialized = false

    /**
     * Initialise the Ketoy SDUI engine.
     *
     * Safe to call multiple times – subsequent calls are no-ops unless
     * [force] is set to true.
     *
     * @param widgetParsers Custom widget parsers to register.
     * @param actionParsers Custom action parsers to register.
     * @param screens       Screens to register for navigation.
     * @param force         If true, reinitialises even if already initialised.
     */
    fun initialize(
        widgetParsers: List<KetoyWidgetParser<*>> = emptyList(),
        actionParsers: List<KetoyActionParser<*>> = emptyList(),
        screens: List<KetoyScreen> = emptyList(),
        force: Boolean = false
    ) {
        if (isInitialized && !force) return

        try {
            // Core registries
            KComponentRegistry.initialize()
            ActionRegistry.clear()
            KetoyVariableRegistry.clear()

            // Custom widget parsers (Stac-like parser system)
            KetoyWidgetRegistry.registerAll(widgetParsers)

            // Custom action parsers
            KetoyActionRegistry.registerAll(actionParsers)

            // Built-in action parsers
            KetoyActionRegistry.register(NavigateActionParser())

            // Screens for navigation
            KetoyScreenRegistry.registerAll(screens)

            isInitialized = true
        } catch (e: Exception) {
            // Swallow – let host app decide on crash policy
            e.printStackTrace()
        }
    }

    /** Whether the SDK has been initialised. */
    fun isInitialized(): Boolean = isInitialized

    /** Reset all internal state – useful for testing. */
    fun reset() {
        KComponentRegistry.reset()
        ActionRegistry.clear()
        KetoyVariableRegistry.clear()
        KetoyWidgetRegistry.clear()
        KetoyActionRegistry.clear()
        KetoyScreenRegistry.clear()
        isInitialized = false
    }
}

/**
 * Legacy alias kept for backwards compatibility.
 * Prefer [Ketoy] directly.
 */
@Deprecated("Use Ketoy object directly", ReplaceWith("Ketoy"))
typealias KetoyInitializer = Ketoy
