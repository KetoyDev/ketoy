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
 * Parse a shape string (e.g. "circle", "rectangle", "rounded_12", "roundedcornershape(8dp)").
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
 * Parse a shape from a [JsonObject] with `type`, `radius`, `percent`, or per-corner values.
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
