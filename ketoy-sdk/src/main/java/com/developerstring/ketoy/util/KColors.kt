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
