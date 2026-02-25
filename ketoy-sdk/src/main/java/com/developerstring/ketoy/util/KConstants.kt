/**
 * Ketoy SDUI constant definitions.
 *
 * This file centralises all string-constant objects used in the Ketoy
 * Kotlin DSL to declaratively describe arrangements, alignments, font
 * weights, text alignment, gradients, and scaffold-related properties.
 * Using these constants instead of raw strings provides compile-time
 * safety and auto-completion across any IDE.
 */
package com.developerstring.ketoy.util

import com.developerstring.ketoy.model.*

// ─────────────────────────────────────────────────────────────
//  Arrangement & Alignment string constants
// ─────────────────────────────────────────────────────────────

/**
 * String constants for Compose `Arrangement` values.
 *
 * Use these when specifying `verticalArrangement` or `horizontalArrangement`
 * in the Kotlin DSL or JSON payloads.
 *
 * ```kotlin
 * KColumn(verticalArrangement = KArrangements.SpaceBetween)
 * ```
 *
 * @see parseVerticalArrangement
 * @see parseHorizontalArrangement
 */
object KArrangements {
    const val Start = "start"
    const val Top = "top"
    const val Center = "center"
    const val End = "end"
    const val Bottom = "bottom"
    const val SpaceBetween = "spaceBetween"
    const val SpaceEvenly = "spaceEvenly"
    const val SpaceAround = "spaceAround"

    /** Returns the `"spacedBy_<dp>"` string recognised by the arrangement parser. */
    fun spacedBy(dp: Int): String = "spacedBy_$dp"
}

/**
 * String constants for Compose `Alignment` values.
 *
 * Covers horizontal, vertical, and two-dimensional alignments for
 * `Row`, `Column`, and `Box` components.
 *
 * ```kotlin
 * KBox(contentAlignment = KAlignments.Center)
 * ```
 *
 * @see parseHorizontalAlignment
 * @see parseVerticalAlignment
 * @see parseContentAlignment
 */
object KAlignments {
    const val Start = "start"
    const val Center = "center"
    const val End = "end"
    const val Top = "top"
    const val Bottom = "bottom"
    const val CenterHorizontally = "centerHorizontally"
    const val CenterVertically = "centerVertically"
    const val TopStart = "topStart"
    const val TopCenter = "topCenter"
    const val TopEnd = "topEnd"
    const val CenterStart = "centerStart"
    const val CenterEnd = "centerEnd"
    const val BottomStart = "bottomStart"
    const val BottomCenter = "bottomCenter"
    const val BottomEnd = "bottomEnd"
}

/**
 * String constants for Compose `FontWeight` values.
 *
 * ```kotlin
 * KText(fontWeight = KFontWeights.Bold)
 * ```
 */
object KFontWeights {
    const val Normal = "normal"
    const val Bold = "bold"
    const val Medium = "medium"
    const val Light = "light"
    const val SemiBold = "semiBold"
}

/**
 * String constants for Compose `TextAlign` values.
 *
 * ```kotlin
 * KText(textAlign = KTextAlign.Center)
 * ```
 */
object KTextAlign {
    const val Start = "start"
    const val Center = "center"
    const val End = "end"
    const val Justify = "justify"
}

/**
 * String constants for Compose `TextOverflow` values.
 *
 * ```kotlin
 * KText(overflow = KTextOverflow.Ellipsis)
 * ```
 */
object KTextOverflow {
    const val Clip = "Clip"
    const val Ellipsis = "Ellipsis"
    const val Visible = "Visible"
}

// ─────────────────────────────────────────────────────────────
//  Gradient builder
// ─────────────────────────────────────────────────────────────

/**
 * Convenience factory for creating [KGradient] model instances in the
 * Kotlin DSL.
 *
 * ```kotlin
 * KBox(
 *     modifier = kModifier(gradient = KGradients.linear(
 *         colors = listOf(KColors.Blue, KColors.Purple),
 *         direction = KGradients.Directions.Horizontal
 *     ))
 * )
 * ```
 *
 * @see parseGradient
 */
object KGradients {
    /**
     * Creates a linear gradient [KGradient].
     *
     * @param colors list of colour hex strings or [KColors] constants.
     * @param direction gradient direction (see [Directions]).
     * @return a [KGradient] model with `type = "linear"`.
     */
    fun linear(colors: List<String>, direction: String = "vertical") =
        KGradient(type = "linear", colors = colors, direction = direction)

    /**
     * Creates a linear gradient [KGradient] defined by an explicit angle.
     *
     * @param colors list of colour strings.
     * @param angleDegrees the angle in degrees (0° = right, 90° = down).
     * @return a [KGradient] model with `type = "linear"` and an angle.
     */
    fun linearAngle(colors: List<String>, angleDegrees: Float) =
        KGradient(type = "linear", colors = colors, angle = angleDegrees)

