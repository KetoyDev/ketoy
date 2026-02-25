package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Gradient
// ─────────────────────────────────────────────────────────────

/**
 * Describes a gradient background that can be applied to any Ketoy component.
 *
 * Ketoy supports three gradient types:
 * - **"linear"** – a straight-line gradient controlled by [direction] or [angle].
 * - **"radial"** – a circular gradient defined by [centerX], [centerY], and [radius].
 * - **"sweep"** – a conical gradient sweeping between [startAngle] and [endAngle].
 *
 * Colors are expressed as hex strings (e.g. `"#FF5733"`) to stay platform-agnostic
 * in the JSON wire format.
 *
 * ### JSON wire-format example
 * ```json
 * {
 *   "type": "linear",
 *   "colors": ["#FF5733", "#33FF57", "#3357FF"],
 *   "direction": "topToBottom"
 * }
 * ```
 *
 * ### Kotlin usage
 * ```kotlin
 * val gradient = KGradient(
 *     type = "radial",
 *     colors = listOf("#FF5733", "#FFFFFF"),
 *     centerX = 0.5f,
 *     centerY = 0.5f,
 *     radius = 200f
 * )
 * ```
 *
 * @property type The gradient type. One of `"linear"`, `"radial"`, or `"sweep"`.
 * @property colors Ordered list of hex color strings that form the gradient stops.
 * @property direction Optional direction keyword for linear gradients
 *   (e.g. `"topToBottom"`, `"leftToRight"`, `"topLeftToBottomRight"`).
 *   Ignored when [angle] is specified.
 * @property angle Optional explicit angle in degrees for linear gradients (0–360).
 *   Takes precedence over [direction] when both are present.
 * @property centerX Horizontal center of a radial or sweep gradient, expressed as a
 *   fraction of the component width (0.0–1.0). Defaults to `0.5` on the renderer side.
 * @property centerY Vertical center of a radial or sweep gradient, expressed as a
 *   fraction of the component height (0.0–1.0). Defaults to `0.5` on the renderer side.
 * @property radius Radius in density-independent pixels for radial gradients.
 * @property startAngle Start angle in degrees for sweep gradients. Defaults to `0`.
 * @property endAngle End angle in degrees for sweep gradients. Defaults to `360`.
 * @see KNode
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
@Serializable
data class KGradient(
    val type: String,
    val colors: List<String>,
    val direction: String? = null,
    val angle: Float? = null,
    val centerX: Float? = null,
    val centerY: Float? = null,
    val radius: Float? = null,
    val startAngle: Float? = null,
    val endAngle: Float? = null
)
