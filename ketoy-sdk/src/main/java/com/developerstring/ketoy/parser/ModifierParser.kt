/**
 * Modifier parser for the Ketoy Server-Driven UI engine.
 *
 * Converts a `"modifier"` JSON object from the component tree into a fully
 * chained Jetpack Compose [Modifier]. Every recognised key (e.g. `padding`,
 * `background`, `shadow`, `gradient`, `border`, `fillMaxWidth`, …) is mapped
 * to its Compose equivalent and applied in the order it appears in the JSON,
 * giving the backend full control over modifier ordering.
 *
 * @see parseShape
 * @see parseGradient
 * @see parseColor
 */
package com.developerstring.ketoy.parser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import com.developerstring.ketoy.model.KScrollConfig
import kotlinx.serialization.json.*

/**
 * Parses a [JsonObject] `"modifier"` key into a Compose [Modifier] chain.
 *
 * The order in which keys appear in the JSON object is preserved,
 * so the server / backend controls the modifier chain ordering.
 *
 * Supported modifier keys include:
 * - **Size**: `fillMaxSize`, `fillMaxWidth`, `fillMaxHeight`, `size`, `width`, `height`,
 *   `wrapContentWidth`, `wrapContentHeight`
 * - **Spacing**: `padding`, `margin`, `paddingHorizontal`, `paddingVertical`, `paddingTop`
 * - **Position**: `offsetX` / `offsetY`
 * - **Appearance**: `background`, `gradient`, `shape`, `cornerRadius`, `border`, `shadow`
 * - **Effects**: `alpha`, `scale`, `scaleX` / `scaleY`, `rotation`
 * - **Interaction**: `clickable`
 * - **Scroll**: `verticalScroll`, `horizontalScroll`
 *
 * Unknown keys are silently ignored, and any exception is caught so that
 * the modifier chain accumulated so far is still returned.
 *
 * @param props the component's top-level [JsonObject]; the function reads `props["modifier"]`.
 * @return a fully composed [Modifier] chain, or [Modifier] if the key is absent/empty.
 * @see parseShape
 * @see parseShapeWithRadius
 * @see parseColor
 * @see parseGradient
 */
@Composable
fun parseModifier(props: JsonObject): Modifier {
    // Always remember scroll states at stable composition positions to avoid
    // recreating ScrollState(0) on every recomposition (which resets scroll
    // position and causes lag).
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Pre-resolve default fling behavior outside try-catch (composable calls
    // are not allowed inside try-catch blocks).
    val defaultFlingBehavior = ScrollableDefaults.flingBehavior()

    var modifier: Modifier = Modifier

    val modifierProps = props["modifier"]?.jsonObject
    if (modifierProps == null || modifierProps.isEmpty()) return modifier

    // Pre-resolve @theme/ colours outside the try-catch (composable calls
    // are not allowed inside try-catch blocks).
    val bgColor: Color? = modifierProps["background"]?.jsonPrimitive?.content
        ?.let { resolveKetoyColor(it) }
    val borderColor: Color? = modifierProps["border"]?.jsonObject
        ?.get("color")?.jsonPrimitive?.content
        ?.let { resolveKetoyColor(it) }

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
                    if (value is JsonPrimitive && bgColor != null) {
                        if (componentShape != RectangleShape) modifier.background(bgColor, componentShape)
                        else modifier.background(bgColor)
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
                        val c = borderColor ?: Color.Black
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
                // Supports both boolean and object form:
                //   "verticalScroll": true
                //   "verticalScroll": { "enabled": true, "reverseScrolling": true }
                "verticalScroll" -> {
                    val config = parseScrollConfig(value)
                    if (config.enabled) modifier.verticalScroll(
                        state = verticalScrollState,
                        reverseScrolling = config.reverseScrolling,
                        flingBehavior = resolveFlingBehavior(config.flingBehavior, defaultFlingBehavior)
                    ) else modifier
                }
                "horizontalScroll" -> {
                    val config = parseScrollConfig(value)
                    if (config.enabled) modifier.horizontalScroll(
                        state = horizontalScrollState,
                        reverseScrolling = config.reverseScrolling,
                        flingBehavior = resolveFlingBehavior(config.flingBehavior, defaultFlingBehavior)
                    ) else modifier
                }
                // Unified scroll shorthand — applies both directions
                "scroll" -> {
                    val config = parseScrollConfig(value)
                    if (config.enabled) {
                        val direction = when (value) {
                            is JsonObject -> value["direction"]?.jsonPrimitive?.content ?: "vertical"
                            else -> "vertical"
                        }
                        val fling = resolveFlingBehavior(config.flingBehavior, defaultFlingBehavior)
                        when (direction) {
                            "horizontal" -> modifier.horizontalScroll(
                                state = horizontalScrollState,
                                reverseScrolling = config.reverseScrolling,
                                flingBehavior = fling
                            )
                            "both" -> modifier
                                .verticalScroll(
                                    state = verticalScrollState,
                                    reverseScrolling = config.reverseScrolling,
                                    flingBehavior = fling
                                )
                                .horizontalScroll(
                                    state = horizontalScrollState,
                                    reverseScrolling = config.reverseScrolling,
                                    flingBehavior = fling
                                )
                            else -> modifier.verticalScroll(
                                state = verticalScrollState,
                                reverseScrolling = config.reverseScrolling,
                                flingBehavior = fling
                            )
                        }
                    } else modifier
                }

                // ── UNKNOWN → skip ──────────────────────────────────────
                else -> modifier
            }
        }
    } catch (_: Exception) {
        // Return whatever was accumulated
    }

    return modifier
}

