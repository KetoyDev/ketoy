package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Gradient
// ─────────────────────────────────────────────────────────────

@Serializable
data class KGradient(
    val type: String,
    val colors: List<String>,
    val direction: String? = null,
    val angle: Float? = null,
    val centerX: Float? = null,
    val centerY: Float? = null,
    val radius: Float? = null,
    val startAngle: Float? = null,
    val endAngle: Float? = null
)
