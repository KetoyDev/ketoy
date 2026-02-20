/**
 * Kotlin DSL builder & factory helpers for the Ketoy SDUI engine.
 *
 * Each top-level function creates a model instance (e.g. [KModifier],
 * [KPadding], [KBorder]) with named parameters, providing a concise,
 * IDE-friendly API for building component trees programmatically.
 *
 * ```kotlin
 * val card = KBox(
 *     modifier = kModifier(
 *         fillMaxWidth = 1f,
 *         padding = kPadding(all = 16),
 *         background = KColors.Surface,
 *         shape = KShapes.Rounded12,
 *         border = kBorder(width = 1, color = KColors.Outline)
 *     )
 * )
 * ```
 *
 * @see KModifier
 * @see KPadding
 * @see KBorder
 */
package com.developerstring.ketoy.util

import com.developerstring.ketoy.model.*

// ─────────────────────────────────────────────────────────────
//  Builder / factory helpers
// ─────────────────────────────────────────────────────────────

/**
 * Creates a [KModifier] with the specified layout, appearance,
 * and interaction properties.
 *
 * All parameters are optional and default to `null` (unset).
 *
 * @param fillMaxSize fraction (0–1) for `Modifier.fillMaxSize`.
 * @param fillMaxWidth fraction (0–1) for `Modifier.fillMaxWidth`.
 * @param fillMaxHeight fraction (0–1) for `Modifier.fillMaxHeight`.
 * @param weight flex weight for parent `Row` / `Column`.
 * @param size uniform size in dp.
 * @param width explicit width in dp.
 * @param height explicit height in dp.
 * @param padding inner padding model.
 * @param margin outer margin model (applied as outer padding).
 * @param background hex colour string for the background.
 * @param gradient gradient descriptor.
 * @param border border descriptor.
 * @param shape shape descriptor string (e.g. `KShapes.Rounded12`).
 * @param cornerRadius shorthand uniform corner radius in dp.
 * @param shadow shadow descriptor.
 * @param clickable whether the component is clickable.
 * @param scale uniform scale factor.
 * @param rotation rotation in degrees.
 * @param alpha opacity (0–1).
 * @param verticalScroll enable vertical scrolling.
 * @param horizontalScroll enable horizontal scrolling.
 * @return a [KModifier] instance.
 */

fun kModifier(
    fillMaxSize: Float? = null,
    fillMaxWidth: Float? = null,
    fillMaxHeight: Float? = null,
    weight: Float? = null,
    size: Int? = null,
    width: Int? = null,
    height: Int? = null,
    padding: KPadding? = null,
    margin: KMargin? = null,
    background: String? = null,
    gradient: KGradient? = null,
    border: KBorder? = null,
    shape: String? = null,
    cornerRadius: Int? = null,
    shadow: KShadow? = null,
    clickable: Boolean? = null,
    scale: Float? = null,
    rotation: Float? = null,
    alpha: Float? = null,
    verticalScroll: Boolean? = null,
    horizontalScroll: Boolean? = null
) = KModifier(
    fillMaxSize, fillMaxWidth, fillMaxHeight, weight, size, width, height,
    padding, margin, background, gradient, border, shape, cornerRadius,
    shadow, clickable, scale, rotation, alpha, verticalScroll, horizontalScroll
)
/**
 * Creates a [KPadding] model.
 *
 * @param all uniform padding on all sides (dp).
 * @param horizontal left + right padding (dp).
 * @param vertical top + bottom padding (dp).
 * @param top top padding (dp).
 * @param bottom bottom padding (dp).
 * @param start start padding (dp, LTR-aware).
 * @param end end padding (dp, LTR-aware).
 * @return a [KPadding] instance.
 */fun kPadding(
    all: Int? = null, horizontal: Int? = null, vertical: Int? = null,
    top: Int? = null, bottom: Int? = null, start: Int? = null, end: Int? = null
) = KPadding(all, horizontal, vertical, top, bottom, start, end)

/**
 * Creates a [KMargin] model (rendered as outer padding).
 *
 * @param all uniform margin on all sides (dp).
 * @param horizontal left + right margin (dp).
 * @param vertical top + bottom margin (dp).
 * @param top top margin (dp).
 * @param bottom bottom margin (dp).
 * @param start start margin (dp, LTR-aware).
 * @param end end margin (dp, LTR-aware).
 * @return a [KMargin] instance.
 */
fun kMargin(
    all: Int? = null, horizontal: Int? = null, vertical: Int? = null,
    top: Int? = null, bottom: Int? = null, start: Int? = null, end: Int? = null
) = KMargin(all, horizontal, vertical, top, bottom, start, end)

/**
 * Creates a [KBorder] model.
 *
 * @param width border width in dp.
 * @param color border colour as a hex string or [KColors] constant.
 * @param shape optional shape descriptor (defaults to the component’s shape).
 * @return a [KBorder] instance.
 */
fun kBorder(width: Int, color: String, shape: String? = null) =
    KBorder(width, color, shape)

/**
 * Creates a [KShadow] model.
 *
 * @param elevation shadow elevation in dp.
 * @param color optional shadow colour hex string.
 * @param offsetX horizontal shadow offset.
 * @param offsetY vertical shadow offset.
 * @param blurRadius shadow blur radius.
 * @return a [KShadow] instance.
 */
fun kShadow(
    elevation: Int, color: String? = null,
    offsetX: Float? = null, offsetY: Float? = null, blurRadius: Float? = null
) = KShadow(elevation, color, offsetX, offsetY, blurRadius)

/**
 * Creates a [KWindowInsets] model for scaffold insets configuration.
 *
 * @param left left inset in dp.
 * @param top top inset in dp.
 * @param right right inset in dp.
 * @param bottom bottom inset in dp.
 * @param type system-defined inset type (e.g. `"statusBars"`).
 * @return a [KWindowInsets] instance.
 * @see parseWindowInsets
 */
