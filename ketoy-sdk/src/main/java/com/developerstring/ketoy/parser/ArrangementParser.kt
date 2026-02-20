/**
 * Arrangement & alignment parser for the Ketoy SDUI engine.
 *
 * Converts JSON layout properties (`verticalArrangement`, `horizontalArrangement`,
 * `horizontalAlignment`, `verticalAlignment`, `contentAlignment`) into their
 * Jetpack Compose equivalents used by `Row`, `Column`, and `Box` layouts.
 * Also provides a general-purpose [parsePadding] helper for `PaddingValues`.
 *
 * @see parseModifier
 */
package com.developerstring.ketoy.parser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

// ─── Vertical Arrangement ───────────────────────────────────────────

/**
 * Parses the `"verticalArrangement"` property from a component [JsonObject]
 * into a Compose [Arrangement.Vertical].
 *
 * The value may be either:
 * - A **string** — `"center"`, `"spaceBetween"`, `"spaceEvenly"`, `"spaceAround"`,
 *   `"top"`, `"bottom"`, or `"spacedBy_<dp>"` (e.g. `"spacedBy_8"`).
 * - An **object** — `{ "type": "spacedBy", "spacing": 8 }`.
 *
 * @param props the component’s top-level [JsonObject].
 * @return the resolved [Arrangement.Vertical]; defaults to [Arrangement.Top].
 */

fun parseVerticalArrangement(props: JsonObject): Arrangement.Vertical {
    val arrangement = props["verticalArrangement"] ?: return Arrangement.Top

    return when (arrangement) {
        is JsonPrimitive -> {
            when (arrangement.content) {
                "center", "centerVertically" -> Arrangement.Center
                "spaceBetween" -> Arrangement.SpaceBetween
                "spaceEvenly" -> Arrangement.SpaceEvenly
                "spaceAround" -> Arrangement.SpaceAround
                "start", "top", "Top" -> Arrangement.Top
                "end", "bottom", "Bottom" -> Arrangement.Bottom
                else -> {
                    if (arrangement.content.startsWith("spacedBy_")) {
                        val spacing = arrangement.content.substringAfter("spacedBy_").toIntOrNull() ?: 0
                        Arrangement.spacedBy(spacing.dp)
                    } else Arrangement.Top
                }
            }
        }
        is JsonObject -> {
            val spacingValue = arrangement["spacing"]?.jsonPrimitive?.intOrNull ?: 0
            when (arrangement["type"]?.jsonPrimitive?.content) {
                "spacedBy" -> Arrangement.spacedBy(spacingValue.dp)
                "center" -> Arrangement.Center
                "spaceBetween" -> Arrangement.SpaceBetween
                "spaceEvenly" -> Arrangement.SpaceEvenly
                "spaceAround" -> Arrangement.SpaceAround
                "start", "top" -> Arrangement.Top
                "end", "bottom" -> Arrangement.Bottom
                else -> Arrangement.spacedBy(spacingValue.dp)
            }
        }
        else -> Arrangement.Top
    }
}

// ─── Horizontal Arrangement ───────────────────────────────────────
/**
 * Parses the `"horizontalArrangement"` property from a component [JsonObject]
 * into a Compose [Arrangement.Horizontal].
 *
 * Accepts the same formats as [parseVerticalArrangement] (primitive string
 * or structured object), using horizontal-axis names (`"start"`, `"end"`,
 * `"center"`, `"spacedBy_<dp>"`, etc.).
 *
 * @param props the component’s top-level [JsonObject].
 * @return the resolved [Arrangement.Horizontal]; defaults to [Arrangement.Start].
 * @see parseVerticalArrangement
 */
fun parseHorizontalArrangement(props: JsonObject): Arrangement.Horizontal {
    val arrangement = props["horizontalArrangement"] ?: return Arrangement.Start

    return when (arrangement) {
        is JsonPrimitive -> {
            when (arrangement.content) {
                "center", "centerHorizontally" -> Arrangement.Center
                "spaceBetween" -> Arrangement.SpaceBetween
                "spaceEvenly" -> Arrangement.SpaceEvenly
                "spaceAround" -> Arrangement.SpaceAround
                "start" -> Arrangement.Start
                "end" -> Arrangement.End
                else -> {
                    if (arrangement.content.startsWith("spacedBy_")) {
                        val spacing = arrangement.content.substringAfter("spacedBy_").toIntOrNull() ?: 0
                        Arrangement.spacedBy(spacing.dp)
                    } else Arrangement.Start
                }
            }
        }
        is JsonObject -> {
            val spacingValue = arrangement["spacing"]?.jsonPrimitive?.intOrNull ?: 0
            when (arrangement["type"]?.jsonPrimitive?.content) {
                "spacedBy" -> Arrangement.spacedBy(spacingValue.dp)
                "center" -> Arrangement.Center
                "spaceBetween" -> Arrangement.SpaceBetween
                "spaceEvenly" -> Arrangement.SpaceEvenly
                "spaceAround" -> Arrangement.SpaceAround
                "start" -> Arrangement.Start
                "end" -> Arrangement.End
                else -> Arrangement.spacedBy(spacingValue.dp)
            }
        }
        else -> Arrangement.Start
    }
}

