package com.developerstring.ketoy.parser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

// ─── Vertical Arrangement ─────────────────────────────────────────

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

fun parseHorizontalAlignment(props: JsonObject): Alignment.Horizontal {
    return when (props["horizontalAlignment"]?.jsonPrimitive?.content) {
        "center", "centerHorizontally" -> Alignment.CenterHorizontally
        "start" -> Alignment.Start
        "end" -> Alignment.End
        else -> Alignment.Start
    }
}

fun parseVerticalAlignment(props: JsonObject): Alignment.Vertical {
    return when (props["verticalAlignment"]?.jsonPrimitive?.content) {
        "center", "centerVertically" -> Alignment.CenterVertically
        "top" -> Alignment.Top
        "bottom" -> Alignment.Bottom
        else -> Alignment.Top
    }
}

fun parseContentAlignment(props: JsonObject): Alignment {
    return parseContentAlignmentFromString(
        props["contentAlignment"]?.jsonPrimitive?.content ?: ""
    )
}

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