// ─── scroll config helpers ───────────────────────────────────────────

/**
 * Parses a scroll JSON value into a [KScrollConfig].
 *
 * Accepts either a boolean primitive (`true`/`false`) or an object with
 * `enabled`, `reverseScrolling`, and `flingBehavior` keys.
 */
private fun parseScrollConfig(value: JsonElement): KScrollConfig {
    return when (value) {
        is JsonPrimitive -> KScrollConfig(
            enabled = value.booleanOrNull == true,
            reverseScrolling = false,
            flingBehavior = null
        )
        is JsonObject -> KScrollConfig(
            enabled = value["enabled"]?.jsonPrimitive?.booleanOrNull != false,
            reverseScrolling = value["reverseScrolling"]?.jsonPrimitive?.booleanOrNull == true,
            flingBehavior = value["flingBehavior"]?.jsonPrimitive?.contentOrNull
        )
        else -> KScrollConfig(enabled = false, reverseScrolling = false, flingBehavior = null)
    }
}

/**
 * Resolves a fling behavior token to a [FlingBehavior] instance.
 *
 * Supported tokens:
 * - `null` or `"default"` → Standard Android fling deceleration
 * - `"none"` → No fling; scrolling stops immediately
 *
 * @param token the fling behavior preset name, or `null` for default.
 * @param defaultBehavior the pre-resolved default fling behavior (must be resolved outside try-catch).
 * @return the resolved [FlingBehavior].
 */
private fun resolveFlingBehavior(token: String?, defaultBehavior: FlingBehavior): FlingBehavior {
    return when (token) {
        null, KScrollConfig.FLING_DEFAULT -> defaultBehavior
        KScrollConfig.FLING_NONE -> NoFlingBehavior
        else -> defaultBehavior // Fallback to default for unknown tokens
    }
}

/**
 * A [FlingBehavior] that performs no fling animation — scrolling stops
 * immediately when the user lifts their finger.
 */
private object NoFlingBehavior : FlingBehavior {
    override suspend fun androidx.compose.foundation.gestures.ScrollScope.performFling(
        initialVelocity: Float
    ): Float = 0f // Consume all velocity, no fling
}

// ─── private helper ─────────────────────────────────────────────────

/**
 * Applies padding to [base] from a JSON value that may be either a single
 * integer (uniform padding) or an object with directional keys.
 *
 * Accepted object keys: `all`, `horizontal`, `vertical`, `top`, `bottom`,
 * `start`, `end`.
 *
 * @param base the [Modifier] to append padding to.
 * @param value the JSON element — a primitive int or a [JsonObject].
 * @return the [Modifier] with padding applied, or [base] if the value is invalid.
 */
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
