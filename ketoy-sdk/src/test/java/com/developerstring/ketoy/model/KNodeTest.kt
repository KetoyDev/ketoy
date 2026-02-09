package com.developerstring.ketoy.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the [KNode] sealed hierarchy and all Props data classes.
 */
class KNodeTest {

    // ─── KTextNode ──────────────────────────────────────────────

    @Test
    fun `KTextNode stores text and style props`() {
        val node = KTextNode(KTextProps(text = "Hello", fontSize = 18, fontWeight = "bold", color = "#000"))
        assertEquals("Hello", node.props.text)
        assertEquals(18, node.props.fontSize)
        assertEquals("bold", node.props.fontWeight)
        assertEquals("#000", node.props.color)
    }

    @Test
    fun `KTextNode defaults`() {
        val node = KTextNode()
        assertEquals("", node.props.text)
        assertNull(node.props.fontSize)
        assertNull(node.props.fontWeight)
        assertNull(node.props.color)
        assertNull(node.props.textAlign)
        assertNull(node.props.maxLines)
        assertNull(node.props.overflow)
        assertNull(node.props.letterSpacing)
        assertNull(node.props.lineHeight)
    }

    // ─── KColumnNode ────────────────────────────────────────────

    @Test
    fun `KColumnNode stores arrangement and alignment`() {
        val node = KColumnNode(
            KColumnProps(verticalArrangement = "spaceBetween", horizontalAlignment = "centerHorizontally"),
            listOf(KTextNode(KTextProps("A")), KTextNode(KTextProps("B")))
        )
        assertEquals("spaceBetween", node.props.verticalArrangement)
        assertEquals("centerHorizontally", node.props.horizontalAlignment)
        assertEquals(2, node.children.size)
    }

    @Test
    fun `KColumnNode default has empty children`() {
        val node = KColumnNode()
        assertTrue(node.children.isEmpty())
        assertNull(node.props.modifier)
        assertNull(node.props.verticalArrangement)
    }

    // ─── KRowNode ───────────────────────────────────────────────

    @Test
    fun `KRowNode stores arrangement and alignment`() {
        val node = KRowNode(
            KRowProps(horizontalArrangement = "spaceEvenly", verticalAlignment = "centerVertically")
        )
        assertEquals("spaceEvenly", node.props.horizontalArrangement)
        assertEquals("centerVertically", node.props.verticalAlignment)
    }

    // ─── KBoxNode ───────────────────────────────────────────────

    @Test
    fun `KBoxNode stores contentAlignment`() {
        val node = KBoxNode(KBoxProps(contentAlignment = "center"))
        assertEquals("center", node.props.contentAlignment)
    }

    // ─── KButtonNode ────────────────────────────────────────────

    @Test
    fun `KButtonNode stores onClick and style`() {
        val node = KButtonNode(
            KButtonProps(onClick = "action_0", containerColor = "#2196F3", shape = "rounded_12"),
            listOf(KTextNode(KTextProps("Click")))
        )
        assertEquals("action_0", node.props.onClick)
        assertEquals("#2196F3", node.props.containerColor)
        assertEquals("rounded_12", node.props.shape)
        assertEquals(1, node.children.size)
    }

    @Test
    fun `KButtonNode default onClick is function`() {
        val node = KButtonNode()
        assertEquals("function", node.props.onClick)
    }

    // ─── KCardNode ──────────────────────────────────────────────

    @Test
    fun `KCardNode stores card props`() {
        val node = KCardNode(
            KCardProps(containerColor = "#FFF", shape = "rounded_16", elevation = 8)
        )
        assertEquals("#FFF", node.props.containerColor)
        assertEquals("rounded_16", node.props.shape)
        assertEquals(8, node.props.elevation)
    }

    @Test
    fun `KCardNode children are mutable`() {
        val node = KCardNode()
        node.children.add(KTextNode(KTextProps("Added")))
        assertEquals(1, node.children.size)
    }

    // ─── KSpacerNode ────────────────────────────────────────────

    @Test
    fun `KSpacerNode stores width and height`() {
        val node = KSpacerNode(KSpacerProps(width = 20, height = 40))
        assertEquals(20, node.props.width)
        assertEquals(40, node.props.height)
    }

    @Test
    fun `KSpacerNode defaults are null`() {
        val node = KSpacerNode()
        assertNull(node.props.width)
        assertNull(node.props.height)
    }

