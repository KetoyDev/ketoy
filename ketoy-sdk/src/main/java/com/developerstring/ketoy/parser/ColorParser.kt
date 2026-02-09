package com.developerstring.ketoy.parser

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.developerstring.ketoy.theme.LocalKetoyColors

// ═══════════════════════════════════════════════════════════════════
//  Non-composable colour parsing  (hex + named colours only)
// ═══════════════════════════════════════════════════════════════════

/**
 * Parse a colour string (hex `#RRGGBB` / `#AARRGGBB` or named) to [Color].
 *
 * This function is **not** composable and does **not** resolve
 * `@theme/` tokens.  For theme-aware resolution use [resolveKetoyColor].
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
    return namedColorOrNull(colorString) ?: Color.Black
}

/**
 * Same as [parseColor] but returns `null` for unknown values.
 */
fun parseColorOrNull(colorString: String?): Color? {
    if (colorString == null) return null
    if (colorString.startsWith("#")) {
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (_: Exception) { null }
    }
    return namedColorOrNull(colorString)
}

// ═══════════════════════════════════════════════════════════════════
//  Composable theme-aware colour resolution
// ═══════════════════════════════════════════════════════════════════

/**
 * Resolve any colour string — `@theme/` tokens, hex, or named colours.
 *
 * Theme tokens are resolved against the [KetoyColorScheme] provided by
 * [KetoyThemeProvider] (via [LocalKetoyColors]).
 * Returns [Color.Unspecified] when the input is `null`.
 */
@Composable
fun resolveKetoyColor(colorString: String?): Color {
    if (colorString == null) return Color.Unspecified
    if (colorString.startsWith("@theme/")) {
        val token = colorString.removePrefix("@theme/")
        return LocalKetoyColors.current.resolve(token) ?: Color.Unspecified
    }
    return parseColor(colorString)
}

/**
 * Same as [resolveKetoyColor] but returns `null` when the input is absent.
 */
@Composable
fun resolveKetoyColorOrNull(colorString: String?): Color? {
    if (colorString == null) return null
    if (colorString.startsWith("@theme/")) {
        val token = colorString.removePrefix("@theme/")
        return LocalKetoyColors.current.resolve(token)
    }
    return parseColorOrNull(colorString)
}

// ─── internal helpers ─────────────────────────────────────────────

private fun namedColorOrNull(name: String): Color? = when (name.lowercase()) {
    "red"         -> Color.Red
    "blue"        -> Color.Blue
    "green"       -> Color.Green
    "yellow"      -> Color.Yellow
    "white"       -> Color.White
    "black"       -> Color.Black
    "gray"        -> Color.Gray
    "transparent" -> Color.Transparent
    "cyan"        -> Color.Cyan
    "magenta"     -> Color.Magenta
    "darkgray"    -> Color.DarkGray
    "lightgray"   -> Color.LightGray
    else          -> null
}