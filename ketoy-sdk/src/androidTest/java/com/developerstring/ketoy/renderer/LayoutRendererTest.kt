package com.developerstring.ketoy.renderer

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.*
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.util.*
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Compose UI tests for layout renderers.
 *
 * These tests verify that KNode DSL trees survive the
 * toJson → JSONStringToUI → Compose render pipeline
 * and produce visible UI elements on screen.
 */
class LayoutRendererTest {

    @get:Rule
    val rule = createComposeRule()

    // ─── Column ─────────────────────────────────────────────────

    @Test
    fun column_renders_text_children() {
        val json = KColumn {
            KText("First")
            KText("Second")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("First").assertIsDisplayed()
        rule.onNodeWithText("Second").assertIsDisplayed()
    }

    @Test
    fun column_with_arrangement_renders() {
        val json = KColumn(verticalArrangement = KArrangements.SpaceBetween) {
            KText("Top")
            KText("Bottom")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Top").assertExists()
        rule.onNodeWithText("Bottom").assertExists()
    }

    @Test
    fun column_with_modifier_renders() {
        val json = KColumn(modifier = kModifier(fillMaxSize = 1f)) {
            KText("Full size column")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Full size column").assertIsDisplayed()
    }

    // ─── Row ────────────────────────────────────────────────────

    @Test
    fun row_renders_text_children() {
        val json = KRow {
            KText("Left")
            KText("Right")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Left").assertIsDisplayed()
        rule.onNodeWithText("Right").assertIsDisplayed()
    }

    @Test
    fun row_with_arrangement_renders() {
        val json = KRow(horizontalArrangement = KArrangements.SpaceEvenly) {
            KText("A")
            KText("B")
            KText("C")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("A").assertExists()
        rule.onNodeWithText("B").assertExists()
        rule.onNodeWithText("C").assertExists()
    }

    // ─── Box ────────────────────────────────────────────────────

    @Test
    fun box_renders_children() {
        val json = KBox(contentAlignment = KAlignments.Center) {
            KText("Centered content")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Centered content").assertIsDisplayed()
    }

    @Test
    fun box_overlapping_children() {
        val json = KBox {
            KText("Background")
            KText("Foreground")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Background").assertExists()
        rule.onNodeWithText("Foreground").assertExists()
    }

    // ─── Nested layouts ─────────────────────────────────────────

    @Test
    fun nested_column_inside_row_renders() {
        val json = KRow {
            KColumn {
                KText("Col Item 1")
                KText("Col Item 2")
            }
            KText("Row Item")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Col Item 1").assertIsDisplayed()
        rule.onNodeWithText("Col Item 2").assertIsDisplayed()
        rule.onNodeWithText("Row Item").assertIsDisplayed()
    }

    @Test
    fun deeply_nested_layouts_render() {
        val json = KColumn {
            KRow {
                KBox {
                    KColumn {
                        KText("Deep text")
                    }
                }
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Deep text").assertIsDisplayed()
    }

    // ─── LazyColumn ─────────────────────────────────────────────

    @Test
    fun lazy_column_renders_items() {
        val json = KLazyColumn {
            item {
                KText("Lazy Item 1")
            }
            item {
                KText("Lazy Item 2")
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Lazy Item 1").assertIsDisplayed()
        rule.onNodeWithText("Lazy Item 2").assertIsDisplayed()
    }

    @Test
    fun lazy_column_with_data_items() {
        val names = listOf("Alice", "Bob", "Charlie")
        val json = KLazyColumn {
            items(names) { name ->
                KText(name)
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("Bob").assertIsDisplayed()
        rule.onNodeWithText("Charlie").assertIsDisplayed()
    }

    // ─── LazyRow ────────────────────────────────────────────────

    @Test
    fun lazy_row_renders_items() {
        val json = KLazyRow {
            item {
                KText("H1")
            }
            item {
                KText("H2")
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("H1").assertIsDisplayed()
        rule.onNodeWithText("H2").assertIsDisplayed()
    }

    // ─── Empty layouts ──────────────────────────────────────────

    @Test
    fun empty_column_renders_without_crash() {
        val json = KColumn { }.toJson()
        rule.setContent { JSONStringToUI(json) }
        rule.waitForIdle()
    }

    @Test
    fun empty_row_renders_without_crash() {
        val json = KRow { }.toJson()
        rule.setContent { JSONStringToUI(json) }
        rule.waitForIdle()
    }

    @Test
    fun empty_box_renders_without_crash() {
        val json = KBox { }.toJson()
        rule.setContent { JSONStringToUI(json) }
        rule.waitForIdle()
    }
}
