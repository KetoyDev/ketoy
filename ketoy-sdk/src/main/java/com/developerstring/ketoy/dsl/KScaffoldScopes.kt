package com.developerstring.ketoy.dsl

import com.developerstring.ketoy.core.ActionRegistry
import com.developerstring.ketoy.model.*

// ─────────────────────────────────────────────────────────────
//  Scaffold-related scopes
// ─────────────────────────────────────────────────────────────

/** Generic scaffold-slot scope – can host any component. */
class KScaffoldScope : KUniversalScope()

/** Scope for TopAppBar / BottomAppBar content slots. */
class KAppBarScope : KUniversalScope() {

    fun KAppBarAction(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        enabled: Boolean? = null, colors: KIconButtonColors? = null,
        interactionSource: KInteractionSource? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val scope = KUniversalScope().apply(content)
        val actionId = ActionRegistry.register(onClick)
        addChild(KAppBarActionNode(
            KAppBarActionProps(actionId, modifier, enabled, colors, interactionSource),
            scope.children
        ))
    }

    fun KIconButton(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        enabled: Boolean? = null, colors: KIconButtonColors? = null,
        content: KUniversalScope.() -> Unit
    ) = KAppBarAction(onClick, modifier, enabled, colors, content = content)
}

/** Scope for NavigationBar items. */
class KNavigationScope : KScope() {

    fun KNavigationDrawerItem(
        selected: Boolean, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        colors: KNavigationDrawerItemColors? = null, shape: String? = null,
        icon: (KUniversalScope.() -> Unit)? = null,
        label: (KUniversalScope.() -> Unit)? = null,
        badge: (KUniversalScope.() -> Unit)? = null
    ) {
        val actionId = ActionRegistry.register(onClick)
        val ic = icon?.let { KUniversalScope().apply(it).children }
        val lc = label?.let { KUniversalScope().apply(it).children }
        val bc = badge?.let { KUniversalScope().apply(it).children }
        addChild(KNavigationDrawerItemNode(
            KNavigationDrawerItemProps(selected, actionId, ic, modifier, enabled, lc, bc, colors, shape)
        ))
    }

    fun KCustomNavigationItem(
        selected: Boolean, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        alwaysShowLabel: Boolean? = null,
        containerColor: String? = null, contentColor: String? = null,
        selectedContainerColor: String? = null, selectedContentColor: String? = null,
        indicatorColor: String? = null, rippleColor: String? = null,
        icon: (KUniversalScope.() -> Unit)? = null,
        selectedIcon: (KUniversalScope.() -> Unit)? = null,
        label: (KUniversalScope.() -> Unit)? = null
    ) {
        val actionId = ActionRegistry.register(onClick)
        val ic = icon?.let { KUniversalScope().apply(it).children }
        val sic = selectedIcon?.let { KUniversalScope().apply(it).children }
        val lc = label?.let { KUniversalScope().apply(it).children }
        addChild(KCustomNavigationItemNode(
            KCustomNavigationItemProps(selected, actionId, ic, sic, modifier, enabled, lc, alwaysShowLabel,
                containerColor, contentColor, selectedContainerColor, selectedContentColor, indicatorColor, rippleColor)
        ))
    }
}

/** Scope for SnackBar action / dismiss content. */
class KSnackBarScope : KUniversalScope() {

    fun KSnackBarAction(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val actionId = ActionRegistry.register(onClick)
        KButton(onClick = { ActionRegistry.get(actionId)?.invoke() }, modifier = modifier, content = content)
    }

    fun KSnackBarDismiss(
        onClick: () -> Unit = {}, modifier: KModifier? = null,
        content: KUniversalScope.() -> Unit
    ) {
        val actionId = ActionRegistry.register(onClick)
        KButton(onClick = { ActionRegistry.get(actionId)?.invoke() }, modifier = modifier, content = content)
    }
}

/** Scope for NavigationRail items. */
class KNavigationRailScope : KScope() {

    fun KNavigationRailItem(
        selected: Boolean, onClick: () -> Unit = {},
        modifier: KModifier? = null, enabled: Boolean? = null,
        alwaysShowLabel: Boolean? = null,
        icon: (KUniversalScope.() -> Unit)? = null,
        selectedIcon: (KUniversalScope.() -> Unit)? = null,
        label: (KUniversalScope.() -> Unit)? = null
    ) {
        val actionId = ActionRegistry.register(onClick)
        val ic = icon?.let { KUniversalScope().apply(it).children }
        val sic = selectedIcon?.let { KUniversalScope().apply(it).children }
        val lc = label?.let { KUniversalScope().apply(it).children }
        addChild(KNavigationRailItemNode(
            KNavigationRailItemProps(selected, actionId, ic, sic, modifier, enabled, lc, alwaysShowLabel)
        ))
    }
}
