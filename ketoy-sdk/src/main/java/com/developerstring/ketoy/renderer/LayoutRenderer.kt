/**
 * Layout renderers for the Ketoy SDUI library.
 *
 * This file converts layout-type [UIComponent] nodes into their Jetpack Compose
 * equivalents. Each function reads the component’s `props` to extract modifiers,
 * arrangement, alignment, scroll behaviour, content-padding, and more — then
 * recursively renders child nodes via [RenderComponent].
 *
 * Supported layout types:
 * - [RenderColumn] — vertical linear layout (`Column`)
 * - [RenderRow]    — horizontal linear layout (`Row`)
 * - [RenderBox]    — stacking layout (`Box`)
 * - [RenderLazyColumn] — vertically-scrolling lazy list (`LazyColumn`)
 * - [RenderLazyRow]    — horizontally-scrolling lazy list (`LazyRow`)
 *
 * All functions support the common modifier JSON schema and the optional
 * `weight` modifier on children for proportional sizing.
 *
 * @see RenderComponent
 * @see UIComponent
 */
package com.developerstring.ketoy.renderer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.parser.*
import kotlinx.serialization.json.*

// ─── Column ───────────────────────────────────────────────────────

/**
 * Renders a vertical `Column` layout from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 * | `verticalArrangement` | string | `"top"` | Arrangement along the main axis |
 * | `horizontalAlignment` | string | `"start"` | Cross-axis alignment |
 *
 * Children that carry a `modifier.weight` property are wrapped in a weighted
 * `Box` so they participate in the Column's proportional sizing.
 *
 * @param component The [UIComponent] whose `type` is `"column"`.
 *
 * @see RenderRow
 * @see RenderComponent
 */
@Composable
internal fun RenderColumn(component: UIComponent) {
    val modifier = component.props?.let { parseModifier(it) } ?: Modifier
    val verticalArrangement = component.props?.let { parseVerticalArrangement(it) } ?: Arrangement.Top
    val horizontalAlignment = component.props?.let { parseHorizontalAlignment(it) } ?: Alignment.Start

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        component.children?.forEach { child ->
            val childWeight = child.props?.get("modifier")?.jsonObject?.get("weight")?.jsonPrimitive?.floatOrNull
            if (childWeight != null) {
                Box(modifier = Modifier.weight(childWeight)) { RenderComponent(child) }
            } else {
                RenderComponent(child)
            }
        }
    }
}

// ─── Row ──────────────────────────────────────────────────────────

/**
 * Renders a horizontal `Row` layout from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 * | `horizontalArrangement` | string | `"start"` | Arrangement along the main axis |
 * | `verticalAlignment` | string | `"top"` | Cross-axis alignment |
 *
 * Children that carry a `modifier.weight` property are wrapped in a weighted
 * `Box` so they participate in the Row's proportional sizing.
 *
 * @param component The [UIComponent] whose `type` is `"row"`.
 *
 * @see RenderColumn
 * @see RenderComponent
 */
@Composable
internal fun RenderRow(component: UIComponent) {
    val modifier = component.props?.let { parseModifier(it) } ?: Modifier
    val horizontalArrangement = component.props?.let { parseHorizontalArrangement(it) } ?: Arrangement.Start
    val verticalAlignment = component.props?.let { parseVerticalAlignment(it) } ?: Alignment.Top

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        component.children?.forEach { child ->
            val childWeight = child.props?.get("modifier")?.jsonObject?.get("weight")?.jsonPrimitive?.floatOrNull
            if (childWeight != null) {
                Box(modifier = Modifier.weight(childWeight)) { RenderComponent(child) }
            } else {
                RenderComponent(child)
            }
        }
    }
}

// ─── Box ──────────────────────────────────────────────────────────

/**
 * Renders a stacking `Box` layout from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 * | `contentAlignment` | string | `"topStart"` | Alignment of stacked children |
 *
 * @param component The [UIComponent] whose `type` is `"box"`.
 *
 * @see RenderComponent
 */
@Composable
internal fun RenderBox(component: UIComponent) {
    val modifier = component.props?.let { parseModifier(it) } ?: Modifier
    val contentAlignment = component.props?.let { parseContentAlignment(it) } ?: Alignment.TopStart

    Box(modifier = modifier, contentAlignment = contentAlignment) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ─── LazyColumn ───────────────────────────────────────────────────

/**
 * Renders a vertically-scrolling `LazyColumn` from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 * | `verticalArrangement` | string | `"top"` | Arrangement along the main axis |
 * | `horizontalAlignment` | string | `"start"` | Cross-axis alignment |
 * | `userScrollEnabled` | boolean | `true` | Whether the user can scroll |
 * | `reverseLayout` | boolean | `false` | Reverse the item order |
 * | `contentPadding` | object/int | `0` | Inner padding around the content |
 *
 * Each child [UIComponent] is emitted as a lazy item.
 *
 * @param component The [UIComponent] whose `type` is `"lazycolumn"`.
 *
 * @see RenderLazyRow
 * @see RenderColumn
 */
@Composable
internal fun RenderLazyColumn(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val verticalArrangement = parseVerticalArrangement(props)
    val horizontalAlignment = parseHorizontalAlignment(props)
    val userScrollEnabled = props["userScrollEnabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val reverseLayout = props["reverseLayout"]?.jsonPrimitive?.booleanOrNull ?: false
    val contentPadding = props["contentPadding"]?.let { parsePadding(it) } ?: PaddingValues(0.dp)

    LazyColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        contentPadding = contentPadding
    ) {
        component.children?.let { children ->
            items(children) { child -> RenderComponent(child) }
        }
    }
}

// ─── LazyRow ──────────────────────────────────────────────────────

/**
 * Renders a horizontally-scrolling `LazyRow` from a [UIComponent] node.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 * | `horizontalArrangement` | string | `"start"` | Arrangement along the main axis |
 * | `verticalAlignment` | string | `"top"` | Cross-axis alignment |
 * | `userScrollEnabled` | boolean | `true` | Whether the user can scroll |
 * | `reverseLayout` | boolean | `false` | Reverse the item order |
 * | `contentPadding` | object/int | `0` | Inner padding around the content |
 *
 * Each child [UIComponent] is emitted as a lazy item.
 *
 * @param component The [UIComponent] whose `type` is `"lazyrow"`.
 *
 * @see RenderLazyColumn
 * @see RenderRow
 */
@Composable
internal fun RenderLazyRow(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifier = parseModifier(props)
    val horizontalArrangement = parseHorizontalArrangement(props)
    val verticalAlignment = parseVerticalAlignment(props)
    val userScrollEnabled = props["userScrollEnabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val reverseLayout = props["reverseLayout"]?.jsonPrimitive?.booleanOrNull ?: false
    val contentPadding = props["contentPadding"]?.let { parsePadding(it) } ?: PaddingValues(0.dp)

    LazyRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        contentPadding = contentPadding
    ) {
        component.children?.let { children ->
            items(children) { child -> RenderComponent(child) }
        }
    }
}
