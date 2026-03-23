package com.developerstring.ketoy.wire

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * RFC 6902 JSON Patch implementation for Ketoy SDUI trees.
 *
 * When a Ketoy screen updates partially (user action → server re-renders
 * a subtree), sending a full JSON tree is wasteful. JSON Patch sends only
 * the delta — typically 10-50x smaller than the full payload for
 * incremental updates.
 *
 * ### Supported operations
 * | Operation | RFC 6902 | Description |
 * |-----------|----------|-------------|
 * | add       | 4.1      | Insert a value at a path |
 * | remove    | 4.2      | Remove the value at a path |
 * | replace   | 4.3      | Replace the value at a path |
 * | move      | 4.4      | Move a value from one path to another |
 * | copy      | 4.5      | Copy a value from one path to another |
 * | test      | 4.6      | Assert a value equals the expected |
 *
 * ### Path syntax
 * JSON Pointer (RFC 6901): `/children/2/props/text`
 * - `/` separates path segments
 * - Numeric segments index into arrays
 * - `-` appends to end of an array
 *
 * ### Usage
 * ```kotlin
 * val patch = listOf(
 *     PatchOp(op = "replace", path = "/children/2/props/text", value = JsonPrimitive("Updated")),
 *     PatchOp(op = "add", path = "/children/3", value = newComponentJson)
 * )
 * val updated = KetoyPatch.apply(originalTree, patch)
 * ```
 *
 * ### Diff generation
 * ```kotlin
 * val patch = KetoyPatch.diff(oldTree, newTree)
 * // Send patch to client instead of full newTree
 * ```
 */
object KetoyPatch {

    /**
     * Apply a list of [PatchOp] operations to a [JsonElement] tree.
     *
     * Operations are applied sequentially. Each operation modifies the
     * document for subsequent operations (as specified by RFC 6902).
     *
     * @param document The original JSON tree.
     * @param operations The patch operations to apply.
     * @return A new [JsonElement] with all patches applied.
     * @throws PatchException if a path is invalid or an operation fails.
     */
    fun apply(document: JsonElement, operations: List<PatchOp>): JsonElement {
        var doc = document
        for (op in operations) {
            doc = when (op.op) {
                "add" -> applyAdd(doc, op.path, op.value ?: JsonNull)
                "remove" -> applyRemove(doc, op.path)
                "replace" -> applyReplace(doc, op.path, op.value ?: JsonNull)
                "move" -> {
                    val from = op.from ?: throw PatchException("'move' requires 'from' field")
                    val value = resolve(doc, parsePath(from))
                    val removed = applyRemove(doc, from)
                    applyAdd(removed, op.path, value)
                }
                "copy" -> {
                    val from = op.from ?: throw PatchException("'copy' requires 'from' field")
                    val value = resolve(doc, parsePath(from))
                    applyAdd(doc, op.path, value)
                }
                "test" -> {
                    val actual = resolve(doc, parsePath(op.path))
                    val expected = op.value ?: JsonNull
                    if (actual != expected) {
                        throw PatchException(
                            "Test failed at '${op.path}': expected $expected, got $actual"
                        )
                    }
                    doc
                }
                else -> throw PatchException("Unknown patch operation: '${op.op}'")
            }
        }
        return doc
    }

    /**
     * Generate a minimal patch that transforms [source] into [target].
     *
     * Uses a recursive structural diff. For arrays, a simple positional
     * comparison is used (not LCS/Myers) since SDUI trees are typically
     * shallow and insertions/deletions are handled by explicit indices.
     *
     * @param source The original JSON tree.
     * @param target The desired JSON tree.
     * @return A list of [PatchOp] operations.
     */
    fun diff(source: JsonElement, target: JsonElement): List<PatchOp> {
        val ops = mutableListOf<PatchOp>()
        diffRecursive("", source, target, ops)
        return ops
    }

    // ── Path parsing ──────────────────────────────────────────

