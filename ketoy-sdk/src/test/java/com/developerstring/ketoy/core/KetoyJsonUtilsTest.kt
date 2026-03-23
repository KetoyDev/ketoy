package com.developerstring.ketoy.core

import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.wire.WireFormatConfig
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for JSON serialisation / deserialisation utilities.
 */
class KetoyJsonUtilsTest {

    // ─── toJson ─────────────────────────────────────────────────

    @Test
    fun `toJson serialises simple text node`() {
        val node = KTextNode(KTextProps(text = "Hello"))
        val json = node.toJson()
        assertTrue(json.contains("\"type\""))
        assertTrue(json.contains("\"Text\""))
        assertTrue(json.contains("\"Hello\""))
    }

    @Test
    fun `toJson serialises column with children`() {
        val node = KColumnNode(
            KColumnProps(verticalArrangement = "top"),
            listOf(KTextNode(KTextProps("A")), KTextNode(KTextProps("B")))
        )
        val json = node.toJson()
        assertTrue(json.contains("\"Column\""))
        assertTrue(json.contains("\"A\""))
        assertTrue(json.contains("\"B\""))
    }

    @Test
    fun `toJson excludes default values`() {
        val node = KSpacerNode(KSpacerProps(height = 16))
        val json = node.toJson()
        assertTrue(json.contains("\"height\""))
        // width should not appear since it's null and encodeDefaults = false
        assertFalse(json.contains("\"width\""))
    }

    @Test
    fun `toJson handles nested layouts`() {
        val node = KColumnNode(
            children = listOf(
                KRowNode(
                    children = listOf(
                        KTextNode(KTextProps("Nested"))
                    )
                )
            )
        )
        val json = node.toJson()
        assertTrue(json.contains("\"Column\""))
        assertTrue(json.contains("\"Row\""))
        assertTrue(json.contains("\"Nested\""))
    }

    // ─── parseKetoyJson ─────────────────────────────────────────

    @Test
    fun `parseKetoyJson round-trips text node`() {
        val original = KTextNode(KTextProps(text = "Round trip", fontSize = 18))
        val json = original.toJson()
        val parsed = parseKetoyJson(json)
        assertTrue(parsed is KTextNode)
        assertEquals("Round trip", (parsed as KTextNode).props.text)
        assertEquals(18, parsed.props.fontSize)
    }

    @Test
    fun `parseKetoyJson round-trips column with children`() {
        val original = KColumnNode(
            KColumnProps(verticalArrangement = "spaceBetween"),
            listOf(KTextNode(KTextProps("Child 1")), KSpacerNode(KSpacerProps(height = 8)))
        )
        val json = original.toJson()
        val parsed = parseKetoyJson(json) as KColumnNode
        assertEquals("spaceBetween", parsed.props.verticalArrangement)
        assertEquals(2, parsed.children.size)
        assertTrue(parsed.children[0] is KTextNode)
        assertTrue(parsed.children[1] is KSpacerNode)
    }

    @Test
    fun `parseKetoyJson round-trips button node`() {
        val original = KButtonNode(
            KButtonProps(containerColor = "#FF0000", shape = "circle"),
            listOf(KTextNode(KTextProps("Go")))
        )
        val json = original.toJson()
        val parsed = parseKetoyJson(json) as KButtonNode
        assertEquals("#FF0000", parsed.props.containerColor)
        assertEquals("circle", parsed.props.shape)
        assertEquals(1, parsed.children.size)
    }

    @Test
    fun `parseKetoyJson invalid JSON returns error text node`() {
        val parsed = parseKetoyJson("{invalid json}")
        assertTrue(parsed is KTextNode)
        assertTrue((parsed as KTextNode).props.text.contains("Failed to parse"))
    }

    @Test
    fun `parseKetoyJson empty object returns error text node`() {
        val parsed = parseKetoyJson("{}")
        assertTrue(parsed is KTextNode)
    }

    // ─── Modifier serialisation ─────────────────────────────────

    @Test
    fun `modifier with scroll fields serialises correctly`() {
        val node = KColumnNode(
            KColumnProps(modifier = KModifier(verticalScroll = KScrollConfig.Default, horizontalScroll = KScrollConfig.Default))
        )
        val json = node.toJson()
        assertTrue(json.contains("\"verticalScroll\""))
        assertTrue(json.contains("\"horizontalScroll\""))
    }

    @Test
    fun `modifier with padding serialises correctly`() {
        val node = KBoxNode(
            KBoxProps(modifier = KModifier(padding = KPadding(all = 16)))
        )
        val json = node.toJson()
        assertTrue(json.contains("\"padding\""))
        assertTrue(json.contains("\"all\""))
    }

    @Test
    fun `modifier with gradient serialises correctly`() {
        val gradient = KGradient(type = "linear", colors = listOf("#FF0000", "#0000FF"))
        val node = KBoxNode(
            KBoxProps(modifier = KModifier(gradient = gradient))
        )
        val json = node.toJson()
        assertTrue(json.contains("\"gradient\""))
        assertTrue(json.contains("\"linear\""))
    }

    // ─── Card node serialisation ────────────────────────────────

