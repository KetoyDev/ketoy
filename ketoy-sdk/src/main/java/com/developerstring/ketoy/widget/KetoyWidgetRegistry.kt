package com.developerstring.ketoy.widget

import kotlinx.serialization.json.JsonObject

/**
 * Global registry for custom [KetoyWidgetParser] instances.
 *
 * When the renderer encounters a JSON `"type"` it doesn't recognise as
 * a built-in widget, it checks this registry for a matching custom parser.
 *
 * ## Registration
 * ```kotlin
 * KetoyWidgetRegistry.register(KetoyBadgeParser())
 * KetoyWidgetRegistry.register(KetoyRatingBarParser())
 * ```
 *
 * ## Resolution (used internally by [KetoyRenderer])
 * ```kotlin
 * val parser = KetoyWidgetRegistry.get("badge")
 * if (parser != null) {
 *     val model = parser.getModel(jsonProps)
 *     parser.parse(model)
 * }
 * ```
 */
object KetoyWidgetRegistry {

    private val parsers = mutableMapOf<String, KetoyWidgetParser<*>>()

    // ── Registration ────────────────────────────────────────────

    /**
     * Register a single custom widget parser.
     *
     * @param parser  The parser to register.
     * @param override If true, replaces any existing parser with the same type.
     */
    fun register(parser: KetoyWidgetParser<*>, override: Boolean = false) {
        if (override || !parsers.containsKey(parser.type)) {
            parsers[parser.type] = parser
        }
    }

    /**
     * Register multiple custom widget parsers at once.
     *
     * Convenience method for batch registration, commonly called during
     * SDK initialisation.
     *
     * ```kotlin
     * KetoyWidgetRegistry.registerAll(
     *     listOf(KetoyBadgeParser(), KetoyRatingBarParser()),
     *     override = true
     * )
     * ```
     *
     * @param parserList List of [KetoyWidgetParser] instances to register.
     * @param override   If `true`, replaces any existing parser whose
     *                   [KetoyWidgetParser.type] collides with one in the list.
     */
    fun registerAll(
        parserList: List<KetoyWidgetParser<*>>,
        override: Boolean = false
    ) {
        parserList.forEach { register(it, override) }
    }

    /**
     * Register multiple parsers via varargs.
     *
     * ```kotlin
     * KetoyWidgetRegistry.registerAll(
     *     KetoyBadgeParser(),
     *     KetoyRatingBarParser()
     * )
     * ```
     *
     * @param parserList One or more [KetoyWidgetParser] instances to register.
     */
    fun registerAll(vararg parserList: KetoyWidgetParser<*>) {
        parserList.forEach { register(it) }
    }

    // ── Retrieval ───────────────────────────────────────────────

    /**
     * Retrieve a registered parser by its type identifier.
     *
     * The generic parameter [T] is inferred by the call-site and the
     * returned parser is cast accordingly. Returns `null` when no parser
     * matches [type].
     *
     * @param T    The expected model type of the parser.
     * @param type The widget type string (e.g. `"badge"`).
     * @return The matching [KetoyWidgetParser], or `null` if not registered.
     * @see isRegistered
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(type: String): KetoyWidgetParser<T>? {
        return parsers[type] as? KetoyWidgetParser<T>
    }

    /**
     * Check whether a parser is registered for the given type.
     *
     * @param type The widget type string to look up (e.g. `"badge"`).
     * @return `true` if a parser is registered for [type], `false` otherwise.
     */
    fun isRegistered(type: String): Boolean = parsers.containsKey(type)

    /**
     * Get all registered type identifiers.
     *
     * Useful for debugging or building a widget catalogue.
     *
     * @return An immutable [Set] of type strings currently registered.
     */
    fun getAllTypes(): Set<String> = parsers.keys.toSet()

    // ── Internal render helper ──────────────────────────────────

    /**
     * Resolve the parser for a given widget type.
     *
     * This is an **internal** helper used by the Ketoy rendering pipeline
     * ([com.developerstring.ketoy.renderer.WidgetRenderer]) to look up
     * custom parsers at render time.
     *
     * @param type The widget type identifier from the JSON `"type"` field.
     * @return The matching [KetoyWidgetParser], or `null` if none is registered.
     */
    internal fun resolveParser(type: String): KetoyWidgetParser<*>? {
        return parsers[type]
    }

    // ── Lifecycle ───────────────────────────────────────────────

    /**
     * Remove a previously registered parser by its type identifier.
     *
     * @param type The widget type string to unregister.
     * @return `true` if a parser was removed, `false` if no parser existed for [type].
     */
    fun remove(type: String): Boolean = parsers.remove(type) != null

    /**
     * Clear all registered parsers.
     *
     * Typically called during testing or when tearing down the Ketoy SDK.
     * After this call, [getAllTypes] returns an empty set.
     */
    fun clear() {
        parsers.clear()
    }
}
