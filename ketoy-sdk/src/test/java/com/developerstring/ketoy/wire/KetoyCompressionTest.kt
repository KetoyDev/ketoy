package com.developerstring.ketoy.wire

import org.junit.Assert.*
import org.junit.Test

class KetoyCompressionTest {

    @Test
    fun `gzip and gunzip round-trip preserves data`() {
        val original = "Hello, Ketoy wire format!".toByteArray(Charsets.UTF_8)
        val compressed = KetoyCompression.gzip(original)
        val decompressed = KetoyCompression.gunzip(compressed)
        assertArrayEquals(original, decompressed)
    }

    @Test
    fun `gzip produces smaller output for repetitive data`() {
        val repetitive = """{"type":"Text","props":{"text":"Hello"}}""".repeat(100)
        val original = repetitive.toByteArray(Charsets.UTF_8)
        val compressed = KetoyCompression.gzip(original)
        assertTrue(
            "Compressed size (${compressed.size}) should be smaller than original (${original.size})",
            compressed.size < original.size
        )
    }

    @Test
    fun `isGzipped detects gzip magic bytes`() {
        val data = "test data".toByteArray(Charsets.UTF_8)
        val compressed = KetoyCompression.gzip(data)
        assertTrue(KetoyCompression.isGzipped(compressed))
        assertFalse(KetoyCompression.isGzipped(data))
    }

    @Test
    fun `isGzipped returns false for empty array`() {
        assertFalse(KetoyCompression.isGzipped(byteArrayOf()))
    }

    @Test
    fun `isGzipped returns false for single byte`() {
        assertFalse(KetoyCompression.isGzipped(byteArrayOf(0x1F)))
    }

    @Test
    fun `gzipString and gunzipToString round-trip`() {
        val original = """{"type":"Column","children":[{"type":"Text","props":{"text":"Hi"}}]}"""
        val compressed = KetoyCompression.gzipString(original)
        val restored = KetoyCompression.gunzipToString(compressed)
        assertEquals(original, restored)
    }

    @Test
    fun `autoDecompress handles gzipped data`() {
        val original = "auto decompress test".toByteArray(Charsets.UTF_8)
        val compressed = KetoyCompression.gzip(original)
        val result = KetoyCompression.autoDecompress(compressed)
        assertArrayEquals(original, result)
    }

    @Test
    fun `autoDecompress passes through non-gzipped data`() {
        val plain = "plain text data".toByteArray(Charsets.UTF_8)
        val result = KetoyCompression.autoDecompress(plain)
        assertArrayEquals(plain, result)
    }

    @Test
    fun `gzip handles empty input`() {
        val empty = byteArrayOf()
        val compressed = KetoyCompression.gzip(empty)
        val decompressed = KetoyCompression.gunzip(compressed)
        assertArrayEquals(empty, decompressed)
    }

    @Test
    fun `gzip handles large payload`() {
        val large = ByteArray(100_000) { (it % 256).toByte() }
        val compressed = KetoyCompression.gzip(large)
        val decompressed = KetoyCompression.gunzip(compressed)
        assertArrayEquals(large, decompressed)
    }
}
