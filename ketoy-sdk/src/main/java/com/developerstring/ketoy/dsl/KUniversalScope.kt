package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.registry.KComponentRegistry
import com.developerstring.ketoy.registry.KetoyFunctionRegistry
import com.developerstring.ketoy.util.KColors
import com.developerstring.ketoy.util.KFabPosition
import com.developerstring.ketoy.util.KFabType
import com.developerstring.ketoy.util.KIconRef
import com.developerstring.ketoy.util.KShapes
import com.developerstring.ketoy.util.KSnackBarDuration
import com.developerstring.ketoy.util.KTopAppBarType

// ─────────────────────────────────────────────────────────────
//  Base scope
// ─────────────────────────────────────────────────────────────

/**
 * Abstract base class for all Ketoy DSL scopes.
 *
 * Every scope in the Ketoy DSL (e.g. [KUniversalScope], [KLazyListScope],
 * [KNavigationScope]) extends this class to maintain a mutable list of
 * [KNode] children that are serialised into JSON for server-driven rendering.
 *
 * You typically do not interact with this class directly; instead, use one
 * of the concrete scope subclasses provided by the DSL.
 *
 * @see KUniversalScope
 */
abstract class KScope {
    /** The mutable list of child nodes accumulated by this scope. */
    val children = mutableListOf<KNode>()
    /** Appends a [node] to this scope’s [children] list. */
    fun addChild(node: KNode) { children += node }
}

// ─────────────────────────────────────────────────────────────
//  Universal scope – works for every container
// ─────────────────────────────────────────────────────────────

/**
 * The primary DSL scope for building Ketoy server-driven UI trees.
 *
 * `KUniversalScope` is the receiver for almost every content lambda in the
 * Ketoy DSL. It exposes builder functions for all built-in components —
 * text, buttons, images, icons, layouts, lazy lists, scaffolds, navigation,
 * cards, text fields, bottom sheets, snackbars, and more.
 *
 * All builder functions append a serialisable [KNode] to the scope’s
 * [children] list, which Ketoy serialises to JSON and renders on the client.
 *
 * ```kotlin
 * KColumn {
 *     KText("Welcome")
 *     KButton(onClick = { }) { KText("Get Started") }
 *     KImage(source = KImageSource.url("https://example.com/hero.png"))
 * }
 * ```
 *
 * @see KScope
 * @see KLazyListScope
 * @see KScaffoldScope
 */
open class KUniversalScope : KScope() {

    // ── Text ────────────────────────────────────────────

    /**
     * Adds a text component to the UI tree.
     *
     * Mirrors Jetpack Compose’s `Text` composable and supports common text
     * styling properties such as font size, weight, colour, alignment, and overflow.
     *
     * ```kotlin
     * KText(
     *     text = "Hello, World!",
     *     fontSize = 20,
     *     fontWeight = "Bold",
     *     color = KColors.Primary
     * )
     * ```
     *
     * @param text           The text content to display.
     * @param modifier       Optional [KModifier] for sizing, padding, background, etc.
     * @param fontSize       Font size in sp.
     * @param fontWeight     Font weight string — e.g. `"Bold"`, `"Normal"`, `"W500"`.
     * @param color          Text colour string (e.g. `KColors.Black`).
     * @param textAlign      Text alignment — e.g. `"Center"`, `"Start"`, `"End"`.
     * @param maxLines       Maximum number of visible lines.
     * @param overflow       Text overflow behaviour — e.g. `"Ellipsis"`, `"Clip"`.
     * @param letterSpacing  Letter spacing in sp.
     * @param lineHeight     Line height in sp.
     */
    fun KText(
        text: String = "", modifier: KModifier? = null,
        fontSize: Int? = null, fontWeight: String? = null,
        color: String? = null, textAlign: String? = null,
        maxLines: Int? = null, overflow: String? = null,
        letterSpacing: Float? = null, lineHeight: Float? = null
    ) {
        addChild(KTextNode(KTextProps(text, modifier, fontSize, fontWeight, color, textAlign, maxLines, overflow, letterSpacing, lineHeight)))
    }

    // ── Button ──────────────────────────────────────────

    /**
     * Adds a Material3 button to the UI tree.
     *
     * The [onClick] callback is registered in [ActionRegistry] and serialised
     * as an action ID. This allows the button tap to be handled both locally
     * and via server-driven updates.
     *
     * ```kotlin
     * KButton(
     *     onClick = { addToCart() },
     *     containerColor = KColors.Primary,
     *     shape = KShapes.Rounded8
     * ) {
     *     KText("Add to Cart")
     * }
     * ```
     *
     * @param modifier       Optional [KModifier].
     * @param onClick        Callback invoked when the button is tapped.
     * @param enabled        Whether the button is enabled.
     * @param containerColor Background colour string.
     * @param contentColor   Foreground colour for the button’s content.
     * @param elevation      Elevation in dp.
     * @param shape          Shape string — e.g. `KShapes.Rounded8`.
     * @param actionId       Optional custom action ID. When set, this ID appears
     *   in the serialised JSON so server-side updates can reference it by name
     *   (e.g. `"onClick": "buy_button"`). When `null` an auto-generated ID is used.
     * @param content        Lambda with [KUniversalScope] receiver for the button’s children.
     */
    fun KButton(
        modifier: KModifier? = null, onClick: () -> Unit = {},
        enabled: Boolean? = null, containerColor: String? = null,
        contentColor: String? = null, elevation: Int? = null,
        shape: String? = null, actionId: String? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        val scope = KUniversalScope().apply(content)
        addChild(KButtonNode(KButtonProps(modifier, resolvedId, enabled, containerColor, contentColor, elevation, shape), scope.children))
    }

    // ── Column ──────────────────────────────────────────

