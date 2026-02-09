package com.developerstring.ketoy.util

import com.developerstring.ketoy.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for helper functions in [Helpers.kt], [KColors], [KShapes], and [KConstants].
 */
class HelpersAndConstantsTest {

    // ─── kModifier ──────────────────────────────────────────────

    @Test
    fun `kModifier creates KModifier with all params`() {
        val mod = kModifier(
            fillMaxSize = 1f, fillMaxWidth = 0.5f, fillMaxHeight = 0.75f,
            weight = 2f, size = 100, width = 200, height = 150,
            padding = kPadding(all = 16), margin = kMargin(all = 8),
            background = "#FF0000",
            gradient = KGradient(type = "linear", colors = listOf("#FFF", "#000")),
            border = kBorder(2, "#000"),
            shape = "rounded_12", cornerRadius = 12,
            shadow = kShadow(elevation = 8),
            clickable = true, scale = 1.5f, rotation = 45f, alpha = 0.8f,
            verticalScroll = true, horizontalScroll = false
        )
        assertEquals(1f, mod.fillMaxSize!!, 0.001f)
        assertEquals(0.5f, mod.fillMaxWidth!!, 0.001f)
        assertEquals(100, mod.size)
        assertEquals(16, mod.padding?.all)
        assertEquals(8, mod.margin?.all)
        assertEquals("#FF0000", mod.background)
        assertEquals("linear", mod.gradient?.type)
        assertEquals(2, mod.border?.width)
        assertEquals("rounded_12", mod.shape)
        assertEquals(12, mod.cornerRadius)
        assertEquals(8, mod.shadow?.elevation)
        assertTrue(mod.clickable!!)
        assertTrue(mod.verticalScroll!!)
        assertFalse(mod.horizontalScroll!!)
    }

    @Test
    fun `kModifier default is all null`() {
        val mod = kModifier()
        assertNull(mod.fillMaxSize)
        assertNull(mod.background)
        assertNull(mod.verticalScroll)
        assertNull(mod.horizontalScroll)
    }

    // ─── kPadding ───────────────────────────────────────────────

    @Test
    fun `kPadding creates KPadding`() {
        val p = kPadding(horizontal = 20, vertical = 12)
        assertEquals(20, p.horizontal)
        assertEquals(12, p.vertical)
    }

    @Test
    fun `kPadding individual edges`() {
        val p = kPadding(top = 4, bottom = 8, start = 12, end = 16)
        assertEquals(4, p.top)
        assertEquals(8, p.bottom)
        assertEquals(12, p.start)
        assertEquals(16, p.end)
    }

    // ─── kMargin ────────────────────────────────────────────────

    @Test
    fun `kMargin creates KMargin`() {
        val m = kMargin(all = 24)
        assertEquals(24, m.all)
    }

    // ─── kBorder ────────────────────────────────────────────────

    @Test
    fun `kBorder creates KBorder`() {
        val b = kBorder(2, "#FF0000", "circle")
        assertEquals(2, b.width)
        assertEquals("#FF0000", b.color)
        assertEquals("circle", b.shape)
    }

    @Test
    fun `kBorder default shape is null`() {
        val b = kBorder(1, "#000")
        assertNull(b.shape)
    }

    // ─── kShadow ────────────────────────────────────────────────

    @Test
    fun `kShadow creates KShadow`() {
        val s = kShadow(elevation = 6, color = "#33000000", offsetX = 1f, offsetY = 2f, blurRadius = 4f)
        assertEquals(6, s.elevation)
        assertEquals("#33000000", s.color)
        assertEquals(1f, s.offsetX!!, 0.001f)
        assertEquals(2f, s.offsetY!!, 0.001f)
        assertEquals(4f, s.blurRadius!!, 0.001f)
    }

    // ─── KColors ────────────────────────────────────────────────

    @Test
    fun `KColors predefined values are valid hex`() {
        assertTrue(KColors.Blue.startsWith("#"))
        assertTrue(KColors.Red.startsWith("#"))
        assertTrue(KColors.Green.startsWith("#"))
        assertTrue(KColors.White.startsWith("#"))
        assertTrue(KColors.Black.startsWith("#"))
        assertTrue(KColors.Transparent.startsWith("#"))
    }

