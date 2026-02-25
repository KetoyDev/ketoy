/**
 * Widget renderers for the Ketoy SDUI library.
 *
 * This file converts widget-type [UIComponent] nodes into native Jetpack Compose
 * Material 3 composables.  Each renderer reads the component's `props` JSON to extract
 * styling, content, and interaction properties, then emits the corresponding Compose
 * widget.
 *
 * Supported widget types:
 * - [RenderText]       — styled text label
 * - [RenderButton]     — filled or custom-background button
 * - [RenderSpacer]     — blank spacing element
 * - [RenderCard]       — Material 3 card with optional click, border, and elevation
 * - [RenderImage]      — multi-source image (URL, resource, icon, base64)
 * - [RenderIcon]       — Material icon resolved by name and style
 * - [RenderIconButton] — icon wrapped in a clickable container
 *
 * Colours can reference the active Ketoy theme via `@theme/` prefixed strings,
 * which are resolved by [resolveKetoyColor] / [resolveKetoyColorOrNull].
 *
 * @see RenderComponent
 * @see UIComponent
 * @see OnClickResolver
 */
package com.developerstring.ketoy.renderer

import android.R as AndroidR
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.KImageSource
import com.developerstring.ketoy.model.KScaleType
import com.developerstring.ketoy.navigation.LocalKetoyNavController
import com.developerstring.ketoy.parser.*
import com.developerstring.ketoy.util.KIcons
import com.developerstring.ketoy.util.resolveIcon
import kotlinx.serialization.json.*

// ─── onClick helper ───────────────────────────────────────────────

/**
 * Resolves the `onClick` prop of a widget into an executable callback.
 *
 * Supports both legacy string action IDs (registered via the DSL) and
 * server-provided JSON action objects. Automatically supplies the current
 * [KetoyNavController] from [LocalKetoyNavController] so that `"navigate"`
 * JSON actions work out of the box.
 *
 * @param props The full `props` JSON object of the component. The function
 *              reads the `"onClick"` key and delegates to [OnClickResolver].
 * @return A lambda that executes the resolved action, or `null` when no
 *         `onClick` is present.
 *
 * @see OnClickResolver
 */
@Composable
private fun rememberOnClick(props: JsonObject): (() -> Unit)? {
    val context = LocalContext.current
    val navController = LocalKetoyNavController.current
    val element = props["onClick"]
    return OnClickResolver.resolve(element, context, navController)
}

// ─── Text ─────────────────────────────────────────────────────────

/**
 * Renders a Material 3 `Text` composable from a [UIComponent] node.
 *
 * The raw `text` value is passed through [KetoyVariableRegistry.resolveTemplate]
 * so that `{{variable}}` placeholders are replaced at render time.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `text` | string | `""` | Display text (supports `{{var}}` templates) |
 * | `fontSize` | int | `14` | Font size in sp |
 * | `fontWeight` | string | `"normal"` | `"bold"`, `"normal"`, `"light"`, `"medium"`, `"semiBold"` |
 * | `color` | string | – | Text colour (supports `@theme/` references) |
 * | `textAlign` | string | `"start"` | `"center"`, `"start"`, `"end"`, `"justify"` |
 * | `maxLines` | int | – | Maximum visible lines before truncation |
 * | `overflow` | string | – | `"Ellipsis"`, `"Clip"`, `"Visible"` |
 * | `letterSpacing` | float | – | Letter spacing in sp |
 * | `lineHeight` | float | – | Line height in sp |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"text"`.
 *
 * @see KetoyVariableRegistry
 */

@Composable
internal fun RenderText(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val rawText = props["text"]?.jsonPrimitive?.content ?: ""
    val resolvedText = KetoyVariableRegistry.resolveTemplate(rawText)
    val fontSize = props["fontSize"]?.jsonPrimitive?.intOrNull?.sp ?: 14.sp
    val fontWeight = when (props["fontWeight"]?.jsonPrimitive?.content) {
        "bold" -> FontWeight.Bold
        "normal" -> FontWeight.Normal
        "light" -> FontWeight.Light
        "medium" -> FontWeight.Medium
        "semiBold" -> FontWeight.SemiBold
        else -> FontWeight.Normal
    }
    val color = resolveKetoyColor(props["color"]?.jsonPrimitive?.content)
    val textAlign = when (props["textAlign"]?.jsonPrimitive?.content) {
        "center" -> TextAlign.Center
        "start" -> TextAlign.Start
        "end" -> TextAlign.End
        "justify" -> TextAlign.Justify
        else -> TextAlign.Start
    }
    val maxLines = props["maxLines"]?.jsonPrimitive?.intOrNull ?: Int.MAX_VALUE
    val overflow = when (props["overflow"]?.jsonPrimitive?.content) {
        "Ellipsis" -> TextOverflow.Ellipsis
        "Clip" -> TextOverflow.Clip
        "Visible" -> TextOverflow.Visible
        else -> TextOverflow.Clip
    }
    val letterSpacing = props["letterSpacing"]?.jsonPrimitive?.floatOrNull?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified
    val lineHeight = props["lineHeight"]?.jsonPrimitive?.floatOrNull?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified
    val modifier = parseModifier(props)

    Text(
        text = resolvedText,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        modifier = modifier
    )
}

