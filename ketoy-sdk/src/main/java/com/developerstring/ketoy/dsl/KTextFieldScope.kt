package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.model.KNode

/**
 * Scope for TextField composable-content slots (label, placeholder, icons, etc.).
 */
class KTextFieldScope : KScope() {

    private var labelContent: List<KNode>? = null
    private var placeholderContent: List<KNode>? = null
    private var leadingIconContent: List<KNode>? = null
    private var trailingIconContent: List<KNode>? = null
    private var prefixContent: List<KNode>? = null
    private var suffixContent: List<KNode>? = null
    private var supportingTextContent: List<KNode>? = null

    fun label(content: KUniversalScope.() -> Unit) {
        labelContent = KUniversalScope().apply(content).children
    }

    fun placeholder(content: KUniversalScope.() -> Unit) {
        placeholderContent = KUniversalScope().apply(content).children
    }

    fun leadingIcon(content: KUniversalScope.() -> Unit) {
        leadingIconContent = KUniversalScope().apply(content).children
    }

    fun trailingIcon(content: KUniversalScope.() -> Unit) {
        trailingIconContent = KUniversalScope().apply(content).children
    }

    fun prefix(content: KUniversalScope.() -> Unit) {
        prefixContent = KUniversalScope().apply(content).children
    }

    fun suffix(content: KUniversalScope.() -> Unit) {
        suffixContent = KUniversalScope().apply(content).children
    }

    fun supportingText(content: KUniversalScope.() -> Unit) {
        supportingTextContent = KUniversalScope().apply(content).children
    }

    fun getLabelContent(): List<KNode>? = labelContent
    fun getPlaceholderContent(): List<KNode>? = placeholderContent
    fun getLeadingIconContent(): List<KNode>? = leadingIconContent
    fun getTrailingIconContent(): List<KNode>? = trailingIconContent
    fun getPrefixContent(): List<KNode>? = prefixContent
    fun getSuffixContent(): List<KNode>? = suffixContent
    fun getSupportingTextContent(): List<KNode>? = supportingTextContent
}