    internal fun parsePath(path: String): List<String> {
        if (path.isEmpty()) return emptyList()
        if (!path.startsWith("/")) {
            throw PatchException("Invalid JSON Pointer: '$path' (must start with '/')")
        }
        return path.substring(1).split("/").map { segment ->
            segment.replace("~1", "/").replace("~0", "~")
        }
    }

    // ── Resolution ────────────────────────────────────────────

    internal fun resolve(doc: JsonElement, segments: List<String>): JsonElement {
        var current = doc
        for (segment in segments) {
            current = when (current) {
                is JsonObject -> current[segment]
                    ?: throw PatchException("Key '$segment' not found in object")
                is JsonArray -> {
                    val index = segment.toIntOrNull()
                        ?: throw PatchException("Invalid array index: '$segment'")
                    if (index < 0 || index >= current.size) {
                        throw PatchException("Array index $index out of bounds (size=${current.size})")
                    }
                    current[index]
                }
                else -> throw PatchException("Cannot traverse into primitive at '$segment'")
            }
        }
        return current
    }

    // ── Add ───────────────────────────────────────────────────

    private fun applyAdd(doc: JsonElement, path: String, value: JsonElement): JsonElement {
        val segments = parsePath(path)
        if (segments.isEmpty()) return value
        return setAtPath(doc, segments, value, insert = true)
    }

    // ── Remove ────────────────────────────────────────────────

    private fun applyRemove(doc: JsonElement, path: String): JsonElement {
        val segments = parsePath(path)
        if (segments.isEmpty()) throw PatchException("Cannot remove root")
        return removeAtPath(doc, segments)
    }

    // ── Replace ───────────────────────────────────────────────

    private fun applyReplace(doc: JsonElement, path: String, value: JsonElement): JsonElement {
        val segments = parsePath(path)
        if (segments.isEmpty()) return value
        return setAtPath(doc, segments, value, insert = false)
    }

    // ── Recursive tree mutation ───────────────────────────────

    private fun setAtPath(
        doc: JsonElement,
        segments: List<String>,
        value: JsonElement,
        insert: Boolean
    ): JsonElement {
        val key = segments.first()
        if (segments.size == 1) {
            return when (doc) {
                is JsonObject -> {
                    if (!insert && key !in doc) {
                        throw PatchException("Key '$key' not found for replace")
                    }
                    JsonObject(doc.toMutableMap().also { it[key] = value })
                }
                is JsonArray -> {
                    val list = doc.toMutableList()
                    if (key == "-") {
                        list.add(value)
                    } else {
                        val index = key.toIntOrNull()
                            ?: throw PatchException("Invalid array index: '$key'")
                        if (insert) {
                            if (index < 0 || index > list.size) {
                                throw PatchException("Index $index out of bounds for insert")
                            }
                            list.add(index, value)
                        } else {
                            if (index < 0 || index >= list.size) {
                                throw PatchException("Index $index out of bounds for replace")
                            }
                            list[index] = value
                        }
                    }
                    JsonArray(list)
                }
                else -> throw PatchException("Cannot set on primitive")
            }
        }

        val rest = segments.subList(1, segments.size)
        return when (doc) {
            is JsonObject -> {
                val child = doc[key]
                    ?: throw PatchException("Key '$key' not found in object")
                JsonObject(doc.toMutableMap().also {
                    it[key] = setAtPath(child, rest, value, insert)
                })
            }
            is JsonArray -> {
                val index = key.toIntOrNull()
                    ?: throw PatchException("Invalid array index: '$key'")
                if (index < 0 || index >= doc.size) {
                    throw PatchException("Array index $index out of bounds")
                }
                val list = doc.toMutableList()
                list[index] = setAtPath(list[index], rest, value, insert)
                JsonArray(list)
            }
            else -> throw PatchException("Cannot traverse into primitive")
        }
    }