    /**
     * Adds a vertical layout (Column) as a child of the current scope.
     *
     * ```kotlin
     * KColumn(verticalArrangement = "SpaceBetween") {
     *     KText("Top")
     *     KText("Bottom")
     * }
     * ```
     *
     * @param modifier             Optional [KModifier].
     * @param verticalArrangement  Arrangement along the main axis.
     * @param horizontalAlignment  Alignment along the cross axis.
     * @param content              Lambda with [KUniversalScope] receiver for children.
     */
    fun KColumn(
        modifier: KModifier? = null, verticalArrangement: String? = null,
        horizontalAlignment: String? = null, content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        addChild(KColumnNode(KColumnProps(modifier, verticalArrangement, horizontalAlignment), scope.children))
    }

    // ── Row ─────────────────────────────────────────────

    /**
     * Adds a horizontal layout (Row) as a child of the current scope.
     *
     * ```kotlin
     * KRow(horizontalArrangement = "SpaceEvenly") {
     *     KIcon(icon = KIcons.Star)
     *     KText("4.8")
     * }
     * ```
     *
     * @param modifier               Optional [KModifier].
     * @param horizontalArrangement  Arrangement along the main axis.
     * @param verticalAlignment      Alignment along the cross axis.
     * @param content                Lambda with [KUniversalScope] receiver for children.
     */
    fun KRow(
        modifier: KModifier? = null, horizontalArrangement: String? = null,
        verticalAlignment: String? = null, content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        addChild(KRowNode(KRowProps(modifier, horizontalArrangement, verticalAlignment), scope.children))
    }

    // ── Box ─────────────────────────────────────────────

    /**
     * Adds an overlay layout (Box) as a child of the current scope.
     *
     * Children are stacked on top of each other.
     *
     * ```kotlin
     * KBox(contentAlignment = "BottomEnd") {
     *     KImage(source = KImageSource.url("..."))
     *     KIcon(icon = KIcons.PlayArrow, color = KColors.White)
     * }
     * ```
     *
     * @param modifier         Optional [KModifier].
     * @param contentAlignment Alignment for children inside the box.
     * @param content          Lambda with [KUniversalScope] receiver for children.
     */
    fun KBox(
        modifier: KModifier? = null, contentAlignment: String? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        addChild(KBoxNode(KBoxProps(modifier, contentAlignment), scope.children))
    }

    // ── LazyColumn ──────────────────────────────────────
    /**
     * Adds a vertically-scrolling lazy list (LazyColumn) to the UI tree.
     *
     * Only the items visible on screen are composed, making this ideal for
     * long or dynamic data-driven lists.
     *
     * ```kotlin
     * KLazyColumn(contentPadding = KPadding(16)) {
     *     items(products) { product -> KText(product.name) }
     * }
     * ```
     *
     * @param modifier              Optional [KModifier].
     * @param verticalArrangement   Arrangement along the main axis.
     * @param horizontalAlignment   Alignment along the cross axis.
     * @param userScrollEnabled     Whether the user can scroll.
     * @param reverseLayout         Whether items are laid out in reverse.
     * @param contentPadding        Padding around the list content.
     * @param beyondBoundsItemCount Extra items composed beyond the visible bounds.
     * @param content               Lambda with [KLazyListScope] receiver.
     *
     * @see KLazyListScope
     */    fun KLazyColumn(
        modifier: KModifier? = null, verticalArrangement: String? = null,
        horizontalAlignment: String? = null, userScrollEnabled: Boolean? = null,
        reverseLayout: Boolean? = null, contentPadding: KPadding? = null,
        beyondBoundsItemCount: Int? = null,
        content: KLazyListScope.() -> Unit = {}
    ) {
        val scope = KLazyListScope().apply(content)
        addChild(KLazyColumnNode(KLazyColumnProps(modifier, verticalArrangement, horizontalAlignment, userScrollEnabled, reverseLayout, contentPadding, beyondBoundsItemCount), scope.children))
    }

    // ── LazyRow ─────────────────────────────────────────
    /**
     * Adds a horizontally-scrolling lazy list (LazyRow) to the UI tree.
     *
     * Ideal for carousels, chip rows, or any horizontal list of server-driven content.
     *
     * ```kotlin
     * KLazyRow(horizontalArrangement = "spacedBy(8)") {
     *     items(banners) { KImage(source = KImageSource.url(it.url)) }
     * }
     * ```
     *
     * @param modifier              Optional [KModifier].
     * @param horizontalArrangement Arrangement along the main axis.
     * @param verticalAlignment     Alignment along the cross axis.
     * @param userScrollEnabled     Whether the user can scroll.
     * @param reverseLayout         Whether items are laid out in reverse.
     * @param contentPadding        Padding around the list content.
     * @param beyondBoundsItemCount Extra items composed beyond the visible bounds.
     * @param content               Lambda with [KLazyListScope] receiver.
     *
     * @see KLazyListScope
     */    fun KLazyRow(
        modifier: KModifier? = null, horizontalArrangement: String? = null,
        verticalAlignment: String? = null, userScrollEnabled: Boolean? = null,
        reverseLayout: Boolean? = null, contentPadding: KPadding? = null,
        beyondBoundsItemCount: Int? = null,
        content: KLazyListScope.() -> Unit = {}
    ) {
        val scope = KLazyListScope().apply(content)
        addChild(KLazyRowNode(KLazyRowProps(modifier, horizontalArrangement, verticalAlignment, userScrollEnabled, reverseLayout, contentPadding, beyondBoundsItemCount), scope.children))
    }

    // ── Spacer ──────────────────────────────────────────

    /**
     * Adds an empty spacer to the UI tree.
     *
     * Use this to create fixed or flexible gaps between components.
     * When used inside a `KColumn`, [height] controls the gap; inside a
     * `KRow`, [width] controls it. A [KModifier] with weight can create
     * flexible space.
     *
     * ```kotlin
     * KSpacer(height = 16)
     * KSpacer(modifier = KModifier().weight(1f))
     * ```
     *
     * @param modifier Optional [KModifier] (e.g. for `weight` or `fillMaxWidth`).
     * @param width    Fixed width in dp.
     * @param height   Fixed height in dp.
     */
    fun KSpacer(modifier: KModifier? = null, width: Int? = null, height: Int? = null) {
        addChild(KSpacerNode(KSpacerProps(modifier, width, height)))
    }

