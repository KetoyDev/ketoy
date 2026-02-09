package com.developerstring.ketoy.parser

import androidx.compose.ui.graphics.Color

/**
 * Parse a colour string (hex `#RRGGBB` / `#AARRGGBB` or named) to [Color].
 */
fun parseColor(colorString: String?): Color {
    if (colorString == null) return Color.Black
    if (colorString.startsWith("#")) {
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (_: Exception) {
            Color.Black
        }
    }
    return when (colorString.lowercase()) {
        "red"       -> Color.Red
        "blue"      -> Color.Blue
        "green"     -> Color.Green
        "yellow"    -> Color.Yellow
        "white"     -> Color.White
        "black"     -> Color.Black
        "gray"      -> Color.Gray
        "transparent" -> Color.Transparent
        "cyan"      -> Color.Cyan
        "magenta"   -> Color.Magenta
        "darkgray"  -> Color.DarkGray
        "lightgray" -> Color.LightGray
        else        -> Color.Black
    }
}

/**
 * Same as [parseColor] but returns `null` for unknown values.
 */
fun parseColorOrNull(colorString: String?): Color? {
    if (colorString == null) return null
    if (colorString.startsWith("#")) {
        return try { Color(android.graphics.Color.parseColor(colorString)) } catch (_: Exception) { null }
    }
    return when (colorString.lowercase()) {
        "red"       -> Color.Red
        "blue"      -> Color.Blue
        "green"     -> Color.Green
        "yellow"    -> Color.Yellow
        "white"     -> Color.White
        "black"     -> Color.Black
        "gray"      -> Color.Gray
        "transparent" -> Color.Transparent
        "cyan"      -> Color.Cyan
        "magenta"   -> Color.Magenta
        "darkgray"  -> Color.DarkGray
        "lightgray" -> Color.LightGray
        else        -> null
    }
}
