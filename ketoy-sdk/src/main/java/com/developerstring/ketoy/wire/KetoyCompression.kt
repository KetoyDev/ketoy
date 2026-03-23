package com.developerstring.ketoy.wire

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Gzip compression and decompression for Ketoy wire payloads.
 *
 * Gzip is the cheapest layer in the compression ladder — a typical SDUI
 * JSON payload compresses 3-5x with zero schema changes. OkHttp
 * transparently decompresses `Content-Encoding: gzip` responses, but
 * this utility is needed for:
 *
 * - Client-side compression of outbound payloads (screen pushes).
 * - Local cache storage in compressed form.
 * - Combining with MessagePack: `msgpack + gzip` outperforms `json + gzip`
 *   because binary encoding reduces entropy before compression.
 *
 * ### Usage
 * ```kotlin
 * val compressed = KetoyCompression.gzip(jsonBytes)
 * val decompressed = KetoyCompression.gunzip(compressed)
 * assert(decompressed.contentEquals(jsonBytes))
 * ```
 */
object KetoyCompression {

    /** Gzip magic bytes: 0x1F 0x8B. Used to detect compressed payloads. */
    private const val GZIP_MAGIC_1: Byte = 0x1F.toByte()
    private const val GZIP_MAGIC_2: Byte = 0x8B.toByte()

    /**
     * Compress [data] using gzip.
     *
     * @param data Raw bytes to compress.
     * @return Gzip-compressed bytes.
     */
    fun gzip(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(data.size / 2)
        GZIPOutputStream(bos).use { gzos ->
            gzos.write(data)
        }
        return bos.toByteArray()
    }

    /**
     * Decompress gzip-compressed [data].
     *
     * @param data Gzip-compressed bytes.
     * @return Decompressed raw bytes.
     * @throws java.util.zip.ZipException if [data] is not valid gzip.
     */
    fun gunzip(data: ByteArray): ByteArray {
        val bis = ByteArrayInputStream(data)
        return GZIPInputStream(bis).use { gzis ->
            gzis.readBytes()
        }
    }

    /**
     * Check if [data] starts with the gzip magic bytes (0x1F 0x8B).
     *
     * Useful for auto-detecting whether a payload is compressed without
     * relying on Content-Encoding headers.
     *
     * @param data Bytes to inspect.
     * @return `true` if the first two bytes match the gzip magic number.
     */
    fun isGzipped(data: ByteArray): Boolean {
        return data.size >= 2 &&
                data[0] == GZIP_MAGIC_1 &&
                data[1] == GZIP_MAGIC_2
    }

    /**
     * Compress a UTF-8 string to gzip bytes.
     *
     * Convenience wrapper around [gzip] for JSON string payloads.
     */
    fun gzipString(text: String): ByteArray = gzip(text.toByteArray(Charsets.UTF_8))

    /**
     * Decompress gzip bytes to a UTF-8 string.
     *
     * Convenience wrapper around [gunzip] for JSON string payloads.
     */
    fun gunzipToString(data: ByteArray): String = gunzip(data).toString(Charsets.UTF_8)

    /**
     * Smartly decompress: returns decompressed bytes if gzipped, otherwise
     * returns the original bytes unchanged.
     */
    fun autoDecompress(data: ByteArray): ByteArray =
        if (isGzipped(data)) gunzip(data) else data
}