    private fun removeAtPath(doc: JsonElement, segments: List<String>): JsonElement {
        val key = segments.first()
        if (segments.size == 1) {
            return when (doc) {
                is JsonObject -> {
                    if (key !in doc) throw PatchException("Key '$key' not found for remove")
                    JsonObject(doc.filterKeys { it != key })
                }
                is JsonArray -> {
                    val index = key.toIntOrNull()
                        ?: throw PatchException("Invalid array index: '$key'")
                    if (index < 0 || index >= doc.size) {
                        throw PatchException("Index $index out of bounds for remove")
                    }
                    JsonArray(doc.toMutableList().also { it.removeAt(index) })
                }
                else -> throw PatchException("Cannot remove from primitive")
            }
        }

        val rest = segments.subList(1, segments.size)
        return when (doc) {
            is JsonObject -> {
                val child = doc[key]
                    ?: throw PatchException("Key '$key' not found in object")
                JsonObject(doc.toMutableMap().also {
                    it[key] = removeAtPath(child, rest)
                })
            }
            is JsonArray -> {
                val index = key.toIntOrNull()
                    ?: throw PatchException("Invalid array index: '$key'")
                if (index < 0 || index >= doc.size) {
                    throw PatchException("Array index $index out of bounds")
                }
                val list = doc.toMutableList()
                list[index] = removeAtPath(list[index], rest)
                JsonArray(list)
            }
            else -> throw PatchException("Cannot traverse into primitive")
        }
    }

    // ── Diff generation ───────────────────────────────────────

    private fun diffRecursive(
        basePath: String,
        source: JsonElement,
        target: JsonElement,
        ops: MutableList<PatchOp>
    ) {
        if (source == target) return

        when {
            source is JsonObject && target is JsonObject -> {
                // Removed keys
                for (key in source.keys) {
                    if (key !in target) {
                        ops.add(PatchOp(op = "remove", path = "$basePath/$key"))
                    }
                }
                // Added or changed keys
                for ((key, targetVal) in target) {
                    val childPath = "$basePath/$key"
                    val sourceVal = source[key]
                    if (sourceVal == null) {
                        ops.add(PatchOp(op = "add", path = childPath, value = targetVal))
                    } else {
                        diffRecursive(childPath, sourceVal, targetVal, ops)
                    }
                }
            }
            source is JsonArray && target is JsonArray -> {
                diffArrays(basePath, source, target, ops)
            }
            else -> {
                // Different types or different primitive values
                ops.add(PatchOp(op = "replace", path = basePath, value = target))
            }
        }
    }

    private fun diffArrays(
        basePath: String,
        source: JsonArray,
        target: JsonArray,
        ops: MutableList<PatchOp>
    ) {
        val minSize = minOf(source.size, target.size)

        // Diff common positions
        for (i in 0 until minSize) {
            diffRecursive("$basePath/$i", source[i], target[i], ops)
        }

        // Remove excess elements from the end (in reverse to preserve indices)
        if (source.size > target.size) {
            for (i in (source.size - 1) downTo target.size) {
                ops.add(PatchOp(op = "remove", path = "$basePath/$i"))
            }
        }

        // Add new elements
        if (target.size > source.size) {
            for (i in source.size until target.size) {
                ops.add(PatchOp(op = "add", path = "$basePath/-", value = target[i]))
            }
        }
    }
}

/**
 * A single JSON Patch operation per RFC 6902.
 *
 * @property op    The operation name: `add`, `remove`, `replace`, `move`, `copy`, `test`.
 * @property path  Target location as a JSON Pointer (RFC 6901).
 * @property value The value to add/replace/test (not used by `remove` or `move`).
 * @property from  Source location for `move` and `copy` operations.
 */
@Serializable
data class PatchOp(
    val op: String,
    val path: String,
    val value: JsonElement? = null,
    val from: String? = null
)

/**
 * Exception thrown when a JSON Patch operation fails.
 */
class PatchException(message: String) : Exception(message)
