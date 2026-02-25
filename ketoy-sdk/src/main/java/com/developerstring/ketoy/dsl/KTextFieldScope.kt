package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.model.KNode

/**
 * DSL scope for configuring the content slots of a `KTextField`.
 *
 * This scope mirrors the slot-based API of Material3’s `TextField` /
 * `OutlinedTextField`, allowing you to define the label, placeholder,
 * leading/trailing icons, prefix, suffix, and supporting text in a
 * type-safe builder block.
 *
 * ```kotlin
 * KTextField(value = name, onValueChange = { name = it }) {
 *     label { KText("Full name") }
 *     placeholder { KText("John Doe") }
 *     leadingIcon { KIcon(icon = KIcons.Person) }
 *     trailingIcon { KIcon(icon = KIcons.Clear) }
 *     supportingText { KText("Required") }
 * }
 * ```
 *
 * @see KUniversalScope.KTextField
 */
class KTextFieldScope : KScope() {

    private var labelContent: List<KNode>? = null
    private var placeholderContent: List<KNode>? = null
    private var leadingIconContent: List<KNode>? = null
    private var trailingIconContent: List<KNode>? = null
    private var prefixContent: List<KNode>? = null
    private var suffixContent: List<KNode>? = null
    private var supportingTextContent: List<KNode>? = null

    /**
     * Defines the **label** displayed above or inside the text field.
     *
     * ```kotlin
     * label { KText("Email address") }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the label widgets.
     */
    fun label(content: KUniversalScope.() -> Unit) {
        labelContent = KUniversalScope().apply(content).children
    }

    /**
     * Defines the **placeholder** shown when the field is empty and unfocused.
     *
     * ```kotlin
     * placeholder { KText("user@example.com") }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the placeholder widgets.
     */
    fun placeholder(content: KUniversalScope.() -> Unit) {
        placeholderContent = KUniversalScope().apply(content).children
    }

    /**
     * Defines the **leading icon** displayed at the start of the text field.
     *
     * ```kotlin
     * leadingIcon { KIcon(icon = KIcons.Search) }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the icon widgets.
     */
    fun leadingIcon(content: KUniversalScope.() -> Unit) {
        leadingIconContent = KUniversalScope().apply(content).children
    }

    /**
     * Defines the **trailing icon** displayed at the end of the text field.
     *
     * ```kotlin
     * trailingIcon { KIcon(icon = KIcons.Clear) }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the icon widgets.
     */
    fun trailingIcon(content: KUniversalScope.() -> Unit) {
        trailingIconContent = KUniversalScope().apply(content).children
    }

    /**
     * Defines the **prefix** text displayed before the input value.
     *
     * ```kotlin
     * prefix { KText("$") }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the prefix widgets.
     */
    fun prefix(content: KUniversalScope.() -> Unit) {
        prefixContent = KUniversalScope().apply(content).children
    }

    /**
     * Defines the **suffix** text displayed after the input value.
     *
     * ```kotlin
     * suffix { KText("kg") }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the suffix widgets.
     */
    fun suffix(content: KUniversalScope.() -> Unit) {
        suffixContent = KUniversalScope().apply(content).children
    }

    /**
     * Defines the **supporting text** displayed below the text field.
     *
     * Commonly used for error messages, character counts, or helper hints.
     *
     * ```kotlin
     * supportingText { KText("Must be at least 8 characters") }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the supporting-text widgets.
     */
    fun supportingText(content: KUniversalScope.() -> Unit) {
        supportingTextContent = KUniversalScope().apply(content).children
    }

    /** Returns the label content nodes, or `null` if [label] was never called. */
    fun getLabelContent(): List<KNode>? = labelContent
    /** Returns the placeholder content nodes, or `null` if [placeholder] was never called. */
    fun getPlaceholderContent(): List<KNode>? = placeholderContent
    /** Returns the leading-icon content nodes, or `null` if [leadingIcon] was never called. */
    fun getLeadingIconContent(): List<KNode>? = leadingIconContent
    /** Returns the trailing-icon content nodes, or `null` if [trailingIcon] was never called. */
    fun getTrailingIconContent(): List<KNode>? = trailingIconContent
    /** Returns the prefix content nodes, or `null` if [prefix] was never called. */
    fun getPrefixContent(): List<KNode>? = prefixContent
    /** Returns the suffix content nodes, or `null` if [suffix] was never called. */
    fun getSuffixContent(): List<KNode>? = suffixContent
    /** Returns the supporting-text content nodes, or `null` if [supportingText] was never called. */
    fun getSupportingTextContent(): List<KNode>? = supportingTextContent
}
