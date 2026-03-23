package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*

/**
 * Orchestrates the Ketoy compression pipeline: a configurable chain of
 * transforms that shrink SDUI JSON payloads by 10-15x.
 *
 * ### Compression ladder (cumulative reduction over raw JSON)
 *
 * | Layer | Transform              | Reduction  |
 * |-------|------------------------|------------|
 * | 1     | gzip                   | ~4x        |
 * | 2a    | key aliasing           | ~1.5-2x    |
 * | 2c    | type → integer         | ~1.2x      |
 * | 3     | MessagePack encoding   | ~2-3x      |
 * | 4     | delta patches          | ~10-50x *  |
 *
 * *Delta patch reduction is for incremental updates only.
 *
 * Combined (gzip + aliasing + types + MessagePack): **10-15x**.
 *
 * ### Encoding pipeline (server → wire)
 * ```
 * KNode/JSON string
 *   → parse to JsonElement
 *   → alias keys (Layer 2a)
 *   → compress types (Layer 2c)
 *   → MessagePack encode (Layer 3)
 *   → gzip compress (Layer 1)
 *   → wire bytes
 * ```
 *
 * ### Decoding pipeline (wire → client)
 * ```
 * wire bytes
 *   → auto-decompress gzip (Layer 1)
 *   → detect binary / JSON
 *   → MessagePack decode or JSON parse (Layer 3)
 *   → expand types (Layer 2c)
 *   → expand keys (Layer 2a)
 *   → JSON string → UIComponent → RenderComponent()
 * ```
 *
 * ### Usage
 * ```kotlin
 * // Full pipeline
 * val config = WireFormatConfig.OPTIMIZED
 * val wire = KetoyWireFormat.encode(jsonString, config)
 * val restored = KetoyWireFormat.decode(wire, config)
 * ```
 */
object KetoyWireFormat {

    /**
     * Content-Type header value for Ketoy binary wire format.
     * Servers should return this when sending optimized payloads.
     */
    const val CONTENT_TYPE_KETOY_BINARY = "application/vnd.ketoy+msgpack"

    /**
     * Content-Type header value for Ketoy aliased JSON.
     * Used when binary encoding is disabled but aliasing is active.
     */
    const val CONTENT_TYPE_KETOY_JSON = "application/vnd.ketoy+json"

    /**
     * Accept header value: tells the server the client supports all formats.
     */
    const val ACCEPT_HEADER = "$CONTENT_TYPE_KETOY_BINARY, $CONTENT_TYPE_KETOY_JSON, application/json"

    // ── Encode ────────────────────────────────────────────────

    /**
     * Encode a JSON string through the full compression pipeline.
     *
     * @param jsonString Raw Ketoy JSON string.
     * @param config Pipeline configuration.
     * @return Compressed wire bytes.
     */
    fun encode(jsonString: String, config: WireFormatConfig = WireFormatConfig.OPTIMIZED): ByteArray {
        val json = Json.parseToJsonElement(jsonString)
        return encodeElement(json, config)
    }

    /**
     * Encode a [JsonElement] through the full compression pipeline.
     *
     * @param element The JSON element tree.
     * @param config Pipeline configuration.
     * @return Compressed wire bytes.
     */
    fun encodeElement(element: JsonElement, config: WireFormatConfig = WireFormatConfig.OPTIMIZED): ByteArray {
        var transformed = element

        // Layer 2a: key aliasing
        if (config.keyAliasing) {
            transformed = KetoyKeyAlias.aliasKeys(transformed)
        }

        // Layer 2c: type compression
        if (config.typeCompression) {
            transformed = KetoyComponentTypeId.compressTypes(transformed)
        }

        // Layer 3: binary encoding
        val bytes = when (config.binaryEncoding) {
            BinaryEncoding.MESSAGE_PACK -> KetoyMessagePack.encode(transformed)
            BinaryEncoding.NONE -> transformed.toString().toByteArray(Charsets.UTF_8)
        }

        // Layer 1: gzip compression
        return if (config.gzipCompression) {
            KetoyCompression.gzip(bytes)
        } else {
            bytes
        }
    }

