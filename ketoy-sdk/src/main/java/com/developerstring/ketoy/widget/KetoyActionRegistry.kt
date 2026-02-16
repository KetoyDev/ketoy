package com.developerstring.ketoy.widget

/**
 * Global registry for [KetoyActionParser] instances.
 *
 * When a JSON action is triggered, the executor looks up the parser
 * by `actionType` and invokes it.
 */
object KetoyActionRegistry {

    private val parsers = mutableMapOf<String, KetoyActionParser<*>>()

    // ── Registration ────────────────────────────────────────────

    fun register(parser: KetoyActionParser<*>, override: Boolean = false) {
        if (override || !parsers.containsKey(parser.actionType)) {
            parsers[parser.actionType] = parser
        }
    }

    fun registerAll(
        parserList: List<KetoyActionParser<*>>,
        override: Boolean = false
    ) {
        parserList.forEach { register(it, override) }
    }

    fun registerAll(vararg parserList: KetoyActionParser<*>) {
        parserList.forEach { register(it) }
    }

    // ── Retrieval ───────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    fun <T> get(actionType: String): KetoyActionParser<T>? {
        return parsers[actionType] as? KetoyActionParser<T>
    }

    fun isRegistered(actionType: String): Boolean =
        parsers.containsKey(actionType)

    fun getAllTypes(): Set<String> = parsers.keys.toSet()

    // ── Lifecycle ───────────────────────────────────────────────

    fun remove(actionType: String): Boolean =
        parsers.remove(actionType) != null

    fun clear() {
        parsers.clear()
    }
}
