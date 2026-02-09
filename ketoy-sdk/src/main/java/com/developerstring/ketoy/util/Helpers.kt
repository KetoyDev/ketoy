package com.developerstring.ketoy.util

import com.developerstring.ketoy.model.*

// ─────────────────────────────────────────────────────────────
//  Builder / factory helpers
// ─────────────────────────────────────────────────────────────

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

fun kPadding(
    all: Int? = null, horizontal: Int? = null, vertical: Int? = null,
    top: Int? = null, bottom: Int? = null, start: Int? = null, end: Int? = null
) = KPadding(all, horizontal, vertical, top, bottom, start, end)

fun kMargin(
    all: Int? = null, horizontal: Int? = null, vertical: Int? = null,
    top: Int? = null, bottom: Int? = null, start: Int? = null, end: Int? = null
) = KMargin(all, horizontal, vertical, top, bottom, start, end)

fun kBorder(width: Int, color: String, shape: String? = null) =
    KBorder(width, color, shape)

fun kShadow(
    elevation: Int, color: String? = null,
    offsetX: Float? = null, offsetY: Float? = null, blurRadius: Float? = null
) = KShadow(elevation, color, offsetX, offsetY, blurRadius)

fun kWindowInsets(
    left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null,
    type: String? = null
) = KWindowInsets(left, top, right, bottom, type)

fun kTopAppBarColors(
    containerColor: String? = null, scrolledContainerColor: String? = null,
    navigationIconContentColor: String? = null, titleContentColor: String? = null,
    actionIconContentColor: String? = null
) = KTopAppBarColors(containerColor, scrolledContainerColor, navigationIconContentColor, titleContentColor, actionIconContentColor)

fun kFabElevation(
    defaultElevation: Int? = null, pressedElevation: Int? = null,
    focusedElevation: Int? = null, hoveredElevation: Int? = null,
    disabledElevation: Int? = null
) = KFloatingActionButtonElevation(defaultElevation, pressedElevation, focusedElevation, hoveredElevation, disabledElevation)

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

fun kIconButtonColors(
    containerColor: String? = null, contentColor: String? = null,
    disabledContainerColor: String? = null, disabledContentColor: String? = null
) = KIconButtonColors(containerColor, contentColor, disabledContainerColor, disabledContentColor)

// Image source helpers
fun kImageRes(resourceName: String) = KResImageSource(resourceName)
fun kImageUrl(url: String) = KUrlImageSource(url)
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