    // ── TextField ───────────────────────────────────────

    /**
     * Adds a Material3 text field to the UI tree.
     *
     * The [content] lambda uses a [KTextFieldScope] receiver to configure
     * slot-based content such as label, placeholder, icons, prefix, suffix,
     * and supporting text.
     *
     * ```kotlin
     * KTextField(
     *     value = email,
     *     onValueChange = { email = it },
     *     singleLine = true
     * ) {
     *     label { KText("Email") }
     *     leadingIcon { KIcon(icon = KIcons.Email) }
     *     supportingText { KText("Required") }
     * }
     * ```
     *
     * @param value                Current text value.
     * @param onValueChange        Callback invoked when the text changes.
     * @param modifier             Optional [KModifier].
     * @param enabled              Whether the field is editable.
     * @param readOnly             Whether the field is read-only.
     * @param textStyle            Custom [KTextStyle].
     * @param isError              Whether the field is in an error state.
     * @param visualTransformation Visual transformation (e.g. password masking).
     * @param keyboardOptions      Keyboard type, IME action, etc.
     * @param keyboardActions      IME action callbacks.
     * @param singleLine           Whether the field is single-line.
     * @param maxLines             Maximum visible lines.
     * @param minLines             Minimum visible lines.
     * @param interactionSource    Optional [KInteractionSource].
     * @param shape                Shape string — e.g. `KShapes.Rounded8`.
     * @param colors               Custom [KTextFieldColors].
     * @param content              Lambda with [KTextFieldScope] receiver for slot content.
     *
     * @see KTextFieldScope
     */
    fun KTextField(
        value: String = "", onValueChange: (String) -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        readOnly: Boolean? = null, textStyle: KTextStyle? = null,
        isError: Boolean? = null, visualTransformation: KVisualTransformation? = null,
        keyboardOptions: KKeyboardOptions? = null, keyboardActions: KKeyboardActions? = null,
        singleLine: Boolean? = null, maxLines: Int? = null, minLines: Int? = null,
        interactionSource: KInteractionSource? = null, shape: String? = null,
        colors: KTextFieldColors? = null, content: KTextFieldScope.() -> Unit = {}
    ) {
        val actionId = ActionRegistry.registerTextChange(onValueChange)
        val tfScope = KTextFieldScope().apply(content)
        addChild(KTextFieldNode(KTextFieldProps(
            value, actionId, modifier, enabled, readOnly, textStyle,
            tfScope.getLabelContent(), tfScope.getPlaceholderContent(),
            tfScope.getLeadingIconContent(), tfScope.getTrailingIconContent(),
            tfScope.getPrefixContent(), tfScope.getSuffixContent(),
            tfScope.getSupportingTextContent(), isError, visualTransformation,
            keyboardOptions, keyboardActions, singleLine, maxLines, minLines,
            interactionSource, shape, colors
        )))
    }

    // ── Image ───────────────────────────────────────────

    /**
     * Adds an image component to the UI tree.
     *
     * Supports remote URLs, local resources, and asset images via [KImageSource].
     *
     * ```kotlin
     * KImage(
     *     source = KImageSource.url("https://example.com/photo.jpg"),
     *     contentDescription = "Profile photo",
     *     scaleType = KScaleType.Crop
     * )
     * ```
     *
     * @param source             The image source (URL, resource, or asset).
     * @param modifier           Optional [KModifier].
     * @param contentDescription Accessibility description for the image.
     * @param scaleType          How the image is scaled — defaults to [KScaleType.FitCenter].
     */
    fun KImage(
        source: KImageSource, modifier: KModifier? = null,
        contentDescription: String? = null, scaleType: String? = KScaleType.FitCenter
    ) {
        addChild(KImageNode(KImageProps(source, modifier, contentDescription, scaleType)))
    }

    // ── Icon ────────────────────────────────────────────

    /**
     * Renders a Material3 Icon from [KIcons] constants.
     *
     * ```kotlin
     * KIcon(icon = KIcons.Home, size = 24, color = KColors.Black)
     * KIcon(icon = KIcons.Settings, style = KIcons.STYLE_OUTLINED)
     * ```
     *
     * @param icon               Icon name from `KIcons` (e.g. `KIcons.Home`).
     * @param modifier           Optional [KModifier].
     * @param size               Icon size in dp.
     * @param color              Tint colour string.
     * @param style              Icon style override (e.g. `KIcons.STYLE_OUTLINED`).
     * @param contentDescription Accessibility description.
     */
    fun KIcon(
        icon: String, modifier: KModifier? = null,
        size: Int? = null, color: String? = null,
        style: String? = null, contentDescription: String? = null
    ) {
        addChild(KIconNode(KIconProps(icon, modifier, size, color, style, contentDescription)))
    }

    /**
     * Renders a Material3 Icon from a [KIconRef] (style-qualified reference).
     *
     * ```kotlin
     * KIcon(icon = KIcons.Outlined.Home, size = 24)
     * KIcon(icon = KIcons.Rounded.Settings, color = KColors.Red)
     * ```
     *
     * @param icon               Style-qualified icon reference from `KIcons`.
     * @param modifier           Optional [KModifier].
     * @param size               Icon size in dp.
     * @param color              Tint colour string.
     * @param contentDescription Accessibility description.
     */
    fun KIcon(
        icon: KIconRef, modifier: KModifier? = null,
        size: Int? = null, color: String? = null,
        contentDescription: String? = null
    ) {
        addChild(KIconNode(KIconProps(icon.name, modifier, size, color, icon.style, contentDescription)))
    }

    // ── IconButton ──────────────────────────────────────

