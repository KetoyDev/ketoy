package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class KetoyWireFormatTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val samplePayload = """
        {"type":"Column","props":{"modifier":{"fillMaxSize":true,"padding":{"all":16}},"verticalArrangement":"spaceBetween"},"children":[{"type":"Text","props":{"text":"Hello World","fontSize":24,"fontWeight":"bold","color":"#333333"}},{"type":"Spacer","props":{"height":16}},{"type":"Row","props":{"modifier":{"fillMaxWidth":true},"horizontalArrangement":"spaceBetween"},"children":[{"type":"Button","children":[{"type":"Text","props":{"text":"Cancel"}}]},{"type":"Button","props":{"containerColor":"#2196F3"},"children":[{"type":"Text","props":{"text":"Submit","color":"#FFFFFF"}}]}]},{"type":"Image","props":{"source":"https://example.com/banner.png","contentDescription":"Banner"}}]}
    """.trimIndent()

    // ── WireFormatConfig.NONE ───────────────────────────────────

    @Test
    fun `NONE config passes through JSON unchanged`() {
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.NONE)
        val decoded = KetoyWireFormat.decode(encoded, WireFormatConfig.NONE)
        val originalElement = json.parseToJsonElement(samplePayload)
        val decodedElement = json.parseToJsonElement(decoded)
        assertEquals(originalElement, decodedElement)
    }

    @Test
    fun `NONE config output is plain UTF-8 JSON`() {
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.NONE)
        val text = encoded.toString(Charsets.UTF_8)
        assertTrue("Should start with {", text.startsWith("{"))
    }

    // ── WireFormatConfig.GZIP_ONLY ──────────────────────────────

    @Test
    fun `GZIP_ONLY round-trip preserves data`() {
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.GZIP_ONLY)
        assertTrue("Output should be gzipped", KetoyCompression.isGzipped(encoded))

        val decoded = KetoyWireFormat.decode(encoded, WireFormatConfig.GZIP_ONLY)
        val originalElement = json.parseToJsonElement(samplePayload)
        val decodedElement = json.parseToJsonElement(decoded)
        assertEquals(originalElement, decodedElement)
    }

    @Test
    fun `GZIP_ONLY reduces payload size`() {
        val originalSize = samplePayload.toByteArray(Charsets.UTF_8).size
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.GZIP_ONLY)
        assertTrue(
            "Gzipped (${encoded.size}) should be smaller than original ($originalSize)",
            encoded.size < originalSize
        )
    }

    // ── WireFormatConfig.ALIASED ────────────────────────────────

    @Test
    fun `ALIASED round-trip preserves data`() {
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.ALIASED)
        val decoded = KetoyWireFormat.decode(encoded, WireFormatConfig.ALIASED)
        val originalElement = json.parseToJsonElement(samplePayload)
        val decodedElement = json.parseToJsonElement(decoded)
        assertEquals(originalElement, decodedElement)
    }

    @Test
    fun `ALIASED reduces size more than GZIP_ONLY`() {
        val gzipOnly = KetoyWireFormat.encode(samplePayload, WireFormatConfig.GZIP_ONLY)
        val aliased = KetoyWireFormat.encode(samplePayload, WireFormatConfig.ALIASED)
        assertTrue(
            "ALIASED (${aliased.size}) should be smaller than GZIP_ONLY (${gzipOnly.size})",
            aliased.size < gzipOnly.size
        )
    }

    // ── WireFormatConfig.OPTIMIZED ──────────────────────────────

    @Test
    fun `OPTIMIZED round-trip preserves data`() {
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.OPTIMIZED)
        val decoded = KetoyWireFormat.decode(encoded, WireFormatConfig.OPTIMIZED)
        val originalElement = json.parseToJsonElement(samplePayload)
        val decodedElement = json.parseToJsonElement(decoded)
        assertEquals(originalElement, decodedElement)
    }

    @Test
    fun `OPTIMIZED produces smallest output`() {
        val none = KetoyWireFormat.encode(samplePayload, WireFormatConfig.NONE)
        val gzipOnly = KetoyWireFormat.encode(samplePayload, WireFormatConfig.GZIP_ONLY)
        val aliased = KetoyWireFormat.encode(samplePayload, WireFormatConfig.ALIASED)
        val optimized = KetoyWireFormat.encode(samplePayload, WireFormatConfig.OPTIMIZED)

        assertTrue("OPTIMIZED (${optimized.size}) < NONE (${none.size})",
            optimized.size < none.size)
        assertTrue("OPTIMIZED (${optimized.size}) < GZIP_ONLY (${gzipOnly.size})",
            optimized.size < gzipOnly.size)
        assertTrue("OPTIMIZED (${optimized.size}) <= ALIASED (${aliased.size})",
            optimized.size <= aliased.size)
    }

    // ── autoDecode ──────────────────────────────────────────────

    @Test
    fun `autoDecode handles plain JSON`() {
        val bytes = samplePayload.toByteArray(Charsets.UTF_8)
        val decoded = KetoyWireFormat.autoDecode(bytes)
        val expected = json.parseToJsonElement(samplePayload)
        assertEquals(expected, decoded)
    }

    @Test
    fun `autoDecode handles gzipped JSON`() {
        val gzipped = KetoyCompression.gzipString(samplePayload)
        val decoded = KetoyWireFormat.autoDecode(gzipped)
        val expected = json.parseToJsonElement(samplePayload)
        assertEquals(expected, decoded)
    }

    @Test
    fun `autoDecode handles OPTIMIZED bytes`() {
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.OPTIMIZED)
        val decoded = KetoyWireFormat.autoDecode(encoded)
        val expected = json.parseToJsonElement(samplePayload)
        assertEquals(expected, decoded)
    }

    @Test
    fun `autoDecode handles ALIASED bytes`() {
        val encoded = KetoyWireFormat.encode(samplePayload, WireFormatConfig.ALIASED)
        val decoded = KetoyWireFormat.autoDecode(encoded)
        val expected = json.parseToJsonElement(samplePayload)
        assertEquals(expected, decoded)
    }

    // ── encodeElement ───────────────────────────────────────────

    @Test
    fun `encodeElement works with JsonElement input`() {
        val element = json.parseToJsonElement(samplePayload)
        val encoded = KetoyWireFormat.encodeElement(element, WireFormatConfig.OPTIMIZED)
        val decoded = KetoyWireFormat.autoDecode(encoded)
        assertEquals(element, decoded)
    }

    // ── measureCompression ──────────────────────────────────────

    @Test
    fun `measureCompression returns valid stats`() {
        val stats = KetoyWireFormat.measureCompression(samplePayload, WireFormatConfig.OPTIMIZED)

        assertTrue("Original size should be positive", stats.originalSize > 0)
        assertTrue("After aliasing should be smaller than original",
            stats.afterAliasing < stats.originalSize)
        assertTrue("After type compression should be smaller than after aliasing",
            stats.afterTypeCompression <= stats.afterAliasing)
        assertTrue("After gzip should be smallest", stats.afterGzip <= stats.afterBinaryEncoding)
        assertTrue("Total reduction should be > 1", stats.totalReduction > 1.0f)
    }

    @Test
    fun `measureCompression with NONE config shows no reduction`() {
        val stats = KetoyWireFormat.measureCompression(samplePayload, WireFormatConfig.NONE)
        assertEquals(stats.originalSize, stats.afterAliasing)
        assertEquals(stats.originalSize, stats.afterTypeCompression)
        assertEquals(1.0f, stats.totalReduction, 0.01f)
    }

    // ── Content-Type constants ──────────────────────────────────

    @Test
    fun `content type constants are well-formed`() {
        assertTrue(KetoyWireFormat.CONTENT_TYPE_KETOY_BINARY.contains("msgpack"))
        assertTrue(KetoyWireFormat.CONTENT_TYPE_KETOY_JSON.contains("json"))
        assertTrue(KetoyWireFormat.ACCEPT_HEADER.contains(KetoyWireFormat.CONTENT_TYPE_KETOY_BINARY))
        assertTrue(KetoyWireFormat.ACCEPT_HEADER.contains("application/json"))
    }

    // ── Edge cases ──────────────────────────────────────────────

    @Test
    fun `encode and decode empty object`() {
        val payload = "{}"
        for (config in listOf(WireFormatConfig.NONE, WireFormatConfig.GZIP_ONLY,
            WireFormatConfig.ALIASED, WireFormatConfig.OPTIMIZED)) {
            val encoded = KetoyWireFormat.encode(payload, config)
            val decoded = KetoyWireFormat.decode(encoded, config)
            assertEquals(json.parseToJsonElement(payload), json.parseToJsonElement(decoded))
        }
    }

    @Test
    fun `full pipeline round-trip with all config presets`() {
        val configs = listOf(
            WireFormatConfig.NONE,
            WireFormatConfig.GZIP_ONLY,
            WireFormatConfig.ALIASED,
            WireFormatConfig.OPTIMIZED
        )
        val originalElement = json.parseToJsonElement(samplePayload)

        for (config in configs) {
            val encoded = KetoyWireFormat.encode(samplePayload, config)
            val decoded = KetoyWireFormat.decode(encoded, config)
            val decodedElement = json.parseToJsonElement(decoded)
            assertEquals(
                "Round-trip failed for config: $config",
                originalElement, decodedElement
            )
        }
    }

    @Test
    fun `custom WireFormatConfig with only key aliasing`() {
        val config = WireFormatConfig(
            keyAliasing = true,
            typeCompression = false,
            binaryEncoding = BinaryEncoding.NONE,
            gzipCompression = false
        )
        val encoded = KetoyWireFormat.encode(samplePayload, config)
        val decoded = KetoyWireFormat.decode(encoded, config)
        val originalElement = json.parseToJsonElement(samplePayload)
        val decodedElement = json.parseToJsonElement(decoded)
        assertEquals(originalElement, decodedElement)
    }

    @Test
    fun `custom WireFormatConfig with only type compression`() {
        val config = WireFormatConfig(
            keyAliasing = false,
            typeCompression = true,
            binaryEncoding = BinaryEncoding.NONE,
            gzipCompression = false
        )
        val encoded = KetoyWireFormat.encode(samplePayload, config)
        val decoded = KetoyWireFormat.decode(encoded, config)
        val originalElement = json.parseToJsonElement(samplePayload)
        val decodedElement = json.parseToJsonElement(decoded)
        assertEquals(originalElement, decodedElement)
    }

    @Test
    fun `custom WireFormatConfig with only MessagePack`() {
        val config = WireFormatConfig(
            keyAliasing = false,
            typeCompression = false,
            binaryEncoding = BinaryEncoding.MESSAGE_PACK,
            gzipCompression = false
        )
        val encoded = KetoyWireFormat.encode(samplePayload, config)
        val decoded = KetoyWireFormat.decode(encoded, config)
        val originalElement = json.parseToJsonElement(samplePayload)
        val decodedElement = json.parseToJsonElement(decoded)
        assertEquals(originalElement, decodedElement)
    }
}