    // ── Decode ────────────────────────────────────────────────

    /**
     * Decode wire bytes through the decompression pipeline back to a JSON string.
     *
     * @param data Wire bytes (potentially compressed and/or binary-encoded).
     * @param config Pipeline configuration.
     * @return Restored JSON string.
     */
    fun decode(data: ByteArray, config: WireFormatConfig = WireFormatConfig.OPTIMIZED): String {
        return decodeToElement(data, config).toString()
    }

    /**
     * Decode wire bytes to a [JsonElement].
     *
     * @param data Wire bytes.
     * @param config Pipeline configuration.
     * @return Restored [JsonElement] tree.
     */
    fun decodeToElement(data: ByteArray, config: WireFormatConfig = WireFormatConfig.OPTIMIZED): JsonElement {
        // Layer 1: auto-decompress gzip
        val decompressed = KetoyCompression.autoDecompress(data)

        // Layer 3: binary decoding
        var element = when (config.binaryEncoding) {
            BinaryEncoding.MESSAGE_PACK -> KetoyMessagePack.decode(decompressed)
            BinaryEncoding.NONE -> Json.parseToJsonElement(decompressed.toString(Charsets.UTF_8))
        }

        // Layer 2c: expand type IDs
        if (config.typeCompression) {
            element = KetoyComponentTypeId.expandTypes(element)
        }

        // Layer 2a: expand key aliases
        if (config.keyAliasing) {
            element = KetoyKeyAlias.expandKeys(element)
        }

        return element
    }

    /**
     * Auto-detect the wire format and decode accordingly.
     *
     * Examines the byte structure to determine whether the payload is:
     * - Gzip-compressed (magic bytes 0x1F 0x8B)
     * - MessagePack binary (starts with map/array marker, not '{' or '[')
     * - Plain JSON (starts with '{' or '[')
     *
     * Then applies the appropriate decoding layers.
     *
     * @param data Raw bytes from the wire.
     * @return Decoded [JsonElement] tree with full (un-aliased) keys.
     */
    fun autoDecode(data: ByteArray): JsonElement {
        val decompressed = KetoyCompression.autoDecompress(data)

        // Detect format
        val element = if (decompressed.isNotEmpty() && isMessagePack(decompressed)) {
            KetoyMessagePack.decode(decompressed)
        } else {
            Json.parseToJsonElement(decompressed.toString(Charsets.UTF_8))
        }

        // Always attempt expansion — safe to apply to non-aliased data
        // because unknown keys are left as-is
        val typeExpanded = KetoyComponentTypeId.expandTypes(element)
        return KetoyKeyAlias.expandKeys(typeExpanded)
    }

    /**
     * Heuristic check: MessagePack maps start with 0x80-0x8f (fixmap),
     * 0xde (map16), or 0xdf (map32). JSON starts with 0x7B ('{') or
     * 0x5B ('[').
     */
    private fun isMessagePack(data: ByteArray): Boolean {
        if (data.isEmpty()) return false
        val first = data[0].toInt() and 0xFF
        // JSON starts with '{' (0x7B) or '[' (0x5B) or whitespace
        // MessagePack maps/arrays start with other byte ranges
        return first != 0x7B && first != 0x5B &&
                first != 0x20 && first != 0x09 &&
                first != 0x0A && first != 0x0D
    }

    // ── Metrics ───────────────────────────────────────────────

