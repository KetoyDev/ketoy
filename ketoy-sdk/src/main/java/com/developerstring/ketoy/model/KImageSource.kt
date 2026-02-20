package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Image source types
// ─────────────────────────────────────────────────────────────

/**
 * Sealed base class for every image source that Ketoy components can display.
 *
 * The JSON discriminator field is the `type` key, whose value selects the
 * concrete subclass:
 *
 * | `type` value | Subclass               | Description                        |
 * |:-------------|:-----------------------|:-----------------------------------|
 * | `"res"`      | [KResImageSource]      | Android drawable resource name     |
 * | `"url"`      | [KUrlImageSource]      | Remote HTTP/HTTPS URL              |
 * | `"base64"`   | [KBase64ImageSource]   | Base-64-encoded image data         |
 * | `"icon"`     | [KIconImageSource]     | Material icon name + optional style|
 *
 * ### JSON wire-format example
 * ```json
 * { "type": "url", "value": "https://example.com/hero.webp" }
 * ```
 *
 * @see KScaleType
 * @see KNode
 */
@Serializable
sealed class KImageSource

/**
 * Image loaded from a local Android drawable resource by name.
 *
 * ```json
 * { "type": "res", "value": "ic_launcher_foreground" }
 * ```
 *
 * @property value The drawable resource name (without the `R.drawable.` prefix).
 * @see KImageSource
 */
@Serializable
@SerialName("res")
data class KResImageSource(val value: String) : KImageSource()

/**
 * Image loaded from a remote URL (HTTP or HTTPS).
 *
 * The Ketoy renderer uses Coil/Glide (depending on project setup) to fetch and
 * cache the image.
 *
 * ```json
 * { "type": "url", "value": "https://cdn.example.com/photo.webp" }
 * ```
 *
 * @property value The fully-qualified image URL.
 * @see KImageSource
 */
@Serializable
@SerialName("url")
data class KUrlImageSource(val value: String) : KImageSource()

/**
 * Image provided as an inline Base-64-encoded string.
 *
 * Useful for small icons or thumbnails that should travel with the JSON payload
 * without an additional network request.
 *
 * ```json
 * { "type": "base64", "value": "iVBORw0KGgoAAAANSUhEUgAA..." }
 * ```
 *
 * @property value The raw Base-64 data (no `data:image/…;base64,` prefix).
 * @see KImageSource
 */
@Serializable
@SerialName("base64")
data class KBase64ImageSource(val value: String) : KImageSource()

/**
 * Image rendered from a Material icon font glyph.
 *
 * ```json
 * { "type": "icon", "value": "home", "style": "outlined" }
 * ```
 *
 * @property value The Material icon name (e.g. `"home"`, `"search"`, `"favorite"`).
 * @property style Optional icon style variant. Common values:
 *   `"filled"`, `"outlined"`, `"rounded"`, `"sharp"`, `"twoTone"`.
 *   When `null`, the renderer's default style is used.
 * @see KImageSource
 */
@Serializable
@SerialName("icon")
data class KIconImageSource(
    val value: String,
    val style: String? = null
) : KImageSource()

/**
 * String constants that map to Jetpack Compose `ContentScale` values.
 *
 * Pass one of these constants as the `scaleType` property of an image [KNode]
 * to control how the image is fitted within its bounds.
 *
 * ### Kotlin usage
 * ```kotlin
 * KNode.Widget(
 *     type = "Image",
 *     props = mapOf(
 *         "src" to KUrlImageSource("https://example.com/img.webp"),
 *         "scaleType" to KScaleType.CenterCrop
 *     )
 * )
 * ```
 *
 * @see KImageSource
 * @see KNode
 */
object KScaleType {
    /** Scales the image uniformly to fit inside the bounds (letterboxing). */
    const val FitCenter = "fitCenter"
    /** Scales the image uniformly to fill the bounds, cropping excess. */
    const val CenterCrop = "centerCrop"
    /** Stretches the image non-uniformly to fill the entire bounds. */
    const val FillBounds = "fillBounds"
    /** Scales down only; images smaller than bounds are not scaled up. */
    const val Inside = "inside"
    /** Scales to match the width exactly; height adjusts proportionally. */
    const val FillWidth = "fillWidth"
    /** Scales to match the height exactly; width adjusts proportionally. */
    const val FillHeight = "fillHeight"
}