    /**
     * Renders a Material3 IconButton with a named icon.
     *
     * ```kotlin
     * KIconButton(icon = KIcons.Menu, onClick = { openDrawer() })
     * KIconButton(icon = KIcons.Favorite, iconColor = KColors.Red, onClick = { like() })
     * ```
     *
     * For custom content inside the IconButton, use the [content] lambda.
     * If [icon] is provided, it takes precedence as the button’s icon.
     *
     * @param icon                    Icon name from `KIcons`.
     * @param onClick                 Callback invoked on tap.
     * @param modifier                Optional [KModifier].
     * @param enabled                 Whether the button is enabled.
     * @param iconSize                Icon size in dp.
     * @param iconColor               Icon tint colour.
     * @param iconStyle               Icon style override.
     * @param containerColor          Background colour.
     * @param contentColor            Foreground colour.
     * @param disabledContainerColor  Background when disabled.
     * @param disabledContentColor    Foreground when disabled.
     * @param contentDescription      Accessibility description.
     * @param actionId                Optional custom action ID for server-side referencing.
     * @param content                 Lambda for additional custom content.
     */
    fun KIconButton(
        icon: String = "", onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        iconSize: Int? = null, iconColor: String? = null,
        iconStyle: String? = null, containerColor: String? = null,
        contentColor: String? = null, disabledContainerColor: String? = null,
        disabledContentColor: String? = null, contentDescription: String? = null,
        actionId: String? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        val scope = KUniversalScope().apply(content)
        addChild(KIconButtonNode(
            KIconButtonProps(icon, modifier, resolvedId, enabled, iconSize, iconColor, iconStyle,
                containerColor, contentColor, disabledContainerColor, disabledContentColor, contentDescription),
            scope.children
        ))
    }

    /**
     * Renders a Material3 IconButton using a [KIconRef] (style-qualified reference).
     *
     * ```kotlin
     * KIconButton(icon = KIcons.Outlined.Menu, onClick = { openDrawer() })
     * KIconButton(icon = KIcons.Filled.Favorite, iconColor = KColors.Red, onClick = { like() })
     * ```
     *
     * @param icon                    Style-qualified icon reference from `KIcons`.
     * @param onClick                 Callback invoked on tap.
     * @param modifier                Optional [KModifier].
     * @param enabled                 Whether the button is enabled.
     * @param iconSize                Icon size in dp.
     * @param iconColor               Icon tint colour.
     * @param containerColor          Background colour.
     * @param contentColor            Foreground colour.
     * @param disabledContainerColor  Background when disabled.
     * @param disabledContentColor    Foreground when disabled.
     * @param contentDescription      Accessibility description.
     * @param actionId                Optional custom action ID for server-side referencing.
     * @param content                 Lambda for additional custom content.
     */
    fun KIconButton(
        icon: KIconRef, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        iconSize: Int? = null, iconColor: String? = null,
        containerColor: String? = null, contentColor: String? = null,
        disabledContainerColor: String? = null, disabledContentColor: String? = null,
        contentDescription: String? = null, actionId: String? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        val scope = KUniversalScope().apply(content)
        addChild(KIconButtonNode(
            KIconButtonProps(icon.name, modifier, resolvedId, enabled, iconSize, iconColor, icon.style,
                containerColor, contentColor, disabledContainerColor, disabledContentColor, contentDescription),
            scope.children
        ))
    }

    // ── Card ────────────────────────────────────────────

    /**
     * Adds a Material3 Card to the UI tree.
     *
     * Cards are surface containers with rounded corners, elevation, and an
     * optional click handler. They’re commonly used for list items, tiles,
     * or content grouping in server-driven layouts.
     *
     * ```kotlin
     * KCard(
     *     modifier = KModifier().fillMaxWidth().padding(8),
     *     elevation = 4,
     *     onClick = { openDetail(item.id) }
     * ) {
     *     KRow {
     *         KImage(source = KImageSource.url(item.thumb))
     *         KColumn { KText(item.title); KText(item.subtitle) }
     *     }
     * }
     * ```
     *
     * @param modifier       Optional [KModifier].
     * @param shape          Shape string — defaults to `KShapes.Rounded12`.
     * @param containerColor Background colour.
     * @param contentColor   Foreground colour.
     * @param elevation      Elevation in dp (defaults to `1`).
     * @param border         Optional [KBorder].
     * @param onClick        Optional click callback.
     * @param enabled        Whether the card is enabled (relevant when clickable).
     * @param actionId       Optional custom action ID for the card’s onClick.
     * @param content        Lambda with [KUniversalScope] receiver for the card’s children.
     */
    fun KCard(
        modifier: KModifier? = null, shape: String? = null,
        containerColor: String? = null, contentColor: String? = null,
        elevation: Int? = null, border: KBorder? = null,
        onClick: (() -> Unit)? = null, enabled: Boolean? = null,
        actionId: String? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val resolvedOnClick = if (onClick != null) {
            if (actionId != null) {
                ActionRegistry.registerAction(actionId, onClick); actionId
            } else ActionRegistry.register(onClick)
        } else null
        val node = KCardNode(KCardProps(
            modifier, shape ?: KShapes.Rounded12, containerColor, contentColor,
            elevation ?: 1, border, resolvedOnClick, enabled
        ))
        val scope = KUniversalScope().apply(content)
        node.children.addAll(scope.children)
        addChild(node)
    }