// ─── Button ───────────────────────────────────────────────────────

/**
 * Renders a Material 3 `Button` from a [UIComponent] node.
 *
 * Two rendering paths:
 * 1. **Custom-background path** — when the modifier specifies a non-transparent
 *    `background`, a `Box` + `clickable` is used instead of a Material `Button`
 *    to avoid the default button tonal overlay.
 * 2. **Standard path** — delegates to `Button()` with optional `containerColor`
 *    and `shape` overrides.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `containerColor` | string | theme default | Button fill colour |
 * | `shape` | string | default | Corner shape descriptor |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * Children are rendered inside the button’s content slot.
 *
 * @param component The [UIComponent] whose `type` is `"button"`.
 *
 * @see OnClickResolver
 */

@Composable
internal fun RenderButton(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifierProps = props["modifier"]?.jsonObject
    val onClickAction = rememberOnClick(props)

    val hasCustomBackground = modifierProps?.get("background")?.let { el ->
        if (el is JsonPrimitive) {
            val bg = el.content
            bg != "transparent" && bg.isNotEmpty()
        } else false
    } ?: false

    if (hasCustomBackground) {
        val modifier = parseModifier(props)
        Box(
            modifier = modifier.clickable { onClickAction?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                component.children?.forEach { child -> RenderComponent(child) }
            }
        }
    } else {
        var buttonModifier: Modifier = Modifier
        modifierProps?.let { mp ->
            if (mp["fillMaxWidth"]?.jsonPrimitive?.booleanOrNull == true) buttonModifier = buttonModifier.fillMaxWidth()
            mp["width"]?.jsonPrimitive?.intOrNull?.let { buttonModifier = buttonModifier.width(it.dp) }
            mp["height"]?.jsonPrimitive?.intOrNull?.let { buttonModifier = buttonModifier.height(it.dp) }
            mp["margin"]?.let { buttonModifier = applyJsonPadding(buttonModifier, it) }
            mp["padding"]?.let { buttonModifier = applyJsonPadding(buttonModifier, it) }
        }

        val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
        val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) }

        Button(
            onClick = { onClickAction?.invoke() },
            modifier = buttonModifier,
            colors = if (containerColor != null) ButtonDefaults.buttonColors(containerColor = containerColor)
                     else ButtonDefaults.buttonColors(),
            shape = shape ?: ButtonDefaults.shape
        ) {
            component.children?.forEach { child -> RenderComponent(child) }
        }
    }
}

// ─── Spacer ───────────────────────────────────────────────────────

/**
 * Renders a `Spacer` composable from a [UIComponent] node.
 *
 * Applies explicit `width` and/or `height` dimensions when provided, otherwise
 * falls back to the modifier-only sizing.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `width` | int | – | Width in dp |
 * | `height` | int | – | Height in dp |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"spacer"`.
 */

@Composable
internal fun RenderSpacer(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val width = props["width"]?.jsonPrimitive?.intOrNull
    val height = props["height"]?.jsonPrimitive?.intOrNull
    val modifier = parseModifier(props)

    when {
        width != null && height != null -> Spacer(modifier = modifier.size(width.dp, height.dp))
        width != null -> Spacer(modifier = modifier.width(width.dp))
        height != null -> Spacer(modifier = modifier.height(height.dp))
        else -> Spacer(modifier = modifier)
    }
}

// ─── Card ─────────────────────────────────────────────────────────