    @Test
    fun `KColors hex normalises 6-char hex`() {
        assertEquals("#FFFF5722", KColors.hex("FF5722"))
    }

    @Test
    fun `KColors hex normalises 7-char hex with hash`() {
        assertEquals("#FF4CAF50", KColors.hex("#4CAF50"))
    }

    @Test
    fun `KColors hex passes through 9-char hex`() {
        assertEquals("#80FF0000", KColors.hex("#80FF0000"))
    }

    @Test
    fun `KColors withAlpha creates correct alpha`() {
        val result = KColors.withAlpha("#FF2196F3", 0.5f)
        assertTrue(result.startsWith("#80") || result.startsWith("#7F"))
    }

    @Test
    fun `KColors withAlpha clamps to 0-1`() {
        val full = KColors.withAlpha("#FF0000", 2f)
        assertTrue(full.startsWith("#FF"))
        val zero = KColors.withAlpha("#FF0000", -1f)
        assertTrue(zero.startsWith("#00"))
    }

    @Test
    fun `KColors withAlphaPercent delegates to withAlpha`() {
        val result = KColors.withAlphaPercent("#FF0000", 50)
        assertNotNull(result)
    }

    // ─── KShapes ────────────────────────────────────────────────

    @Test
    fun `KShapes constants are correct`() {
        assertEquals("rectangle", KShapes.Rectangle)
        assertEquals("circle", KShapes.Circle)
        assertEquals("clip", KShapes.Clip)
        assertEquals("rounded_4", KShapes.Rounded4)
        assertEquals("rounded_8", KShapes.Rounded8)
        assertEquals("rounded_12", KShapes.Rounded12)
        assertEquals("rounded_16", KShapes.Rounded16)
        assertEquals("rounded_20", KShapes.Rounded20)
        assertEquals("rounded_24", KShapes.Rounded24)
        assertEquals("rounded_28", KShapes.Rounded28)
        assertEquals("rounded_32", KShapes.Rounded32)
    }

    @Test
    fun `KShapes rounded with radius`() {
        assertEquals("rounded_18", KShapes.rounded(18))
    }

    @Test
    fun `KShapes rounded with corners`() {
        val result = KShapes.rounded(topStart = 24, topEnd = 24, bottomEnd = 0, bottomStart = 0)
        assertEquals("rounded_corners_24_24_0_0", result)
    }

    @Test
    fun `KShapes factory functions`() {
        assertEquals("circle", KShapes.circle())
        assertEquals("rectangle", KShapes.rectangle())
        assertEquals("clip", KShapes.clip())
    }

    @Test
    fun `kRounded helper delegates to KShapes`() {
        assertEquals("rounded_20", kRounded(20))
    }

    @Test
    fun `kCircle helper returns circle`() {
        assertEquals("circle", kCircle())
    }

    // ─── KArrangements ──────────────────────────────────────────

    @Test
    fun `KArrangements constants are correct strings`() {
        assertEquals("start", KArrangements.Start)
        assertEquals("top", KArrangements.Top)
        assertEquals("center", KArrangements.Center)
        assertEquals("end", KArrangements.End)
        assertEquals("bottom", KArrangements.Bottom)
        assertEquals("spaceBetween", KArrangements.SpaceBetween)
        assertEquals("spaceEvenly", KArrangements.SpaceEvenly)
        assertEquals("spaceAround", KArrangements.SpaceAround)
    }

    // ─── KAlignments ────────────────────────────────────────────

    @Test
    fun `KAlignments basic constants`() {
        assertEquals("start", KAlignments.Start)
        assertEquals("center", KAlignments.Center)
        assertEquals("end", KAlignments.End)
        assertEquals("top", KAlignments.Top)
        assertEquals("bottom", KAlignments.Bottom)
        assertEquals("centerHorizontally", KAlignments.CenterHorizontally)
        assertEquals("centerVertically", KAlignments.CenterVertically)
    }

