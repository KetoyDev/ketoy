package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.model.KNode

/**
 * DSL scope for lazy-list builders (`KLazyColumn` / `KLazyRow`).
 *
 * Mirrors Jetpack Compose’s `LazyListScope` and provides [item], [items], and
 * [itemsIndexed] functions to populate a lazy list in a type-safe, declarative
 * manner. Only the items visible on screen are composed, which makes lazy lists
 * ideal for long or dynamic server-driven content.
 *
 * ```kotlin
 * KLazyColumn {
 *     item { KText("Header") }
 *     items(products) { product -> KText(product.name) }
 *     item { KText("Footer") }
 * }
 * ```
 *
 * @see KUniversalScope.KLazyColumn
 * @see KUniversalScope.KLazyRow
 */
class KLazyListScope : KScope() {

    /**
     * Adds a single item to the lazy list.
     *
     * ```kotlin
     * KLazyColumn {
     *     item {
     *         KText("This is a single item")
     *     }
     * }
     * ```
     *
     * @param content Lambda with [KUniversalScope] receiver to declare the item’s widgets.
     */
    fun item(content: KUniversalScope.() -> Unit) {
        KUniversalScope().apply(content).children.forEach { addChild(it) }
    }

    /**
     * Adds multiple items to the lazy list from a [List].
     *
     * Each element in [items] is passed to [itemContent], where you build the
     * UI for that element inside a [KUniversalScope].
     *
     * ```kotlin
     * KLazyColumn {
     *     items(users) { user ->
     *         KRow {
     *             KIcon(icon = KIcons.Person)
     *             KText(user.name)
     *         }
     *     }
     * }
     * ```
     *
     * @param T           The type of each list element.
     * @param items       The data list to iterate over.
     * @param itemContent Lambda invoked for every element; builds the item UI.
     */
    inline fun <T> items(items: List<T>, crossinline itemContent: KUniversalScope.(T) -> Unit) {
        items.forEach { listItem ->
            val scope = KUniversalScope()
            scope.itemContent(listItem)
            scope.children.forEach { addChild(it) }
        }
    }

    /**
     * Adds multiple items **with their index** to the lazy list.
     *
     * Identical to [items] but additionally passes the zero-based index of
     * each element, which is useful for alternating row styles or numbering.
     *
     * ```kotlin
     * KLazyColumn {
     *     itemsIndexed(tasks) { index, task ->
     *         KText("${index + 1}. ${task.title}")
     *     }
     * }
     * ```
     *
     * @param T           The type of each list element.
     * @param items       The data list to iterate over.
     * @param itemContent Lambda invoked for every element; receives the index and the element.
     */
    inline fun <T> itemsIndexed(items: List<T>, crossinline itemContent: KUniversalScope.(Int, T) -> Unit) {
        items.forEachIndexed { index, listItem ->
            val scope = KUniversalScope()
            scope.itemContent(index, listItem)
            scope.children.forEach { addChild(it) }
        }
    }

    /**
     * Adds multiple items to the lazy list from an [Array].
     *
     * Convenience overload that converts the array to a list and delegates to [items].
     *
     * @param T           The type of each array element.
     * @param items       The data array to iterate over.
     * @param itemContent Lambda invoked for every element; builds the item UI.
     */
    inline fun <T> items(items: Array<T>, crossinline itemContent: KUniversalScope.(T) -> Unit) {
        items(items.toList(), itemContent)
    }

    /**
     * Adds a fixed [count] of items to the lazy list.
     *
     * Each invocation of [itemContent] receives the zero-based index, making
     * this useful for placeholder / shimmer rows or index-based rendering.
     *
     * ```kotlin
     * KLazyColumn {
     *     items(10) { index ->
     *         KText("Item #$index")
     *     }
     * }
     * ```
     *
     * @param count       Number of items to add.
     * @param itemContent Lambda invoked for each index; builds the item UI.
     */
    inline fun items(count: Int, crossinline itemContent: KUniversalScope.(Int) -> Unit) {
        repeat(count) { index ->
            val scope = KUniversalScope()
            scope.itemContent(index)
            scope.children.forEach { addChild(it) }
        }
    }
}
