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

@Composable
internal fun RenderBox(component: UIComponent) {
    val modifier = component.props?.let { parseModifier(it) } ?: Modifier
    val contentAlignment = component.props?.let { parseContentAlignment(it) } ?: Alignment.TopStart

    Box(modifier = modifier, contentAlignment = contentAlignment) {
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ─── LazyColumn ───────────────────────────────────────────────────

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
