package com.developerstring.ketoy.core

/**
 * Global registry that maps action IDs to callback lambdas.
 *
 * Used by the DSL layer to capture `onClick` / `onValueChange` lambdas
 * and by the renderer layer to invoke them when the user interacts.
 */
object ActionRegistry {

    private val actions = mutableMapOf<String, () -> Unit>()
    private val textChangeActions = mutableMapOf<String, (String) -> Unit>()
    private var counter = 0

    /* ── Register ────────────────────────────────────── */

    fun register(action: () -> Unit): String {
        val id = "action_${counter++}"
        actions[id] = action
        return id
    }

    fun registerAction(id: String, action: () -> Unit) {
        actions[id] = action
    }

    fun registerTextChange(action: (String) -> Unit): String {
        val id = "textChange_${counter++}"
        textChangeActions[id] = action
        return id
    }

    fun registerTextChange(id: String, action: (String) -> Unit) {
        textChangeActions[id] = action
    }

    /* ── Retrieve / Execute ──────────────────────────── */

    fun get(id: String): (() -> Unit)? = actions[id]

    fun getTextChange(id: String): ((String) -> Unit)? = textChangeActions[id]

    fun execute(id: String) {
        actions[id]?.invoke()
    }

    fun executeTextChange(id: String, value: String) {
        textChangeActions[id]?.invoke(value)
    }

    /* ── Lifecycle ───────────────────────────────────── */

    fun clear() {
        actions.clear()
        textChangeActions.clear()
        counter = 0
    }
}
