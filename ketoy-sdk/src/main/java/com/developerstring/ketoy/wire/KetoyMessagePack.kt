package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.charset.Charset

/**
 * Zero-dependency MessagePack encoder/decoder operating on [JsonElement] trees.
 *
 * MessagePack is a binary serialization format that produces 2-3x smaller
 * payloads than equivalent JSON. This implementation covers the subset of
 * the MessagePack spec needed for SDUI payloads:
 *
 * - **nil** (JSON null)
 * - **bool** (true / false)
 * - **int** (positive/negative fixint, int8/16/32/64)
 * - **float64** (JSON fractional numbers)
 * - **str** (fixstr, str8, str16, str32)
 * - **array** (fixarray, array16, array32)
 * - **map** (fixmap, map16, map32)
 *
 * ### Why not use a library?
 * Ketoy SDK targets minimal dependency footprint. The full MessagePack
 * spec is simple enough (~200 LOC) that a self-contained implementation
 * avoids pulling in Jackson or msgpack-core.
 *
 * ### Usage
 * ```kotlin
 * val json: JsonElement = Json.parseToJsonElement("""{"type":"Text","props":{"text":"Hi"}}""")
 * val packed: ByteArray = KetoyMessagePack.encode(json)
 * val decoded: JsonElement = KetoyMessagePack.decode(packed)
 * assert(json == decoded)
 * ```
 *
 * @see [MessagePack specification](https://github.com/msgpack/msgpack/blob/master/spec.md)
 */
object KetoyMessagePack {

    // ── Format markers ────────────────────────────────────────
    // Positive fixint:  0x00 - 0x7f
    // Negative fixint:  0xe0 - 0xff
    // fixmap:           0x80 - 0x8f
    // fixarray:         0x90 - 0x9f
    // fixstr:           0xa0 - 0xbf
    private const val NIL: Byte = 0xc0.toByte()
    private const val FALSE: Byte = 0xc2.toByte()
    private const val TRUE: Byte = 0xc3.toByte()
    private const val FLOAT64: Byte = 0xcb.toByte()
    private const val UINT8: Byte = 0xcc.toByte()
    private const val UINT16: Byte = 0xcd.toByte()
    private const val UINT32: Byte = 0xce.toByte()
    private const val INT8: Byte = 0xd0.toByte()
    private const val INT16: Byte = 0xd1.toByte()
    private const val INT32: Byte = 0xd2.toByte()
    private const val INT64: Byte = 0xd3.toByte()
    private const val STR8: Byte = 0xd9.toByte()
    private const val STR16: Byte = 0xda.toByte()
    private const val STR32: Byte = 0xdb.toByte()
    private const val ARRAY16: Byte = 0xdc.toByte()
    private const val ARRAY32: Byte = 0xdd.toByte()
    private const val MAP16: Byte = 0xde.toByte()
    private const val MAP32: Byte = 0xdf.toByte()

    // ── Public API ────────────────────────────────────────────

    /**
     * Encode a [JsonElement] tree into MessagePack binary format.
     *
     * @param element The JSON element tree to encode.
     * @return MessagePack-encoded bytes.
     */
    fun encode(element: JsonElement): ByteArray {
        val bos = ByteArrayOutputStream(256)
        val dos = DataOutputStream(bos)
        writeElement(dos, element)
        dos.flush()
        return bos.toByteArray()
    }

    /**
     * Decode MessagePack binary data back into a [JsonElement] tree.
     *
     * @param data MessagePack-encoded bytes.
     * @return The decoded JSON element tree.
     */
    fun decode(data: ByteArray): JsonElement {
        val dis = DataInputStream(ByteArrayInputStream(data))
        return readElement(dis)
    }

    // ── Encoder ───────────────────────────────────────────────

    private fun writeElement(out: DataOutputStream, element: JsonElement) {
        when (element) {
            is JsonNull -> out.writeByte(NIL.toInt())
            is JsonPrimitive -> writePrimitive(out, element)
            is JsonArray -> writeArray(out, element)
            is JsonObject -> writeMap(out, element)
        }
    }

    private fun writePrimitive(out: DataOutputStream, prim: JsonPrimitive) {
        when {
            prim is JsonNull -> out.writeByte(NIL.toInt())
            prim.isString -> writeString(out, prim.content)
            prim.booleanOrNull != null -> {
                out.writeByte(if (prim.boolean) TRUE.toInt() else FALSE.toInt())
            }
            prim.longOrNull != null -> writeInt(out, prim.long)
            prim.doubleOrNull != null -> writeFloat64(out, prim.double)
            else -> writeString(out, prim.content)
        }
    }

    private fun writeInt(out: DataOutputStream, value: Long) {
        when {
            value in 0..127 -> {
                // positive fixint
                out.writeByte(value.toInt())
            }
            value in -32..-1 -> {
                // negative fixint
                out.writeByte((value.toInt() and 0xFF))
            }
            value in Byte.MIN_VALUE..Byte.MAX_VALUE -> {
                out.writeByte(INT8.toInt())
                out.writeByte(value.toInt())
            }
            value in 0..255 -> {
                out.writeByte(UINT8.toInt())
                out.writeByte(value.toInt())
            }
            value in Short.MIN_VALUE..Short.MAX_VALUE -> {
                out.writeByte(INT16.toInt())
                out.writeShort(value.toInt())
            }
            value in 0..65535 -> {
                out.writeByte(UINT16.toInt())
                out.writeShort(value.toInt())
            }
            value in Int.MIN_VALUE..Int.MAX_VALUE -> {
                out.writeByte(INT32.toInt())
                out.writeInt(value.toInt())
            }
            value in 0..0xFFFFFFFFL -> {
                out.writeByte(UINT32.toInt())
                out.writeInt(value.toInt())
            }
            else -> {
                out.writeByte(INT64.toInt())
                out.writeLong(value)
            }
        }
    }