    /**
     * Calculate compression statistics for a JSON payload.
     *
     * Useful for debugging and monitoring wire format effectiveness.
     *
     * @param jsonString The original JSON string.
     * @param config The wire format configuration to evaluate.
     * @return [CompressionStats] with size breakdowns.
     */
    fun measureCompression(
        jsonString: String,
        config: WireFormatConfig = WireFormatConfig.OPTIMIZED
    ): CompressionStats {
        val originalBytes = jsonString.toByteArray(Charsets.UTF_8)
        val json = Json.parseToJsonElement(jsonString)

        // Measure each layer independently
        val aliased = if (config.keyAliasing) KetoyKeyAlias.aliasKeys(json) else json
        val typed = if (config.typeCompression) KetoyComponentTypeId.compressTypes(aliased) else aliased

        val afterAliasBytes = aliased.toString().toByteArray(Charsets.UTF_8).size
        val afterTypeBytes = typed.toString().toByteArray(Charsets.UTF_8).size

        val binaryBytes = when (config.binaryEncoding) {
            BinaryEncoding.MESSAGE_PACK -> KetoyMessagePack.encode(typed).size
            BinaryEncoding.NONE -> afterTypeBytes
        }

        val finalBytes = encode(jsonString, config).size

        return CompressionStats(
            originalSize = originalBytes.size,
            afterAliasing = afterAliasBytes,
            afterTypeCompression = afterTypeBytes,
            afterBinaryEncoding = binaryBytes,
            afterGzip = finalBytes,
            totalReduction = originalBytes.size.toFloat() / finalBytes.toFloat()
        )
    }
}

/**
 * Configuration for the Ketoy wire format pipeline.
 *
 * @property keyAliasing Enable Layer 2a (property name aliasing).
 * @property typeCompression Enable Layer 2c (component type → integer).
 * @property binaryEncoding Layer 3 binary encoding format.
 * @property gzipCompression Enable Layer 1 (gzip compression).
 */
data class WireFormatConfig(
    val keyAliasing: Boolean = true,
    val typeCompression: Boolean = true,
    val binaryEncoding: BinaryEncoding = BinaryEncoding.NONE,
    val gzipCompression: Boolean = true
) {
    companion object {
        /** No compression — raw JSON pass-through. */
        val NONE = WireFormatConfig(
            keyAliasing = false,
            typeCompression = false,
            binaryEncoding = BinaryEncoding.NONE,
            gzipCompression = false
        )

        /** Minimal: gzip only (3-5x reduction, zero schema knowledge needed). */
        val GZIP_ONLY = WireFormatConfig(
            keyAliasing = false,
            typeCompression = false,
            binaryEncoding = BinaryEncoding.NONE,
            gzipCompression = true
        )

        /** Moderate: gzip + key aliasing + type compression (7-8x reduction). */
        val ALIASED = WireFormatConfig(
            keyAliasing = true,
            typeCompression = true,
            binaryEncoding = BinaryEncoding.NONE,
            gzipCompression = true
        )

        /** Full: gzip + aliasing + types + MessagePack (10-15x reduction). */
        val OPTIMIZED = WireFormatConfig(
            keyAliasing = true,
            typeCompression = true,
            binaryEncoding = BinaryEncoding.MESSAGE_PACK,
            gzipCompression = true
        )
    }
}

/**
 * Selects the binary encoding format for Layer 3.
 */
enum class BinaryEncoding {
    /** No binary encoding — use JSON text. */
    NONE,
    /** MessagePack binary encoding (2-3x over JSON). */
    MESSAGE_PACK
}

/**
 * Compression statistics for a single payload through the pipeline.
 *
 * @property originalSize Original JSON size in bytes.
 * @property afterAliasing Size after key aliasing (bytes).
 * @property afterTypeCompression Size after type compression (bytes).
 * @property afterBinaryEncoding Size after binary encoding (bytes).
 * @property afterGzip Final size after gzip compression (bytes).
 * @property totalReduction Overall compression ratio (original / final).
 */
data class CompressionStats(
    val originalSize: Int,
    val afterAliasing: Int,
    val afterTypeCompression: Int,
    val afterBinaryEncoding: Int,
    val afterGzip: Int,
    val totalReduction: Float
)