/**
 * Renders a Material 3 `Card` from a [UIComponent] node.
 *
 * Supports both clickable and non-clickable variants. When a child (or the
 * card itself) carries a `gradient` or `background` modifier, the card's own
 * `containerColor` is forced to `Color.Transparent` to avoid a double-layer
 * visual artifact.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `containerColor` | string | theme default | Card surface colour |
 * | `contentColor` | string | theme default | Default text/icon colour |
 * | `elevation` | int | `1` | Default shadow elevation in dp |
 * | `shape` | string | rounded 12 dp | Corner shape descriptor |
 * | `border` | object | – | `{ "width": int, "color": string }` |
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `enabled` | boolean | `true` | Whether click is active |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"card"`.
 *
 * @see OnClickResolver
 */

@Composable
internal fun RenderCard(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
    val elevation = props["elevation"]?.jsonPrimitive?.intOrNull ?: 1
    val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) } ?: RoundedCornerShape(12.dp)
    val border = props["border"]?.jsonObject?.let { borderObj ->
        val w = borderObj["width"]?.jsonPrimitive?.intOrNull ?: 1
        val c = resolveKetoyColorOrNull(borderObj["color"]?.jsonPrimitive?.contentOrNull) ?: Color.Gray
        BorderStroke(w.dp, c)
    }
    val onClickAction = rememberOnClick(props)
    val hasOnClick = props["onClick"] != null
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val modifier = parseModifier(props)

    // When a direct child carries a gradient or custom background on its
    // modifier the Card's own containerColor must be transparent, otherwise
    // the solid colour paints behind the child and creates a visible
    // double-layer artifact.
    val childHasVisualBg = component.children?.any { child ->
        val childMod = child.props?.get("modifier")
        if (childMod is JsonObject) {
            childMod.containsKey("gradient") || childMod.containsKey("background")
        } else false
    } ?: false

    // Also if the Card's own modifier has a gradient/background, go transparent.
    val selfHasVisualBg = props["modifier"]?.let { m ->
        if (m is JsonObject) m.containsKey("gradient") || m.containsKey("background") else false
    } ?: false

    val effectiveContainerColor = if (childHasVisualBg || selfHasVisualBg) {
        Color.Transparent
    } else {
        containerColor ?: CardDefaults.cardColors().containerColor
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = effectiveContainerColor,
        contentColor = contentColor ?: CardDefaults.cardColors().contentColor
    )
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)

    if (hasOnClick && enabled) {
        Card(onClick = { onClickAction?.invoke() }, modifier = modifier, enabled = enabled, shape = shape,
            colors = cardColors, elevation = cardElevation, border = border) {
            component.children?.forEach { child -> RenderComponent(child) }
        }
    } else {
        Card(modifier = modifier, shape = shape, colors = cardColors,
            elevation = cardElevation, border = border) {
            component.children?.forEach { child -> RenderComponent(child) }
        }
    }
}

// ─── Image ────────────────────────────────────────────────────────

/**
 * Renders an image from a [UIComponent] node, supporting multiple source types.
 *
 * The `source` JSON object specifies how the image is loaded:
 *
 * | `source.type` | Behaviour |
 * |---------------|-----------|
 * | `"url"`   | Loads asynchronously via Coil `AsyncImage` |
 * | `"res"`   | Loads a drawable / mipmap from the app's resources |
 * | `"icon"`  | Resolves a Material icon by name and style |
 * | `"base64"` | Placeholder — not yet fully implemented |
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `source` | object | – | `{ "type": string, "value": string, "style"?: string }` |
 * | `contentDescription` | string | `null` | Accessibility description |
 * | `scaleType` | string | `"fitCenter"` | One of [KScaleType] constants |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"image"`.
 *
 * @see KImageSource
 * @see KScaleType
 */

