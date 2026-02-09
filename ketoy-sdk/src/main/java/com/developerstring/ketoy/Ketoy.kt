package com.developerstring.ketoy

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.registry.KComponentRegistry

/**
 * Main entry point for the Ketoy SDK.
 *
 * Call [Ketoy.initialize] at app startup (e.g. inside your root
 * composable or Application.onCreate) before rendering any
 * server-driven UI.
 */
object Ketoy {

    private var isInitialized = false

    /**
     * Initialise the Ketoy SDUI engine.
     *
     * Safe to call multiple times – subsequent calls are no-ops.
     */
    fun initialize() {
        if (isInitialized) return

        try {
            KComponentRegistry.initialize()
            ActionRegistry.clear()
            KetoyVariableRegistry.clear()
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
        isInitialized = false
    }
}

/**
 * Legacy alias kept for backwards compatibility.
 * Prefer [Ketoy] directly.
 */
@Deprecated("Use Ketoy object directly", ReplaceWith("Ketoy"))
typealias KetoyInitializer = Ketoy
