package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Image source types
// ─────────────────────────────────────────────────────────────

@Serializable
sealed class KImageSource

@Serializable
@SerialName("res")
data class KResImageSource(val value: String) : KImageSource()

@Serializable
@SerialName("url")
data class KUrlImageSource(val value: String) : KImageSource()

@Serializable
@SerialName("base64")
data class KBase64ImageSource(val value: String) : KImageSource()

@Serializable
@SerialName("icon")
data class KIconImageSource(
    val value: String,
    val style: String? = null
) : KImageSource()

/** ContentScale constants for images. */
object KScaleType {
    const val FitCenter = "fitCenter"
    const val CenterCrop = "centerCrop"
    const val FillBounds = "fillBounds"
    const val Inside = "inside"
    const val FillWidth = "fillWidth"
    const val FillHeight = "fillHeight"
}
