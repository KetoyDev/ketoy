/**
 * Shape descriptor constants and factory functions for the Ketoy SDUI
 * Kotlin DSL.
 *
 * Shape descriptors are plain strings (e.g. `"circle"`, `"rounded_12"`,
 * `"rounded_corners_4_8_4_8"`) that are resolved at render-time by
 * [parseShape]. This file provides both constant values for common shapes
 * and factory helpers for dynamic radius values.
 *
 * @see parseShape
 * @see parseShapeWithRadius
 */
package com.developerstring.ketoy.util

/**
 * Shape descriptor constants and factory functions.
 *
 * Use these in the Kotlin DSL when setting the `shape` property on
 * modifiers or components:
 *
 * ```kotlin
 * kModifier(shape = KShapes.Rounded12)
 * kModifier(shape = KShapes.rounded(16))
 * kModifier(shape = KShapes.rounded(topStart = 8, topEnd = 8))
 * ```
 */
object KShapes {
    /**
     * Creates a uniform rounded-corner shape descriptor.
     *
     * @param radius the corner radius in dp.
     * @return a descriptor string like `"rounded_12"`.
     */
    fun rounded(radius: Int) = "rounded_$radius"

    /**
     * Creates a per-corner rounded shape descriptor.
     *
     * @param topStart top-start corner radius in dp.
     * @param topEnd top-end corner radius in dp.
     * @param bottomEnd bottom-end corner radius in dp.
     * @param bottomStart bottom-start corner radius in dp.
     * @return a descriptor string like `"rounded_corners_8_8_0_0"`.
     */
    fun rounded(topStart: Int = 0, topEnd: Int = 0, bottomEnd: Int = 0, bottomStart: Int = 0) =
        "rounded_corners_${topStart}_${topEnd}_${bottomEnd}_${bottomStart}"

    /** Returns the circle shape descriptor (`"circle"`). */
    fun circle() = "circle"

    /** Returns the rectangle shape descriptor (`"rectangle"`). */
    fun rectangle() = "rectangle"

    /** Returns the clip shape descriptor (`"clip"`). */
    fun clip() = "clip"

    /** Rectangle (no rounding). */
    const val Rectangle = "rectangle"
    /** Perfect circle. */
    const val Circle = "circle"
    /** Clip shape (equivalent to rectangle for clipping). */
    const val Clip = "clip"
    /** Rounded corners — 4 dp. */
    const val Rounded4 = "rounded_4"
    /** Rounded corners — 8 dp. */
    const val Rounded8 = "rounded_8"
    /** Rounded corners — 12 dp. */
    const val Rounded12 = "rounded_12"
    /** Rounded corners — 16 dp. */
    const val Rounded16 = "rounded_16"
    /** Rounded corners — 20 dp. */
    const val Rounded20 = "rounded_20"
    /** Rounded corners — 24 dp. */
    const val Rounded24 = "rounded_24"
    /** Rounded corners — 28 dp. */
    const val Rounded28 = "rounded_28"
    /** Rounded corners — 32 dp. */
    const val Rounded32 = "rounded_32"
}

/**
 * Shorthand for [KShapes.rounded] with a uniform radius.
 *
 * @param radius the corner radius in dp.
 * @return a shape descriptor string.
 * @see KShapes.rounded
 */
fun kRounded(radius: Int) = KShapes.rounded(radius)

/**
 * Shorthand for [KShapes.rounded] with per-corner radii.
 *
 * @param topStart top-start corner radius in dp.
 * @param topEnd top-end corner radius in dp.
 * @param bottomEnd bottom-end corner radius in dp.
 * @param bottomStart bottom-start corner radius in dp.
 * @return a shape descriptor string.
 * @see KShapes.rounded
 */
fun kRounded(topStart: Int = 0, topEnd: Int = 0, bottomEnd: Int = 0, bottomStart: Int = 0) =
    KShapes.rounded(topStart, topEnd, bottomEnd, bottomStart)

/**
 * Shorthand for [KShapes.circle].
 *
 * @return `"circle"`.
 */
fun kCircle() = KShapes.circle()

/**
 * Shorthand for [KShapes.rectangle].
 *
 * @return `"rectangle"`.
 */
fun kRectangle() = KShapes.rectangle()

/**
 * Shorthand for [KShapes.clip].
 *
 * @return `"clip"`.
 */
fun kClip() = KShapes.clip()