    // ── Custom component (vararg) ───────────────────────
    /**
     * Adds a registered custom component to the UI tree (vararg overload).
     *
     * Custom components are user-defined widgets registered via
     * [KComponentRegistry][com.developerstring.ketoy.registry.KComponentRegistry].
     * Properties are passed as key-value pairs.
     *
     * ```kotlin
     * KComponent(
     *     name = "RatingBar",
     *     "stars" to 4,
     *     "maxStars" to 5
     * )
     * ```
     *
     * @param name             Registered component name.
     * @param modifier         Optional [KModifier].
     * @param properties       Key-value pairs forwarded as component properties.
     * @param version          Component version string (defaults to `"1.0"`).
     * @param requiredImports  List of imports required for the component.
     * @param fallbackComponent Name of a fallback component if this one is unavailable.
     * @param content          Lambda with [KUniversalScope] receiver for child content.
     */    fun KComponent(
        name: String, modifier: KModifier? = null,
        vararg properties: Pair<String, Any>,
        version: String = "1.0", requiredImports: List<String> = emptyList(),
        fallbackComponent: String? = null, content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        val metadata = KComponentRegistry.getMetadata(name)
        addChild(KComponentNode(
            KComponentProps(name, name, mapOf(*properties), emptyMap(), modifier, version, requiredImports, fallbackComponent),
            scope.children, metadata
        ))
    }

    // ── Custom component (map) ──────────────────────────

    /**
     * Adds a registered custom component to the UI tree (map overload).
     *
     * Use this overload when your properties are already in a `Map<String, Any>`.
     * Both [properties] and [props] are merged (with [props] values taking precedence).
     *
     * ```kotlin
     * val attrs = mapOf("stars" to 4, "maxStars" to 5)
     * KComponent(name = "RatingBar", properties = attrs)
     * ```
     *
     * @param name             Registered component name.
     * @param properties       Primary properties map.
     * @param props            Additional properties map (merged with [properties]).
     * @param modifier         Optional [KModifier].
     * @param version          Component version string (defaults to `"1.0"`).
     * @param requiredImports  List of imports required for the component.
     * @param fallbackComponent Name of a fallback component if this one is unavailable.
     * @param content          Lambda with [KUniversalScope] receiver for child content.
     */
    fun KComponent(
        name: String, properties: Map<String, Any> = emptyMap(),
        props: Map<String, Any> = emptyMap(), modifier: KModifier? = null,
        version: String = "1.0", requiredImports: List<String> = emptyList(),
        fallbackComponent: String? = null, content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        val merged = properties + props
        val metadata = KComponentRegistry.getMetadata(name)
        addChild(KComponentNode(
            KComponentProps(name, name, merged, merged, modifier, version, requiredImports, fallbackComponent),
            scope.children, metadata
        ))
    }

    // ── Smart component loader ──────────────────────────

    /**
     * Adds a custom component with **auto-loading** from the component registry.
     *
     * If the component is not yet available and [autoLoad] is `true`, this
     * function attempts to load it from its registered metadata. If loading
     * fails and [showError] is `true`, a red error card is rendered in place
     * of the component so developers can diagnose the issue at a glance.
     *
     * ```kotlin
     * KComponentSmart("AdvancedChart", properties = mapOf("data" to chartData))
     * ```
     *
     * @param name       Registered component name.
     * @param properties Properties map forwarded to the component.
     * @param modifier   Optional [KModifier].
     * @param autoLoad   Whether to attempt auto-loading if the component is missing.
     * @param showError  Whether to render an error card when the component cannot be loaded.
     */
    fun KComponentSmart(
        name: String, properties: Map<String, Any> = emptyMap(),
        modifier: KModifier? = null, autoLoad: Boolean = true, showError: Boolean = true
    ) {
        if (!KComponentRegistry.isAvailable(name) && autoLoad) {
            val metadata = KComponentRegistry.getMetadata(name)
            if (metadata != null) {
                val loaded = KComponentRegistry.loadFromMetadata(metadata)
                if (!loaded && showError) {
                    KCard(modifier = modifier, containerColor = KColors.withAlpha(KColors.Red, 0.1f), border = com.developerstring.ketoy.model.KBorder(1, KColors.Red)) {
                        KText(text = "⚠️ Component '$name' could not be loaded", color = KColors.Red, fontSize = 12)
                    }
                    return
                }
            }
        }
        KComponent(name = name, properties = properties, modifier = modifier)
    }

    // ── Function call (for onClick / server-driven actions) ──

    /**
     * Creates an onClick action that calls a registered [KetoyFunctionRegistry] function.
     *
     * Returns the action ID string for use in onClick props. The function is
     * invoked at render time when the user taps the component.
     *
     * ```kotlin
     * KButton(
     *     onClick = { KetoyFunctionRegistry.call("addToCart", mapOf("id" to "123")) },
     *     actionId = "add_to_cart_btn"
     * ) {
     *     KText("Add to Cart")
     * }
     * ```
     *
     * Or use [KFunctionCall] to generate an onClick that maps directly
     * to a registered function, which serialises to JSON so the server
     * can trigger the same function:
     *
     * ```kotlin
     * val onClickId = KFunctionCall("addToCart", "id" to "SKU-123", "quantity" to 2)
     * ```
     *
     * @param functionName Name of the function registered in [KetoyFunctionRegistry].
     * @param arguments    Key-value pairs passed as function arguments.
     * @return The generated action ID string.
     */
    fun KFunctionCall(
        functionName: String,
        vararg arguments: Pair<String, Any>
    ): String {
        val args = mapOf(*arguments)
        return ActionRegistry.register {
            KetoyFunctionRegistry.call(functionName, args)
        }
    }

    /**
     * Creates an onClick action from a function name + argument map.
     *
     * Overload of [KFunctionCall] that accepts a pre-built `Map` instead of vararg pairs.
     *
     * @param functionName Name of the function registered in [KetoyFunctionRegistry].
     * @param arguments    Argument map passed to the function (defaults to empty).
     * @return The generated action ID string.
     */
    fun KFunctionCall(
        functionName: String,
        arguments: Map<String, Any> = emptyMap()
    ): String {
        return ActionRegistry.register {
            KetoyFunctionRegistry.call(functionName, arguments)
        }
    }

    // ── Scaffold ────────────────────────────────────────

