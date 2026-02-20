/**
 * Scaffold-related DSL scopes for the Ketoy SDUI library.
 *
 * These scopes provide type-safe builders for the various slots of a Material3
 * Scaffold — top/bottom app bars, navigation bars/rails/drawers, FABs, and
 * snackbar hosts — letting you compose server-driven UI that maps directly to
 * Jetpack Compose scaffold components.
 *
 * @see KUniversalScope.KScaffold
 */
package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.model.*

// ─────────────────────────────────────────────────────────────
//  Scaffold-related scopes
// ─────────────────────────────────────────────────────────────

/**
 * Generic scaffold-slot scope that can host any Ketoy component.
 *
 * Extends [KUniversalScope], so every widget builder (e.g. [KText][KUniversalScope.KText],
 * [KTopAppBar][KUniversalScope.KTopAppBar]) is available inside scaffold slot lambdas
 * such as `topBar`, `bottomBar`, `floatingActionButton`, and `snackbarHost`.
 *
 * ```kotlin
 * KScaffold(
 *     topBar = { KTopAppBar(title = { KText("Home") }) },
 *     floatingActionButton = {
 *         KFloatingActionButton(onClick = { }) { KIcon(icon = KIcons.Add) }
 *     }
 * ) { /* body */ }
 * ```
 *
 * @see KUniversalScope.KScaffold
 */
class KScaffoldScope : KUniversalScope()

/**
 * DSL scope for the content slots of `KTopAppBar` and `KBottomAppBar`.
 *
 * Extends [KUniversalScope] and adds [KAppBarAction] and [KIconButton]
 * builders that produce action buttons appropriate for app-bar layouts.
 *
 * ```kotlin
 * KTopAppBar(
 *     title = { KText("Settings") },
 *     navigationIcon = { KIconButton(icon = KIcons.ArrowBack, onClick = { goBack() }) },
 *     actions = {
 *         KAppBarAction(onClick = { search() }) { KIcon(icon = KIcons.Search) }
 *     }
 * )
 * ```
 *
 * @see KUniversalScope.KTopAppBar
 * @see KUniversalScope.KBottomAppBar
 */
class KAppBarScope : KUniversalScope() {

    /**
     * Adds an action button to the app bar’s action row.
     *
     * Wraps a [KAppBarActionNode] and registers the click handler via [ActionRegistry].
     *
     * @param onClick           Callback invoked when the action is tapped.
     * @param modifier          Optional [KModifier].
     * @param enabled           Whether the action is enabled.
     * @param colors            Custom [KIconButtonColors] for the icon button.
     * @param interactionSource Optional [KInteractionSource].
     * @param actionId          Optional custom action ID for server-side referencing.
     * @param content           Lambda to declare the action’s visual content (typically a [KIcon][KUniversalScope.KIcon]).
     */
    fun KAppBarAction(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        enabled: Boolean? = null, colors: KIconButtonColors? = null,
        interactionSource: KInteractionSource? = null, actionId: String? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val scope = KUniversalScope().apply(content)
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        addChild(KAppBarActionNode(
            KAppBarActionProps(resolvedId, modifier, enabled, colors, interactionSource),
            scope.children
        ))
    }

    /**
     * Convenience alias for [KAppBarAction] that delegates all parameters.
     *
     * Use this when you want a straightforward icon button inside an app bar
     * without needing the additional `interactionSource` or `actionId` parameters.
     *
     * @param onClick  Callback invoked when the button is tapped.
     * @param modifier Optional [KModifier].
     * @param enabled  Whether the button is enabled.
     * @param colors   Custom [KIconButtonColors].
     * @param content  Lambda to declare the button’s visual content.
     */
    fun KIconButton(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        enabled: Boolean? = null, colors: KIconButtonColors? = null,
        content: KUniversalScope.() -> Unit
    ) = KAppBarAction(onClick, modifier, enabled, colors, content = content)
}

/**
 * DSL scope for navigation components — [KNavigationBarItem], [KNavigationDrawerItem],
 * and [KCustomNavigationItem].
 *
 * Use this scope inside [KUniversalScope.KNavigationBar] to declare navigation
 * destinations in a type-safe manner.
 *
 * ```kotlin
 * KNavigationBar {
 *     KNavigationBarItem(
 *         selected = currentRoute == "home",
 *         onClick = { navigate("home") },
 *         icon = { KIcon(icon = KIcons.Home) },
 *         label = { KText("Home") }
 *     )
 * }
 * ```
 *
 * @see KUniversalScope.KNavigationBar
 */
class KNavigationScope : KScope() {

    /**
     * Adds a navigation-drawer item to the scope.
     *
     * Renders a Material3 `NavigationDrawerItem` with icon, label, and optional badge.
     *
     * @param selected  Whether this item is currently selected.
     * @param onClick   Callback invoked when the item is tapped.
     * @param modifier  Optional [KModifier].
     * @param enabled   Whether the item is enabled.
     * @param colors    Custom [KNavigationDrawerItemColors].
     * @param shape     Shape string — e.g. `KShapes.Rounded12`.
     * @param actionId  Optional custom action ID for server-side referencing.
     * @param icon      Lambda to declare the item’s icon.
     * @param label     Lambda to declare the item’s label.
     * @param badge     Lambda to declare an optional badge overlay.
     */
    fun KNavigationDrawerItem(
        selected: Boolean, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        colors: KNavigationDrawerItemColors? = null, shape: String? = null,
        actionId: String? = null,
        icon: (KUniversalScope.() -> Unit)? = null,
        label: (KUniversalScope.() -> Unit)? = null,
        badge: (KUniversalScope.() -> Unit)? = null
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        val ic = icon?.let { KUniversalScope().apply(it).children }
        val lc = label?.let { KUniversalScope().apply(it).children }
        val bc = badge?.let { KUniversalScope().apply(it).children }
        addChild(KNavigationDrawerItemNode(
            KNavigationDrawerItemProps(selected, resolvedId, ic, modifier, enabled, lc, bc, colors, shape)
        ))
    }

    /**
     * Adds a Material3 navigation-bar item to the scope.
     *
     * Mirrors `NavigationBarItem` from Jetpack Compose Material3 and supports
     * separate icons for selected / unselected states.
     *
     * ```kotlin
     * KNavigationBarItem(
     *     selected = page == 0,
     *     onClick = { page = 0 },
     *     icon = { KIcon(icon = KIcons.Outlined.Home) },
     *     selectedIcon = { KIcon(icon = KIcons.Filled.Home) },
     *     label = { KText("Home") }
     * )
     * ```
     *
     * @param selected        Whether this item is currently selected.
     * @param onClick         Callback invoked when the item is tapped.
     * @param modifier        Optional [KModifier].
     * @param enabled         Whether the item is enabled.
     * @param alwaysShowLabel Whether the label is always visible (even when unselected).
     * @param colors          Custom [KNavigationBarItemColors].
     * @param actionId        Optional custom action ID for server-side referencing.
     * @param icon            Lambda to declare the unselected icon.
     * @param selectedIcon    Lambda to declare the selected icon.
     * @param label           Lambda to declare the label text.
     */
    fun KNavigationBarItem(
        selected: Boolean, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        alwaysShowLabel: Boolean? = null,
        colors: KNavigationBarItemColors? = null, actionId: String? = null,
        icon: (KUniversalScope.() -> Unit)? = null,
        selectedIcon: (KUniversalScope.() -> Unit)? = null,
        label: (KUniversalScope.() -> Unit)? = null
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        val ic = icon?.let { KUniversalScope().apply(it).children }
        val sic = selectedIcon?.let { KUniversalScope().apply(it).children }
        val lc = label?.let { KUniversalScope().apply(it).children }
        addChild(KNavigationBarItemNode(
            KNavigationBarItemProps(selected, resolvedId, modifier, enabled, alwaysShowLabel, ic, sic, lc, colors)
        ))
    }

    /**
     * Adds a fully-customisable navigation item to the scope.
     *
     * Unlike [KNavigationBarItem], this variant exposes granular colour overrides
     * for container, content, selected state, indicator, and ripple — useful when
     * your design system deviates from stock Material3 theming.
     *
     * @param selected                Whether this item is currently selected.
     * @param onClick                 Callback invoked when the item is tapped.
     * @param modifier                Optional [KModifier].
     * @param enabled                 Whether the item is enabled.
     * @param alwaysShowLabel         Whether the label is always visible.
     * @param containerColor          Background colour in the default state.
     * @param contentColor            Content colour in the default state.
     * @param selectedContainerColor  Background colour when selected.
     * @param selectedContentColor    Content colour when selected.
     * @param indicatorColor          Colour of the selection indicator.
     * @param rippleColor             Colour of the touch ripple effect.
     * @param actionId                Optional custom action ID for server-side referencing.
     * @param icon                    Lambda to declare the unselected icon.
     * @param selectedIcon            Lambda to declare the selected icon.
     * @param label                   Lambda to declare the label text.
     */
    fun KCustomNavigationItem(
        selected: Boolean, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        alwaysShowLabel: Boolean? = null,
        containerColor: String? = null, contentColor: String? = null,
        selectedContainerColor: String? = null, selectedContentColor: String? = null,
        indicatorColor: String? = null, rippleColor: String? = null,
        actionId: String? = null,
        icon: (KUniversalScope.() -> Unit)? = null,
        selectedIcon: (KUniversalScope.() -> Unit)? = null,
        label: (KUniversalScope.() -> Unit)? = null
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        val ic = icon?.let { KUniversalScope().apply(it).children }
        val sic = selectedIcon?.let { KUniversalScope().apply(it).children }
        val lc = label?.let { KUniversalScope().apply(it).children }
        addChild(KCustomNavigationItemNode(
            KCustomNavigationItemProps(selected, resolvedId, ic, sic, modifier, enabled, lc, alwaysShowLabel,
                containerColor, contentColor, selectedContainerColor, selectedContentColor, indicatorColor, rippleColor)
        ))
    }
}

/**
 * DSL scope for snackbar action and dismiss content.
 *
 * Use this scope inside [KUniversalScope.KSnackBar] to define the action
 * button and the optional dismiss button of a Material3 snackbar.
 *
 * ```kotlin
 * KSnackBar(message = "Item deleted") {
 *     KSnackBarAction(onClick = { undo() }) { KText("Undo") }
 *     KSnackBarDismiss(onClick = { dismiss() }) { KIcon(icon = KIcons.Close) }
 * }
 * ```
 *
 * @see KUniversalScope.KSnackBar
 */
class KSnackBarScope : KUniversalScope() {

    /**
     * Defines the **action** button of the snackbar (e.g. “Undo”, “Retry”).
     *
     * @param onClick  Callback invoked when the action is tapped.
     * @param modifier Optional [KModifier].
     * @param content  Lambda to declare the action’s visual content.
     */
    fun KSnackBarAction(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val actionId = ActionRegistry.register(onClick)
        KButton(onClick = { ActionRegistry.get(actionId)?.invoke() }, modifier = modifier, content = content)
    }

    /**
     * Defines the **dismiss** button of the snackbar (typically a close icon).
     *
     * @param onClick  Callback invoked when the dismiss button is tapped.
     * @param modifier Optional [KModifier].
     * @param content  Lambda to declare the dismiss button’s visual content.
     */
    fun KSnackBarDismiss(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val actionId = ActionRegistry.register(onClick)
        KButton(onClick = { ActionRegistry.get(actionId)?.invoke() }, modifier = modifier, content = content)
    }
}

/**
 * DSL scope for navigation-rail items.
 *
 * Use this scope inside [KUniversalScope.KNavigationRail] to declare
 * rail destinations for tablet or desktop form factors.
 *
 * ```kotlin
 * KNavigationRail {
 *     KNavigationRailItem(
 *         selected = true,
 *         onClick = { },
 *         icon = { KIcon(icon = KIcons.Home) },
 *         label = { KText("Home") }
 *     )
 * }
 * ```
 *
 * @see KUniversalScope.KNavigationRail
 */
class KNavigationRailScope : KScope() {

    /**
     * Adds a navigation-rail item to the scope.
     *
     * Mirrors Material3’s `NavigationRailItem` and supports separate icons
     * for selected / unselected states.
     *
     * @param selected        Whether this item is currently selected.
     * @param onClick         Callback invoked when the item is tapped.
     * @param modifier        Optional [KModifier].
     * @param enabled         Whether the item is enabled.
     * @param alwaysShowLabel Whether the label is always visible.
     * @param actionId        Optional custom action ID for server-side referencing.
     * @param icon            Lambda to declare the unselected icon.
     * @param selectedIcon    Lambda to declare the selected icon.
     * @param label           Lambda to declare the label text.
     */
    fun KNavigationRailItem(
        selected: Boolean, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        alwaysShowLabel: Boolean? = null, actionId: String? = null,
        icon: (KUniversalScope.() -> Unit)? = null,
        selectedIcon: (KUniversalScope.() -> Unit)? = null,
        label: (KUniversalScope.() -> Unit)? = null
    ) {
        val resolvedId = if (actionId != null) {
            ActionRegistry.registerAction(actionId, onClick); actionId
        } else ActionRegistry.register(onClick)
        val ic = icon?.let { KUniversalScope().apply(it).children }
        val sic = selectedIcon?.let { KUniversalScope().apply(it).children }
        val lc = label?.let { KUniversalScope().apply(it).children }
        addChild(KNavigationRailItemNode(
            KNavigationRailItemProps(selected, resolvedId, ic, sic, modifier, enabled, lc, alwaysShowLabel)
        ))
    }
}
