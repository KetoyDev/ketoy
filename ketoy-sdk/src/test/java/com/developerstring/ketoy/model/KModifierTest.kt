package com.developerstring.ketoy.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [KModifier] and related spacing / border / shadow primitives.
 */
class KModifierTest {

    // ─── Default construction ───────────────────────────────────

    @Test
    fun `default KModifier has all null fields`() {
        val mod = KModifier()
        assertNull(mod.fillMaxSize)
        assertNull(mod.fillMaxWidth)
        assertNull(mod.fillMaxHeight)
        assertNull(mod.weight)
        assertNull(mod.size)
        assertNull(mod.width)
        assertNull(mod.height)
        assertNull(mod.padding)
        assertNull(mod.margin)
        assertNull(mod.background)
        assertNull(mod.gradient)
        assertNull(mod.border)
        assertNull(mod.shape)
        assertNull(mod.cornerRadius)
        assertNull(mod.shadow)
        assertNull(mod.clickable)
        assertNull(mod.scale)
        assertNull(mod.rotation)
        assertNull(mod.alpha)
        assertNull(mod.verticalScroll)
        assertNull(mod.horizontalScroll)
    }

    // ─── Fill / size ────────────────────────────────────────────

    @Test
    fun `fillMaxSize stores fraction`() {
        val mod = KModifier(fillMaxSize = 0.5f)
        assertEquals(0.5f, mod.fillMaxSize!!, 0.001f)
    }

    @Test
    fun `fillMaxWidth and fillMaxHeight store fractions`() {
        val mod = KModifier(fillMaxWidth = 1f, fillMaxHeight = 0.75f)
        assertEquals(1f, mod.fillMaxWidth!!, 0.001f)
        assertEquals(0.75f, mod.fillMaxHeight!!, 0.001f)
    }

    @Test
    fun `size width height are in dp`() {
        val mod = KModifier(size = 48, width = 100, height = 200)
        assertEquals(48, mod.size)
        assertEquals(100, mod.width)
        assertEquals(200, mod.height)
    }

    @Test
    fun `weight stores float value`() {
        val mod = KModifier(weight = 2f)
        assertEquals(2f, mod.weight!!, 0.001f)
    }

    // ─── Scroll ─────────────────────────────────────────────────

    @Test
    fun `verticalScroll defaults to null`() {
        assertNull(KModifier().verticalScroll)
    }

    @Test
    fun `horizontalScroll defaults to null`() {
        assertNull(KModifier().horizontalScroll)
    }

    @Test
    fun `verticalScroll can be set to true`() {
        val mod = KModifier(verticalScroll = true)
        assertTrue(mod.verticalScroll!!)
    }

    @Test
    fun `horizontalScroll can be set to true`() {
        val mod = KModifier(horizontalScroll = true)
        assertTrue(mod.horizontalScroll!!)
    }

    @Test
    fun `both scroll directions can be enabled simultaneously`() {
        val mod = KModifier(verticalScroll = true, horizontalScroll = true)
        assertTrue(mod.verticalScroll!!)
        assertTrue(mod.horizontalScroll!!)
    }

    // ─── Appearance ─────────────────────────────────────────────

    @Test
    fun `background colour stores hex string`() {
        val mod = KModifier(background = "#FF5722")
        assertEquals("#FF5722", mod.background)
    }

    @Test
    fun `shape stores descriptor string`() {
        val mod = KModifier(shape = "rounded_16")
        assertEquals("rounded_16", mod.shape)
    }

    @Test
    fun `cornerRadius stores integer`() {
        val mod = KModifier(cornerRadius = 12)
        assertEquals(12, mod.cornerRadius)
    }

    @Test
    fun `alpha stores float`() {
        val mod = KModifier(alpha = 0.5f)
        assertEquals(0.5f, mod.alpha!!, 0.001f)
    }

    @Test
    fun `scale stores float`() {
        val mod = KModifier(scale = 1.5f)
        assertEquals(1.5f, mod.scale!!, 0.001f)
    }

    @Test
    fun `rotation stores float`() {
        val mod = KModifier(rotation = 90f)
        assertEquals(90f, mod.rotation!!, 0.001f)
    }

