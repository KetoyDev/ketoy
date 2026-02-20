/**
 * Predefined colour constants and helpers for the Ketoy SDUI Kotlin DSL.
 *
 * This file provides:
 * - **Static hex colours** (e.g. [Blue], [Red]) that can be used directly in
 *   the DSL or in JSON payloads.
 * - **Material 3 theme-aware colour references** (e.g. [Primary], [Surface])
 *   using `@theme/` tokens that are resolved at render-time via
 *   [LocalKetoyColors].
 * - **Utility functions** ([hex], [withAlpha], [withAlphaPercent]) for
 *   normalising and manipulating hex colour strings.
 *
 * @see resolveKetoyColor
 * @see parseColor
 */
package com.developerstring.ketoy.util

/**
 * Predefined hex-colour string constants and theme-aware colour references
 * for use with the Ketoy Kotlin DSL and JSON payloads.
 *
 * ### Static colours
 * ```kotlin
 * KText(text = "Hello", color = KColors.Blue)
 * ```
 *
 * ### Theme-aware colours
 * ```kotlin
 * KText(text = "Hello", color = KColors.Primary)  // resolves at runtime
 * ```
 *
 * ### Hex helpers
 * ```kotlin
 * KColors.hex("FF5722")              // → "#FFFF5722"
 * KColors.withAlpha(KColors.Blue, 0.5f) // → "#802196F3"
 * ```
 */
object KColors {
    const val Blue = "#FF2196F3"
    const val Red = "#FFF44336"
    const val Green = "#FF4CAF50"
    const val Orange = "#FFFF9800"
    const val Purple = "#FF9C27B0"
    const val Teal = "#FF009688"
    const val Gray = "#FF9E9E9E"
    const val Black = "#FF000000"
    const val White = "#FFFFFFFF"
    const val Transparent = "#00000000"

    // ── Material3 theme-aware colour references ─────────
    // These are resolved at render-time via MaterialTheme.colorScheme
    const val Primary = "@theme/primary"
    const val OnPrimary = "@theme/onPrimary"
    const val PrimaryContainer = "@theme/primaryContainer"
    const val OnPrimaryContainer = "@theme/onPrimaryContainer"
    const val Secondary = "@theme/secondary"
    const val OnSecondary = "@theme/onSecondary"
    const val SecondaryContainer = "@theme/secondaryContainer"
    const val OnSecondaryContainer = "@theme/onSecondaryContainer"
    const val Tertiary = "@theme/tertiary"
    const val OnTertiary = "@theme/onTertiary"
    const val TertiaryContainer = "@theme/tertiaryContainer"
    const val OnTertiaryContainer = "@theme/onTertiaryContainer"
    const val Error = "@theme/error"
    const val OnError = "@theme/onError"
    const val ErrorContainer = "@theme/errorContainer"
    const val OnErrorContainer = "@theme/onErrorContainer"
    const val Background = "@theme/background"
    const val OnBackground = "@theme/onBackground"
    const val Surface = "@theme/surface"
    const val OnSurface = "@theme/onSurface"
    const val SurfaceVariant = "@theme/surfaceVariant"
    const val OnSurfaceVariant = "@theme/onSurfaceVariant"
    const val Outline = "@theme/outline"
    const val OutlineVariant = "@theme/outlineVariant"
    const val InversePrimary = "@theme/inversePrimary"
    const val InverseSurface = "@theme/inverseSurface"
    const val InverseOnSurface = "@theme/inverseOnSurface"
    /** Theme-aware reference for `MaterialTheme.colorScheme.surfaceTint`. */
    const val SurfaceTint = "@theme/surfaceTint"

    /**
     * Normalises a raw hex colour string into the `#AARRGGBB` format.
     *
     * Accepts 6-char (`RRGGBB`), 7-char (`#RRGGBB`), 8-char (`AARRGGBB`),
     * and 9-char (`#AARRGGBB`) inputs. When no alpha is provided, full
     * opacity (`FF`) is assumed.
     *
     * @param hex the input hex string.
     * @return the normalised `#AARRGGBB` string; falls back to
     *   `"#FF000000"` (black) for unrecognised formats.
     */
    fun hex(hex: String): String = when {
        hex.startsWith("#") && hex.length == 7 -> "#FF${hex.substring(1)}"
        hex.startsWith("#") && hex.length == 9 -> hex
        hex.length == 6 -> "#FF$hex"
        hex.length == 8 -> "#$hex"
        else -> "#FF000000"
    }

    /**
     * Returns a copy of [color] with the specified [alpha] value applied.
     *
     * @param color a hex colour string (e.g. `"#FF2196F3"`).
     * @param alpha the alpha value in the range `0f` (fully transparent)
     *   to `1f` (fully opaque).
     * @return the `#AARRGGBB` string with the new alpha.
     */
    fun withAlpha(color: String, alpha: Float): String {
        val a = alpha.coerceIn(0f, 1f)
        val alphaHex = (a * 255).toInt().toString(16).uppercase().padStart(2, '0')
        return if (color.startsWith("#") && color.length >= 7) {
            val rgb = color.substring(color.length - 6)
            "#$alphaHex$rgb"
        } else "#${alphaHex}000000"
    }

    /**
     * Returns a copy of [color] with alpha specified as a percentage.
     *
     * @param color a hex colour string.
     * @param alphaPercent the alpha as an integer percentage (0–100).
     * @return the `#AARRGGBB` string with the new alpha.
     * @see withAlpha
     */
    fun withAlphaPercent(color: String, alphaPercent: Int): String =
        withAlpha(color, alphaPercent / 100f)
}
