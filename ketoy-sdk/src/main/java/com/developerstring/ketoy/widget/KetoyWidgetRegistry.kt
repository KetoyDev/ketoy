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
     */
    fun registerAll(
        parserList: List<KetoyWidgetParser<*>>,
        override: Boolean = false
    ) {
        parserList.forEach { register(it, override) }
    }

    /**
     * Register multiple parsers via varargs.
     */
    fun registerAll(vararg parserList: KetoyWidgetParser<*>) {
        parserList.forEach { register(it) }
    }

    // ── Retrieval ───────────────────────────────────────────────

    /**
     * Get a parser by its type identifier.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(type: String): KetoyWidgetParser<T>? {
        return parsers[type] as? KetoyWidgetParser<T>
    }

    /**
     * Check whether a parser is registered for the given type.
     */
    fun isRegistered(type: String): Boolean = parsers.containsKey(type)

    /**
     * Get all registered type identifiers.
     */
    fun getAllTypes(): Set<String> = parsers.keys.toSet()

    // ── Internal render helper ──────────────────────────────────

    /**
     * Attempt to parse and render a custom widget from a JSON object.
     *
     * @param type The widget type identifier.
     * @param json The full JSON object of the widget node.
     * @return A composable lambda, or null if no parser is registered.
     */
    internal fun resolveParser(type: String): KetoyWidgetParser<*>? {
        return parsers[type]
    }

    // ── Lifecycle ───────────────────────────────────────────────

    /**
     * Remove a parser by type.
     */
    fun remove(type: String): Boolean = parsers.remove(type) != null

    /**
     * Clear all registered parsers.
     */
    fun clear() {
        parsers.clear()
    }
}
