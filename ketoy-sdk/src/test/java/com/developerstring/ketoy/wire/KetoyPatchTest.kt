package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class KetoyPatchTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Add ─────────────────────────────────────────────────────

    @Test
    fun `add inserts value at object path`() {
        val doc = json.parseToJsonElement("""{"a":1}""")
        val ops = listOf(PatchOp(op = "add", path = "/b", value = JsonPrimitive(2)))
        val result = KetoyPatch.apply(doc, ops)
        assertEquals(2, result.jsonObject["b"]!!.jsonPrimitive.int)
    }

    @Test
    fun `add inserts value into array`() {
        val doc = json.parseToJsonElement("""{"items":[1,2,3]}""")
        val ops = listOf(PatchOp(op = "add", path = "/items/1", value = JsonPrimitive(99)))
        val result = KetoyPatch.apply(doc, ops)
        val items = result.jsonObject["items"]!!.jsonArray
        assertEquals(4, items.size)
        assertEquals(99, items[1].jsonPrimitive.int)
        assertEquals(2, items[2].jsonPrimitive.int)
    }

    @Test
    fun `add appends to array with dash`() {
        val doc = json.parseToJsonElement("""{"items":[1,2]}""")
        val ops = listOf(PatchOp(op = "add", path = "/items/-", value = JsonPrimitive(3)))
        val result = KetoyPatch.apply(doc, ops)
        val items = result.jsonObject["items"]!!.jsonArray
        assertEquals(3, items.size)
        assertEquals(3, items[2].jsonPrimitive.int)
    }

    @Test
    fun `add replaces root`() {
        val doc = json.parseToJsonElement("""{"old":"data"}""")
        val ops = listOf(PatchOp(op = "add", path = "", value = JsonPrimitive("replaced")))
        val result = KetoyPatch.apply(doc, ops)
        assertEquals("replaced", result.jsonPrimitive.content)
    }

    // ── Remove ──────────────────────────────────────────────────

    @Test
    fun `remove deletes key from object`() {
        val doc = json.parseToJsonElement("""{"a":1,"b":2}""")
        val ops = listOf(PatchOp(op = "remove", path = "/b"))
        val result = KetoyPatch.apply(doc, ops)
        assertFalse("b" in result.jsonObject)
        assertEquals(1, result.jsonObject["a"]!!.jsonPrimitive.int)
    }

    @Test
    fun `remove deletes element from array`() {
        val doc = json.parseToJsonElement("""{"items":[1,2,3]}""")
        val ops = listOf(PatchOp(op = "remove", path = "/items/1"))
        val result = KetoyPatch.apply(doc, ops)
        val items = result.jsonObject["items"]!!.jsonArray
        assertEquals(2, items.size)
        assertEquals(1, items[0].jsonPrimitive.int)
        assertEquals(3, items[1].jsonPrimitive.int)
    }

    @Test(expected = PatchException::class)
    fun `remove root throws`() {
        val doc = json.parseToJsonElement("""{"a":1}""")
        KetoyPatch.apply(doc, listOf(PatchOp(op = "remove", path = "")))
    }

    // ── Replace ─────────────────────────────────────────────────

    @Test
    fun `replace updates existing value`() {
        val doc = json.parseToJsonElement("""{"a":1,"b":2}""")
        val ops = listOf(PatchOp(op = "replace", path = "/a", value = JsonPrimitive(99)))
        val result = KetoyPatch.apply(doc, ops)
        assertEquals(99, result.jsonObject["a"]!!.jsonPrimitive.int)
    }

    @Test(expected = PatchException::class)
    fun `replace non-existent key throws`() {
        val doc = json.parseToJsonElement("""{"a":1}""")
        KetoyPatch.apply(doc, listOf(PatchOp(op = "replace", path = "/z", value = JsonPrimitive(1))))
    }

    // ── Move ────────────────────────────────────────────────────

    @Test
    fun `move relocates value`() {
        val doc = json.parseToJsonElement("""{"a":1,"b":2}""")
        val ops = listOf(PatchOp(op = "move", path = "/c", from = "/a"))
        val result = KetoyPatch.apply(doc, ops)
        assertFalse("a" in result.jsonObject)
        assertEquals(1, result.jsonObject["c"]!!.jsonPrimitive.int)
    }

    // ── Copy ────────────────────────────────────────────────────

    @Test
    fun `copy duplicates value`() {
        val doc = json.parseToJsonElement("""{"a":1}""")
        val ops = listOf(PatchOp(op = "copy", path = "/b", from = "/a"))
        val result = KetoyPatch.apply(doc, ops)
        assertEquals(1, result.jsonObject["a"]!!.jsonPrimitive.int)
        assertEquals(1, result.jsonObject["b"]!!.jsonPrimitive.int)
    }

    // ── Test ────────────────────────────────────────────────────

    @Test
    fun `test passes when value matches`() {
        val doc = json.parseToJsonElement("""{"a":1}""")
        val ops = listOf(PatchOp(op = "test", path = "/a", value = JsonPrimitive(1)))
        val result = KetoyPatch.apply(doc, ops)
        assertEquals(doc, result)
    }

    @Test(expected = PatchException::class)
    fun `test fails when value differs`() {
        val doc = json.parseToJsonElement("""{"a":1}""")
        KetoyPatch.apply(doc, listOf(PatchOp(op = "test", path = "/a", value = JsonPrimitive(2))))
    }

    // ── Diff ────────────────────────────────────────────────────

    @Test
    fun `diff identical documents produces empty patch`() {
        val doc = json.parseToJsonElement("""{"a":1,"b":"hello"}""")
        val patch = KetoyPatch.diff(doc, doc)
        assertTrue(patch.isEmpty())
    }

    @Test
    fun `diff detects added key`() {
        val source = json.parseToJsonElement("""{"a":1}""")
        val target = json.parseToJsonElement("""{"a":1,"b":2}""")
        val patch = KetoyPatch.diff(source, target)

        assertTrue(patch.any { it.op == "add" && it.path == "/b" })
    }

    @Test
    fun `diff detects removed key`() {
        val source = json.parseToJsonElement("""{"a":1,"b":2}""")
        val target = json.parseToJsonElement("""{"a":1}""")
        val patch = KetoyPatch.diff(source, target)

        assertTrue(patch.any { it.op == "remove" && it.path == "/b" })
    }

    @Test
    fun `diff detects replaced value`() {
        val source = json.parseToJsonElement("""{"a":1}""")
        val target = json.parseToJsonElement("""{"a":99}""")
        val patch = KetoyPatch.diff(source, target)

        assertTrue(patch.any { it.op == "replace" && it.path == "/a" })
    }

    @Test
    fun `diff and apply round-trip produces target`() {
        val source = json.parseToJsonElement("""
            {"type":"Column","children":[
                {"type":"Text","props":{"text":"Hello"}},
                {"type":"Spacer","props":{"height":8}}
            ]}
        """.trimIndent())

        val target = json.parseToJsonElement("""
            {"type":"Column","children":[
                {"type":"Text","props":{"text":"Updated Title"}},
                {"type":"Spacer","props":{"height":16}},
                {"type":"Button","children":[{"type":"Text","props":{"text":"New"}}]}
            ]}
        """.trimIndent())

        val patch = KetoyPatch.diff(source, target)
        val result = KetoyPatch.apply(source, patch)
        assertEquals(target, result)
    }

    @Test
    fun `diff handles array shrinkage`() {
        val source = json.parseToJsonElement("""[1,2,3,4,5]""")
        val target = json.parseToJsonElement("""[1,2]""")
        val patch = KetoyPatch.diff(source, target)
        val result = KetoyPatch.apply(source, patch)
        assertEquals(target, result)
    }

    @Test
    fun `diff handles array growth`() {
        val source = json.parseToJsonElement("""[1,2]""")
        val target = json.parseToJsonElement("""[1,2,3,4]""")
        val patch = KetoyPatch.diff(source, target)
        val result = KetoyPatch.apply(source, patch)
        assertEquals(target, result)
    }

    // ── Path parsing ────────────────────────────────────────────

    @Test
    fun `parsePath handles tilde escaping`() {
        val segments = KetoyPatch.parsePath("/a~0b/c~1d")
        assertEquals(listOf("a~b", "c/d"), segments)
    }

    @Test
    fun `parsePath handles empty path`() {
        val segments = KetoyPatch.parsePath("")
        assertTrue(segments.isEmpty())
    }

    @Test(expected = PatchException::class)
    fun `parsePath rejects path without leading slash`() {
        KetoyPatch.parsePath("invalid/path")
    }

    // ── Sequential operations ───────────────────────────────────

    @Test
    fun `multiple operations apply sequentially`() {
        val doc = json.parseToJsonElement("""{"a":1,"b":2}""")
        val ops = listOf(
            PatchOp(op = "add", path = "/c", value = JsonPrimitive(3)),
            PatchOp(op = "remove", path = "/a"),
            PatchOp(op = "replace", path = "/b", value = JsonPrimitive(20))
        )
        val result = KetoyPatch.apply(doc, ops)
        assertFalse("a" in result.jsonObject)
        assertEquals(20, result.jsonObject["b"]!!.jsonPrimitive.int)
        assertEquals(3, result.jsonObject["c"]!!.jsonPrimitive.int)
    }

    // ── SDUI-specific scenarios ─────────────────────────────────

    @Test
    fun `patch updates text in nested component`() {
        val doc = json.parseToJsonElement("""
            {"type":"Column","children":[
                {"type":"Text","props":{"text":"Old Title"}}
            ]}
        """.trimIndent())

        val ops = listOf(
            PatchOp(op = "replace", path = "/children/0/props/text", value = JsonPrimitive("New Title"))
        )

        val result = KetoyPatch.apply(doc, ops)
        val text = result.jsonObject["children"]!!.jsonArray[0]
            .jsonObject["props"]!!.jsonObject["text"]!!.jsonPrimitive.content
        assertEquals("New Title", text)
    }

    @Test
    fun `diff for SDUI text change produces small patch`() {
        val source = json.parseToJsonElement("""
            {"type":"Column","children":[
                {"type":"Text","props":{"text":"Hello","fontSize":24,"fontWeight":"bold","color":"#000"}},
                {"type":"Spacer","props":{"height":8}},
                {"type":"Image","props":{"source":"img.png","contentDescription":"photo"}}
            ]}
        """.trimIndent())

        val target = json.parseToJsonElement("""
            {"type":"Column","children":[
                {"type":"Text","props":{"text":"Updated","fontSize":24,"fontWeight":"bold","color":"#000"}},
                {"type":"Spacer","props":{"height":8}},
                {"type":"Image","props":{"source":"img.png","contentDescription":"photo"}}
            ]}
        """.trimIndent())

        val patch = KetoyPatch.diff(source, target)

        // Only the text value changed — patch should be very small
        assertEquals(1, patch.size)
        assertEquals("replace", patch[0].op)
        assertTrue(patch[0].path.endsWith("/text"))
    }
}