@Composable
internal fun RenderImage(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val context = LocalContext.current
    val modifier = parseModifier(props)
    val contentDescription = props["contentDescription"]?.jsonPrimitive?.contentOrNull
    val scaleType = props["scaleType"]?.jsonPrimitive?.contentOrNull ?: KScaleType.FitCenter

    val contentScale = when (scaleType) {
        KScaleType.CenterCrop -> ContentScale.Crop
        KScaleType.FitCenter -> ContentScale.Fit
        KScaleType.FillBounds -> ContentScale.FillBounds
        KScaleType.Inside -> ContentScale.Inside
        KScaleType.FillWidth -> ContentScale.FillWidth
        KScaleType.FillHeight -> ContentScale.FillHeight
        else -> ContentScale.Fit
    }

    val source = props["source"]?.jsonObject
    if (source != null) {
        val sourceType = source["type"]?.jsonPrimitive?.contentOrNull
        val value = source["value"]?.jsonPrimitive?.contentOrNull

        when (sourceType) {
            "icon" -> {
                if (value != null) {
                    val style = source["style"]?.jsonPrimitive?.contentOrNull ?: KIcons.STYLE_FILLED
                    val imageVector = resolveIcon(value, style)
                    if (imageVector != null) {
                        Icon(
                            imageVector = imageVector,
                            contentDescription = contentDescription,
                            modifier = modifier,
                            tint = LocalContentColor.current
                        )
                    } else {
                        Box(modifier = modifier, contentAlignment = Alignment.Center) {
                            Text(text = "Icon not found: $value", color = Color.Gray)
                        }
                    }
                }
            }
            "res" -> {
                if (value != null) {
                    val resId = context.resources.getIdentifier(value, "drawable", context.packageName)
                        .takeIf { it != 0 }
                        ?: context.resources.getIdentifier(value, "mipmap", context.packageName)
                    if (resId != 0) {
                        Image(painter = painterResource(id = resId), contentDescription = contentDescription,
                            modifier = modifier, contentScale = contentScale)
                    } else {
                        Box(modifier = modifier, contentAlignment = Alignment.Center) {
                            Text(text = "Image not found: $value", color = Color.Gray)
                        }
                    }
                }
            }
            "url" -> {
                if (value != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(value).crossfade(true).build(),
                        contentDescription = contentDescription,
                        modifier = modifier,
                        contentScale = contentScale,
                        placeholder = painterResource(AndroidR.drawable.ic_menu_gallery),
                        error = painterResource(AndroidR.drawable.ic_menu_close_clear_cancel)
                    )
                } else {
                    Box(modifier = modifier, contentAlignment = Alignment.Center) {
                        Text(text = "No URL provided", color = Color.Gray)
                    }
                }
            }
            "base64" -> {
                Box(modifier = modifier.background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Text(text = "Base64 Image", color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            }
            else -> {
                Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    Text(text = "Unknown image source", color = Color.Red)
                }
            }
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "No image source", color = Color.Gray)
        }
    }
}

// ─── Icon ─────────────────────────────────────────────────────────

/**
 * Renders a Material icon from a [UIComponent] node.
 *
 * The icon is resolved by name and style through [resolveIcon]. If the icon
 * name cannot be found in the built-in Material icon set, a small warning
 * placeholder is shown instead.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `icon` | string | `""` | Icon name (e.g. `"home"`, `"settings"`) |
 * | `style` | string | `"filled"` | Icon style — see [KIcons] constants |
 * | `size` | int | – | Optional explicit size in dp |
 * | `color` | string | inherited | Tint colour (supports `@theme/` refs) |
 * | `contentDescription` | string | `null` | Accessibility label |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"icon"`.
 *
 * @see KIcons
 * @see resolveIcon
 */

