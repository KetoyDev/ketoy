package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Variable system – immutable / mutable state holders
// ─────────────────────────────────────────────────────────────

@Serializable
sealed class KetoyVariable<T> {
    abstract val id: String
    abstract val value: T

    @Serializable
    @SerialName("ImmutableVariable")
    data class Immutable<T>(
        override val id: String,
        override val value: T
    ) : KetoyVariable<T>()

    @Serializable
    @SerialName("MutableVariable")
    data class Mutable<T>(
        override val id: String,
        override val value: T
    ) : KetoyVariable<T>()
}

// ─────────────────────────────────────────────────────────────
//  Reference helpers – clean API for data / enum refs
// ─────────────────────────────────────────────────────────────

class DataReference(private val id: String) {
    fun field(name: String): String = "{{data:$id:$name}}"
    operator fun get(fieldName: String): String = field(fieldName)
}

class EnumReference(private val id: String) {
    val selectedValue: String get() = "{{enum:$id:selectedValue}}"
    val values: String get() = "{{enum:$id:values}}"
    val enumName: String get() = "{{enum:$id:enumName}}"
    operator fun get(propertyName: String): String = "{{enum:$id:$propertyName}}"

    fun updateSelectedValue(newValue: String): Boolean {
        return com.developerstring.ketoy.core.KetoyVariableRegistry.updateValue(
            "$id.selectedValue", newValue
        )
    }
}

fun dataRef(id: String) = DataReference(id)
fun enumRef(id: String) = EnumReference(id)

object KReferences {
    fun data(id: String) = DataReference(id)
    fun enum(id: String) = EnumReference(id)
}
