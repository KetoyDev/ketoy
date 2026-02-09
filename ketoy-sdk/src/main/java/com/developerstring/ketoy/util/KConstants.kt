package com.developerstring.ketoy.util

import com.developerstring.ketoy.model.*

// ─────────────────────────────────────────────────────────────
//  Arrangement & Alignment string constants
// ─────────────────────────────────────────────────────────────

object KArrangements {
    const val Start = "start"
    const val Top = "top"
    const val Center = "center"
    const val End = "end"
    const val Bottom = "bottom"
    const val SpaceBetween = "spaceBetween"
    const val SpaceEvenly = "spaceEvenly"
    const val SpaceAround = "spaceAround"
}

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

object KFontWeights {
    const val Normal = "normal"
    const val Bold = "bold"
    const val Medium = "medium"
    const val Light = "light"
    const val SemiBold = "semiBold"
}

object KTextAlign {
    const val Start = "start"
    const val Center = "center"
    const val End = "end"
    const val Justify = "justify"
}

// ─────────────────────────────────────────────────────────────
//  Gradient builder
// ─────────────────────────────────────────────────────────────

object KGradients {
    fun linear(colors: List<String>, direction: String = "vertical") =
        KGradient(type = "linear", colors = colors, direction = direction)

    fun linearAngle(colors: List<String>, angleDegrees: Float) =
        KGradient(type = "linear", colors = colors, angle = angleDegrees)

    fun radial(colors: List<String>, radius: Float? = null) =
        KGradient(type = "radial", colors = colors, radius = radius, centerX = 0.5f, centerY = 0.5f)

    fun radialCenter(colors: List<String>, centerX: Float, centerY: Float, radius: Float? = null) =
        KGradient(type = "radial", colors = colors, centerX = centerX, centerY = centerY, radius = radius)

    fun sweep(colors: List<String>, centerX: Float = 0.5f, centerY: Float = 0.5f) =
        KGradient(type = "sweep", colors = colors, centerX = centerX, centerY = centerY)

    fun sweepAngles(colors: List<String>, startAngle: Float, endAngle: Float, centerX: Float = 0.5f, centerY: Float = 0.5f) =
        KGradient(type = "sweep", colors = colors, startAngle = startAngle, endAngle = endAngle, centerX = centerX, centerY = centerY)

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

object KFabPosition {
    const val Start = "start"
    const val Center = "center"
    const val End = "end"
    const val EndOverlay = "endOverlay"
    const val CenterDocked = "centerDocked"
    const val EndDocked = "endDocked"
}

object KTopAppBarType {
    const val Small = "small"
    const val CenterAligned = "centerAligned"
    const val Medium = "medium"
    const val Large = "large"
}

object KFabType {
    const val Regular = "regular"
    const val Small = "small"
    const val Large = "large"
    const val Extended = "extended"
}

object KSnackBarDuration {
    const val Short = "short"
    const val Long = "long"
    const val Indefinite = "indefinite"
}

object KTopAppBarScrollBehaviorDefaults {
    const val PinnedScroll = "pinnedScroll"
    const val EnterAlwaysScroll = "enterAlwaysScroll"
    const val ExitUntilCollapsedScroll = "exitUntilCollapsedScroll"
}

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