    @Test
    fun `clickable flag stores boolean`() {
        val mod = KModifier(clickable = true)
        assertTrue(mod.clickable!!)
    }

    // ─── KPadding ───────────────────────────────────────────────

    @Test
    fun `KPadding all sets uniform padding`() {
        val p = KPadding(all = 16)
        assertEquals(16, p.all)
        assertNull(p.horizontal)
        assertNull(p.vertical)
    }

    @Test
    fun `KPadding horizontal and vertical`() {
        val p = KPadding(horizontal = 20, vertical = 12)
        assertEquals(20, p.horizontal)
        assertEquals(12, p.vertical)
    }

    @Test
    fun `KPadding individual sides`() {
        val p = KPadding(top = 8, bottom = 16, start = 4, end = 24)
        assertEquals(8, p.top)
        assertEquals(16, p.bottom)
        assertEquals(4, p.start)
        assertEquals(24, p.end)
    }

    // ─── KMargin ────────────────────────────────────────────────

    @Test
    fun `KMargin all sets uniform margin`() {
        val m = KMargin(all = 8)
        assertEquals(8, m.all)
    }

    @Test
    fun `KMargin directional values`() {
        val m = KMargin(top = 4, bottom = 8, start = 12, end = 16)
        assertEquals(4, m.top)
        assertEquals(8, m.bottom)
        assertEquals(12, m.start)
        assertEquals(16, m.end)
    }

    // ─── KBorder ────────────────────────────────────────────────

    @Test
    fun `KBorder stores width and colour`() {
        val b = KBorder(width = 2, color = "#000000")
        assertEquals(2, b.width)
        assertEquals("#000000", b.color)
        assertNull(b.shape)
    }

    @Test
    fun `KBorder with shape`() {
        val b = KBorder(width = 1, color = "#FF0000", shape = "circle")
        assertEquals("circle", b.shape)
    }

    // ─── KShadow ────────────────────────────────────────────────

    @Test
    fun `KShadow stores elevation`() {
        val s = KShadow(elevation = 8)
        assertEquals(8, s.elevation)
        assertNull(s.color)
    }

    @Test
    fun `KShadow full constructor`() {
        val s = KShadow(elevation = 4, color = "#33000000", offsetX = 2f, offsetY = 4f, blurRadius = 8f)
        assertEquals(4, s.elevation)
        assertEquals("#33000000", s.color)
        assertEquals(2f, s.offsetX!!, 0.001f)
        assertEquals(4f, s.offsetY!!, 0.001f)
        assertEquals(8f, s.blurRadius!!, 0.001f)
    }

    // ─── Data class equality & copy ─────────────────────────────

    @Test
    fun `KModifier data class equality`() {
        val a = KModifier(fillMaxSize = 1f, background = "#FFF")
        val b = KModifier(fillMaxSize = 1f, background = "#FFF")
        assertEquals(a, b)
    }

    @Test
    fun `KModifier copy changes single field`() {
        val original = KModifier(fillMaxSize = 1f)
        val copy = original.copy(background = "#000")
        assertNull(original.background)
        assertEquals("#000", copy.background)
        assertEquals(original.fillMaxSize, copy.fillMaxSize)
    }

    // ─── Gradient in modifier ───────────────────────────────────

    @Test
    fun `modifier can hold gradient`() {
        val g = KGradient(type = "linear", colors = listOf("#FF0000", "#0000FF"))
        val mod = KModifier(gradient = g)
        assertNotNull(mod.gradient)
        assertEquals("linear", mod.gradient!!.type)
        assertEquals(2, mod.gradient!!.colors.size)
    }

    // ─── Border in modifier ─────────────────────────────────────

    @Test
    fun `modifier can hold border`() {
        val b = KBorder(width = 2, color = "#000")
        val mod = KModifier(border = b)
        assertNotNull(mod.border)
        assertEquals(2, mod.border!!.width)
    }

    // ─── Shadow in modifier ─────────────────────────────────────

    @Test
    fun `modifier can hold shadow`() {
        val s = KShadow(elevation = 6)
        val mod = KModifier(shadow = s)
        assertNotNull(mod.shadow)
        assertEquals(6, mod.shadow!!.elevation)
    }
}
