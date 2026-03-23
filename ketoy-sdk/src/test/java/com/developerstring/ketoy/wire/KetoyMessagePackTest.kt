package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class KetoyMessagePackTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Primitives ──────────────────────────────────────────────

    @Test
    fun `round-trip null`() {
        val input = JsonNull
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip boolean true`() {
        val input = JsonPrimitive(true)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip boolean false`() {
        val input = JsonPrimitive(false)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip positive fixint`() {
        val input = JsonPrimitive(42)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(42, decoded.jsonPrimitive.int)
    }

    @Test
    fun `round-trip zero`() {
        val input = JsonPrimitive(0)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(0, decoded.jsonPrimitive.int)
    }

    @Test
    fun `round-trip negative fixint`() {
        val input = JsonPrimitive(-1)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(-1, decoded.jsonPrimitive.int)
    }

    @Test
    fun `round-trip int16`() {
        val input = JsonPrimitive(1000)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(1000, decoded.jsonPrimitive.int)
    }

    @Test
    fun `round-trip negative int16`() {
        val input = JsonPrimitive(-1000)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(-1000, decoded.jsonPrimitive.int)
    }

    @Test
    fun `round-trip int32`() {
        val input = JsonPrimitive(100_000)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(100_000, decoded.jsonPrimitive.int)
    }

    @Test
    fun `round-trip float64`() {
        val input = JsonPrimitive(3.14159)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(3.14159, decoded.jsonPrimitive.double, 0.00001)
    }

    @Test
    fun `round-trip short string`() {
        val input = JsonPrimitive("Hello")
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals("Hello", decoded.jsonPrimitive.content)
    }

    @Test
    fun `round-trip empty string`() {
        val input = JsonPrimitive("")
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals("", decoded.jsonPrimitive.content)
    }

    @Test
    fun `round-trip long string (str8)`() {
        val longStr = "A".repeat(200)
        val input = JsonPrimitive(longStr)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(longStr, decoded.jsonPrimitive.content)
    }

    // ── Collections ─────────────────────────────────────────────

    @Test
    fun `round-trip empty array`() {
        val input = JsonArray(emptyList())
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip array with mixed types`() {
        val input = buildJsonArray {
            add(JsonPrimitive(1))
            add(JsonPrimitive("two"))
            add(JsonPrimitive(true))
            add(JsonNull)
        }
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip empty object`() {
        val input = JsonObject(emptyMap())
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip simple object`() {
        val input = buildJsonObject {
            put("name", "Ketoy")
            put("version", 1)
            put("active", true)
        }
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    // ── SDUI payloads ───────────────────────────────────────────

    @Test
    fun `round-trip realistic SDUI component tree`() {
        val input = json.parseToJsonElement("""
            {
                "type": "Column",
                "props": {
                    "modifier": {
                        "fillMaxSize": true,
                        "padding": {"all": 16}
                    },
                    "verticalArrangement": "spaceBetween"
                },
                "children": [
                    {"type": "Text", "props": {"text": "Hello World", "fontSize": 24, "fontWeight": "bold"}},
                    {"type": "Spacer", "props": {"height": 16}},
                    {"type": "Button", "children": [
                        {"type": "Text", "props": {"text": "Click Me"}}
                    ]},
                    {"type": "Image", "props": {"source": "https://example.com/img.png"}}
                ]
            }
        """.trimIndent())

        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `MessagePack produces smaller output than JSON for SDUI payload`() {
        val payload = json.parseToJsonElement("""
            {"type":"Column","props":{"modifier":{"fillMaxSize":true}},"children":[
                {"type":"Text","props":{"text":"Hello","fontSize":24}},
                {"type":"Text","props":{"text":"World","fontSize":18}},
                {"type":"Spacer","props":{"height":8}},
                {"type":"Button","children":[{"type":"Text","props":{"text":"Go"}}]}
            ]}
        """.trimIndent())

        val jsonBytes = payload.toString().toByteArray(Charsets.UTF_8)
        val msgpackBytes = KetoyMessagePack.encode(payload)

        assertTrue(
            "MessagePack (${msgpackBytes.size}) should be smaller than JSON (${jsonBytes.size})",
            msgpackBytes.size < jsonBytes.size
        )
    }

    @Test
    fun `round-trip nested arrays`() {
        val input = buildJsonArray {
            add(buildJsonArray {
                add(JsonPrimitive(1))
                add(JsonPrimitive(2))
            })
            add(buildJsonArray {
                add(JsonPrimitive(3))
                add(JsonPrimitive(4))
            })
        }
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip deeply nested object`() {
        val input = buildJsonObject {
            put("a", buildJsonObject {
                put("b", buildJsonObject {
                    put("c", buildJsonObject {
                        put("value", 42)
                    })
                })
            })
        }
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(input, decoded)
    }

    @Test
    fun `round-trip large int64 value`() {
        val input = JsonPrimitive(Long.MAX_VALUE)
        val packed = KetoyMessagePack.encode(input)
        val decoded = KetoyMessagePack.decode(packed)
        assertEquals(Long.MAX_VALUE, decoded.jsonPrimitive.long)
    }
}
