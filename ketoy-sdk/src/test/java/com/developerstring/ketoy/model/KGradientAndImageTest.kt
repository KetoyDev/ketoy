package com.developerstring.ketoy.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [KGradient], [KImageSource], and [KScaleType].
 */
class KGradientAndImageTest {

    // ─── KGradient ──────────────────────────────────────────────

    @Test
    fun `linear gradient stores type and colours`() {
        val g = KGradient(type = "linear", colors = listOf("#FF0000", "#00FF00", "#0000FF"))
        assertEquals("linear", g.type)
        assertEquals(3, g.colors.size)
    }

    @Test
    fun `linear gradient with direction`() {
        val g = KGradient(type = "linear", colors = listOf("#FFF", "#000"), direction = "horizontal")
        assertEquals("horizontal", g.direction)
    }

    @Test
    fun `linear gradient with angle`() {
        val g = KGradient(type = "linear", colors = listOf("#FFF", "#000"), angle = 135f)
        assertEquals(135f, g.angle!!, 0.001f)
    }

    @Test
    fun `radial gradient stores center and radius`() {
        val g = KGradient(type = "radial", colors = listOf("#FFF", "#000"), centerX = 0.5f, centerY = 0.5f, radius = 200f)
        assertEquals("radial", g.type)
        assertEquals(0.5f, g.centerX!!, 0.001f)
        assertEquals(0.5f, g.centerY!!, 0.001f)
        assertEquals(200f, g.radius!!, 0.001f)
    }

    @Test
    fun `sweep gradient stores angles`() {
        val g = KGradient(type = "sweep", colors = listOf("#F00", "#0F0"), startAngle = 0f, endAngle = 360f)
        assertEquals("sweep", g.type)
        assertEquals(0f, g.startAngle!!, 0.001f)
        assertEquals(360f, g.endAngle!!, 0.001f)
    }

    @Test
    fun `gradient defaults are null`() {
        val g = KGradient(type = "linear", colors = listOf("#FFF"))
        assertNull(g.direction)
        assertNull(g.angle)
        assertNull(g.centerX)
        assertNull(g.centerY)
        assertNull(g.radius)
        assertNull(g.startAngle)
        assertNull(g.endAngle)
    }

    // ─── KImageSource ───────────────────────────────────────────

    @Test
    fun `KResImageSource stores resource name`() {
        val src = KResImageSource("ic_launcher")
        assertEquals("ic_launcher", src.value)
    }

    @Test
    fun `KUrlImageSource stores URL`() {
        val src = KUrlImageSource("https://img.com/photo.jpg")
        assertEquals("https://img.com/photo.jpg", src.value)
    }

    @Test
    fun `KBase64ImageSource stores base64 string`() {
        val src = KBase64ImageSource("iVBORw0KGgo=")
        assertEquals("iVBORw0KGgo=", src.value)
    }

    @Test
    fun `image sources are sealed class instances`() {
        val sources: List<KImageSource> = listOf(
            KResImageSource("ic"), KUrlImageSource("url"), KBase64ImageSource("b64")
        )
        assertEquals(3, sources.size)
    }

    // ─── KScaleType ─────────────────────────────────────────────

    @Test
    fun `KScaleType constants are correct`() {
        assertEquals("fitCenter", KScaleType.FitCenter)
        assertEquals("centerCrop", KScaleType.CenterCrop)
        assertEquals("fillBounds", KScaleType.FillBounds)
        assertEquals("inside", KScaleType.Inside)
        assertEquals("fillWidth", KScaleType.FillWidth)
        assertEquals("fillHeight", KScaleType.FillHeight)
    }
}
