package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.util.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DSL builder functions in [KUniversalScope] and [TopLevelBuilders].
 */
class DslBuildersTest {

    @Before
    fun setUp() {
        ActionRegistry.clear()
    }

    @After
    fun tearDown() {
        ActionRegistry.clear()
    }

    // ─── Top-level KColumn ──────────────────────────────────────

    @Test
    fun `top-level KColumn creates KColumnNode`() {
        val node = KColumn {
            KText("Hello")
        }
        assertTrue(node is KColumnNode)
        assertEquals(1, node.children.size)
        assertTrue(node.children[0] is KTextNode)
    }

    @Test
    fun `top-level KColumn passes modifier and arrangement`() {
        val mod = KModifier(fillMaxSize = 1f)
        val node = KColumn(
            modifier = mod,
            verticalArrangement = KArrangements.SpaceBetween,
            horizontalAlignment = KAlignments.CenterHorizontally
        ) {
            KText("A")
        }
        assertEquals(mod, node.props.modifier)
        assertEquals(KArrangements.SpaceBetween, node.props.verticalArrangement)
        assertEquals(KAlignments.CenterHorizontally, node.props.horizontalAlignment)
    }

    // ─── Top-level KRow ─────────────────────────────────────────

    @Test
    fun `top-level KRow creates KRowNode`() {
        val node = KRow {
            KText("Left")
            KText("Right")
        }
        assertTrue(node is KRowNode)
        assertEquals(2, node.children.size)
    }

    @Test
    fun `top-level KRow passes arrangement`() {
        val node = KRow(
            horizontalArrangement = KArrangements.SpaceEvenly,
            verticalAlignment = KAlignments.CenterVertically
        ) { }
        assertEquals(KArrangements.SpaceEvenly, node.props.horizontalArrangement)
        assertEquals(KAlignments.CenterVertically, node.props.verticalAlignment)
    }

    // ─── Top-level KBox ─────────────────────────────────────────

    @Test
    fun `top-level KBox creates KBoxNode`() {
        val node = KBox(contentAlignment = KAlignments.Center) {
            KText("Centered")
        }
        assertTrue(node is KBoxNode)
        assertEquals(KAlignments.Center, node.props.contentAlignment)
        assertEquals(1, node.children.size)
    }

    // ─── Nested scope KText ─────────────────────────────────────

    @Test
    fun `KText in scope captures text and style`() {
        val node = KColumn {
            KText("Hello", fontSize = 18, fontWeight = KFontWeights.Bold, color = "#000")
        }
        val text = node.children[0] as KTextNode
        assertEquals("Hello", text.props.text)
        assertEquals(18, text.props.fontSize)
        assertEquals(KFontWeights.Bold, text.props.fontWeight)
        assertEquals("#000", text.props.color)
    }

    @Test
    fun `KText supports all optional props`() {
        val node = KColumn {
            KText(
                "Test", modifier = KModifier(alpha = 0.5f),
                fontSize = 12, fontWeight = KFontWeights.Light,
                color = "#FFF", textAlign = KTextAlign.Center,
                maxLines = 3, overflow = "ellipsis",
                letterSpacing = 1.5f, lineHeight = 20f
            )
        }
        val text = node.children[0] as KTextNode
        assertEquals(KTextAlign.Center, text.props.textAlign)
        assertEquals(3, text.props.maxLines)
        assertEquals("ellipsis", text.props.overflow)
        assertEquals(1.5f, text.props.letterSpacing!!, 0.001f)
        assertEquals(20f, text.props.lineHeight!!, 0.001f)
        assertEquals(0.5f, text.props.modifier?.alpha!!, 0.001f)
    }

    // ─── Nested scope KButton ───────────────────────────────────

    @Test
    fun `KButton registers onClick and creates node`() {
        var clicked = false
        val node = KColumn {
            KButton(onClick = { clicked = true }, containerColor = "#2196F3") {
                KText("Go")
            }
        }
        val button = node.children[0] as KButtonNode
        assertEquals("#2196F3", button.props.containerColor)
        assertEquals(1, button.children.size)
        // Verify action registered
        val clickId = (button.props.onClick as kotlinx.serialization.json.JsonPrimitive).content
        assertNotNull(ActionRegistry.get(clickId))
        ActionRegistry.execute(clickId)
        assertTrue(clicked)
    }

    @Test
    fun `KButton default onClick is a no-op`() {
        val node = KColumn {
            KButton { KText("No-op") }
        }
        val button = node.children[0] as KButtonNode
        // Should not throw
        val clickId = (button.props.onClick as kotlinx.serialization.json.JsonPrimitive).content
        ActionRegistry.execute(clickId)
    }

    // ─── Nested scope KSpacer ───────────────────────────────────

    @Test
    fun `KSpacer captures height`() {
        val node = KColumn {
            KSpacer(height = 24)
        }
        val spacer = node.children[0] as KSpacerNode
        assertEquals(24, spacer.props.height)
        assertNull(spacer.props.width)
    }

    @Test
    fun `KSpacer captures width`() {
        val node = KRow {
            KSpacer(width = 16)
        }
        val spacer = node.children[0] as KSpacerNode
        assertEquals(16, spacer.props.width)
    }

    // ─── Nested scope KCard ─────────────────────────────────────

