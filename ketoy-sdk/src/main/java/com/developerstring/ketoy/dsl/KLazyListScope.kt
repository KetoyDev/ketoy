package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.model.KNode

/**
 * Scope for LazyColumn / LazyRow – mirrors Compose's `LazyListScope`.
 */
class KLazyListScope : KScope() {

    fun item(content: KUniversalScope.() -> Unit) {
        KUniversalScope().apply(content).children.forEach { addChild(it) }
    }

    inline fun <T> items(items: List<T>, crossinline itemContent: KUniversalScope.(T) -> Unit) {
        items.forEach { listItem ->
            val scope = KUniversalScope()
            scope.itemContent(listItem)
            scope.children.forEach { addChild(it) }
        }
    }

    inline fun <T> itemsIndexed(items: List<T>, crossinline itemContent: KUniversalScope.(Int, T) -> Unit) {
        items.forEachIndexed { index, listItem ->
            val scope = KUniversalScope()
            scope.itemContent(index, listItem)
            scope.children.forEach { addChild(it) }
        }
    }

    inline fun <T> items(items: Array<T>, crossinline itemContent: KUniversalScope.(T) -> Unit) {
        items(items.toList(), itemContent)
    }

    inline fun items(count: Int, crossinline itemContent: KUniversalScope.(Int) -> Unit) {
        repeat(count) { index ->
            val scope = KUniversalScope()
            scope.itemContent(index)
            scope.children.forEach { addChild(it) }
        }
    }
}
