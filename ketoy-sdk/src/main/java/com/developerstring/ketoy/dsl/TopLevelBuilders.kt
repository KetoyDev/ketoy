package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.util.KFabPosition

// ─────────────────────────────────────────────────────────────
//  Top-level DSL builder functions
//  (used outside of any scope)
// ─────────────────────────────────────────────────────────────

fun KColumn(
    modifier: KModifier? = null, verticalArrangement: String? = null,
    horizontalAlignment: String? = null, content: KUniversalScope.() -> Unit
): KColumnNode {
    val scope = KUniversalScope().apply(content)
    return KColumnNode(KColumnProps(modifier, verticalArrangement, horizontalAlignment), scope.children)
}

fun KRow(
    modifier: KModifier? = null, horizontalArrangement: String? = null,
    verticalAlignment: String? = null, content: KUniversalScope.() -> Unit
): KRowNode {
    val scope = KUniversalScope().apply(content)
    return KRowNode(KRowProps(modifier, horizontalArrangement, verticalAlignment), scope.children)
}

fun KBox(
    modifier: KModifier? = null, contentAlignment: String? = null,
    content: KUniversalScope.() -> Unit
): KBoxNode {
    val scope = KUniversalScope().apply(content)
    return KBoxNode(KBoxProps(modifier, contentAlignment), scope.children)
}

fun KLazyColumn(
    modifier: KModifier? = null, verticalArrangement: String? = null,
    horizontalAlignment: String? = null, userScrollEnabled: Boolean? = null,
    reverseLayout: Boolean? = null, contentPadding: KPadding? = null,
    beyondBoundsItemCount: Int? = null, content: KLazyListScope.() -> Unit
): KLazyColumnNode {
    val scope = KLazyListScope().apply(content)
    return KLazyColumnNode(KLazyColumnProps(modifier, verticalArrangement, horizontalAlignment, userScrollEnabled, reverseLayout, contentPadding, beyondBoundsItemCount), scope.children)
}

fun KLazyRow(
    modifier: KModifier? = null, horizontalArrangement: String? = null,
    verticalAlignment: String? = null, userScrollEnabled: Boolean? = null,
    reverseLayout: Boolean? = null, contentPadding: KPadding? = null,
    beyondBoundsItemCount: Int? = null, content: KLazyListScope.() -> Unit
): KLazyRowNode {
    val scope = KLazyListScope().apply(content)
    return KLazyRowNode(KLazyRowProps(modifier, horizontalArrangement, verticalAlignment, userScrollEnabled, reverseLayout, contentPadding, beyondBoundsItemCount), scope.children)
}

fun KScaffold(
    modifier: KModifier? = null, containerColor: String? = null,
    contentColor: String? = null, contentWindowInsets: KWindowInsets? = null,
    topBar: (KScaffoldScope.() -> Unit)? = null,
    bottomBar: (KScaffoldScope.() -> Unit)? = null,
    snackbarHost: (KScaffoldScope.() -> Unit)? = null,
    floatingActionButton: (KScaffoldScope.() -> Unit)? = null,
    floatingActionButtonPosition: String? = null,
    content: KUniversalScope.() -> Unit
): KScaffoldNode {
    val body = KUniversalScope().apply(content)
    val tb = topBar?.let { KScaffoldScope().apply(it).children }
    val bb = bottomBar?.let { KScaffoldScope().apply(it).children }
    val sh = snackbarHost?.let { KScaffoldScope().apply(it).children }
    val fab = floatingActionButton?.let { KScaffoldScope().apply(it).children }
    return KScaffoldNode(
        KScaffoldProps(modifier, tb, bb, sh, fab, floatingActionButtonPosition ?: KFabPosition.End, containerColor, contentColor, contentWindowInsets),
        body.children
    )
}
