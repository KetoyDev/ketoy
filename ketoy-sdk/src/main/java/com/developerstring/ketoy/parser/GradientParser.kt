package com.developerstring.ketoy.parser

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import kotlinx.serialization.json.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Parse a gradient [JsonObject] to a Compose [Brush].
 *
 * Supported gradient types: `linear`, `radial`, `angular` / `sweep`.
 */
fun parseGradient(gradientObject: JsonObject): Brush? {
    try {
        val type = gradientObject["type"]?.jsonPrimitive?.content ?: "linear"
        val colorsList = gradientObject["colors"]?.jsonArray

        if (colorsList == null || colorsList.isEmpty()) return null

        // Parse colours
        val colors = colorsList.mapNotNull { colorElement ->
            when (colorElement) {
                is JsonPrimitive -> parseColor(colorElement.content)
                is JsonObject -> {
                    val colorString = colorElement["color"]?.jsonPrimitive?.content
                    val alpha = colorElement["alpha"]?.jsonPrimitive?.floatOrNull
                    val color = parseColor(colorString)
                    if (alpha != null && alpha != 1.0f) color.copy(alpha = alpha) else color
                }
                else -> null
            }
        }

        if (colors.size < 2) return null

        return when (type.lowercase()) {
            "linear" -> {
                val direction = gradientObject["direction"]?.jsonPrimitive?.content ?: "vertical"
                val angle = gradientObject["angle"]?.jsonPrimitive?.floatOrNull

                if (angle != null) {
                    val radians = Math.toRadians(angle.toDouble())
                    val startX = 0.5f - cos(radians).toFloat() * 0.5f
                    val startY = 0.5f - sin(radians).toFloat() * 0.5f
                    val endX = 0.5f + cos(radians).toFloat() * 0.5f
                    val endY = 0.5f + sin(radians).toFloat() * 0.5f

                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY)
                    )
                } else {
                    when (direction.lowercase()) {
                        "horizontal", "right" -> Brush.horizontalGradient(colors)
                        "vertical", "down" -> Brush.verticalGradient(colors)
                        "topleft" -> Brush.linearGradient(
                            colors = colors,
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                        "topright" -> Brush.linearGradient(
                            colors = colors,
                            start = Offset(Float.POSITIVE_INFINITY, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                        else -> Brush.verticalGradient(colors)
                    }
                }
            }

            "radial" -> {
                val centerX = gradientObject["centerX"]?.jsonPrimitive?.floatOrNull ?: 0.5f
                val centerY = gradientObject["centerY"]?.jsonPrimitive?.floatOrNull ?: 0.5f
                val radius = gradientObject["radius"]?.jsonPrimitive?.floatOrNull ?: 0.5f

                Brush.radialGradient(
                    colors = colors,
                    center = Offset(centerX, centerY),
                    radius = radius * 1000f,
                    tileMode = TileMode.Clamp
                )
            }

            "angular", "sweep" -> {
                val centerX = gradientObject["centerX"]?.jsonPrimitive?.floatOrNull ?: 0.5f
                val centerY = gradientObject["centerY"]?.jsonPrimitive?.floatOrNull ?: 0.5f

                Brush.sweepGradient(
                    colors = colors,
                    center = Offset(centerX, centerY)
                )
            }

            else -> Brush.verticalGradient(colors)
        }
    } catch (e: Exception) {
        return null
    }
}
