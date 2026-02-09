package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Layout component properties
// ─────────────────────────────────────────────────────────────

@Serializable
data class KColumnProps(
    val modifier: KModifier? = null,
    val verticalArrangement: String? = null,
    val horizontalAlignment: String? = null
)

@Serializable
data class KRowProps(
    val modifier: KModifier? = null,
    val horizontalArrangement: String? = null,
    val verticalAlignment: String? = null
)

@Serializable
data class KBoxProps(
    val modifier: KModifier? = null,
    val contentAlignment: String? = null
)

@Serializable
data class KLazyColumnProps(
    val modifier: KModifier? = null,
    val verticalArrangement: String? = null,
    val horizontalAlignment: String? = null,
    val userScrollEnabled: Boolean? = null,
    val reverseLayout: Boolean? = null,
    val contentPadding: KPadding? = null,
    val beyondBoundsItemCount: Int? = null
)

@Serializable
data class KLazyRowProps(
    val modifier: KModifier? = null,
    val horizontalArrangement: String? = null,
    val verticalAlignment: String? = null,
    val userScrollEnabled: Boolean? = null,
    val reverseLayout: Boolean? = null,
    val contentPadding: KPadding? = null,
    val beyondBoundsItemCount: Int? = null
)

// ─────────────────────────────────────────────────────────────
//  Leaf-widget properties
// ─────────────────────────────────────────────────────────────

@Serializable
data class KTextProps(
    val text: String = "",
    val modifier: KModifier? = null,
    val fontSize: Int? = null,
    val fontWeight: String? = null,
    val color: String? = null,
    val textAlign: String? = null,
    val maxLines: Int? = null,
    val overflow: String? = null,
    val letterSpacing: Float? = null,
    val lineHeight: Float? = null
)

@Serializable
data class KButtonProps(
    val modifier: KModifier? = null,
    val onClick: String = "function",
    val enabled: Boolean? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val elevation: Int? = null,
    val shape: String? = null
)

@Serializable
data class KSpacerProps(
    val modifier: KModifier? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class KCardProps(
    val modifier: KModifier? = null,
    val shape: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val elevation: Int? = null,
    val border: KBorder? = null,
    val onClick: String? = null,
    val enabled: Boolean? = null
)

@Serializable
data class KImageProps(
    val source: KImageSource? = null,
    val modifier: KModifier? = null,
    val contentDescription: String? = null,
    val scaleType: String? = KScaleType.FitCenter
)

@Serializable
data class KIconProps(
    val icon: String = "",
    val modifier: KModifier? = null,
    val size: Int? = null,
    val color: String? = null,
    val style: String? = null,
    val contentDescription: String? = null
)

@Serializable
data class KIconButtonProps(
    val icon: String = "",
    val modifier: KModifier? = null,
    val onClick: String = "function",
    val enabled: Boolean? = null,
    val iconSize: Int? = null,
    val iconColor: String? = null,
    val iconStyle: String? = null,
    val containerColor: String? = null,
    val contentColor: String? = null,
    val disabledContainerColor: String? = null,
    val disabledContentColor: String? = null,
    val contentDescription: String? = null
)

@Serializable
data class KComponentProps(
    val name: String = "",
    val componentName: String = "",
    val properties: Map<String, @Serializable(with = AnyValueSerializer::class) Any> = emptyMap(),
    val props: Map<String, @Serializable(with = AnyValueSerializer::class) Any> = emptyMap(),
    val modifier: KModifier? = null,
    val version: String = "1.0",
    val requiredImports: List<String> = emptyList(),
    val fallbackComponent: String? = null
)

// ─────────────────────────────────────────────────────────────
//  Data-class / Enum properties
// ─────────────────────────────────────────────────────────────

@Serializable
data class KDataClassProps(
    val id: String = "",
    val className: String = "",
    val fields: Map<String, @Serializable(with = AnyValueSerializer::class) Any> = emptyMap()
)

@Serializable
data class KEnumProps(
    val id: String = "",
    val enumName: String = "",
    val values: List<String> = emptyList(),
    val selectedValue: String = "",
    val onSelectionChange: String? = null
)