// ─── Alignments ───────────────────────────────────────────────────

/**
 * Parses the `"horizontalAlignment"` property into an [Alignment.Horizontal].
 *
 * Recognised values: `"center"` / `"centerHorizontally"`, `"start"`, `"end"`.
 *
 * @param props the component’s top-level [JsonObject].
 * @return the resolved horizontal alignment; defaults to [Alignment.Start].
 */
fun parseHorizontalAlignment(props: JsonObject): Alignment.Horizontal {
    return when (props["horizontalAlignment"]?.jsonPrimitive?.content) {
        "center", "centerHorizontally" -> Alignment.CenterHorizontally
        "start" -> Alignment.Start
        "end" -> Alignment.End
        else -> Alignment.Start
    }
}

/**
 * Parses the `"verticalAlignment"` property into an [Alignment.Vertical].
 *
 * Recognised values: `"center"` / `"centerVertically"`, `"top"`, `"bottom"`.
 *
 * @param props the component’s top-level [JsonObject].
 * @return the resolved vertical alignment; defaults to [Alignment.Top].
 */
fun parseVerticalAlignment(props: JsonObject): Alignment.Vertical {
    return when (props["verticalAlignment"]?.jsonPrimitive?.content) {
        "center", "centerVertically" -> Alignment.CenterVertically
        "top" -> Alignment.Top
        "bottom" -> Alignment.Bottom
        else -> Alignment.Top
    }
}

/**
 * Parses the `"contentAlignment"` property into a two-dimensional [Alignment]
 * suitable for `Box` composables.
 *
 * @param props the component’s top-level [JsonObject].
 * @return the resolved [Alignment]; defaults to [Alignment.TopStart].
 * @see parseContentAlignmentFromString
 */
fun parseContentAlignment(props: JsonObject): Alignment {
    return parseContentAlignmentFromString(
        props["contentAlignment"]?.jsonPrimitive?.content ?: ""
    )
}

/**
 * Resolves a raw alignment string into a two-dimensional [Alignment].
 *
 * Recognised values: `"center"`, `"topStart"`, `"topCenter"`, `"topEnd"`,
 * `"centerStart"`, `"centerEnd"`, `"bottomStart"`, `"bottomCenter"`,
 * `"bottomEnd"`, and shorthand aliases (`"top"`, `"bottom"`, `"start"`, `"end"`).
 *
 * @param alignment the alignment string from the JSON payload.
 * @return the corresponding [Alignment]; defaults to [Alignment.TopStart].
 */
fun parseContentAlignmentFromString(alignment: String): Alignment {
    return when (alignment) {
        "center" -> Alignment.Center
        "topStart" -> Alignment.TopStart
        "topCenter" -> Alignment.TopCenter
        "topEnd" -> Alignment.TopEnd
        "centerStart" -> Alignment.CenterStart
        "centerEnd" -> Alignment.CenterEnd
        "bottomStart" -> Alignment.BottomStart
        "bottomCenter" -> Alignment.BottomCenter
        "bottomEnd" -> Alignment.BottomEnd
        "top" -> Alignment.TopCenter
        "bottom" -> Alignment.BottomCenter
        "start" -> Alignment.CenterStart
        "end" -> Alignment.CenterEnd
        else -> Alignment.TopStart
    }
}

// ─── Padding ──────────────────────────────────────────────────────

/**
 * Converts a JSON element into Compose [PaddingValues].
 *
 * Accepted formats:
 * - **Object** with keys: `all`, `horizontal`, `vertical`, `top`, `bottom`,
 *   `start`, `end` (all in dp).
 * - Any other element type returns `PaddingValues(0.dp)`.
 *
 * @param paddingElement the raw [JsonElement] from the component tree.
 * @return the resulting [PaddingValues].
 */
fun parsePadding(paddingElement: JsonElement): PaddingValues {
    return when (paddingElement) {
        is JsonObject -> {
            val all = paddingElement["all"]?.jsonPrimitive?.intOrNull?.dp
            val horizontal = paddingElement["horizontal"]?.jsonPrimitive?.intOrNull?.dp
            val vertical = paddingElement["vertical"]?.jsonPrimitive?.intOrNull?.dp
            val top = paddingElement["top"]?.jsonPrimitive?.intOrNull?.dp
            val bottom = paddingElement["bottom"]?.jsonPrimitive?.intOrNull?.dp
            val start = paddingElement["start"]?.jsonPrimitive?.intOrNull?.dp
            val end = paddingElement["end"]?.jsonPrimitive?.intOrNull?.dp

            when {
                all != null -> PaddingValues(all)
                horizontal != null || vertical != null -> PaddingValues(
                    horizontal = horizontal ?: 0.dp,
                    vertical = vertical ?: 0.dp
                )
                else -> PaddingValues(
                    top = top ?: 0.dp,
                    bottom = bottom ?: 0.dp,
                    start = start ?: 0.dp,
                    end = end ?: 0.dp
                )
            }
        }
        else -> PaddingValues(0.dp)
    }
}