    // ─── KImageNode ─────────────────────────────────────────────

    @Test
    fun `KImageNode with URL source`() {
        val node = KImageNode(KImageProps(source = KUrlImageSource("https://img.com/a.png")))
        assertTrue(node.props.source is KUrlImageSource)
        assertEquals("https://img.com/a.png", (node.props.source as KUrlImageSource).value)
    }

    @Test
    fun `KImageNode with resource source`() {
        val node = KImageNode(KImageProps(source = KResImageSource("ic_logo")))
        assertTrue(node.props.source is KResImageSource)
    }

    @Test
    fun `KImageNode default scaleType is fitCenter`() {
        val node = KImageNode()
        assertEquals(KScaleType.FitCenter, node.props.scaleType)
    }

    // ─── KLazyColumnNode ────────────────────────────────────────

    @Test
    fun `KLazyColumnNode stores lazy props`() {
        val node = KLazyColumnNode(
            KLazyColumnProps(userScrollEnabled = false, reverseLayout = true, beyondBoundsItemCount = 3)
        )
        assertEquals(false, node.props.userScrollEnabled)
        assertEquals(true, node.props.reverseLayout)
        assertEquals(3, node.props.beyondBoundsItemCount)
    }

    // ─── KLazyRowNode ───────────────────────────────────────────

    @Test
    fun `KLazyRowNode stores lazy props`() {
        val node = KLazyRowNode(
            KLazyRowProps(userScrollEnabled = true, reverseLayout = false)
        )
        assertEquals(true, node.props.userScrollEnabled)
        assertEquals(false, node.props.reverseLayout)
    }

    // ─── KComponentNode ─────────────────────────────────────────

    @Test
    fun `KComponentNode stores component name and properties`() {
        val node = KComponentNode(
            KComponentProps(name = "CustomWidget", properties = mapOf("color" to "#FF0000"))
        )
        assertEquals("CustomWidget", node.props.name)
        assertEquals("#FF0000", node.props.properties["color"])
    }

    // ─── KScaffoldNode ──────────────────────────────────────────

    @Test
    fun `KScaffoldNode stores scaffold props`() {
        val topBar = listOf(KTextNode(KTextProps("Title")))
        val node = KScaffoldNode(
            KScaffoldProps(containerColor = "#FFFFFF", topBar = topBar)
        )
        assertEquals("#FFFFFF", node.props.containerColor)
        assertNotNull(node.props.topBar)
        assertEquals(1, node.props.topBar!!.size)
    }

    // ─── KTextFieldNode ─────────────────────────────────────────

    @Test
    fun `KTextFieldNode stores value and action`() {
        val node = KTextFieldNode(KTextFieldProps(value = "hello", onValueChange = "textChange_0"))
        assertEquals("hello", node.props.value)
        assertEquals("textChange_0", node.props.onValueChange)
    }

    // ─── Type identity ──────────────────────────────────────────

    @Test
    fun `all node types are KNode instances`() {
        val nodes: List<KNode> = listOf(
            KColumnNode(), KRowNode(), KBoxNode(), KTextNode(),
            KButtonNode(), KSpacerNode(), KCardNode(), KImageNode(),
            KLazyColumnNode(), KLazyRowNode(), KComponentNode(),
            KScaffoldNode(), KTextFieldNode(), KTopAppBarNode(),
            KBottomAppBarNode(), KNavigationBarNode(),
            KFloatingActionButtonNode(), KSnackBarNode(),
            KSnackBarHostNode(), KDataClassNode(), KEnumNode()
        )
        assertEquals(21, nodes.size)
        nodes.forEach { assertTrue(it is KNode) }
    }

    // ─── Data class copy ────────────────────────────────────────

    @Test
    fun `KTextProps copy preserves unchanged fields`() {
        val original = KTextProps(text = "Hello", fontSize = 16, color = "#000")
        val copy = original.copy(text = "World")
        assertEquals("World", copy.text)
        assertEquals(16, copy.fontSize)
        assertEquals("#000", copy.color)
    }

    @Test
    fun `KColumnProps copy preserves arrangement`() {
        val original = KColumnProps(verticalArrangement = "top")
        val copy = original.copy(horizontalAlignment = "center")
        assertEquals("top", copy.verticalArrangement)
        assertEquals("center", copy.horizontalAlignment)
    }
}
