package com.developerstring.ketoy.renderer

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.KColumn
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.util.*
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for the main [JSONStringToUI] entry-point.
 *
 * Verifies that raw JSON strings are correctly parsed and rendered,
 * including error-handling for malformed input.
 */
class KetoyRendererTest {

    @get:Rule
    val rule = createComposeRule()

    // ─── Valid JSON ─────────────────────────────────────────────

    @Test
    fun valid_json_renders_text() {
        val json = """
            {
                "type": "Text",
                "props": {
                    "text": "Raw JSON Text"
                }
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Raw JSON Text").assertIsDisplayed()
    }

    @Test
    fun valid_json_column_with_children() {
        val json = """
            {
                "type": "Column",
                "children": [
                    { "type": "Text", "props": { "text": "Child 1" } },
                    { "type": "Text", "props": { "text": "Child 2" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Child 1").assertIsDisplayed()
        rule.onNodeWithText("Child 2").assertIsDisplayed()
    }

    @Test
    fun valid_json_row_renders() {
        val json = """
            {
                "type": "Row",
                "children": [
                    { "type": "Text", "props": { "text": "A" } },
                    { "type": "Text", "props": { "text": "B" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("A").assertIsDisplayed()
        rule.onNodeWithText("B").assertIsDisplayed()
    }

    @Test
    fun valid_json_button_renders() {
        val json = """
            {
                "type": "Button",
                "props": {
                    "onClick": "noop",
                    "containerColor": "#FF2196F3"
                },
                "children": [
                    { "type": "Text", "props": { "text": "Click" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Click").assertIsDisplayed()
    }

    @Test
    fun valid_json_spacer_renders_without_crash() {
        val json = """
            {
                "type": "Column",
                "children": [
                    { "type": "Text", "props": { "text": "Above" } },
                    { "type": "Spacer", "props": { "height": 16 } },
                    { "type": "Text", "props": { "text": "Below" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Above").assertIsDisplayed()
        rule.onNodeWithText("Below").assertIsDisplayed()
    }

    @Test
    fun valid_json_card_renders() {
        val json = """
            {
                "type": "Card",
                "props": {
                    "elevation": 4,
                    "shape": "rounded_12"
                },
                "children": [
                    { "type": "Text", "props": { "text": "Card Content" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Card Content").assertIsDisplayed()
    }

    @Test
    fun valid_json_box_renders() {
        val json = """
            {
                "type": "Box",
                "props": {
                    "contentAlignment": "center"
                },
                "children": [
                    { "type": "Text", "props": { "text": "Boxed" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Boxed").assertIsDisplayed()
    }

    // ─── DSL round-trip ─────────────────────────────────────────

    @Test
    fun dsl_to_json_to_ui_round_trip() {
        val node = KColumn(modifier = kModifier(fillMaxSize = 1f)) {
            KText("DSL → JSON → UI", fontSize = 20)
            KSpacer(height = 8)
            KRow {
                KText("A")
                KText("B")
            }
        }
        val json = node.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("DSL → JSON → UI").assertIsDisplayed()
        rule.onNodeWithText("A").assertIsDisplayed()
        rule.onNodeWithText("B").assertIsDisplayed()
    }

    // ─── Unknown type ───────────────────────────────────────────

    @Test
    fun unknown_type_renders_fallback_text() {
        val json = """
            { "type": "UnknownWidget", "props": {} }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Unknown component: UnknownWidget").assertIsDisplayed()
    }

    // ─── Modifier features ──────────────────────────────────────

    @Test
    fun modifier_with_padding_renders() {
        val json = """
            {
                "type": "Column",
                "props": {
                    "modifier": {
                        "padding": { "all": 16 },
                        "fillMaxWidth": 1.0
                    }
                },
                "children": [
                    { "type": "Text", "props": { "text": "Padded" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Padded").assertIsDisplayed()
    }

    @Test
    fun modifier_with_background_renders() {
        val json = """
            {
                "type": "Box",
                "props": {
                    "modifier": {
                        "background": "#FFE3F2FD",
                        "cornerRadius": 12
                    }
                },
                "children": [
                    { "type": "Text", "props": { "text": "Coloured" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Coloured").assertIsDisplayed()
    }

    @Test
    fun modifier_with_scroll_renders() {
        val json = """
            {
                "type": "Column",
                "props": {
                    "modifier": {
                        "verticalScroll": true,
                        "fillMaxSize": 1.0
                    }
                },
                "children": [
                    { "type": "Text", "props": { "text": "Scrollable" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Scrollable").assertIsDisplayed()
    }

    // ─── Nested deep tree from JSON ─────────────────────────────

    @Test
    fun deeply_nested_json_renders() {
        val json = """
            {
                "type": "Column",
                "children": [
                    {
                        "type": "Row",
                        "children": [
                            {
                                "type": "Box",
                                "children": [
                                    {
                                        "type": "Column",
                                        "children": [
                                            { "type": "Text", "props": { "text": "Level 4" } }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Level 4").assertIsDisplayed()
    }

    // ─── LazyColumn from JSON ───────────────────────────────────

    @Test
    fun lazy_column_from_json_renders() {
        val json = """
            {
                "type": "LazyColumn",
                "children": [
                    { "type": "Text", "props": { "text": "Lazy 1" } },
                    { "type": "Text", "props": { "text": "Lazy 2" } },
                    { "type": "Text", "props": { "text": "Lazy 3" } }
                ]
            }
        """.trimIndent()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Lazy 1").assertIsDisplayed()
        rule.onNodeWithText("Lazy 2").assertIsDisplayed()
        rule.onNodeWithText("Lazy 3").assertIsDisplayed()
    }
}
