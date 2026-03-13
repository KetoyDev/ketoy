package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Modifier & layout primitives
// ─────────────────────────────────────────────────────────────

/**
 * Describes the visual and layout modifiers for a UI component in Ketoy's
 * Server-Driven UI framework.
 *
 * [KModifier] is the JSON-serializable counterpart of Jetpack Compose's
 * `Modifier` chain. The Ketoy renderer converts each non-null property into
 * the corresponding Compose `Modifier` call (e.g. `Modifier.fillMaxWidth()`,
 * `Modifier.padding()`, `Modifier.background()`), so your backend can fully
 * control sizing, spacing, decoration, and interaction behavior without
 * shipping a new app release.
 *
 * ### JSON wire format example
 * ```json
 * {
 *   "modifier": {
 *     "fillMaxWidth": 1.0,
 *     "padding": { "horizontal": 16, "vertical": 8 },
 *     "background": "#FFFFFF",
 *     "cornerRadius": 12,
 *     "border": { "width": 1, "color": "#E0E0E0" },
 *     "shadow": { "elevation": 4 },
 *     "clickable": true,
 *     "alpha": 0.95
 *   }
 * }
 * ```
 *
 * @property fillMaxSize Fraction (0.0–1.0) passed to `Modifier.fillMaxSize()`. When `null`, the
 *   modifier is omitted and the component uses its intrinsic size.
 * @property fillMaxWidth Fraction (0.0–1.0) passed to `Modifier.fillMaxWidth()`.
 * @property fillMaxHeight Fraction (0.0–1.0) passed to `Modifier.fillMaxHeight()`.
 * @property weight Flex weight used inside a `Row` or `Column` layout, mapped to
 *   `Modifier.weight()`.
 * @property size Fixed size in density-independent pixels (dp) applied to both width and height
 *   via `Modifier.size()`.
 * @property width Fixed width in dp applied via `Modifier.width()`.
 * @property height Fixed height in dp applied via `Modifier.height()`.
 * @property padding Inner spacing applied via `Modifier.padding()`.
 * @property margin Outer spacing emulated by wrapping the component in an outer `Modifier.padding()`
 *   (Compose has no first-class margin concept).
 * @property background Solid background color as a CSS-style hex string (e.g. `"#FF5722"` or
 *   `"#80FF5722"` for alpha), mapped to `Modifier.background()`.
 * @property gradient Gradient background that takes precedence over [background] when both are set.
 * @property border Stroke drawn around the component via `Modifier.border()`.
 * @property shape Shape token (e.g. `"circle"`, `"rectangle"`, `"roundedCorner"`) used for
 *   clipping and as the shape parameter of [border] and [background].
 * @property cornerRadius Corner radius in dp used when [shape] is `"roundedCorner"` or when no
 *   explicit [shape] is provided, mapped to `RoundedCornerShape()`.
 * @property shadow Elevation shadow rendered via `Modifier.shadow()`.
 * @property clickable When `true`, the component responds to tap events and can trigger navigation
 *   or action handlers registered in the Ketoy runtime.
 * @property scale Uniform scale factor applied via `Modifier.graphicsLayer { scaleX; scaleY }`.
 * @property rotation Rotation angle in degrees applied via `Modifier.graphicsLayer { rotationZ }`.
 * @property alpha Opacity value (0.0–1.0) applied via `Modifier.alpha()`.
 * @property verticalScroll Vertical scroll configuration. Accepts either a boolean shorthand
 *   (`true`/`false`) or a full [KScrollConfig] object with `enabled`, `reverseScrolling`,
 *   and `flingBehavior` options.
 * @property horizontalScroll Horizontal scroll configuration. Accepts either a boolean shorthand
 *   or a full [KScrollConfig] object.
 * @see KPadding
 * @see KMargin
 * @see KBorder
 * @see KShadow
 * @see KGradient
 * @see KScrollConfig
 */
@Serializable
data class KModifier(
    val fillMaxSize: Float? = null,
    val fillMaxWidth: Float? = null,
    val fillMaxHeight: Float? = null,
    val weight: Float? = null,
    val size: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val padding: KPadding? = null,
    val margin: KMargin? = null,
    val background: String? = null,
    val gradient: KGradient? = null,
    val border: KBorder? = null,
    val shape: String? = null,
    val cornerRadius: Int? = null,
    val shadow: KShadow? = null,
    val clickable: Boolean? = null,
    val scale: Float? = null,
    val rotation: Float? = null,
    val alpha: Float? = null,
    val verticalScroll: KScrollConfig? = null,
    val horizontalScroll: KScrollConfig? = null
)