    /**
     * Adds a Material3 Scaffold as a child of the current scope.
     *
     * This is the **in-scope** variant; use the top-level [KScaffold][com.developerstring.ketoy.dsl.KScaffold]
     * function when you need a root-level scaffold.
     *
     * @param modifier                    Optional [KModifier].
     * @param containerColor              Background colour.
     * @param contentColor                Default content colour.
     * @param contentWindowInsets         Window insets for the content area.
     * @param topBar                      Optional top-bar slot ([KScaffoldScope] receiver).
     * @param bottomBar                   Optional bottom-bar slot.
     * @param snackbarHost                Optional snackbar-host slot.
     * @param floatingActionButton        Optional FAB slot.
     * @param floatingActionButtonPosition FAB position (defaults to [KFabPosition.End]).
     * @param content                     Body content ([KUniversalScope] receiver).
     *
     * @see KScaffoldScope
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
    ) {
        val body = KUniversalScope().apply(content)
        val topBarNodes = topBar?.let { KScaffoldScope().apply(it).children }
        val bottomBarNodes = bottomBar?.let { KScaffoldScope().apply(it).children }
        val snackbarNodes = snackbarHost?.let { KScaffoldScope().apply(it).children }
        val fabNodes = floatingActionButton?.let { KScaffoldScope().apply(it).children }
        addChild(KScaffoldNode(
            KScaffoldProps(modifier, topBarNodes, bottomBarNodes, snackbarNodes, fabNodes,
                floatingActionButtonPosition ?: KFabPosition.End, containerColor, contentColor, contentWindowInsets),
            body.children
        ))
    }

    // ── TopAppBar ───────────────────────────────────────

    /**
     * Adds a Material3 TopAppBar to the UI tree.
     *
     * Supports multiple bar types via [type] ([KTopAppBarType]):
     * `Small`, `CenterAligned`, `Medium`, or `Large`.
     *
     * ```kotlin
     * KTopAppBar(
     *     type = KTopAppBarType.CenterAligned,
     *     title = { KText("Dashboard") },
     *     navigationIcon = { KIconButton(icon = KIcons.Menu, onClick = { }) },
     *     actions = { KAppBarAction(onClick = { }) { KIcon(icon = KIcons.Search) } }
     * )
     * ```
     *
     * @param modifier        Optional [KModifier].
     * @param colors          Custom [KTopAppBarColors].
     * @param windowInsets    Window insets for the app bar.
     * @param scrollBehavior  Scroll behaviour configuration.
     * @param type            Bar type — defaults to [KTopAppBarType.Small].
     * @param expandedHeight  Expanded height in dp (for `Medium` / `Large` types).
     * @param title           Title slot ([KAppBarScope] receiver).
     * @param navigationIcon  Navigation-icon slot ([KAppBarScope] receiver).
     * @param actions         Actions slot ([KAppBarScope] receiver).
     *
     * @see KAppBarScope
     */
    fun KTopAppBar(
        modifier: KModifier? = null, colors: KTopAppBarColors? = null,
        windowInsets: KWindowInsets? = null, scrollBehavior: KTopAppBarScrollBehavior? = null,
        type: String? = null, expandedHeight: Int? = null,
        title: (KAppBarScope.() -> Unit)? = null,
        navigationIcon: (KAppBarScope.() -> Unit)? = null,
        actions: (KAppBarScope.() -> Unit)? = null
    ) {
        val t = title?.let { KAppBarScope().apply(it).children }
        val n = navigationIcon?.let { KAppBarScope().apply(it).children }
        val a = actions?.let { KAppBarScope().apply(it).children }
        addChild(KTopAppBarNode(KTopAppBarProps(modifier, t, n, a, windowInsets, colors, scrollBehavior, type ?: KTopAppBarType.Small, expandedHeight)))
    }

    // ── BottomAppBar ────────────────────────────────────

    /**
     * Adds a Material3 BottomAppBar to the UI tree.
     *
     * ```kotlin
     * KBottomAppBar(containerColor = KColors.Surface) {
     *     KIconButton(icon = KIcons.Home, onClick = { })
     *     KSpacer(modifier = KModifier().weight(1f))
     *     KIconButton(icon = KIcons.Settings, onClick = { })
     * }
     * ```
     *
     * @param modifier        Optional [KModifier].
     * @param containerColor  Background colour.
     * @param contentColor    Foreground colour.
     * @param tonalElevation  Tonal elevation in dp.
     * @param contentPadding  Padding inside the bar.
     * @param windowInsets    Window insets.
     * @param scrollBehavior  Scroll behaviour configuration.
     * @param content         Lambda with [KUniversalScope] receiver for bar content.
     */
    fun KBottomAppBar(
        modifier: KModifier? = null, containerColor: String? = null,
        contentColor: String? = null, tonalElevation: Int? = null,
        contentPadding: KPadding? = null, windowInsets: KWindowInsets? = null,
        scrollBehavior: KBottomAppBarScrollBehavior? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        addChild(KBottomAppBarNode(KBottomAppBarProps(modifier, containerColor, contentColor, tonalElevation, contentPadding, windowInsets, scrollBehavior), scope.children))
    }

    // ── NavigationBar ───────────────────────────────────

    /**
     * Adds a Material3 NavigationBar (bottom navigation) to the UI tree.
     *
     * ```kotlin
     * KNavigationBar {
     *     KNavigationBarItem(
     *         selected = true, onClick = { },
     *         icon = { KIcon(icon = KIcons.Home) },
     *         label = { KText("Home") }
     *     )
     * }
     * ```
     *
     * @param modifier       Optional [KModifier].
     * @param containerColor Background colour.
     * @param contentColor   Foreground colour.
     * @param tonalElevation Tonal elevation in dp.
     * @param windowInsets   Window insets.
     * @param content        Lambda with [KNavigationScope] receiver for navigation items.
     *
     * @see KNavigationScope
     */
    fun KNavigationBar(
        modifier: KModifier? = null, containerColor: String? = null,
        contentColor: String? = null, tonalElevation: Int? = null,
        windowInsets: KWindowInsets? = null,
        content: KNavigationScope.() -> Unit
    ) {
        val scope = KNavigationScope().apply(content)
        addChild(KNavigationBarNode(KNavigationBarProps(modifier, containerColor, contentColor, tonalElevation, windowInsets), scope.children))
    }