    @Test
    fun `card node round-trips`() {
        val card = KCardNode(
            KCardProps(containerColor = "#FFFFFF", elevation = 4, shape = "rounded_16"),
            mutableListOf(KTextNode(KTextProps("Inside card")))
        )
        val json = card.toJson()
        val parsed = parseKetoyJson(json) as KCardNode
        assertEquals("#FFFFFF", parsed.props.containerColor)
        assertEquals(4, parsed.props.elevation)
        assertEquals(1, parsed.children.size)
    }

    // ─── Image node serialisation ───────────────────────────────

    @Test
    fun `image node with URL round-trips`() {
        val img = KImageNode(KImageProps(
            source = KUrlImageSource("https://example.com/img.png"),
            contentDescription = "test",
            scaleType = KScaleType.CenterCrop
        ))
        val json = img.toJson()
        val parsed = parseKetoyJson(json) as KImageNode
        assertTrue(parsed.props.source is KUrlImageSource)
        assertEquals("test", parsed.props.contentDescription)
        assertEquals(KScaleType.CenterCrop, parsed.props.scaleType)
    }

    // ─── Spacer ─────────────────────────────────────────────────

    @Test
    fun `spacer node round-trips`() {
        val spacer = KSpacerNode(KSpacerProps(height = 32))
        val json = spacer.toJson()
        val parsed = parseKetoyJson(json) as KSpacerNode
        assertEquals(32, parsed.props.height)
        assertNull(parsed.props.width)
    }

    // ─── Deep nesting ───────────────────────────────────────────

    @Test
    fun `deeply nested tree round-trips correctly`() {
        val tree = KColumnNode(children = listOf(
            KRowNode(children = listOf(
                KBoxNode(children = listOf(
                    KTextNode(KTextProps("Deep"))
                ))
            ))
        ))
        val json = tree.toJson()
        val parsed = parseKetoyJson(json) as KColumnNode
        val row = parsed.children[0] as KRowNode
        val box = row.children[0] as KBoxNode
        val text = box.children[0] as KTextNode
        assertEquals("Deep", text.props.text)
    }

    // ─── toMinifiedJson ─────────────────────────────────────────

    @Test
    fun `toMinifiedJson produces compact output without whitespace`() {
        val node = KColumnNode(
            KColumnProps(verticalArrangement = "top"),
            listOf(KTextNode(KTextProps("Hello")))
        )
        val minified = node.toMinifiedJson()
        assertFalse("Should not contain newlines", minified.contains("\n"))
        assertTrue(minified.contains("\"Hello\""))
        assertTrue(minified.length < node.toJson().length)
    }

    // ─── toWireBytes / parseKetoyWireBytes ───────────────────────

    @Test
    fun `toWireBytes and parseKetoyWireBytes round-trip text node`() {
        val original = KTextNode(KTextProps(text = "Wire test", fontSize = 20))
        val wireBytes = original.toWireBytes()
        val parsed = parseKetoyWireBytes(wireBytes)
        assertTrue(parsed is KTextNode)
        assertEquals("Wire test", (parsed as KTextNode).props.text)
        assertEquals(20, parsed.props.fontSize)
    }

    @Test
    fun `toWireBytes and parseKetoyWireBytes round-trip complex tree`() {
        val original = KColumnNode(
            KColumnProps(verticalArrangement = "spaceBetween"),
            listOf(
                KTextNode(KTextProps("Child 1", fontSize = 18)),
                KSpacerNode(KSpacerProps(height = 8)),
                KButtonNode(
                    KButtonProps(containerColor = "#2196F3"),
                    listOf(KTextNode(KTextProps("Click")))
                )
            )
        )
        val wireBytes = original.toWireBytes()
        val parsed = parseKetoyWireBytes(wireBytes) as KColumnNode
        assertEquals(3, parsed.children.size)
        assertTrue(parsed.children[0] is KTextNode)
        assertTrue(parsed.children[1] is KSpacerNode)
        assertTrue(parsed.children[2] is KButtonNode)
    }

    @Test
    fun `toWireBytes produces smaller output than toJson`() {
        val node = KColumnNode(
            KColumnProps(
                verticalArrangement = "spaceBetween",
                modifier = KModifier(fillMaxSize = 1f, padding = KPadding(all = 16))
            ),
            listOf(
                KTextNode(KTextProps("Hello", fontSize = 24, fontWeight = "bold")),
                KTextNode(KTextProps("World", fontSize = 18)),
                KSpacerNode(KSpacerProps(height = 16)),
                KButtonNode(children = listOf(KTextNode(KTextProps("Go"))))
            )
        )
        val jsonSize = node.toJson().toByteArray(Charsets.UTF_8).size
        val wireSize = node.toWireBytes().size
        assertTrue(
            "Wire bytes ($wireSize) should be smaller than JSON ($jsonSize)",
            wireSize < jsonSize
        )
    }

    @Test
    fun `toWireBytes with GZIP_ONLY config round-trips`() {
        val original = KTextNode(KTextProps(text = "Gzip test"))
        val wireBytes = original.toWireBytes(WireFormatConfig.GZIP_ONLY)
        val parsed = parseKetoyWireBytes(wireBytes)
        assertTrue(parsed is KTextNode)
        assertEquals("Gzip test", (parsed as KTextNode).props.text)
    }

    @Test
    fun `parseKetoyWireBytes returns error node for invalid data`() {
        val garbage = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val result = parseKetoyWireBytes(garbage)
        assertTrue(result is KTextNode)
        assertTrue((result as KTextNode).props.text.contains("Failed to decode"))
    }
}
