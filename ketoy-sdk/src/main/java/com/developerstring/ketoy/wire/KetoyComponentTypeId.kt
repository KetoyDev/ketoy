package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*

/**
 * Bidirectional registry mapping Ketoy component type strings to compact
 * integer identifiers.
 *
 * Component type strings like `"Column"`, `"Text"`, `"FloatingActionButton"`
 * are the single most-repeated values in an SDUI JSON tree. Replacing them
 * with 1-2 byte integers reduces payload size significantly, especially
 * when combined with binary encoding (MessagePack) where small integers
 * are encoded as a single byte.
 *
 * ### ID assignment
 * IDs are densely packed starting at 1. The assignment is stable:
 * once a component type gets an ID, it never changes. New types are
 * appended at the end. ID 0 is reserved.
 *
 * ### Encoding
 * ```kotlin
 * val compressed = KetoyComponentTypeId.compressTypes(jsonElement)
 * // "type": "Column" → "type": 2
 * ```
 *
 * ### Decoding
 * ```kotlin
 * val expanded = KetoyComponentTypeId.expandTypes(jsonElement)
 * // "type": 2 → "type": "Column"
 * ```
 */
object KetoyComponentTypeId {

    /** Component type name → integer ID. */
    val typeToId: Map<String, Int> = buildMap {
        // ── Layout containers (1-10) ──────────────────────────
        put("Column", 1)
        put("Row", 2)
        put("Box", 3)
        put("LazyColumn", 4)
        put("LazyRow", 5)

        // ── Leaf widgets (11-30) ──────────────────────────────
        put("Text", 11)
        put("Button", 12)
        put("Spacer", 13)
        put("Card", 14)
        put("TextField", 15)
        put("Image", 16)
        put("Icon", 17)
        put("IconButton", 18)
        put("Component", 19)

        // ── Scaffold components (31-60) ───────────────────────
        put("Scaffold", 31)
        put("TopAppBar", 32)
        put("BottomAppBar", 33)
        put("NavigationBar", 34)
        put("FloatingActionButton", 35)
        put("SnackBar", 36)
        put("SnackBarHost", 37)
        put("NavigationDrawerItem", 38)
        put("CustomNavigationItem", 39)
        put("NavigationRail", 40)
        put("NavigationRailItem", 41)
        put("AppBarAction", 42)
        put("NavigationBarItem", 43)
        put("ModalBottomSheet", 44)

        // ── Data constructs (61-70) ───────────────────────────
        put("DataClass", 61)
        put("Enum", 62)
        put("DataList", 63)
    }

    /** Integer ID → component type name (inverse of [typeToId]). */
    val idToType: Map<Int, String> =
        typeToId.entries.associate { (name, id) -> id to name }

    /**
     * Case-insensitive lookup of type name to ID.
     * Returns null if the type is not a known Ketoy component.
     */
    fun resolveId(typeName: String): Int? {
        return typeToId[typeName]
            ?: typeToId.entries.firstOrNull {
                it.key.equals(typeName, ignoreCase = true)
            }?.value
    }

    /**
     * Recursively replace `"type": "SomeName"` with `"type": <int>` in
     * the JSON tree.
     *
     * Only string values under a key named `"type"` (or its alias `"t"`)
     * are transformed. Unrecognised type names are left as strings to
     * preserve forward-compatibility with custom component types.
     */
    fun compressTypes(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject(
            element.entries.associate { (key, value) ->
                if ((key == "type" || key == "t") && value is JsonPrimitive && value.isString) {
                    val typeName = value.content
                    val id = resolveId(typeName)
                    key to if (id != null) JsonPrimitive(id) else value
                } else {
                    key to compressTypes(value)
                }
            }
        )
        is JsonArray -> JsonArray(element.map { compressTypes(it) })
        is JsonPrimitive -> element
    }

    /**
     * Recursively replace `"type": <int>` with `"type": "SomeName"` in
     * the JSON tree (inverse of [compressTypes]).
     *
     * Integer values under a `"type"` / `"t"` key are resolved back to
     * their string name. Unknown IDs are left as-is (rendered as
     * `"Unknown component: <id>"` by the fallback renderer).
     */
    fun expandTypes(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject(
            element.entries.associate { (key, value) ->
                if ((key == "type" || key == "t") && value is JsonPrimitive) {
                    val intVal = value.intOrNull
                    if (intVal != null) {
                        val typeName = idToType[intVal]
                        key to if (typeName != null) JsonPrimitive(typeName) else value
                    } else {
                        key to value
                    }
                } else {
                    key to expandTypes(value)
                }
            }
        )
        is JsonArray -> JsonArray(element.map { expandTypes(it) })
        is JsonPrimitive -> element
    }
}
