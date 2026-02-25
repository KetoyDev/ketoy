/**
 * Top-level DSL builder functions for the Ketoy SDUI library.
 *
 * These functions create **root-level** UI nodes outside of any DSL scope and are
 * the primary entry-point when building a server-driven screen in Kotlin. Each
 * builder mirrors its Jetpack Compose counterpart and produces a serialisable
 * [KNode][com.developerstring.ketoy.model.KNode] tree that the Ketoy renderer draws on the client.
 *
 * ## Quick start
 * ```kotlin
 * val screen = KColumn(
 *     modifier = KModifier().fillMaxSize(),
 *     verticalArrangement = "SpaceBetween"
 * ) {
 *     KText("Hello from Ketoy!")
 *     KButton(onClick = { /* handle */ }) { KText("Tap me") }
 * }
 * ```
 *
 * @see KUniversalScope
 * @see KLazyListScope
 * @see KScaffoldScope
 */
package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.util.KFabPosition

// ─────────────────────────────────────────────────────────────
//  Top-level DSL builder functions
//  (used outside of any scope)
// ─────────────────────────────────────────────────────────────

/**
 * Creates a root-level vertical layout (Column) node.
 *
 * Arranges children vertically, mirroring Jetpack Compose's `Column`.
 * Use this as the top-level entry-point when building a server-driven screen.
 *
 * ```kotlin
 * val ui = KColumn(
 *     modifier = KModifier().fillMaxSize().padding(16),
 *     verticalArrangement = "SpaceBetween",
 *     horizontalAlignment = "CenterHorizontally"
 * ) {
 *     KText("Title")
 *     KButton(onClick = { }) { KText("Action") }
 * }
 * ```
 *
 * @param modifier             Optional [KModifier] for sizing, padding, background, etc.
 * @param verticalArrangement  Arrangement along the main (vertical) axis — e.g. `"Top"`, `"SpaceBetween"`.
 * @param horizontalAlignment  Alignment along the cross (horizontal) axis — e.g. `"CenterHorizontally"`.
 * @param content              Lambda with [KUniversalScope] receiver to declare child widgets.
 * @return A [KColumnNode] representing the serialisable column tree.
 *
 * @see KUniversalScope.KColumn
 */
fun KColumn(
    modifier: KModifier? = null, verticalArrangement: String? = null,
    horizontalAlignment: String? = null, content: KUniversalScope.() -> Unit
): KColumnNode {
    val scope = KUniversalScope().apply(content)
    return KColumnNode(KColumnProps(modifier, verticalArrangement, horizontalAlignment), scope.children)
}

/**
 * Creates a root-level horizontal layout (Row) node.
 *
 * Arranges children horizontally, mirroring Jetpack Compose's `Row`.
 *
 * ```kotlin
 * val row = KRow(horizontalArrangement = "SpaceEvenly") {
 *     KIcon(icon = KIcons.Home)
 *     KText("Dashboard")
 * }
 * ```
 *
 * @param modifier               Optional [KModifier] for sizing, padding, background, etc.
 * @param horizontalArrangement  Arrangement along the main (horizontal) axis — e.g. `"Start"`, `"SpaceEvenly"`.
 * @param verticalAlignment      Alignment along the cross (vertical) axis — e.g. `"CenterVertically"`.
 * @param content                Lambda with [KUniversalScope] receiver to declare child widgets.
 * @return A [KRowNode] representing the serialisable row tree.
 *
 * @see KUniversalScope.KRow
 */
fun KRow(
    modifier: KModifier? = null, horizontalArrangement: String? = null,
    verticalAlignment: String? = null, content: KUniversalScope.() -> Unit
): KRowNode {
    val scope = KUniversalScope().apply(content)
    return KRowNode(KRowProps(modifier, horizontalArrangement, verticalAlignment), scope.children)
}

/**
 * Creates a root-level overlay layout (Box) node.
 *
 * Children are stacked on top of each other, mirroring Jetpack Compose's `Box`.
 *
 * ```kotlin
 * val box = KBox(contentAlignment = "Center") {
 *     KImage(source = KImageSource.url("https://example.com/bg.jpg"))
 *     KText("Overlay text", color = KColors.White)
 * }
 * ```
 *
 * @param modifier         Optional [KModifier] for sizing, padding, background, etc.
 * @param contentAlignment Alignment for children inside the box — e.g. `"Center"`, `"TopStart"`.
 * @param content          Lambda with [KUniversalScope] receiver to declare child widgets.
 * @return A [KBoxNode] representing the serialisable box tree.
 *
 * @see KUniversalScope.KBox
 */
fun KBox(
    modifier: KModifier? = null, contentAlignment: String? = null,
    content: KUniversalScope.() -> Unit
): KBoxNode {
    val scope = KUniversalScope().apply(content)
    return KBoxNode(KBoxProps(modifier, contentAlignment), scope.children)
}

/**
 * Creates a root-level vertically-scrolling lazy list (LazyColumn) node.
 *
 * Only items visible on screen are composed, making this ideal for long or
 * dynamic lists of server-driven content.
 *
 * ```kotlin
 * val list = KLazyColumn(
 *     contentPadding = KPadding(horizontal = 16, vertical = 8)
 * ) {
 *     items(products) { product -> KText(product.name) }
 * }
 * ```
 *
 * @param modifier              Optional [KModifier] for sizing, padding, background, etc.
 * @param verticalArrangement   Arrangement along the main (vertical) axis.
 * @param horizontalAlignment   Alignment along the cross (horizontal) axis.
 * @param userScrollEnabled     Whether the user can scroll. Defaults to `true`.
 * @param reverseLayout         Whether the list should be rendered in reverse order.
 * @param contentPadding        Padding applied around the entire list content.
 * @param beyondBoundsItemCount Number of items to compose beyond the visible bounds.
 * @param content               Lambda with [KLazyListScope] receiver for `item {}`, `items {}`, etc.
 * @return A [KLazyColumnNode] representing the serialisable lazy column tree.
 *
 * @see KLazyListScope
 * @see KUniversalScope.KLazyColumn
 */
fun KLazyColumn(
    modifier: KModifier? = null, verticalArrangement: String? = null,
    horizontalAlignment: String? = null, userScrollEnabled: Boolean? = null,
    reverseLayout: Boolean? = null, contentPadding: KPadding? = null,
    beyondBoundsItemCount: Int? = null, content: KLazyListScope.() -> Unit
): KLazyColumnNode {
    val scope = KLazyListScope().apply(content)
    return KLazyColumnNode(KLazyColumnProps(modifier, verticalArrangement, horizontalAlignment, userScrollEnabled, reverseLayout, contentPadding, beyondBoundsItemCount), scope.children)
}

/**
 * Creates a root-level horizontally-scrolling lazy list (LazyRow) node.
 *
 * Ideal for carousels, chip rows, or any horizontally-scrollable
 * server-driven content list.
 *
 * ```kotlin
 * val carousel = KLazyRow(horizontalArrangement = "spacedBy(8)") {
 *     items(banners) { banner ->
 *         KImage(source = KImageSource.url(banner.imageUrl))
 *     }
 * }
 * ```
 *
 * @param modifier              Optional [KModifier] for sizing, padding, background, etc.
 * @param horizontalArrangement Arrangement along the main (horizontal) axis.
 * @param verticalAlignment     Alignment along the cross (vertical) axis.
 * @param userScrollEnabled     Whether the user can scroll. Defaults to `true`.
 * @param reverseLayout         Whether the list should be rendered in reverse order.
 * @param contentPadding        Padding applied around the entire list content.
 * @param beyondBoundsItemCount Number of items to compose beyond the visible bounds.
 * @param content               Lambda with [KLazyListScope] receiver for `item {}`, `items {}`, etc.
 * @return A [KLazyRowNode] representing the serialisable lazy row tree.
 *
 * @see KLazyListScope
 * @see KUniversalScope.KLazyRow
 */
fun KLazyRow(
    modifier: KModifier? = null, horizontalArrangement: String? = null,
    verticalAlignment: String? = null, userScrollEnabled: Boolean? = null,
    reverseLayout: Boolean? = null, contentPadding: KPadding? = null,
    beyondBoundsItemCount: Int? = null, content: KLazyListScope.() -> Unit
): KLazyRowNode {
    val scope = KLazyListScope().apply(content)
    return KLazyRowNode(KLazyRowProps(modifier, horizontalArrangement, verticalAlignment, userScrollEnabled, reverseLayout, contentPadding, beyondBoundsItemCount), scope.children)
}

/**
 * Creates a root-level Material3 Scaffold node.
 *
 * Provides the standard visual structure for a screen — top app bar, bottom bar,
 * floating action button, snackbar host, and a content body — all expressed as
 * server-driven nodes.
 *
 * ```kotlin
 * val screen = KScaffold(
 *     topBar = { KTopAppBar(title = { KText("Home") }) },
 *     floatingActionButton = {
 *         KFloatingActionButton(onClick = { }) { KIcon(icon = KIcons.Add) }
 *     }
 * ) {
 *     KLazyColumn { items(data) { KText(it.name) } }
 * }
 * ```
 *
 * @param modifier                    Optional [KModifier] applied to the scaffold root.
 * @param containerColor              Background colour of the scaffold (e.g. `KColors.White`).
 * @param contentColor                Default content colour for children.
 * @param contentWindowInsets         Window insets applied to the content area.
 * @param topBar                      Optional lambda to declare a top app bar inside a [KScaffoldScope].
 * @param bottomBar                   Optional lambda to declare a bottom bar inside a [KScaffoldScope].
 * @param snackbarHost                Optional lambda to declare a snackbar host inside a [KScaffoldScope].
 * @param floatingActionButton        Optional lambda to declare a FAB inside a [KScaffoldScope].
 * @param floatingActionButtonPosition FAB position — one of [KFabPosition] constants (e.g. `KFabPosition.End`).
 * @param content                     Lambda with [KUniversalScope] receiver for the main body content.
 * @return A [KScaffoldNode] representing the serialisable scaffold tree.
 *
 * @see KScaffoldScope
 * @see KUniversalScope.KScaffold
 */
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
