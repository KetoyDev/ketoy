package com.developerstring.ketoy.parser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

/**
 * Parse a [JsonObject] `"modifier"` key into a Compose [Modifier] chain.
 *
 * The order in which keys appear in the JSON object is preserved,
 * so the developer controls the modifier chain ordering.
 */
fun parseModifier(props: JsonObject): Modifier {
    var modifier: Modifier = Modifier

    val modifierProps = props["modifier"]?.jsonObject
    if (modifierProps == null || modifierProps.isEmpty()) return modifier

    try {
        // Pre-compute shape for reuse across background / clip / border
        val componentShape: Shape = when {
            modifierProps["shape"]?.let { it is JsonPrimitive } == true ->
                parseShape(modifierProps["shape"]!!.jsonPrimitive.content)
            modifierProps["shape"]?.let { it is JsonObject } == true ->
                parseShapeWithRadius(modifierProps["shape"]!!.jsonObject)
            modifierProps["cornerRadius"]?.let { it is JsonPrimitive } == true -> {
                val radius = modifierProps["cornerRadius"]!!.jsonPrimitive.int
                RoundedCornerShape(radius.dp)
            }
            else -> RectangleShape
        }

        // Walk keys in order so the developer controls the modifier chain
        modifierProps.forEach { (key, value) ->
            modifier = when (key) {
                // ── MARGIN (outer padding) ──────────────────────────────
                "margin" -> applyPaddingFromJson(modifier, value)

                // ── SIZE ────────────────────────────────────────────────
                "fillMaxSize" -> {
                    val f = value.jsonPrimitive.floatOrNull
                    if (f != null) modifier.fillMaxSize(f)
                    else if (value.jsonPrimitive.booleanOrNull == true) modifier.fillMaxSize()
                    else modifier
                }
                "fillMaxWidth" -> {
                    val f = value.jsonPrimitive.floatOrNull
                    if (f != null) modifier.fillMaxWidth(f)
                    else if (value.jsonPrimitive.booleanOrNull == true) modifier.fillMaxWidth()
                    else modifier
                }
                "fillMaxHeight" -> {
                    val f = value.jsonPrimitive.floatOrNull
                    if (f != null) modifier.fillMaxHeight(f)
                    else if (value.jsonPrimitive.booleanOrNull == true) modifier.fillMaxHeight()
                    else modifier
                }
                "wrapContentWidth" ->
                    if (value.jsonPrimitive.booleanOrNull == true) modifier.wrapContentWidth() else modifier
                "wrapContentHeight" ->
                    if (value.jsonPrimitive.booleanOrNull == true) modifier.wrapContentHeight() else modifier
                "size" ->
                    value.jsonPrimitive.intOrNull?.let { modifier.size(it.dp) } ?: modifier
                "width" ->
                    value.jsonPrimitive.intOrNull?.let { modifier.width(it.dp) } ?: modifier
                "height" ->
                    value.jsonPrimitive.intOrNull?.let { modifier.height(it.dp) } ?: modifier
                "weight" -> modifier // handled by parent scope (Row/Column)

                // ── POSITION ────────────────────────────────────────────
                "offsetX" -> {
                    val x = value.jsonPrimitive.intOrNull ?: 0
                    val y = modifierProps["offsetY"]?.jsonPrimitive?.intOrNull ?: 0
                    modifier.offset(x = x.dp, y = y.dp)
                }

                // ── SHADOW ──────────────────────────────────────────────
                "shadow" -> {
                    if (value is JsonObject) {
                        val elevation = value["elevation"]?.jsonPrimitive?.intOrNull?.dp ?: 4.dp
                        val shadowShape = when {
                            value["shape"]?.let { it is JsonPrimitive } == true ->
                                parseShape(value["shape"]!!.jsonPrimitive.content)
                            value["shape"]?.let { it is JsonObject } == true ->
                                parseShapeWithRadius(value["shape"]!!.jsonObject)
                            else -> componentShape
                        }
                        val ambientColor =
                            parseColorOrNull(value["ambientColor"]?.jsonPrimitive?.content)
                                ?: Color.Black.copy(alpha = 0.2f)
                        val spotColor =
                            parseColorOrNull(value["spotColor"]?.jsonPrimitive?.content)
                                ?: Color.Black.copy(alpha = 0.2f)
                        modifier.shadow(elevation, shadowShape, ambientColor = ambientColor, spotColor = spotColor)
                    } else modifier
                }

                // ── BACKGROUND ──────────────────────────────────────────
                "background" -> {
                    if (value is JsonPrimitive) {
                        val color = parseColor(value.content)
                        if (componentShape != RectangleShape) modifier.background(color, componentShape)
                        else modifier.background(color)
                    } else modifier
                }

                // ── GRADIENT BACKGROUND ─────────────────────────────────
                "gradient" -> {
                    if (value is JsonObject) {
                        val brush = parseGradient(value)
                        if (brush != null) {
                            if (componentShape != RectangleShape) modifier.background(brush, componentShape)
                            else modifier.background(brush)
                        } else modifier
                    } else modifier
                }

                // ── CLIP (auto-applied via shape) ───────────────────────
                "shape", "cornerRadius" -> {
                    if (componentShape != RectangleShape) modifier.clip(componentShape) else modifier
                }

                // ── BORDER ──────────────────────────────────────────────
                "border" -> {
                    if (value is JsonObject) {
                        val w = value["width"]?.jsonPrimitive?.intOrNull?.dp ?: 1.dp
                        val c = parseColor(value["color"]?.jsonPrimitive?.content)
                        val borderShape = when {
                            value["shape"]?.let { it is JsonPrimitive } == true ->
                                parseShape(value["shape"]!!.jsonPrimitive.content)
                            value["shape"]?.let { it is JsonObject } == true ->
                                parseShapeWithRadius(value["shape"]!!.jsonObject)
                            else -> componentShape
                        }
                        modifier.border(w, c, borderShape)
                    } else modifier
                }

                // ── PADDING (inner) ─────────────────────────────────────
                "padding" -> applyPaddingFromJson(modifier, value)

                // ── LEGACY PADDING ──────────────────────────────────────
                "paddingHorizontal" ->
                    value.jsonPrimitive.intOrNull?.let { modifier.padding(horizontal = it.dp) } ?: modifier
                "paddingVertical" ->
                    value.jsonPrimitive.intOrNull?.let { modifier.padding(vertical = it.dp) } ?: modifier
                "paddingTop" -> {
                    val top = value.jsonPrimitive.intOrNull?.dp ?: 0.dp
                    val bottom = modifierProps["paddingBottom"]?.jsonPrimitive?.intOrNull?.dp ?: 0.dp
                    val start = modifierProps["paddingStart"]?.jsonPrimitive?.intOrNull?.dp ?: 0.dp
                    val end = modifierProps["paddingEnd"]?.jsonPrimitive?.intOrNull?.dp ?: 0.dp
                    modifier.padding(top = top, bottom = bottom, start = start, end = end)
                }

                // ── EFFECTS ─────────────────────────────────────────────
                "alpha" ->
                    value.jsonPrimitive.floatOrNull?.let { modifier.alpha(it) } ?: modifier
                "scale" ->
                    value.jsonPrimitive.floatOrNull?.let { modifier.scale(it) } ?: modifier
                "scaleX" -> {
                    val sx = value.jsonPrimitive.floatOrNull ?: 1f
                    val sy = modifierProps["scaleY"]?.jsonPrimitive?.floatOrNull ?: 1f
                    modifier.scale(sx, sy)
                }
                "rotation" ->
                    value.jsonPrimitive.floatOrNull?.let { modifier.rotate(it) } ?: modifier

                // ── INTERACTION ─────────────────────────────────────────
                "clickable" ->
                    if (value.jsonPrimitive.booleanOrNull == true) modifier.clickable { } else modifier

                // ── SCROLL ──────────────────────────────────────────────
                "verticalScroll" ->
                    if (value.jsonPrimitive.booleanOrNull == true) modifier.verticalScroll(ScrollState(0)) else modifier
                "horizontalScroll" ->
                    if (value.jsonPrimitive.booleanOrNull == true) modifier.horizontalScroll(ScrollState(0)) else modifier

                // ── UNKNOWN → skip ──────────────────────────────────────
                else -> modifier
            }
        }
    } catch (_: Exception) {
        // Return whatever was accumulated
    }

    return modifier
}