    private fun writeFloat64(out: DataOutputStream, value: Double) {
        out.writeByte(FLOAT64.toInt())
        out.writeDouble(value)
    }

    private fun writeString(out: DataOutputStream, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        val len = bytes.size
        when {
            len <= 31 -> {
                // fixstr
                out.writeByte(0xa0 or len)
            }
            len <= 255 -> {
                out.writeByte(STR8.toInt())
                out.writeByte(len)
            }
            len <= 65535 -> {
                out.writeByte(STR16.toInt())
                out.writeShort(len)
            }
            else -> {
                out.writeByte(STR32.toInt())
                out.writeInt(len)
            }
        }
        out.write(bytes)
    }

    private fun writeArray(out: DataOutputStream, arr: JsonArray) {
        val size = arr.size
        when {
            size <= 15 -> out.writeByte(0x90 or size)
            size <= 65535 -> {
                out.writeByte(ARRAY16.toInt())
                out.writeShort(size)
            }
            else -> {
                out.writeByte(ARRAY32.toInt())
                out.writeInt(size)
            }
        }
        arr.forEach { writeElement(out, it) }
    }

    private fun writeMap(out: DataOutputStream, obj: JsonObject) {
        val size = obj.size
        when {
            size <= 15 -> out.writeByte(0x80 or size)
            size <= 65535 -> {
                out.writeByte(MAP16.toInt())
                out.writeShort(size)
            }
            else -> {
                out.writeByte(MAP32.toInt())
                out.writeInt(size)
            }
        }
        obj.forEach { (key, value) ->
            writeString(out, key)
            writeElement(out, value)
        }
    }

    // ── Decoder ───────────────────────────────────────────────

    private fun readElement(input: DataInputStream): JsonElement {
        val b = input.readUnsignedByte()
        return when {
            // positive fixint (0x00 - 0x7f)
            b in 0x00..0x7f -> JsonPrimitive(b)
            // fixmap (0x80 - 0x8f)
            b in 0x80..0x8f -> readMapBody(input, b and 0x0f)
            // fixarray (0x90 - 0x9f)
            b in 0x90..0x9f -> readArrayBody(input, b and 0x0f)
            // fixstr (0xa0 - 0xbf)
            b in 0xa0..0xbf -> readStringBody(input, b and 0x1f)
            // nil
            b == 0xc0 -> JsonNull
            // false
            b == 0xc2 -> JsonPrimitive(false)
            // true
            b == 0xc3 -> JsonPrimitive(true)
            // float64
            b == 0xcb -> JsonPrimitive(input.readDouble())
            // uint8
            b == 0xcc -> JsonPrimitive(input.readUnsignedByte())
            // uint16
            b == 0xcd -> JsonPrimitive(input.readUnsignedShort())
            // uint32
            b == 0xce -> JsonPrimitive(input.readInt().toLong() and 0xFFFFFFFFL)
            // int8
            b == 0xd0 -> JsonPrimitive(input.readByte().toInt())
            // int16
            b == 0xd1 -> JsonPrimitive(input.readShort().toInt())
            // int32
            b == 0xd2 -> JsonPrimitive(input.readInt())
            // int64
            b == 0xd3 -> JsonPrimitive(input.readLong())
            // str8
            b == 0xd9 -> readStringBody(input, input.readUnsignedByte())
            // str16
            b == 0xda -> readStringBody(input, input.readUnsignedShort())
            // str32
            b == 0xdb -> readStringBody(input, input.readInt())
            // array16
            b == 0xdc -> readArrayBody(input, input.readUnsignedShort())
            // array32
            b == 0xdd -> readArrayBody(input, input.readInt())
            // map16
            b == 0xde -> readMapBody(input, input.readUnsignedShort())
            // map32
            b == 0xdf -> readMapBody(input, input.readInt())
            // negative fixint (0xe0 - 0xff)
            b in 0xe0..0xff -> JsonPrimitive(b.toByte().toInt())
            else -> throw IllegalStateException("Unknown MessagePack type: 0x${b.toString(16)}")
        }
    }

    private fun readStringBody(input: DataInputStream, length: Int): JsonPrimitive {
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return JsonPrimitive(String(bytes, Charsets.UTF_8))
    }

    private fun readArrayBody(input: DataInputStream, count: Int): JsonArray {
        val elements = ArrayList<JsonElement>(count)
        repeat(count) { elements.add(readElement(input)) }
        return JsonArray(elements)
    }

    private fun readMapBody(input: DataInputStream, count: Int): JsonObject {
        val entries = LinkedHashMap<String, JsonElement>(count)
        repeat(count) {
            val key = readElement(input)
            val keyStr = if (key is JsonPrimitive && key.isString) {
                key.content
            } else {
                key.toString()
            }
            entries[keyStr] = readElement(input)
        }
        return JsonObject(entries)
    }
}