    // ── NavigationRail ──────────────────────────────────

    /**
     * Adds a Material3 NavigationRail to the UI tree.
     *
     * Navigation rails are ideal for tablet and desktop form factors where
     * a vertical navigation strip sits at the edge of the screen.
     *
     * ```kotlin
     * KNavigationRail(
     *     header = { KFloatingActionButton(onClick = { }) { KIcon(icon = KIcons.Add) } }
     * ) {
     *     KNavigationRailItem(selected = true, onClick = { }, icon = { KIcon(icon = KIcons.Home) })
     * }
     * ```
     *
     * @param modifier       Optional [KModifier].
     * @param containerColor Background colour.
     * @param contentColor   Foreground colour.
     * @param windowInsets   Window insets.
     * @param header         Optional header slot (e.g. a FAB).
     * @param content        Lambda with [KNavigationRailScope] receiver for rail items.
     *
     * @see KNavigationRailScope
     */
    fun KNavigationRail(
        modifier: KModifier? = null, containerColor: String? = null,
        contentColor: String? = null, windowInsets: KWindowInsets? = null,
        header: (KUniversalScope.() -> Unit)? = null,
        content: KNavigationRailScope.() -> Unit
    ) {
        val scope = KNavigationRailScope().apply(content)
        val headerNodes = header?.let { KUniversalScope().apply(it).children }
        addChild(KNavigationRailNode(KNavigationRailProps(modifier, containerColor, contentColor, headerNodes, windowInsets), scope.children))
    }

    // ── FAB ─────────────────────────────────────────────

    /**
     * Adds a Material3 FloatingActionButton to the UI tree.
     *
     * ```kotlin
     * KFloatingActionButton(
     *     onClick = { createNew() },
     *     containerColor = KColors.Primary
     * ) {
     *     KIcon(icon = KIcons.Add, color = KColors.OnPrimary)
     * }
     * ```
     *
     * @param onClick           Callback invoked on tap.
     * @param modifier          Optional [KModifier].
     * @param shape             Shape string — defaults to [KShapes.Circle].
     * @param containerColor    Background colour.
     * @param contentColor      Foreground colour.
     * @param elevation         Custom [KFloatingActionButtonElevation].
     * @param interactionSource Optional [KInteractionSource].
     * @param type              FAB type — defaults to [KFabType.Regular].
     * @param actionId          Optional custom action ID for server-side referencing.
     * @param content           Lambda with [KUniversalScope] receiver for the FAB’s icon/label.
     */
    fun KFloatingActionButton(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        shape: String? = null, containerColor: String? = null,
        contentColor: String? = null, elevation: KFloatingActionButtonElevation? = null,
        interactionSource: KInteractionSource? = null, type: String? = null,
        actionId: String? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val scope = KUniversalScope().apply(content)
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        addChild(KFloatingActionButtonNode(KFloatingActionButtonProps(modifier, resolvedId, shape ?: KShapes.Circle, containerColor, contentColor, elevation, interactionSource, type ?: KFabType.Regular), scope.children))
    }

    // ── SnackBar ────────────────────────────────────────

    /**
     * Adds a Material3 Snackbar to the UI tree.
     *
     * Snackbars display brief messages at the bottom of the screen and can
     * include an action button and a dismiss button.
     *
     * ```kotlin
     * KSnackBar(
     *     message = "Item deleted",
     *     duration = KSnackBarDuration.Long,
     *     action = { KSnackBarAction(onClick = { undo() }) { KText("Undo") } }
     * )
     * ```
     *
     * @param modifier                 Optional [KModifier].
     * @param actionOnNewLine          Whether the action button appears on a separate line.
     * @param shape                    Shape string — defaults to [KShapes.Rounded4].
     * @param containerColor           Background colour.
     * @param contentColor             Text colour.
     * @param actionContentColor       Action button text colour.
     * @param dismissActionContentColor Dismiss button text colour.
     * @param message                  The message to display.
     * @param duration                 Display duration — defaults to [KSnackBarDuration.Short].
     * @param action                   Optional action slot ([KSnackBarScope] receiver).
     * @param dismissAction            Optional dismiss slot ([KSnackBarScope] receiver).
     *
     * @see KSnackBarScope
     */
    fun KSnackBar(
        modifier: KModifier? = null, actionOnNewLine: Boolean? = null,
        shape: String? = null, containerColor: String? = null,
        contentColor: String? = null, actionContentColor: String? = null,
        dismissActionContentColor: String? = null, message: String? = null,
        duration: String? = null,
        action: (KSnackBarScope.() -> Unit)? = null,
        dismissAction: (KSnackBarScope.() -> Unit)? = null
    ) {
        val ac = action?.let { KSnackBarScope().apply(it).children }
        val dc = dismissAction?.let { KSnackBarScope().apply(it).children }
        addChild(KSnackBarNode(KSnackBarProps(modifier, ac, dc, actionOnNewLine, shape ?: KShapes.Rounded4, containerColor, contentColor, actionContentColor, dismissActionContentColor, message, duration ?: KSnackBarDuration.Short)))
    }

    // ── ModalBottomSheet ────────────────────────────────

