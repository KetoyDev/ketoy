package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class KetoyKeyAliasTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `aliasKeys replaces known keys with short aliases`() {
        val input = json.parseToJsonElement("""{"type":"Text","props":{"text":"Hello"}}""")
        val aliased = KetoyKeyAlias.aliasKeys(input)
        val obj = aliased.jsonObject

        assertTrue("Should have alias 't' for 'type'", "t" in obj)
        assertTrue("Should have alias 'p' for 'props'", "p" in obj)
        assertFalse("Original key 'type' should not be present", "type" in obj)
        assertFalse("Original key 'props' should not be present", "props" in obj)
    }

    @Test
    fun `expandKeys restores aliases to full names`() {
        val aliased = json.parseToJsonElement("""{"t":"Text","p":{"tx":"Hello"}}""")
        val expanded = KetoyKeyAlias.expandKeys(aliased)
        val obj = expanded.jsonObject

        assertTrue("Should have 'type'", "type" in obj)
        assertTrue("Should have 'props'", "props" in obj)
    }

    @Test
    fun `round-trip alias then expand preserves structure`() {
        val original = json.parseToJsonElement("""
            {
                "type": "Column",
                "props": {
                    "modifier": {
                        "fillMaxSize": true,
                        "padding": {"all": 16},
                        "background": "#FFFFFF"
                    },
                    "verticalArrangement": "spaceBetween"
                },
                "children": [
                    {"type": "Text", "props": {"text": "Hello", "fontSize": 18}},
                    {"type": "Spacer", "props": {"height": 8}}
                ]
            }
        """.trimIndent())

        val aliased = KetoyKeyAlias.aliasKeys(original)
        val restored = KetoyKeyAlias.expandKeys(aliased)

        assertEquals(original, restored)
    }

    @Test
    fun `aliasKeys reduces payload size`() {
        val original = """{"type":"Column","props":{"modifier":{"fillMaxSize":true,"background":"#FFF","padding":{"all":16}},"verticalArrangement":"center"},"children":[{"type":"Text","props":{"text":"Hello","fontSize":24,"fontWeight":"bold","color":"#000"}}]}"""
        val originalElement = json.parseToJsonElement(original)
        val aliased = KetoyKeyAlias.aliasKeys(originalElement)

        val originalSize = original.length
        val aliasedSize = aliased.toString().length

        assertTrue(
            "Aliased ($aliasedSize) should be smaller than original ($originalSize)",
            aliasedSize < originalSize
        )
    }

    @Test
    fun `unknown keys are preserved unchanged`() {
        val input = json.parseToJsonElement("""{"type":"Text","customField":"value"}""")
        val aliased = KetoyKeyAlias.aliasKeys(input)
        val obj = aliased.jsonObject

        assertTrue("Unknown key should remain", "customField" in obj)
        assertEquals("value", obj["customField"]!!.jsonPrimitive.content)
    }

    @Test
    fun `aliasKeys handles nested arrays`() {
        val input = json.parseToJsonElement("""
            {"type":"Column","children":[
                {"type":"Text","props":{"text":"A"}},
                {"type":"Text","props":{"text":"B"}}
            ]}
        """.trimIndent())

        val aliased = KetoyKeyAlias.aliasKeys(input)
        val children = aliased.jsonObject["c"]?.jsonArray
        assertNotNull("Should have aliased children as 'c'", children)
        assertEquals(2, children!!.size)
    }

    @Test
    fun `encode and decode maps are bijective`() {
        // Every alias should map back to exactly one key
        val encode = KetoyKeyAlias.encode
        val decode = KetoyKeyAlias.decode

        assertEquals("Maps should have same size", encode.size, decode.size)

        for ((fullName, alias) in encode) {
            assertEquals(
                "Alias '$alias' should decode back to '$fullName'",
                fullName, decode[alias]
            )
        }
    }

    @Test
    fun `aliasKeys handles empty object`() {
        val input = json.parseToJsonElement("{}")
        val aliased = KetoyKeyAlias.aliasKeys(input)
        assertEquals(JsonObject(emptyMap()), aliased)
    }

    @Test
    fun `aliasKeys handles primitives`() {
        val input = JsonPrimitive("hello")
        val aliased = KetoyKeyAlias.aliasKeys(input)
        assertEquals(input, aliased)
    }

    @Test
    fun `aliasKeys handles modifier properties`() {
        val input = json.parseToJsonElement("""
            {"modifier": {"fillMaxWidth": true, "height": 100, "border": {"width": 1, "color": "#000"}}}
        """.trimIndent())

        val aliased = KetoyKeyAlias.aliasKeys(input)
        val obj = aliased.jsonObject

        assertTrue("'modifier' should be aliased to 'md'", "md" in obj)
        val mod = obj["md"]!!.jsonObject
        assertTrue("'fillMaxWidth' should be aliased to 'fw'", "fw" in mod)
        assertTrue("'height' should be aliased to 'h'", "h" in mod)
        assertTrue("'border' should be aliased to 'bd'", "bd" in mod)
    }
}
