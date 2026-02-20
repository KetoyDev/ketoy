package com.developerstring.ketoy.widget

/**
 * Global registry for [KetoyActionParser] instances.
 *
 * When a JSON action is triggered (e.g. via an `"onClick"` handler), the
 * Ketoy action executor looks up the parser by its `"actionType"` key and
 * delegates execution to [KetoyActionParser.onCall].
 *
 * ## Built-in actions
 * Ketoy ships with two built-in action parsers registered automatically:
 * - `"navigate"` — handled by [com.developerstring.ketoy.widget.builtin.NavigateActionParser]
 * - `"callFunction"` — handled by [com.developerstring.ketoy.widget.builtin.CallFunctionActionParser]
 *
 * ## Registering custom actions
 * ```kotlin
 * KetoyActionRegistry.register(ShowToastActionParser())
 * KetoyActionRegistry.register(AnalyticsEventActionParser())
 * ```
 *
 * ## JSON trigger example
 * ```json
 * {
 *   "onClick": {
 *     "actionType": "showToast",
 *     "message": "Hello from Ketoy!"
 *   }
 * }
 * ```
 *
 * @see KetoyActionParser
 * @see ActionContext
 */
object KetoyActionRegistry {

    private val parsers = mutableMapOf<String, KetoyActionParser<*>>()

    // ── Registration ────────────────────────────────────────────
    /**
     * Register a single custom action parser.
     *
     * ```kotlin
     * KetoyActionRegistry.register(ShowToastActionParser())
     * ```
     *
     * @param parser   The [KetoyActionParser] to register.
     * @param override If `true`, replaces any existing parser with the same
     *                 [KetoyActionParser.actionType]. Defaults to `false`.
     */    fun register(parser: KetoyActionParser<*>, override: Boolean = false) {
        if (override || !parsers.containsKey(parser.actionType)) {
            parsers[parser.actionType] = parser
        }
    }

    /**
     * Register multiple action parsers from a [List].
     *
     * @param parserList List of [KetoyActionParser] instances to register.
     * @param override   If `true`, replaces colliding action types.
     */
    fun registerAll(
        parserList: List<KetoyActionParser<*>>,
        override: Boolean = false
    ) {
        parserList.forEach { register(it, override) }
    }

    /**
     * Register multiple action parsers via varargs.
     *
     * @param parserList One or more [KetoyActionParser] instances to register.
     */
    fun registerAll(vararg parserList: KetoyActionParser<*>) {
        parserList.forEach { register(it) }
    }

    // ── Retrieval ───────────────────────────────────────────────

    /**
     * Retrieve a registered action parser by its action type.
     *
     * @param T          The expected model type of the parser.
     * @param actionType The action type string (e.g. `"navigate"`, `"callFunction"`).
     * @return The matching [KetoyActionParser], or `null` if not registered.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(actionType: String): KetoyActionParser<T>? {
        return parsers[actionType] as? KetoyActionParser<T>
    }

    /**
     * Check whether an action parser is registered for the given type.
     *
     * @param actionType The action type string to look up.
     * @return `true` if a parser is registered, `false` otherwise.
     */
    fun isRegistered(actionType: String): Boolean =
        parsers.containsKey(actionType)

    /**
     * Get all registered action type identifiers.
     *
     * @return An immutable [Set] of action type strings currently registered.
     */
    fun getAllTypes(): Set<String> = parsers.keys.toSet()

    // ── Lifecycle ───────────────────────────────────────────────

    /**
     * Remove a previously registered action parser by its type.
     *
     * @param actionType The action type string to unregister.
     * @return `true` if a parser was removed, `false` if none existed.
     */
    fun remove(actionType: String): Boolean =
        parsers.remove(actionType) != null

    /**
     * Clear all registered action parsers.
     *
     * Typically used in tests or when reinitialising the SDK.
     */
    fun clear() {
        parsers.clear()
    }
}