    /**
     * Creates a centred radial gradient [KGradient].
     *
     * @param colors list of colour strings.
     * @param radius optional explicit radius (fraction of size).
     * @return a [KGradient] model with `type = "radial"`.
     */
    fun radial(colors: List<String>, radius: Float? = null) =
        KGradient(type = "radial", colors = colors, radius = radius, centerX = 0.5f, centerY = 0.5f)

    /**
     * Creates a radial gradient [KGradient] with a custom centre.
     *
     * @param colors list of colour strings.
     * @param centerX horizontal centre as a fraction (0–1).
     * @param centerY vertical centre as a fraction (0–1).
     * @param radius optional explicit radius.
     * @return a [KGradient] model.
     */
    fun radialCenter(colors: List<String>, centerX: Float, centerY: Float, radius: Float? = null) =
        KGradient(type = "radial", colors = colors, centerX = centerX, centerY = centerY, radius = radius)

    /**
     * Creates a sweep (angular) gradient [KGradient].
     *
     * @param colors list of colour strings.
     * @param centerX horizontal centre (default 0.5).
     * @param centerY vertical centre (default 0.5).
     * @return a [KGradient] model with `type = "sweep"`.
     */
    fun sweep(colors: List<String>, centerX: Float = 0.5f, centerY: Float = 0.5f) =
        KGradient(type = "sweep", colors = colors, centerX = centerX, centerY = centerY)

    /**
     * Creates a sweep gradient [KGradient] constrained to explicit angles.
     *
     * @param colors list of colour strings.
     * @param startAngle starting angle in degrees.
     * @param endAngle ending angle in degrees.
     * @param centerX horizontal centre (default 0.5).
     * @param centerY vertical centre (default 0.5).
     * @return a [KGradient] model.
     */
    fun sweepAngles(colors: List<String>, startAngle: Float, endAngle: Float, centerX: Float = 0.5f, centerY: Float = 0.5f) =
        KGradient(type = "sweep", colors = colors, startAngle = startAngle, endAngle = endAngle, centerX = centerX, centerY = centerY)

    /** Predefined direction strings for linear gradients. */
    object Directions {
        const val Horizontal = "horizontal"
        const val Vertical = "vertical"
        const val Diagonal = "diagonal"
        const val DiagonalReverse = "diagonalReverse"
    }
}

// ─────────────────────────────────────────────────────────────
//  Scaffold constants
// ─────────────────────────────────────────────────────────────

/** String constants for FAB position inside a `Scaffold`. */
object KFabPosition {
    const val Start = "start"
    const val Center = "center"
    const val End = "end"
    const val EndOverlay = "endOverlay"
    const val CenterDocked = "centerDocked"
    const val EndDocked = "endDocked"
}

/** String constants for Material 3 TopAppBar variant types. */
object KTopAppBarType {
    const val Small = "small"
    const val CenterAligned = "centerAligned"
    const val Medium = "medium"
    const val Large = "large"
}

/** String constants for FAB size variants. */
object KFabType {
    const val Regular = "regular"
    const val Small = "small"
    const val Large = "large"
    const val Extended = "extended"
}

/** String constants for `SnackbarDuration`. */
object KSnackBarDuration {
    const val Short = "short"
    const val Long = "long"
    const val Indefinite = "indefinite"
}

/** String constants for TopAppBar scroll behaviour types. */
object KTopAppBarScrollBehaviorDefaults {
    const val PinnedScroll = "pinnedScroll"
    const val EnterAlwaysScroll = "enterAlwaysScroll"
    const val ExitUntilCollapsedScroll = "exitUntilCollapsedScroll"
}

/** String constants for system `WindowInsets` types. */
object KWindowInsetsDefaults {
    const val StatusBars = "statusBars"
    const val NavigationBars = "navigationBars"
    const val Ime = "ime"
    const val SystemBars = "systemBars"
    const val SystemGestures = "systemGestures"
    const val MandatorySystemGestures = "mandatorySystemGestures"
    const val TappableElement = "tappableElement"
    const val WaterFall = "waterFall"
    const val DisplayCutout = "displayCutout"
    const val CaptionBar = "captionBar"
}

/**
 * Default Material 3 hex-colour constants for common scaffold slots.
 *
 * These are provided as sensible defaults when the JSON payload does not
 * specify custom colours.
 */
object KScaffoldDefaults {
    const val ContainerColor = "#FFFBFE"
    const val ContentColor = "#1D1B20"
    const val TopAppBarContainerColor = "#FFFBFE"
    const val TopAppBarContentColor = "#1D1B20"
    const val BottomAppBarContainerColor = "#FFFBFE"
    const val BottomAppBarContentColor = "#1D1B20"
    const val NavigationBarContainerColor = "#FFFBFE"
    const val NavigationBarContentColor = "#1D1B20"
    const val FabContainerColor = "#6750A4"
    const val FabContentColor = "#FFFFFF"
    const val SnackBarContainerColor = "#313033"
    const val SnackBarContentColor = "#E6E1E5"
    const val SnackBarActionColor = "#D0BCFF"
}
