/**
 * Shape parser for the Ketoy SDUI engine.
 *
 * Translates shape descriptors — either plain strings (e.g. `"circle"`,
 * `"rounded_12"`) or structured [JsonObject]s — into Jetpack Compose [Shape]
 * instances used for clipping, backgrounds, and borders.
 *
 * @see parseModifier
 */
package com.developerstring.ketoy.parser

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses a shape descriptor string into a Compose [Shape].
 *
 * ### Accepted formats
 * | Input string                         | Result                              |
 * |--------------------------------------|-------------------------------------|
 * | `"circle"`                           | [CircleShape]                       |
 * | `"rectangle"` / `"clip"`             | [RectangleShape]                    |
 * | `"rounded_12"`                       | [RoundedCornerShape] with 12 dp     |
 * | `"rounded_corners_4_8_4_8"`          | Per-corner [RoundedCornerShape]     |
 * | `"roundedcornershape(16dp)"`         | [RoundedCornerShape] with 16 dp     |
 * | `"roundedcornershape(50%)"`          | 50 % rounded corners                |
 * | `"roundedcornershape(4,8,4,8)"`      | Per-corner dp values                |
 *
 * @param shapeType the descriptor string (case-insensitive), or `null`.
 * @return the corresponding [Shape]; defaults to [RectangleShape] for
 *   unrecognised or `null` input.
 * @see parseShapeWithRadius
 */
fun parseShape(shapeType: String?): Shape {
    return when {
        shapeType?.lowercase() == "circle" -> CircleShape
        shapeType?.lowercase() == "rectangle" -> RectangleShape
        shapeType?.lowercase() == "clip" -> RectangleShape

        // Dynamic format: "rounded_X"
        shapeType?.startsWith("rounded_") == true -> {
            if (shapeType.startsWith("rounded_corners_")) {
                val cornersStr = shapeType.substringAfter("rounded_corners_")
                val corners = cornersStr.split("_").mapNotNull { it.toIntOrNull() }
                if (corners.size == 4) {
                    RoundedCornerShape(
                        topStart = corners[0].dp,
                        topEnd = corners[1].dp,
                        bottomEnd = corners[2].dp,
                        bottomStart = corners[3].dp
                    )
                } else {
                    RectangleShape
                }
            } else {
                val radius = shapeType.substringAfter("rounded_").toIntOrNull() ?: 8
                RoundedCornerShape(radius.dp)
            }
        }

        shapeType?.startsWith("roundedcornershape(") == true -> {
            val content = shapeType.substringAfter("(").substringBefore(")")
            when {
                content.endsWith("dp") -> {
                    val radius = content.substringBefore("dp").toIntOrNull() ?: 0
                    RoundedCornerShape(radius.dp)
                }
                content.endsWith("%") -> {
                    val percent = content.substringBefore("%").toIntOrNull() ?: 0
                    RoundedCornerShape(percent = percent)
                }
                content.contains(",") -> {
                    val corners = content.split(",").map { it.trim() }
                    if (corners.size == 4) {
                        RoundedCornerShape(
                            topStart = (corners[0].toIntOrNull() ?: 0).dp,
                            topEnd = (corners[1].toIntOrNull() ?: 0).dp,
                            bottomEnd = (corners[2].toIntOrNull() ?: 0).dp,
                            bottomStart = (corners[3].toIntOrNull() ?: 0).dp
                        )
                    } else {
                        RoundedCornerShape((corners[0].toIntOrNull() ?: 0).dp)
                    }
                }
                else -> {
                    RoundedCornerShape((content.toIntOrNull() ?: 0).dp)
                }
            }
        }

        else -> RectangleShape
    }
}

/**
 * Parses a shape from a structured [JsonObject] with explicit type and
 * radius/percent/per-corner values.
 *
 * ### Expected JSON keys
 * | Key           | Type   | Description                                 |
 * |---------------|--------|---------------------------------------------|
 * | `type`        | String | `"circle"`, `"rectangle"`, `"rounded"`       |
 * | `radius`      | Int    | Uniform corner radius in dp                 |
 * | `percent`     | Int    | Corner radius as a percentage (0–100)       |
 * | `topLeft`     | Int    | Top-start corner radius in dp               |
 * | `topRight`    | Int    | Top-end corner radius in dp                 |
 * | `bottomLeft`  | Int    | Bottom-start corner radius in dp            |
 * | `bottomRight` | Int    | Bottom-end corner radius in dp              |
 *
 * @param shapeProps the JSON object describing the shape, or `null`.
 * @return the corresponding [Shape]; defaults to [RectangleShape] for
 *   `null` or unrecognised `type` values.
 * @see parseShape
 */
fun parseShapeWithRadius(shapeProps: JsonObject?): Shape {
    if (shapeProps == null) return RectangleShape

    val type = shapeProps["type"]?.jsonPrimitive?.content?.lowercase()

    return when (type) {
        "circle" -> CircleShape
        "rectangle" -> RectangleShape
        "rounded", "roundedcornershape" -> {
            when {
                shapeProps["radius"] != null -> {
                    val radius = shapeProps["radius"]?.jsonPrimitive?.intOrNull ?: 0
                    RoundedCornerShape(radius.dp)
                }
                shapeProps["percent"] != null -> {
                    val percent = shapeProps["percent"]?.jsonPrimitive?.intOrNull ?: 0
                    RoundedCornerShape(percent = percent)
                }
                shapeProps["topLeft"] != null || shapeProps["topRight"] != null ||
                    shapeProps["bottomLeft"] != null || shapeProps["bottomRight"] != null -> {
                    RoundedCornerShape(
                        topStart = (shapeProps["topLeft"]?.jsonPrimitive?.intOrNull ?: 0).dp,
                        topEnd = (shapeProps["topRight"]?.jsonPrimitive?.intOrNull ?: 0).dp,
                        bottomEnd = (shapeProps["bottomRight"]?.jsonPrimitive?.intOrNull ?: 0).dp,
                        bottomStart = (shapeProps["bottomLeft"]?.jsonPrimitive?.intOrNull ?: 0).dp
                    )
                }
                else -> RoundedCornerShape(8.dp)
            }
        }
        else -> RectangleShape
    }
}
