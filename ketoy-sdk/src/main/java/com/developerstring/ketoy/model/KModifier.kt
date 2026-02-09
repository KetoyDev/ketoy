package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Modifier & layout primitives
// ─────────────────────────────────────────────────────────────

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
    val verticalScroll: Boolean? = null,
    val horizontalScroll: Boolean? = null
)

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

@Serializable
data class KBorder(
    val width: Int,
    val color: String,
    val shape: String? = null
)

@Serializable
data class KShadow(
    val elevation: Int,
    val color: String? = null,
    val offsetX: Float? = null,
    val offsetY: Float? = null,
    val blurRadius: Float? = null
)
