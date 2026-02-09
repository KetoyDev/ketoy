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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.KImageSource
import com.developerstring.ketoy.model.KScaleType
import com.developerstring.ketoy.parser.*
import com.developerstring.ketoy.util.KIcons
import com.developerstring.ketoy.util.resolveIcon
import kotlinx.serialization.json.*

// ─── Text ─────────────────────────────────────────────────────────

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
        else -> FontWeight.Normal
    }
    val color = parseColor(props["color"]?.jsonPrimitive?.content)
    val textAlign = when (props["textAlign"]?.jsonPrimitive?.content) {
        "center" -> TextAlign.Center
        "start" -> TextAlign.Start
        "end" -> TextAlign.End
        else -> TextAlign.Start
    }
    val modifier = parseModifier(props)

    Text(
        text = resolvedText,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}

// ─── Button ───────────────────────────────────────────────────────

@Composable
internal fun RenderButton(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val modifierProps = props["modifier"]?.jsonObject
    val onClickActionId = props["onClick"]?.jsonPrimitive?.content

    val hasCustomBackground = modifierProps?.get("background")?.let { el ->
        if (el is JsonPrimitive) {
            val bg = el.content
            bg != "transparent" && bg.isNotEmpty()
        } else false
    } ?: false

    if (hasCustomBackground) {
        val modifier = parseModifier(props)
        Box(
            modifier = modifier.clickable {
                onClickActionId?.let { ActionRegistry.get(it)?.invoke() }
            },
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

        val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
        val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) }

        Button(
            onClick = { onClickActionId?.let { ActionRegistry.get(it)?.invoke() } },
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

@Composable
internal fun RenderCard(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val elevation = props["elevation"]?.jsonPrimitive?.intOrNull ?: 1
    val shape = props["shape"]?.jsonPrimitive?.contentOrNull?.let { parseShape(it) } ?: RoundedCornerShape(12.dp)
    val border = props["border"]?.jsonObject?.let { borderObj ->
        val w = borderObj["width"]?.jsonPrimitive?.intOrNull ?: 1
        val c = borderObj["color"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) } ?: Color.Gray
        BorderStroke(w.dp, c)
    }
    val onClick = props["onClick"]?.jsonPrimitive?.contentOrNull
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

    if (onClick != null && enabled) {
        Card(onClick = { }, modifier = modifier, enabled = enabled, shape = shape,
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

@Composable
internal fun RenderIcon(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val iconName = props["icon"]?.jsonPrimitive?.contentOrNull ?: ""
    val style = props["style"]?.jsonPrimitive?.contentOrNull ?: KIcons.STYLE_FILLED
    val size = props["size"]?.jsonPrimitive?.intOrNull
    val color = props["color"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
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

@Composable
internal fun RenderIconButton(component: UIComponent) {
    val props = component.props ?: JsonObject(emptyMap())
    val iconName = props["icon"]?.jsonPrimitive?.contentOrNull ?: ""
    val style = props["iconStyle"]?.jsonPrimitive?.contentOrNull ?: KIcons.STYLE_FILLED
    val onClick = props["onClick"]?.jsonPrimitive?.contentOrNull
    val modifier = parseModifier(props)
    val enabled = props["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
    val iconSize = props["iconSize"]?.jsonPrimitive?.intOrNull
    val iconColor = props["iconColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val containerColor = props["containerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val contentColor = props["contentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val disabledContainerColor = props["disabledContainerColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val disabledContentColor = props["disabledContentColor"]?.jsonPrimitive?.contentOrNull?.let { parseColor(it) }
    val contentDescription = props["contentDescription"]?.jsonPrimitive?.contentOrNull

    val colors = IconButtonDefaults.iconButtonColors(
        containerColor = containerColor ?: Color.Transparent,
        contentColor = contentColor ?: LocalContentColor.current,
        disabledContainerColor = disabledContainerColor ?: Color.Transparent,
        disabledContentColor = disabledContentColor ?: LocalContentColor.current.copy(alpha = 0.38f)
    )

    IconButton(
        onClick = { onClick?.let { ActionRegistry.get(it)?.invoke() } },
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
