package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.model.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [KLazyListScope] and [KTextFieldScope].
 */
class ScopeTest {

    @Before
    fun setUp() {
        ActionRegistry.clear()
    }

    @After
    fun tearDown() {
        ActionRegistry.clear()
    }

    // ─── KLazyListScope ─────────────────────────────────────────

    @Test
    fun `item adds children to scope`() {
        val scope = KLazyListScope()
        scope.item {
            KText("A")
            KText("B")
        }
        assertEquals(2, scope.children.size)
    }

    @Test
    fun `items with list creates child per item`() {
        val scope = KLazyListScope()
        scope.items(listOf("X", "Y", "Z")) { item ->
            KText(item)
        }
        assertEquals(3, scope.children.size)
        assertEquals("X", (scope.children[0] as KTextNode).props.text)
        assertEquals("Z", (scope.children[2] as KTextNode).props.text)
    }

    @Test
    fun `items with count creates N children`() {
        val scope = KLazyListScope()
        scope.items(4) { index ->
            KText("Item $index")
        }
        assertEquals(4, scope.children.size)
        assertEquals("Item 0", (scope.children[0] as KTextNode).props.text)
        assertEquals("Item 3", (scope.children[3] as KTextNode).props.text)
    }

    @Test
    fun `itemsIndexed passes index and item`() {
        val scope = KLazyListScope()
        scope.itemsIndexed(listOf("A", "B")) { index, item ->
            KText("$index: $item")
        }
        assertEquals(2, scope.children.size)
        assertEquals("0: A", (scope.children[0] as KTextNode).props.text)
        assertEquals("1: B", (scope.children[1] as KTextNode).props.text)
    }

    @Test
    fun `items with array creates children`() {
        val scope = KLazyListScope()
        scope.items(arrayOf(1, 2, 3)) { num ->
            KText("$num")
        }
        assertEquals(3, scope.children.size)
    }

    @Test
    fun `empty item adds no children`() {
        val scope = KLazyListScope()
        scope.item { }
        assertEquals(0, scope.children.size)
    }

    @Test
    fun `items with empty list adds no children`() {
        val scope = KLazyListScope()
        scope.items(emptyList<String>()) { KText(it) }
        assertEquals(0, scope.children.size)
    }

    // ─── KTextFieldScope ────────────────────────────────────────

    @Test
    fun `label slot captures content`() {
        val scope = KTextFieldScope()
        scope.label { KText("Email") }
        val content = scope.getLabelContent()
        assertNotNull(content)
        assertEquals(1, content!!.size)
        assertEquals("Email", (content[0] as KTextNode).props.text)
    }

    @Test
    fun `placeholder slot captures content`() {
        val scope = KTextFieldScope()
        scope.placeholder { KText("Enter text") }
        val content = scope.getPlaceholderContent()
        assertNotNull(content)
    }

    @Test
    fun `leadingIcon slot captures content`() {
        val scope = KTextFieldScope()
        scope.leadingIcon { KText("📧") }
        assertNotNull(scope.getLeadingIconContent())
    }

    @Test
    fun `trailingIcon slot captures content`() {
        val scope = KTextFieldScope()
        scope.trailingIcon { KText("❌") }
        assertNotNull(scope.getTrailingIconContent())
    }

    @Test
    fun `prefix slot captures content`() {
        val scope = KTextFieldScope()
        scope.prefix { KText("$") }
        assertNotNull(scope.getPrefixContent())
    }

    @Test
    fun `suffix slot captures content`() {
        val scope = KTextFieldScope()
        scope.suffix { KText("USD") }
        assertNotNull(scope.getSuffixContent())
    }

    @Test
    fun `supportingText slot captures content`() {
        val scope = KTextFieldScope()
        scope.supportingText { KText("Required field") }
        assertNotNull(scope.getSupportingTextContent())
    }

    @Test
    fun `unused slots return null`() {
        val scope = KTextFieldScope()
        assertNull(scope.getLabelContent())
        assertNull(scope.getPlaceholderContent())
        assertNull(scope.getLeadingIconContent())
        assertNull(scope.getTrailingIconContent())
        assertNull(scope.getPrefixContent())
        assertNull(scope.getSuffixContent())
        assertNull(scope.getSupportingTextContent())
    }

    // ─── KScaffoldScope ─────────────────────────────────────────

    @Test
    fun `KScaffoldScope extends KUniversalScope`() {
        val scope = KScaffoldScope()
        scope.KText("Scaffold content")
        assertEquals(1, scope.children.size)
    }

    // ─── KAppBarScope ───────────────────────────────────────────

    @Test
    fun `KAppBarScope can add actions`() {
        val scope = KAppBarScope()
        scope.KAppBarAction(onClick = { }) {
            KText("Settings")
        }
        assertEquals(1, scope.children.size)
        assertTrue(scope.children[0] is KAppBarActionNode)
    }

    @Test
    fun `KAppBarScope KIconButton delegates to KAppBarAction`() {
        val scope = KAppBarScope()
        scope.KIconButton(onClick = { }) {
            KText("Icon")
        }
        assertEquals(1, scope.children.size)
        assertTrue(scope.children[0] is KAppBarActionNode)
    }

    // ─── KScope base ────────────────────────────────────────────

    @Test
    fun `addChild adds node to children list`() {
        val scope = KUniversalScope()
        val node = KTextNode(KTextProps("Test"))
        scope.addChild(node)
        assertEquals(1, scope.children.size)
        assertSame(node, scope.children[0])
    }

    @Test
    fun `addComponent is alias for addChild`() {
        val scope = KUniversalScope()
        val node = KSpacerNode(KSpacerProps(height = 8))
        scope.addComponent(node)
        assertEquals(1, scope.children.size)
    }
}
