/**
 * Colour-string parser for the Ketoy SDUI engine.
 *
 * Provides both **non-composable** helpers ([parseColor], [parseColorOrNull])
 * that handle hex (`#RRGGBB`, `#AARRGGBB`) and named colours, and
 * **composable** helpers ([resolveKetoyColor], [resolveKetoyColorOrNull])
 * that additionally resolve `@theme/` tokens against the current
 * [KetoyColorScheme] via [LocalKetoyColors].
 *
 * @see resolveKetoyColor
 * @see parseColor
 */
package com.developerstring.ketoy.parser

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.developerstring.ketoy.theme.LocalKetoyColors

// ═══════════════════════════════════════════════════════════════════
//  Non-composable colour parsing  (hex + named colours only)
// ═══════════════════════════════════════════════════════════════════

/**
 * Parses a colour string (hex `#RRGGBB` / `#AARRGGBB` or named) to a Compose [Color].
 *
 * This function is **not** composable and does **not** resolve
 * `@theme/` tokens. For theme-aware resolution use [resolveKetoyColor].
 *
 * Recognised named colours: `red`, `blue`, `green`, `yellow`, `white`,
 * `black`, `gray`, `transparent`, `cyan`, `magenta`, `darkgray`, `lightgray`.
 *
 * @param colorString a hex string (`"#FF0000"`) or a named colour (`"red"`),
 *   or `null`.
 * @return the parsed [Color], or [Color.Black] when the input is `null` or
 *   unrecognised.
 * @see resolveKetoyColor
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
 * Same as [parseColor] but returns `null` for unknown or absent values
 * instead of a fallback colour.
 *
 * @param colorString a hex or named colour string, or `null`.
 * @return the parsed [Color], or `null` when the input is absent or
 *   unrecognised.
 * @see parseColor
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
 * Resolves any colour string — `@theme/` tokens, hex, or named colours.
 *
 * Theme tokens (e.g. `"@theme/primary"`) are resolved against the
 * [KetoyColorScheme] provided by [KetoyThemeProvider] via [LocalKetoyColors].
 * Falls back to [parseColor] for non-theme strings.
 *
 * @param colorString a theme token, hex string, named colour, or `null`.
 * @return the resolved [Color], or [Color.Unspecified] when the input is `null`.
 * @see resolveKetoyColorOrNull
 * @see parseColor
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
 * Same as [resolveKetoyColor] but returns `null` when the input is `null`
 * or when a `@theme/` token cannot be resolved.
 *
 * @param colorString a theme token, hex string, named colour, or `null`.
 * @return the resolved [Color], or `null` when unresolvable.
 * @see resolveKetoyColor
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

/**
 * Maps a well-known colour name (case-insensitive) to a Compose [Color].
 *
 * @param name the colour name to look up (e.g. `"red"`, `"transparent"`).
 * @return the matching [Color], or `null` if the name is not recognised.
 */
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