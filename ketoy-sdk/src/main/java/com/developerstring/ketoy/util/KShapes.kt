package com.developerstring.ketoy.util

/** Shape descriptor constants & factory functions. */
object KShapes {
    fun rounded(radius: Int) = "rounded_$radius"
    fun rounded(topStart: Int = 0, topEnd: Int = 0, bottomEnd: Int = 0, bottomStart: Int = 0) =
        "rounded_corners_${topStart}_${topEnd}_${bottomEnd}_${bottomStart}"
    fun circle() = "circle"
    fun rectangle() = "rectangle"
    fun clip() = "clip"

    const val Rectangle = "rectangle"
    const val Circle = "circle"
    const val Clip = "clip"
    const val Rounded4 = "rounded_4"
    const val Rounded8 = "rounded_8"
    const val Rounded12 = "rounded_12"
    const val Rounded16 = "rounded_16"
    const val Rounded20 = "rounded_20"
    const val Rounded24 = "rounded_24"
    const val Rounded28 = "rounded_28"
    const val Rounded32 = "rounded_32"
}

fun kRounded(radius: Int) = KShapes.rounded(radius)
fun kRounded(topStart: Int = 0, topEnd: Int = 0, bottomEnd: Int = 0, bottomStart: Int = 0) =
    KShapes.rounded(topStart, topEnd, bottomEnd, bottomStart)
fun kCircle() = KShapes.circle()
fun kRectangle() = KShapes.rectangle()
fun kClip() = KShapes.clip()