    @Test
    fun `KCard captures props and children`() {
        val node = KColumn {
            KCard(
                containerColor = "#FFF",
                shape = KShapes.Rounded16,
                elevation = 4,
                border = KBorder(1, "#000")
            ) {
                KText("Inside card")
            }
        }
        val card = node.children[0] as KCardNode
        assertEquals("#FFF", card.props.containerColor)
        assertEquals(KShapes.Rounded16, card.props.shape)
        assertEquals(4, card.props.elevation)
        assertNotNull(card.props.border)
        assertEquals(1, card.children.size)
    }

    @Test
    fun `KCard default shape is Rounded12`() {
        val node = KColumn {
            KCard { KText("X") }
        }
        val card = node.children[0] as KCardNode
        assertEquals(KShapes.Rounded12, card.props.shape)
    }

    // ─── Nested Column / Row / Box ──────────────────────────────

    @Test
    fun `nested KColumn within KColumn`() {
        val node = KColumn {
            KColumn(verticalArrangement = KArrangements.Center) {
                KText("Nested")
            }
        }
        val inner = node.children[0] as KColumnNode
        assertEquals(KArrangements.Center, inner.props.verticalArrangement)
    }

    @Test
    fun `nested KRow within KColumn`() {
        val node = KColumn {
            KRow(horizontalArrangement = KArrangements.SpaceBetween) {
                KText("L")
                KText("R")
            }
        }
        val row = node.children[0] as KRowNode
        assertEquals(KArrangements.SpaceBetween, row.props.horizontalArrangement)
        assertEquals(2, row.children.size)
    }

    @Test
    fun `nested KBox within KRow`() {
        val node = KRow {
            KBox(contentAlignment = KAlignments.BottomEnd) {
                KText("Corner")
            }
        }
        val box = node.children[0] as KBoxNode
        assertEquals(KAlignments.BottomEnd, box.props.contentAlignment)
    }

    // ─── KImage ─────────────────────────────────────────────────

    @Test
    fun `KImage captures source and props`() {
        val node = KColumn {
            KImage(
                source = KUrlImageSource("https://img.com/a.png"),
                modifier = KModifier(size = 100),
                contentDescription = "photo",
                scaleType = KScaleType.CenterCrop
            )
        }
        val img = node.children[0] as KImageNode
        assertTrue(img.props.source is KUrlImageSource)
        assertEquals("photo", img.props.contentDescription)
        assertEquals(KScaleType.CenterCrop, img.props.scaleType)
        assertEquals(100, img.props.modifier?.size)
    }

    // ─── KLazyColumn / KLazyRow ─────────────────────────────────

    @Test
    fun `KLazyColumn with items`() {
        val node = KColumn {
            KLazyColumn(verticalArrangement = "spacedBy_8") {
                item {
                    KText("Item 1")
                    KText("Item 2")
                }
            }
        }
        val lazy = node.children[0] as KLazyColumnNode
        assertEquals("spacedBy_8", lazy.props.verticalArrangement)
        assertEquals(2, lazy.children.size)
    }

    @Test
    fun `KLazyRow with items list`() {
        val data = listOf("A", "B", "C")
        val node = KRow {
            KLazyRow {
                items(data) { item ->
                    KText(item)
                }
            }
        }
        val lazy = node.children[0] as KLazyRowNode
        assertEquals(3, lazy.children.size)
    }

    // ─── KIf / KForEach / KRepeat ───────────────────────────────

    @Test
    fun `KIf true adds children`() {
        val node = KColumn {
            KIf(true) { KText("Visible") }
        }
        assertEquals(1, node.children.size)
    }

    @Test
    fun `KIf false adds no children`() {
        val node = KColumn {
            KIf(false) { KText("Hidden") }
        }
        assertEquals(0, node.children.size)
    }

    @Test
    fun `KForEach iterates items`() {
        val items = listOf("A", "B", "C")
        val node = KColumn {
            KForEach(items) { item -> KText(item) }
        }
        assertEquals(3, node.children.size)
    }

    @Test
    fun `KRepeat creates N children`() {
        val node = KColumn {
            KRepeat(5) { i -> KText("Item $i") }
        }
        assertEquals(5, node.children.size)
        assertEquals("Item 0", (node.children[0] as KTextNode).props.text)
        assertEquals("Item 4", (node.children[4] as KTextNode).props.text)
    }

    // ─── Complex tree ───────────────────────────────────────────

    @Test
    fun `complex tree structure is built correctly`() {
        val node = KColumn(modifier = kModifier(fillMaxSize = 1f)) {
            KText("Header", fontSize = 24, fontWeight = KFontWeights.Bold)
            KSpacer(height = 16)
            KRow(horizontalArrangement = KArrangements.SpaceBetween) {
                KButton(containerColor = "#FF0000") { KText("Cancel") }
                KButton(containerColor = "#00FF00") { KText("OK") }
            }
            KCard(elevation = 8) {
                KColumn {
                    KText("Card Title")
                    KText("Card Body")
                }
            }
        }

        assertEquals(4, node.children.size)
        assertTrue(node.children[0] is KTextNode)
        assertTrue(node.children[1] is KSpacerNode)
        assertTrue(node.children[2] is KRowNode)
        assertTrue(node.children[3] is KCardNode)

        val row = node.children[2] as KRowNode
        assertEquals(2, row.children.size)

        val card = node.children[3] as KCardNode
        val innerCol = card.children[0] as KColumnNode
        assertEquals(2, innerCol.children.size)
    }
}