@Composable
internal fun RenderIcon(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val iconName = props["icon"]?.jsonPrimitive?.contentOrNull ?: ""
    val style = props["style"]?.jsonPrimitive?.contentOrNull ?: KIcons.STYLE_FILLED
    val size = props["size"]?.jsonPrimitive?.intOrNull
    val color = resolveKetoyColorOrNull(props["color"]?.jsonPrimitive?.contentOrNull)
    val contentDescription = props["contentDescription"]?.jsonPrimitive?.contentOrNull
    val modifier = parseModifier(props)

    val imageVector = resolveIcon(iconName, style)

    if (imageVector != null) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = if (size != null) modifier.size(size.dp) else modifier,
            tint = color ?: LocalContentColor.current
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "⚠ Icon: $iconName", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

// ─── IconButton ───────────────────────────────────────────────────

/**
 * Renders a Material 3 `IconButton` from a [UIComponent] node.
 *
 * Resolves the named icon via [resolveIcon], applies colour and sizing
 * overrides, and also renders any additional [UIComponent.children] inside
 * the button's content slot.
 *
 * ### Supported props
 * | Prop | Type | Default | Description |
 * |------|------|---------|-------------|
 * | `icon` | string | `""` | Icon name |
 * | `iconStyle` | string | `"filled"` | Icon style — see [KIcons] |
 * | `onClick` | string/object | – | Action resolved by [OnClickResolver] |
 * | `enabled` | boolean | `true` | Whether the button is interactive |
 * | `iconSize` | int | – | Explicit icon size in dp |
 * | `iconColor` | string | inherited | Icon tint colour |
 * | `containerColor` | string | transparent | Button background colour |
 * | `contentColor` | string | inherited | Default content colour |
 * | `disabledContainerColor` | string | transparent | Background when disabled |
 * | `disabledContentColor` | string | inherited @ 38% | Content colour when disabled |
 * | `contentDescription` | string | `null` | Accessibility label |
 * | `modifier` | object | – | Standard Ketoy modifier JSON |
 *
 * @param component The [UIComponent] whose `type` is `"iconbutton"`.
 *
 * @see RenderIcon
 * @see OnClickResolver
 */

@Composable
internal fun RenderIconButton(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val iconName = props["icon"]?.jsonPrimitive?.contentOrNull ?: ""
    val style = props["iconStyle"]?.jsonPrimitive?.contentOrNull ?: KIcons.STYLE_FILLED
    val onClickAction = rememberOnClick(props)
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val iconSize = props["iconSize"]?.jsonPrimitive?.intOrNull
    val iconColor = resolveKetoyColorOrNull(props["iconColor"]?.jsonPrimitive?.contentOrNull)
    val containerColor = resolveKetoyColorOrNull(props["containerColor"]?.jsonPrimitive?.contentOrNull)
    val contentColor = resolveKetoyColorOrNull(props["contentColor"]?.jsonPrimitive?.contentOrNull)
    val disabledContainerColor = resolveKetoyColorOrNull(props["disabledContainerColor"]?.jsonPrimitive?.contentOrNull)
    val disabledContentColor = resolveKetoyColorOrNull(props["disabledContentColor"]?.jsonPrimitive?.contentOrNull)
    val contentDescription = props["contentDescription"]?.jsonPrimitive?.contentOrNull

    val colors = IconButtonDefaults.iconButtonColors(
        containerColor = containerColor ?: Color.Transparent,
        contentColor = contentColor ?: LocalContentColor.current,
        disabledContainerColor = disabledContainerColor ?: Color.Transparent,
        disabledContentColor = disabledContentColor ?: LocalContentColor.current.copy(alpha = 0.38f)
    )

    IconButton(
        onClick = { onClickAction?.invoke() },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        if (iconName.isNotEmpty()) {
            val imageVector = resolveIcon(iconName, style)
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    modifier = if (iconSize != null) Modifier.size(iconSize.dp) else Modifier,
                    tint = iconColor ?: LocalContentColor.current
                )
            } else {
                Text(text = "⚠", fontSize = 14.sp)
            }
        }
        // Also render any children (custom content inside IconButton)
        component.children?.forEach { child -> RenderComponent(child) }
    }
}

// ─── Private helpers ──────────────────────────────────────────────
/**
 * Applies JSON-specified padding to a [Modifier].
 *
 * Supports multiple formats:
 * - A plain integer — uniform padding on all sides.
 * - An object with `all`, `horizontal` / `vertical`, or per-edge keys
 *   (`top`, `bottom`, `start`, `end`).
 *
 * @param base    The existing modifier to extend.
 * @param element The JSON element describing the padding.
 * @return The modifier with padding applied.
 */
private fun applyJsonPadding(base: Modifier, element: JsonElement): Modifier {
    return when (element) {
        is JsonPrimitive -> element.intOrNull?.let { base.padding(it.dp) } ?: base
        is JsonObject -> {
            val all = element["all"]?.jsonPrimitive?.intOrNull?.dp
            val h = element["horizontal"]?.jsonPrimitive?.intOrNull?.dp
            val v = element["vertical"]?.jsonPrimitive?.intOrNull?.dp
            val top = element["top"]?.jsonPrimitive?.intOrNull?.dp
            val bottom = element["bottom"]?.jsonPrimitive?.intOrNull?.dp
            val start = element["start"]?.jsonPrimitive?.intOrNull?.dp
            val end = element["end"]?.jsonPrimitive?.intOrNull?.dp
            when {
                all != null -> base.padding(all)
                h != null && v != null -> base.padding(horizontal = h, vertical = v)
                h != null -> base.padding(horizontal = h)
                v != null -> base.padding(vertical = v)
                top != null || bottom != null || start != null || end != null ->
                    base.padding(top = top ?: 0.dp, bottom = bottom ?: 0.dp, start = start ?: 0.dp, end = end ?: 0.dp)
                else -> base
            }
        }
        else -> base
    }
}
