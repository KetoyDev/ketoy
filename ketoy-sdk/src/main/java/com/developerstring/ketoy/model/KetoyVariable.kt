package com.developerstring.ketoy.model

import kotlinx.serialization.*

// ─────────────────────────────────────────────────────────────
//  Variable system – immutable / mutable state holders
// ─────────────────────────────────────────────────────────────

/**
 * A typed variable that lives in the Ketoy variable registry and can be
 * referenced from JSON templates using the `{{data:id:field}}` syntax.
 *
 * Two concrete variants exist:
 * - [Immutable] – a read-only value set once and never changed.
 * - [Mutable] – a value that can be updated at runtime (e.g. from user interaction).
 *
 * Variables are registered into
 * [KetoyVariableRegistry][com.developerstring.ketoy.core.KetoyVariableRegistry]
 * and resolved by the renderer when it encounters template placeholders.
 *
 * ### JSON wire-format example
 * ```json
 * { "type": "ImmutableVariable", "id": "user.name", "value": "Alice" }
 * ```
 *
 * ### Kotlin usage
 * ```kotlin
 * val name = KetoyVariable.Immutable(id = "user.name", value = "Alice")
 * val counter = KetoyVariable.Mutable(id = "tap.count", value = 0)
 * ```
 *
 * @param T The type of the value held by this variable.
 * @property id A unique dot-separated identifier used in template expressions.
 * @property value The current value of the variable.
 * @see com.developerstring.ketoy.core.KetoyVariableRegistry
 * @see DataReference
 * @see EnumReference
 */
@Serializable
sealed class KetoyVariable<T> {
    abstract val id: String
    abstract val value: T

    /**
     * A read-only variable whose [value] is set at registration time and
     * never changes during the lifecycle of the current Ketoy session.
     *
     * @param T The type of the held value.
     * @property id Unique identifier for template resolution.
     * @property value The constant value.
     * @see KetoyVariable
     */
    @Serializable
    @SerialName("ImmutableVariable")
    data class Immutable<T>(
        override val id: String,
        override val value: T
    ) : KetoyVariable<T>()

    /**
     * A read-write variable whose [value] can be modified at runtime via
     * [KetoyVariableRegistry.updateValue][com.developerstring.ketoy.core.KetoyVariableRegistry.updateValue].
     *
     * Commonly used for form inputs, counters, or any reactive UI state
     * driven by the server.
     *
     * @param T The type of the held value.
     * @property id Unique identifier for template resolution.
     * @property value The initial (and current) value.
     * @see KetoyVariable
     */
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

/**
 * A type-safe builder for **data** variable template expressions.
 *
 * Use [DataReference] (or the [dataRef] shorthand) to construct placeholder
 * strings like `"{{data:user:name}}"` that the renderer resolves at display time.
 *
 * ### Kotlin usage
 * ```kotlin
 * val userRef = dataRef("user")
 * val greeting = "Hello, ${userRef.field("name")}!"  // "Hello, {{data:user:name}}!"
 * // or via operator:
 * val greeting2 = "Hello, ${userRef["name"]}!"
 * ```
 *
 * @property id The variable identifier registered in the variable registry.
 * @see KetoyVariable
 * @see KReferences
 * @see com.developerstring.ketoy.core.KetoyVariableRegistry
 */
class DataReference(private val id: String) {
    /**
     * Returns a template placeholder for the given [name] field.
     *
     * @param name The field name within the data variable.
     * @return A template string in the form `"{{data:<id>:<name>}}"`.
     */
    fun field(name: String): String = "{{data:$id:$name}}"

    /**
     * Shorthand operator for [field].
     *
     * @param fieldName The field name within the data variable.
     * @return A template string in the form `"{{data:<id>:<fieldName>}}"`.
     */
    operator fun get(fieldName: String): String = field(fieldName)
}

/**
 * A type-safe builder for **enum** variable template expressions and runtime
 * updates.
 *
 * Enum variables represent a set of predefined values where one is currently
 * selected. The renderer can bind the selected value, all possible values,
 * and the enum's display name into template slots.
 *
 * ### Kotlin usage
 * ```kotlin
 * val themeRef = enumRef("theme")
 * println(themeRef.selectedValue)  // "{{enum:theme:selectedValue}}"
 * themeRef.updateSelectedValue("dark")
 * ```
 *
 * @property id The enum variable identifier in the variable registry.
 * @see KetoyVariable
 * @see KReferences
 * @see com.developerstring.ketoy.core.KetoyVariableRegistry
 */
class EnumReference(private val id: String) {
    /** Template placeholder that resolves to the currently selected value. */
    val selectedValue: String get() = "{{enum:$id:selectedValue}}"
    /** Template placeholder that resolves to the full list of possible values. */
    val values: String get() = "{{enum:$id:values}}"
    /** Template placeholder that resolves to the human-readable enum name. */
    val enumName: String get() = "{{enum:$id:enumName}}"

    /**
     * Returns a template placeholder for an arbitrary enum property.
     *
     * @param propertyName The property key.
     * @return A template string in the form `"{{enum:<id>:<propertyName>}}"`.
     */
    operator fun get(propertyName: String): String = "{{enum:$id:$propertyName}}"

    /**
     * Mutates the `selectedValue` of this enum variable at runtime.
     *
     * This immediately updates the value in
     * [KetoyVariableRegistry][com.developerstring.ketoy.core.KetoyVariableRegistry],
     * which in turn triggers recomposition in any Composable observing the variable.
     *
     * @param newValue The new selected value.
     * @return `true` if the update was applied, `false` if the variable was not found
     *   or is immutable.
     * @see com.developerstring.ketoy.core.KetoyVariableRegistry.updateValue
     */
    fun updateSelectedValue(newValue: String): Boolean {
        return com.developerstring.ketoy.core.KetoyVariableRegistry.updateValue(
            "$id.selectedValue", newValue
        )
    }
}

/**
 * Creates a [DataReference] for the given variable [id].
 *
 * @param id The variable identifier registered in the variable registry.
 * @return A new [DataReference] instance.
 * @see DataReference
 * @see KReferences.data
 */
fun dataRef(id: String) = DataReference(id)

/**
 * Creates an [EnumReference] for the given variable [id].
 *
 * @param id The enum variable identifier.
 * @return A new [EnumReference] instance.
 * @see EnumReference
 * @see KReferences.enum
 */
fun enumRef(id: String) = EnumReference(id)

/**
 * Central factory for creating variable reference helpers.
 *
 * Prefer this entry point in Kotlin DSL code for discoverability:
 * ```kotlin
 * val userRef  = KReferences.data("user")
 * val themeRef = KReferences.enum("theme")
 * ```
 *
 * @see DataReference
 * @see EnumReference
 * @see dataRef
 * @see enumRef
 */
object KReferences {
    /**
     * Creates a [DataReference] for the specified variable [id].
     *
     * @param id The data variable identifier.
     * @return A new [DataReference] ready to generate template placeholders.
     */
    fun data(id: String) = DataReference(id)

    /**
     * Creates an [EnumReference] for the specified variable [id].
     *
     * @param id The enum variable identifier.
     * @return A new [EnumReference] ready to generate template placeholders.
     */
    fun enum(id: String) = EnumReference(id)
}
