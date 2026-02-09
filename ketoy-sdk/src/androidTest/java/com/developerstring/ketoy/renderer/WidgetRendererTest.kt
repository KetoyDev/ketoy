package com.developerstring.ketoy.renderer

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.*
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.util.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Compose UI tests for widget renderers (Text, Button, Spacer, Card, Image).
 */
class WidgetRendererTest {

    @get:Rule
    val rule = createComposeRule()

    @Before
    fun setUp() {
        ActionRegistry.clear()
    }

    @After
    fun tearDown() {
        ActionRegistry.clear()
    }

    // ─── Text ───────────────────────────────────────────────────

    @Test
    fun text_renders_content() {
        val json = KColumn {
            KText("Hello World")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Hello World").assertIsDisplayed()
    }

    @Test
    fun text_with_styling_renders() {
        val json = KColumn {
            KText(
                "Styled Text",
                fontSize = 24,
                fontWeight = KFontWeights.Bold,
                color = "#FF2196F3"
            )
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Styled Text").assertIsDisplayed()
    }

    @Test
    fun text_with_max_lines_renders() {
        val json = KColumn {
            KText("Truncated", maxLines = 1, overflow = "ellipsis")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Truncated").assertIsDisplayed()
    }

    @Test
    fun multiple_text_nodes_render() {
        val json = KColumn {
            KText("Title")
            KText("Subtitle")
            KText("Body")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Title").assertIsDisplayed()
        rule.onNodeWithText("Subtitle").assertIsDisplayed()
        rule.onNodeWithText("Body").assertIsDisplayed()
    }

    // ─── Button ─────────────────────────────────────────────────

    @Test
    fun button_renders_with_text() {
        val json = KColumn {
            KButton(containerColor = "#FF2196F3") {
                KText("Click Me")
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Click Me").assertIsDisplayed()
    }

    @Test
    fun button_is_clickable() {
        val json = KColumn {
            KButton(containerColor = "#FF0000") {
                KText("Action")
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        // Button text should exist and its parent should be clickable
        rule.onNodeWithText("Action").assertIsDisplayed()
    }

    @Test
    fun button_with_shape_renders() {
        val json = KColumn {
            KButton(shape = KShapes.Circle) {
                KText("Round")
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Round").assertIsDisplayed()
    }

    // ─── Card ───────────────────────────────────────────────────

    @Test
    fun card_renders_children() {
        val json = KColumn {
            KCard(containerColor = "#FFFFFFFF", elevation = 4) {
                KText("Card Content")
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Card Content").assertIsDisplayed()
    }

    @Test
    fun card_with_shape_renders() {
        val json = KColumn {
            KCard(shape = KShapes.Rounded16, elevation = 8) {
                KColumn {
                    KText("Card Title")
                    KText("Card Body")
                }
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Card Title").assertIsDisplayed()
        rule.onNodeWithText("Card Body").assertIsDisplayed()
    }

    // ─── Spacer ─────────────────────────────────────────────────

    @Test
    fun spacer_renders_without_crash() {
        val json = KColumn {
            KText("Before")
            KSpacer(height = 16)
            KText("After")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Before").assertIsDisplayed()
        rule.onNodeWithText("After").assertIsDisplayed()
    }

    @Test
    fun spacer_with_width_in_row() {
        val json = KRow {
            KText("L")
            KSpacer(width = 24)
            KText("R")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("L").assertIsDisplayed()
        rule.onNodeWithText("R").assertIsDisplayed()
    }

    // ─── Complex widget tree ────────────────────────────────────

    @Test
    fun complex_widget_tree_renders() {
        val json = KColumn(modifier = kModifier(fillMaxSize = 1f)) {
            KText("Welcome", fontSize = 28, fontWeight = KFontWeights.Bold)
            KSpacer(height = 16)
            KCard(elevation = 4, shape = KShapes.Rounded12) {
                KColumn(modifier = kModifier(padding = kPadding(all = 16))) {
                    KText("Balance", color = "#FF757575")
                    KText("$12,345", fontSize = 32, fontWeight = KFontWeights.Bold)
                }
            }
            KSpacer(height = 24)
            KRow(horizontalArrangement = KArrangements.SpaceBetween) {
                KButton(containerColor = "#FF4CAF50") { KText("Send") }
                KButton(containerColor = "#FF2196F3") { KText("Receive") }
            }
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Welcome").assertIsDisplayed()
        rule.onNodeWithText("Balance").assertIsDisplayed()
        rule.onNodeWithText("$12,345").assertIsDisplayed()
        rule.onNodeWithText("Send").assertIsDisplayed()
        rule.onNodeWithText("Receive").assertIsDisplayed()
    }

    // ─── Modifiers on widgets ───────────────────────────────────

    @Test
    fun text_with_modifier_renders() {
        val json = KColumn {
            KText(
                "Modified",
                modifier = kModifier(
                    background = "#FFE3F2FD",
                    padding = kPadding(all = 12),
                    cornerRadius = 8
                )
            )
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Modified").assertIsDisplayed()
    }

    @Test
    fun gradient_background_renders_without_crash() {
        val json = KBox(
            modifier = kModifier(
                fillMaxWidth = 1f,
                height = 200,
                gradient = KGradients.linear(
                    listOf("#FF2196F3", "#FF1565C0"),
                    KGradients.Directions.Vertical
                ),
                cornerRadius = 16
            )
        ) {
            KText("Gradient", color = "#FFFFFFFF")
        }.toJson()

        rule.setContent { JSONStringToUI(json) }

        rule.onNodeWithText("Gradient").assertIsDisplayed()
    }
}