    /**
     * Adds a Material3 ModalBottomSheet to the UI tree.
     *
     * ```kotlin
     * KModalBottomSheet(
     *     onDismissRequest = { showSheet = false },
     *     containerColor = KColors.Surface
     * ) {
     *     KColumn {
     *         KText("Sheet content")
     *         KButton(onClick = { showSheet = false }) { KText("Close") }
     *     }
     * }
     * ```
     *
     * @param onDismissRequest Callback when the sheet is dismissed (swipe or scrim tap).
     * @param modifier         Optional [KModifier].
     * @param shape            Shape string.
     * @param containerColor   Background colour.
     * @param contentColor     Foreground colour.
     * @param tonalElevation   Tonal elevation in dp.
     * @param scrimColor       Scrim overlay colour.
     * @param actionId         Optional custom action ID for the dismiss callback.
     * @param dragHandle       Optional drag-handle slot ([KUniversalScope] receiver).
     * @param content          Lambda with [KUniversalScope] receiver for sheet body.
     */
    fun KModalBottomSheet(
        onDismissRequest: () -> Unit = {}, modifier: KModifier? = null,
        shape: String? = null, containerColor: String? = null,
        contentColor: String? = null, tonalElevation: Int? = null,
        scrimColor: String? = null, actionId: String? = null,
        dragHandle: (KUniversalScope.() -> Unit)? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onDismissRequest); actionId
        } else ActionRegistry.register(onDismissRequest)
        val body = KUniversalScope().apply(content)
        val dragNodes = dragHandle?.let { KUniversalScope().apply(it).children }
        addChild(KModalBottomSheetNode(
            KModalBottomSheetProps(modifier, resolvedId, null, shape, containerColor, contentColor, tonalElevation, scrimColor, dragNodes),
            body.children
        ))
    }

    // ── SnackBarHost ────────────────────────────────────

    /**
     * Adds a SnackbarHost to the UI tree.
     *
     * A snackbar host manages the display queue of snackbar messages.
     * Typically placed inside a scaffold’s `snackbarHost` slot.
     *
     * @param hostState Host state identifier.
     * @param modifier  Optional [KModifier].
     * @param snackbar  Optional custom snackbar template ([KSnackBarScope] receiver).
     */
    fun KSnackBarHost(
        hostState: String? = null, modifier: KModifier? = null,
        snackbar: (KSnackBarScope.() -> Unit)? = null
    ) {
        val content = snackbar?.let { KSnackBarScope().apply(it).children }
        addChild(KSnackBarHostNode(KSnackBarHostProps(hostState, modifier, content)))
    }

    // ── Dynamic / conditional ───────────────────────────

    /**
     * Programmatically adds a pre-built [KNode] to the current scope.
     *
     * Use this when you have a node created outside the DSL (e.g. from
     * JSON parsing or a helper function) and want to insert it into the tree.
     *
     * @param component The [KNode] to add.
     */
    fun addComponent(component: KNode) { addChild(component) }

    /**
     * Conditionally includes child widgets in the tree.
     *
     * ```kotlin
     * KIf(user.isLoggedIn) {
     *     KText("Welcome, ${user.name}")
     * }
     * ```
     *
     * @param condition When `true`, the [content] block is executed.
     * @param content   Lambda with [KUniversalScope] receiver for conditional children.
     */
    inline fun KIf(condition: Boolean, content: KUniversalScope.() -> Unit) {
        if (condition) content()
    }

    /**
     * Iterates over [items] and adds children for each element.
     *
     * ```kotlin
     * KForEach(tags) { tag -> KText("#$tag") }
     * ```
     *
     * @param T       The element type.
     * @param items   Iterable to loop over.
     * @param content Lambda invoked for each element.
     */
    inline fun <T> KForEach(items: Iterable<T>, content: KUniversalScope.(T) -> Unit) {
        items.forEach { content(it) }
    }

    /**
     * Repeats the [content] block [times] times, passing the zero-based index.
     *
     * ```kotlin
     * KRepeat(3) { index -> KText("Row $index") }
     * ```
     *
     * @param times   Number of repetitions.
     * @param content Lambda invoked for each index.
     */
    inline fun KRepeat(times: Int, content: KUniversalScope.(Int) -> Unit) {
        repeat(times) { content(it) }
    }

    // ── Data structures ─────────────────────────────────

    fun KDataClass(id: String, className: String, vararg fields: Pair<String, Any>) {
        addChild(KDataClassNode(KDataClassProps(id, className, fields.toMap())))
        fields.forEach { (k, v) -> KetoyVariableRegistry.register(KetoyVariable.Immutable("$id.$k", v)) }
    }

    /**
     * Declares a server-driven enum node and registers its values and selection
     * state as variables in [KetoyVariableRegistry].
     *
     * @param id                Unique identifier for this enum instance.
     * @param enumName          Logical enum name.
     * @param values            List of possible enum values.
     * @param selectedValue     The currently selected value.
     * @param onSelectionChange Optional callback when the selection changes.
     * @param actionId          Optional custom action ID for the selection change.
     */
    fun KEnum(
        id: String, enumName: String, values: List<String>,
        selectedValue: String = "", onSelectionChange: (() -> Unit)? = null,
        actionId: String? = null
    ) {
        val resolvedId = onSelectionChange?.let { cb ->
            if (actionId != null) { ActionRegistry.registerAction(actionId, cb); actionId }
            else ActionRegistry.register(cb)
        }
        addChild(KEnumNode(KEnumProps(id, enumName, values, selectedValue, resolvedId)))
        KetoyVariableRegistry.register(KetoyVariable.Mutable("$id.selectedValue", selectedValue))
        KetoyVariableRegistry.register(KetoyVariable.Immutable("$id.values", values))
        KetoyVariableRegistry.register(KetoyVariable.Immutable("$id.enumName", enumName))
    }

    // ── Extension convenience ───────────────────────────

    /** Alias for [addComponent] — adds any pre-built [KNode] to the tree. */
    fun addAny(component: KNode) = addComponent(component)

    /**
     * Convenience helper that calls [KText] with the given [text] and optional [color].
     *
     * @param text  Text content.
     * @param color Optional colour string.
     */
    fun createText(text: String, color: String? = null) = KText(text = text, color = color)

    /**
     * Convenience helper that creates a [KButton] containing a [KText] label.
     *
     * @param text    Button label.
     * @param color   Optional container colour.
     * @param onClick Callback invoked on tap.
     */
    fun createButton(text: String, color: String? = null, onClick: () -> Unit = {}) {
        KButton(containerColor = color, onClick = onClick) { KText(text) }
    }
}