// ─── private helper ─────────────────────────────────────────────────

private fun applyPaddingFromJson(base: Modifier, value: JsonElement): Modifier {
    return when (value) {
        is JsonPrimitive -> value.intOrNull?.let { base.padding(it.dp) } ?: base
        is JsonObject -> {
            val all = value["all"]?.jsonPrimitive?.intOrNull?.dp
            val h = value["horizontal"]?.jsonPrimitive?.intOrNull?.dp
            val v = value["vertical"]?.jsonPrimitive?.intOrNull?.dp
            val top = value["top"]?.jsonPrimitive?.intOrNull?.dp
            val bottom = value["bottom"]?.jsonPrimitive?.intOrNull?.dp
            val start = value["start"]?.jsonPrimitive?.intOrNull?.dp
            val end = value["end"]?.jsonPrimitive?.intOrNull?.dp

            when {
                all != null -> base.padding(all)
                h != null && v != null -> base.padding(horizontal = h, vertical = v)
                h != null -> base.padding(horizontal = h)
                v != null -> base.padding(vertical = v)
                top != null || bottom != null || start != null || end != null ->
                    base.padding(top = top ?: 0.dp, bottom = bottom ?: 0.dp, start = start ?: 0.dp, end = end ?: 0.dp)
                else -> base
            }
        }
        else -> base
    }
}