/**
 * Defines inner spacing (padding) for a Ketoy UI component.
 *
 * [KPadding] maps directly to Jetpack Compose's `PaddingValues` and is applied
 * via `Modifier.padding()`. Properties are evaluated with the following
 * precedence (highest first):
 *
 * 1. **Edge-specific** — [top], [bottom], [start], [end]
 * 2. **Axis-specific** — [horizontal], [vertical]
 * 3. **Uniform** — [all]
 *
 * All values are expressed in density-independent pixels (dp). The [start] and
 * [end] properties use layout-direction-aware semantics so that RTL (right-to-left)
 * locales are supported automatically.
 *
 * ### JSON wire format example
 * ```json
 * { "padding": { "horizontal": 16, "top": 24, "bottom": 8 } }
 * ```
 *
 * @property all Uniform padding applied equally on all four edges.
 * @property horizontal Padding applied to both the start and end edges.
 * @property vertical Padding applied to both the top and bottom edges.
 * @property top Padding applied to the top edge only.
 * @property bottom Padding applied to the bottom edge only.
 * @property start Padding applied to the layout-direction-aware start edge (left in LTR,
 *   right in RTL).
 * @property end Padding applied to the layout-direction-aware end edge (right in LTR,
 *   left in RTL).
 * @see KModifier
 * @see KMargin
 */
@Serializable
data class KPadding(
    val all: Int? = null,
    val horizontal: Int? = null,
    val vertical: Int? = null,
    val top: Int? = null,
    val bottom: Int? = null,
    val start: Int? = null,
    val end: Int? = null
)

/**
 * Defines outer spacing (margin) for a Ketoy UI component.
 *
 * Jetpack Compose does not have a native margin modifier. The Ketoy renderer
 * emulates margin by applying an additional `Modifier.padding()` on an outer
 * wrapper, producing the same visual effect as CSS `margin`. Properties follow
 * the same precedence rules and RTL-aware semantics as [KPadding].
 *
 * ### JSON wire format example
 * ```json
 * { "margin": { "all": 8 } }
 * ```
 *
 * @property all Uniform margin applied equally on all four edges.
 * @property horizontal Margin applied to both the start and end edges.
 * @property vertical Margin applied to both the top and bottom edges.
 * @property top Margin applied to the top edge only.
 * @property bottom Margin applied to the bottom edge only.
 * @property start Margin applied to the layout-direction-aware start edge (left in LTR,
 *   right in RTL).
 * @property end Margin applied to the layout-direction-aware end edge (right in LTR,
 *   left in RTL).
 * @see KModifier
 * @see KPadding
 */
@Serializable
data class KMargin(
    val all: Int? = null,
    val horizontal: Int? = null,
    val vertical: Int? = null,
    val top: Int? = null,
    val bottom: Int? = null,
    val start: Int? = null,
    val end: Int? = null
)

/**
 * Describes a border stroke drawn around a Ketoy UI component.
 *
 * [KBorder] maps to Jetpack Compose's `Modifier.border(BorderStroke, Shape)`.
 * The [shape] parameter, when provided, overrides the parent [KModifier.shape]
 * and [KModifier.cornerRadius] for the border outline only.
 *
 * ### JSON wire format example
 * ```json
 * { "border": { "width": 2, "color": "#3F51B5", "shape": "roundedCorner" } }
 * ```
 *
 * @property width Border stroke width in density-independent pixels (dp).
 * @property color Border color as a CSS-style hex string (e.g. `"#3F51B5"`).
 * @property shape Optional shape token for the border outline (e.g. `"circle"`,
 *   `"roundedCorner"`, `"rectangle"`). When `null`, the shape is inherited from
 *   [KModifier.shape] or [KModifier.cornerRadius].
 * @see KModifier
 * @see KShadow
 */
@Serializable
data class KBorder(
    val width: Int,
    val color: String,
    val shape: String? = null
)

/**
 * Describes an elevation shadow rendered beneath a Ketoy UI component.
 *
 * [KShadow] maps to Jetpack Compose's `Modifier.shadow()`. On Android,
 * the [elevation] value drives the platform shadow; [color], [offsetX],
 * [offsetY], and [blurRadius] provide additional fine-tuning that the
 * renderer applies via a `graphicsLayer` when the platform shadow API is
 * insufficient.
 *
 * ### JSON wire format example
 * ```json
 * { "shadow": { "elevation": 8, "color": "#40000000", "offsetY": 2.0, "blurRadius": 6.0 } }
 * ```
 *
 * @property elevation Shadow elevation in density-independent pixels (dp), passed to
 *   `Modifier.shadow(elevation)`.
 * @property color Shadow color as a CSS-style hex string. Defaults to the platform's
 *   default shadow color when `null`.
 * @property offsetX Horizontal offset of the shadow in dp. Defaults to `0` when `null`.
 * @property offsetY Vertical offset of the shadow in dp. Defaults to `0` when `null`.
 * @property blurRadius Blur radius of the shadow in dp. Larger values produce a softer,
 *   more diffused shadow. Defaults to `0` when `null`.
 * @see KModifier
 * @see KBorder
 */
@Serializable
data class KShadow(
    val elevation: Int,
    val color: String? = null,
    val offsetX: Float? = null,
    val offsetY: Float? = null,
    val blurRadius: Float? = null
)