fun kWindowInsets(
    left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null,
    type: String? = null
) = KWindowInsets(left, top, right, bottom, type)

/**
 * Creates a [KTopAppBarColors] model for top app bar colour customisation.
 *
 * @param containerColor background colour.
 * @param scrolledContainerColor background colour when scrolled.
 * @param navigationIconContentColor tint for the navigation icon.
 * @param titleContentColor tint for the title text.
 * @param actionIconContentColor tint for action icons.
 * @return a [KTopAppBarColors] instance.
 */
fun kTopAppBarColors(
    containerColor: String? = null, scrolledContainerColor: String? = null,
    navigationIconContentColor: String? = null, titleContentColor: String? = null,
    actionIconContentColor: String? = null
) = KTopAppBarColors(containerColor, scrolledContainerColor, navigationIconContentColor, titleContentColor, actionIconContentColor)

/**
 * Creates a [KFloatingActionButtonElevation] model.
 *
 * @param defaultElevation elevation in dp at rest.
 * @param pressedElevation elevation in dp when pressed.
 * @param focusedElevation elevation in dp when focused.
 * @param hoveredElevation elevation in dp when hovered.
 * @param disabledElevation elevation in dp when disabled.
 * @return a [KFloatingActionButtonElevation] instance.
 */
fun kFabElevation(
    defaultElevation: Int? = null, pressedElevation: Int? = null,
    focusedElevation: Int? = null, hoveredElevation: Int? = null,
    disabledElevation: Int? = null
) = KFloatingActionButtonElevation(defaultElevation, pressedElevation, focusedElevation, hoveredElevation, disabledElevation)

/**
 * Creates a [KNavigationDrawerItemColors] model.
 *
 * @param selectedContainerColor container colour when selected.
 * @param unselectedContainerColor container colour when not selected.
 * @param selectedIconColor icon tint when selected.
 * @param unselectedIconColor icon tint when not selected.
 * @param selectedTextColor text colour when selected.
 * @param unselectedTextColor text colour when not selected.
 * @param selectedBadgeColor badge colour when selected.
 * @param unselectedBadgeColor badge colour when not selected.
 * @return a [KNavigationDrawerItemColors] instance.
 */
fun kNavigationDrawerItemColors(
    selectedContainerColor: String? = null, unselectedContainerColor: String? = null,
    selectedIconColor: String? = null, unselectedIconColor: String? = null,
    selectedTextColor: String? = null, unselectedTextColor: String? = null,
    selectedBadgeColor: String? = null, unselectedBadgeColor: String? = null
) = KNavigationDrawerItemColors(
    selectedContainerColor, unselectedContainerColor,
    selectedIconColor, unselectedIconColor,
    selectedTextColor, unselectedTextColor,
    selectedBadgeColor, unselectedBadgeColor
)

/**
 * Creates a [KIconButtonColors] model.
 *
 * @param containerColor button background colour.
 * @param contentColor icon/content tint colour.
 * @param disabledContainerColor background when disabled.
 * @param disabledContentColor content tint when disabled.
 * @return a [KIconButtonColors] instance.
 */
fun kIconButtonColors(
    containerColor: String? = null, contentColor: String? = null,
    disabledContainerColor: String? = null, disabledContentColor: String? = null
) = KIconButtonColors(containerColor, contentColor, disabledContainerColor, disabledContentColor)

// Image source helpers

/**
 * Creates a resource-based image source.
 *
 * @param resourceName the Android drawable resource name.
 * @return a [KResImageSource] instance.
 */
fun kImageRes(resourceName: String) = KResImageSource(resourceName)

/**
 * Creates a URL-based image source.
 *
 * @param url the remote image URL.
 * @return a [KUrlImageSource] instance.
 */
fun kImageUrl(url: String) = KUrlImageSource(url)

/**
 * Creates a Base64-encoded image source.
 *
 * @param base64String the Base64-encoded image data.
 * @return a [KBase64ImageSource] instance.
 */
fun kImageBase64(base64String: String) = KBase64ImageSource(base64String)

/**
 * Creates an icon-based image source using a Material3 icon name.
 *
 * ```kotlin
 * KImage(source = kImageIcon(KIcons.Home))
 * KImage(source = kImageIcon(KIcons.Settings, style = KIcons.STYLE_OUTLINED))
 * ```
 */
fun kImageIcon(iconName: String, style: String? = null) = KIconImageSource(iconName, style)

// ─────────────────────────────────────────────────────────────
//  Icon helpers
// ─────────────────────────────────────────────────────────────

/**
 * Creates a [KIconProps] model for use as a standalone icon reference.
 *
 * ```kotlin
 * val myIcon = kIcon(icon = KIcons.Home, size = 24, color = KColors.Black)
 * ```
 */
fun kIcon(
    icon: String, size: Int? = null, color: String? = null,
    style: String? = null, contentDescription: String? = null
) = KIconProps(icon = icon, size = size, color = color, style = style, contentDescription = contentDescription)

/**
 * Creates a [KIconButtonProps] model for an icon-button reference.
 *
 * ```kotlin
 * val myBtn = kIconButton(icon = KIcons.Favorite, iconColor = KColors.Red)
 * ```
 */
fun kIconButton(
    icon: String, iconSize: Int? = null, iconColor: String? = null,
    iconStyle: String? = null, containerColor: String? = null,
    contentColor: String? = null, contentDescription: String? = null
) = KIconButtonProps(
    icon = icon, iconSize = iconSize, iconColor = iconColor, iconStyle = iconStyle,
    containerColor = containerColor, contentColor = contentColor, contentDescription = contentDescription
)
