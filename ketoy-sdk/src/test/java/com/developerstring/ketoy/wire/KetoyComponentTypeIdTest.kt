package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class KetoyComponentTypeIdTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `compressTypes replaces type strings with integers`() {
        val input = json.parseToJsonElement("""{"type":"Column","children":[{"type":"Text"}]}""")
        val compressed = KetoyComponentTypeId.compressTypes(input)
        val obj = compressed.jsonObject

        assertEquals(1, obj["type"]!!.jsonPrimitive.int)
        val child = obj["children"]!!.jsonArray[0].jsonObject
        assertEquals(11, child["type"]!!.jsonPrimitive.int)
    }

    @Test
    fun `expandTypes restores integers to type strings`() {
        val input = json.parseToJsonElement("""{"type":1,"children":[{"type":11}]}""")
        val expanded = KetoyComponentTypeId.expandTypes(input)
        val obj = expanded.jsonObject

        assertEquals("Column", obj["type"]!!.jsonPrimitive.content)
        val child = obj["children"]!!.jsonArray[0].jsonObject
        assertEquals("Text", child["type"]!!.jsonPrimitive.content)
    }

    @Test
    fun `round-trip compress then expand preserves structure`() {
        val original = json.parseToJsonElement("""
            {"type":"Scaffold","children":[
                {"type":"TopAppBar","props":{"title":"Home"}},
                {"type":"Column","children":[
                    {"type":"Text","props":{"text":"Hello"}},
                    {"type":"Button","children":[{"type":"Text","props":{"text":"Click"}}]},
                    {"type":"Image","props":{"source":"url"}},
                    {"type":"Spacer","props":{"height":8}}
                ]}
            ]}
        """.trimIndent())

        val compressed = KetoyComponentTypeId.compressTypes(original)
        val restored = KetoyComponentTypeId.expandTypes(compressed)

        assertEquals(original, restored)
    }

    @Test
    fun `resolveId is case-insensitive`() {
        assertEquals(1, KetoyComponentTypeId.resolveId("Column"))
        assertEquals(1, KetoyComponentTypeId.resolveId("column"))
        assertEquals(1, KetoyComponentTypeId.resolveId("COLUMN"))
    }

    @Test
    fun `resolveId returns null for unknown types`() {
        assertNull(KetoyComponentTypeId.resolveId("UnknownWidget"))
    }

    @Test
    fun `unknown type names are preserved as strings during compression`() {
        val input = json.parseToJsonElement("""{"type":"MyCustomWidget","props":{}}""")
        val compressed = KetoyComponentTypeId.compressTypes(input)
        val typeValue = compressed.jsonObject["type"]!!.jsonPrimitive

        assertTrue("Custom types should remain as strings", typeValue.isString)
        assertEquals("MyCustomWidget", typeValue.content)
    }

    @Test
    fun `unknown integer IDs are preserved during expansion`() {
        val input = json.parseToJsonElement("""{"type":999}""")
        val expanded = KetoyComponentTypeId.expandTypes(input)
        val typeValue = expanded.jsonObject["type"]!!.jsonPrimitive

        assertEquals(999, typeValue.int)
    }

    @Test
    fun `all layout types have IDs in range 1-10`() {
        val layouts = listOf("Column", "Row", "Box", "LazyColumn", "LazyRow")
        for (layout in layouts) {
            val id = KetoyComponentTypeId.resolveId(layout)
            assertNotNull("$layout should have an ID", id)
            assertTrue("$layout ID ($id) should be in 1-10", id!! in 1..10)
        }
    }

    @Test
    fun `all widget types have IDs in range 11-30`() {
        val widgets = listOf("Text", "Button", "Spacer", "Card", "TextField", "Image", "Icon", "IconButton", "Component")
        for (widget in widgets) {
            val id = KetoyComponentTypeId.resolveId(widget)
            assertNotNull("$widget should have an ID", id)
            assertTrue("$widget ID ($id) should be in 11-30", id!! in 11..30)
        }
    }

    @Test
    fun `all scaffold types have IDs in range 31-60`() {
        val scaffolds = listOf("Scaffold", "TopAppBar", "BottomAppBar", "NavigationBar",
            "FloatingActionButton", "SnackBar", "SnackBarHost", "NavigationDrawerItem",
            "CustomNavigationItem", "NavigationRail", "NavigationRailItem", "AppBarAction",
            "NavigationBarItem", "ModalBottomSheet")
        for (scaffold in scaffolds) {
            val id = KetoyComponentTypeId.resolveId(scaffold)
            assertNotNull("$scaffold should have an ID", id)
            assertTrue("$scaffold ID ($id) should be in 31-60", id!! in 31..60)
        }
    }

    @Test
    fun `data construct types have IDs in range 61-70`() {
        val data = listOf("DataClass", "Enum", "DataList")
        for (dc in data) {
            val id = KetoyComponentTypeId.resolveId(dc)
            assertNotNull("$dc should have an ID", id)
            assertTrue("$dc ID ($id) should be in 61-70", id!! in 61..70)
        }
    }

    @Test
    fun `typeToId and idToType are consistent inverses`() {
        for ((name, id) in KetoyComponentTypeId.typeToId) {
            assertEquals(
                "idToType[$id] should be $name",
                name, KetoyComponentTypeId.idToType[id]
            )
        }
    }

    @Test
    fun `compressTypes handles aliased key t`() {
        val input = json.parseToJsonElement("""{"t":"Text","p":{"tx":"Hi"}}""")
        val compressed = KetoyComponentTypeId.compressTypes(input)
        assertEquals(11, compressed.jsonObject["t"]!!.jsonPrimitive.int)
    }
}
