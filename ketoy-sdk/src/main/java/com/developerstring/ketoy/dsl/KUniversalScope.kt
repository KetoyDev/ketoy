package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.registry.KComponentRegistry
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

abstract class KScope {
    val children = mutableListOf<KNode>()
    fun addChild(node: KNode) { children += node }
}

// ─────────────────────────────────────────────────────────────
//  Universal scope – works for every container
// ─────────────────────────────────────────────────────────────

open class KUniversalScope : KScope() {

    // ── Text ────────────────────────────────────────────

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

    fun KButton(
        modifier: KModifier? = null, onClick: () -> Unit = {},
        enabled: Boolean? = null, containerColor: String? = null,
        contentColor: String? = null, elevation: Int? = null,
        shape: String? = null, content: KUniversalScope.() -> Unit = {}
    ) {
        val actionId = ActionRegistry.register(onClick)
        val scope = KUniversalScope().apply(content)
        addChild(KButtonNode(KButtonProps(modifier, actionId, enabled, containerColor, contentColor, elevation, shape), scope.children))
    }

    // ── Column ──────────────────────────────────────────

    fun KColumn(
        modifier: KModifier? = null, verticalArrangement: String? = null,
        horizontalAlignment: String? = null, content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        addChild(KColumnNode(KColumnProps(modifier, verticalArrangement, horizontalAlignment), scope.children))
    }

    // ── Row ─────────────────────────────────────────────

    fun KRow(
        modifier: KModifier? = null, horizontalArrangement: String? = null,
        verticalAlignment: String? = null, content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        addChild(KRowNode(KRowProps(modifier, horizontalArrangement, verticalAlignment), scope.children))
    }

    // ── Box ─────────────────────────────────────────────

    fun KBox(
        modifier: KModifier? = null, contentAlignment: String? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val scope = KUniversalScope().apply(content)
        addChild(KBoxNode(KBoxProps(modifier, contentAlignment), scope.children))
    }

    // ── LazyColumn ──────────────────────────────────────