    @Test
    fun `KAlignments content alignment constants`() {
        assertEquals("topStart", KAlignments.TopStart)
        assertEquals("topCenter", KAlignments.TopCenter)
        assertEquals("topEnd", KAlignments.TopEnd)
        assertEquals("centerStart", KAlignments.CenterStart)
        assertEquals("centerEnd", KAlignments.CenterEnd)
        assertEquals("bottomStart", KAlignments.BottomStart)
        assertEquals("bottomCenter", KAlignments.BottomCenter)
        assertEquals("bottomEnd", KAlignments.BottomEnd)
    }

    // ─── KFontWeights ───────────────────────────────────────────

    @Test
    fun `KFontWeights constants`() {
        assertEquals("normal", KFontWeights.Normal)
        assertEquals("bold", KFontWeights.Bold)
        assertEquals("medium", KFontWeights.Medium)
        assertEquals("light", KFontWeights.Light)
        assertEquals("semiBold", KFontWeights.SemiBold)
    }

    // ─── KTextAlign ─────────────────────────────────────────────

    @Test
    fun `KTextAlign constants`() {
        assertEquals("start", KTextAlign.Start)
        assertEquals("center", KTextAlign.Center)
        assertEquals("end", KTextAlign.End)
        assertEquals("justify", KTextAlign.Justify)
    }

    // ─── KGradients factory ─────────────────────────────────────

    @Test
    fun `KGradients linear creates correct gradient`() {
        val g = KGradients.linear(listOf("#FFF", "#000"))
        assertEquals("linear", g.type)
        assertEquals("vertical", g.direction) // default
        assertEquals(2, g.colors.size)
    }

    @Test
    fun `KGradients linear with direction`() {
        val g = KGradients.linear(listOf("#FFF", "#000"), KGradients.Directions.Horizontal)
        assertEquals("horizontal", g.direction)
    }

    @Test
    fun `KGradients linearAngle sets angle`() {
        val g = KGradients.linearAngle(listOf("#F00", "#00F"), 45f)
        assertEquals(45f, g.angle!!, 0.001f)
    }

    @Test
    fun `KGradients radial sets center and radius`() {
        val g = KGradients.radial(listOf("#FFF", "#000"), 100f)
        assertEquals("radial", g.type)
        assertEquals(0.5f, g.centerX!!, 0.001f)
        assertEquals(0.5f, g.centerY!!, 0.001f)
        assertEquals(100f, g.radius!!, 0.001f)
    }

    @Test
    fun `KGradients radialCenter sets custom center`() {
        val g = KGradients.radialCenter(listOf("#FFF", "#000"), 0.3f, 0.7f)
        assertEquals(0.3f, g.centerX!!, 0.001f)
        assertEquals(0.7f, g.centerY!!, 0.001f)
    }

    @Test
    fun `KGradients sweep creates sweep gradient`() {
        val g = KGradients.sweep(listOf("#F00", "#0F0", "#00F"))
        assertEquals("sweep", g.type)
        assertEquals(3, g.colors.size)
    }

    @Test
    fun `KGradients sweepAngles sets angles`() {
        val g = KGradients.sweepAngles(listOf("#FFF", "#000"), 0f, 270f)
        assertEquals(0f, g.startAngle!!, 0.001f)
        assertEquals(270f, g.endAngle!!, 0.001f)
    }

    @Test
    fun `KGradients Directions constants`() {
        assertEquals("horizontal", KGradients.Directions.Horizontal)
        assertEquals("vertical", KGradients.Directions.Vertical)
        assertEquals("diagonal", KGradients.Directions.Diagonal)
        assertEquals("diagonalReverse", KGradients.Directions.DiagonalReverse)
    }

    // ─── Image source helpers ───────────────────────────────────

    @Test
    fun `kImageRes creates KResImageSource`() {
        val src = kImageRes("ic_logo")
        assertTrue(src is KResImageSource)
        assertEquals("ic_logo", (src as KResImageSource).value)
    }

    @Test
    fun `kImageUrl creates KUrlImageSource`() {
        val src = kImageUrl("https://example.com/img.png")
        assertTrue(src is KUrlImageSource)
    }

    @Test
    fun `kImageBase64 creates KBase64ImageSource`() {
        val src = kImageBase64("abc123==")
        assertTrue(src is KBase64ImageSource)
    }
}
