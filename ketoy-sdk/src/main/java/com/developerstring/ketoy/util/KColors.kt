package com.developerstring.ketoy.util

/** Predefined hex-colour constants. */
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
    const val SurfaceTint = "@theme/surfaceTint"

    fun hex(hex: String): String = when {
        hex.startsWith("#") && hex.length == 7 -> "#FF${hex.substring(1)}"
        hex.startsWith("#") && hex.length == 9 -> hex
        hex.length == 6 -> "#FF$hex"
        hex.length == 8 -> "#$hex"
        else -> "#FF000000"
    }

    fun withAlpha(color: String, alpha: Float): String {
        val a = alpha.coerceIn(0f, 1f)
        val alphaHex = (a * 255).toInt().toString(16).uppercase().padStart(2, '0')
        return if (color.startsWith("#") && color.length >= 7) {
            val rgb = color.substring(color.length - 6)
            "#$alphaHex$rgb"
        } else "#${alphaHex}000000"
    }

    fun withAlphaPercent(color: String, alphaPercent: Int): String =
        withAlpha(color, alphaPercent / 100f)
}