    fun KLazyColumn(
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

    fun KLazyRow(
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

    fun KSpacer(modifier: KModifier? = null, width: Int? = null, height: Int? = null) {
        addChild(KSpacerNode(KSpacerProps(modifier, width, height)))
    }

    // ── TextField ───────────────────────────────────────

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
     * If [icon] is provided, it takes precedence as the button's icon.
     */
    fun KIconButton(
        icon: String = "", onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        iconSize: Int? = null, iconColor: String? = null,
        iconStyle: String? = null, containerColor: String? = null,
        contentColor: String? = null, disabledContainerColor: String? = null,
        disabledContentColor: String? = null, contentDescription: String? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val actionId = ActionRegistry.register(onClick)
        val scope = KUniversalScope().apply(content)
        addChild(KIconButtonNode(
            KIconButtonProps(icon, modifier, actionId, enabled, iconSize, iconColor, iconStyle,
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
     */
    fun KIconButton(
        icon: KIconRef, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        iconSize: Int? = null, iconColor: String? = null,
        containerColor: String? = null, contentColor: String? = null,
        disabledContainerColor: String? = null, disabledContentColor: String? = null,
        contentDescription: String? = null,
        content: KUniversalScope.() -> Unit = {}
    ) {
        val actionId = ActionRegistry.register(onClick)
        val scope = KUniversalScope().apply(content)
        addChild(KIconButtonNode(
            KIconButtonProps(icon.name, modifier, actionId, enabled, iconSize, iconColor, icon.style,
                containerColor, contentColor, disabledContainerColor, disabledContentColor, contentDescription),
            scope.children
        ))
    }

    // ── Card ────────────────────────────────────────────

    fun KCard(
        modifier: KModifier? = null, shape: String? = null,
        containerColor: String? = null, contentColor: String? = null,
        elevation: Int? = null, border: KBorder? = null,
        onClick: (() -> Unit)? = null, enabled: Boolean? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val node = KCardNode(KCardProps(
            modifier, shape ?: KShapes.Rounded12, containerColor, contentColor,
            elevation ?: 1, border,
            if (onClick != null) "cardClick" else null, enabled
        ))
        val scope = KUniversalScope().apply(content)
        node.children.addAll(scope.children)
        addChild(node)
    }

    // ── Custom component (vararg) ───────────────────────

    fun KComponent(
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

    // ── Scaffold ────────────────────────────────────────

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

    fun KFloatingActionButton(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        shape: String? = null, containerColor: String? = null,
        contentColor: String? = null, elevation: KFloatingActionButtonElevation? = null,
        interactionSource: KInteractionSource? = null, type: String? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val scope = KUniversalScope().apply(content)
        val actionId = ActionRegistry.register(onClick)
        addChild(KFloatingActionButtonNode(KFloatingActionButtonProps(modifier, actionId, shape ?: KShapes.Circle, containerColor, contentColor, elevation, interactionSource, type ?: KFabType.Regular), scope.children))
    }

    // ── SnackBar ────────────────────────────────────────

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

    fun KModalBottomSheet(
        onDismissRequest: () -> Unit = {}, modifier: KModifier? = null,
        shape: String? = null, containerColor: String? = null,
        contentColor: String? = null, tonalElevation: Int? = null,
        scrimColor: String? = null,
        dragHandle: (KUniversalScope.() -> Unit)? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val actionId = ActionRegistry.register(onDismissRequest)
        val body = KUniversalScope().apply(content)
        val dragNodes = dragHandle?.let { KUniversalScope().apply(it).children }
        addChild(KModalBottomSheetNode(
            KModalBottomSheetProps(modifier, actionId, null, shape, containerColor, contentColor, tonalElevation, scrimColor, dragNodes),
            body.children
        ))
    }

    // ── SnackBarHost ────────────────────────────────────

    fun KSnackBarHost(
        hostState: String? = null, modifier: KModifier? = null,
        snackbar: (KSnackBarScope.() -> Unit)? = null
    ) {
        val content = snackbar?.let { KSnackBarScope().apply(it).children }
        addChild(KSnackBarHostNode(KSnackBarHostProps(hostState, modifier, content)))
    }

    // ── Dynamic / conditional ───────────────────────────

    fun addComponent(component: KNode) { addChild(component) }

    inline fun KIf(condition: Boolean, content: KUniversalScope.() -> Unit) {
        if (condition) content()
    }

    inline fun <T> KForEach(items: Iterable<T>, content: KUniversalScope.(T) -> Unit) {
        items.forEach { content(it) }
    }

    inline fun KRepeat(times: Int, content: KUniversalScope.(Int) -> Unit) {
        repeat(times) { content(it) }
    }

    // ── Data structures ─────────────────────────────────

    fun KDataClass(id: String, className: String, vararg fields: Pair<String, Any>) {
        addChild(KDataClassNode(KDataClassProps(id, className, fields.toMap())))
        fields.forEach { (k, v) -> KetoyVariableRegistry.register(KetoyVariable.Immutable("$id.$k", v)) }
    }

    fun KEnum(
        id: String, enumName: String, values: List<String>,
        selectedValue: String = "", onSelectionChange: (() -> Unit)? = null
    ) {
        val actionId = onSelectionChange?.let { ActionRegistry.register(it) }
        addChild(KEnumNode(KEnumProps(id, enumName, values, selectedValue, actionId)))
        KetoyVariableRegistry.register(KetoyVariable.Mutable("$id.selectedValue", selectedValue))
        KetoyVariableRegistry.register(KetoyVariable.Immutable("$id.values", values))
        KetoyVariableRegistry.register(KetoyVariable.Immutable("$id.enumName", enumName))
    }

    // ── Extension convenience ───────────────────────────

    fun addAny(component: KNode) = addComponent(component)
    fun createText(text: String, color: String? = null) = KText(text = text, color = color)
    fun createButton(text: String, color: String? = null, onClick: () -> Unit = {}) {
        KButton(containerColor = color, onClick = onClick) { KText(text) }
    }
}
