package com.developerstring.ketoy_app.screens

import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.util.*

/**
 * Build a KNode tree from a KUniversalScope DSL block.
 * Use this as the entry point for screen builder functions.
 */
fun ketoyRoot(content: KUniversalScope.() -> Unit): com.developerstring.ketoy.model.KNode {
    val scope = KUniversalScope()
    scope.content()
    // If no children built, create an empty Box as fallback
    if (scope.children.isEmpty()) {
        scope.KBox {}
    }
    return scope.children.first()
}

/**
 * Shared colour tokens used across all screen builders.
 * Supports both light and dark theme variants.
 */
object AppColors {
    private const val lightPrimary = "#6750A4"
    private const val lightOnPrimary = "#FFFFFF"
    private const val lightOnSurface = "#1D1B20"
    private const val lightOnSurfaceVariant = "#49454F"
    private const val lightOutline = "#79747E"
    private const val lightPrimaryContainer = "#EADDFF"
    private const val lightSecondaryContainer = "#E8DEF8"
    private const val lightTertiaryContainer = "#FFD8E4"
    private const val lightErrorContainer = "#F9DEDC"
    private const val lightOnErrorContainer = "#410E0B"
    private const val lightSurfaceContainerLow = "#F7F2FA"
    private const val lightCardBg = "#FFFFFF"
    private const val lightGreen = "#1B7D46"
    private const val lightGreenContainer = "#A8F5C4"
    private const val lightRed = "#BA1A1A"
    private const val lightOnSecondaryContainer = "#4A4458"
    private const val lightOnTertiaryContainer = "#7D5260"

    private const val darkPrimary = "#D0BCFF"
    private const val darkOnPrimary = "#381E72"
    private const val darkOnSurface = "#E6E0E9"
    private const val darkOnSurfaceVariant = "#CAC4D0"
    private const val darkOutline = "#938F99"
    private const val darkPrimaryContainer = "#4F378B"
    private const val darkSecondaryContainer = "#4A4458"
    private const val darkTertiaryContainer = "#633B48"
    private const val darkErrorContainer = "#8C1D18"
    private const val darkOnErrorContainer = "#F9DEDC"
    private const val darkSurfaceContainerLow = "#1D1B20"
    private const val darkCardBg = "#2B2930"
    private const val darkGreen = "#6DD58C"
    private const val darkGreenContainer = "#0A5C30"
    private const val darkRed = "#FFB4AB"
    private const val darkOnSecondaryContainer = "#CCC5DC"
    private const val darkOnTertiaryContainer = "#EFB8C8"

    fun primary(d: Boolean) = if (d) darkPrimary else lightPrimary
    fun onPrimary(d: Boolean) = if (d) darkOnPrimary else lightOnPrimary
    fun onSurface(d: Boolean) = if (d) darkOnSurface else lightOnSurface
    fun onSurfaceVariant(d: Boolean) = if (d) darkOnSurfaceVariant else lightOnSurfaceVariant
    fun outline(d: Boolean) = if (d) darkOutline else lightOutline
    fun primaryContainer(d: Boolean) = if (d) darkPrimaryContainer else lightPrimaryContainer
    fun secondaryContainer(d: Boolean) = if (d) darkSecondaryContainer else lightSecondaryContainer
    fun tertiaryContainer(d: Boolean) = if (d) darkTertiaryContainer else lightTertiaryContainer
    fun errorContainer(d: Boolean) = if (d) darkErrorContainer else lightErrorContainer
    fun onErrorContainer(d: Boolean) = if (d) darkOnErrorContainer else lightOnErrorContainer
    fun surfaceContainerLow(d: Boolean) = if (d) darkSurfaceContainerLow else lightSurfaceContainerLow
    fun cardBg(d: Boolean) = if (d) darkCardBg else lightCardBg
    fun green(d: Boolean) = if (d) darkGreen else lightGreen
    fun greenContainer(d: Boolean) = if (d) darkGreenContainer else lightGreenContainer
    fun red(d: Boolean) = if (d) darkRed else lightRed
    fun onSecondaryContainer(d: Boolean) = if (d) darkOnSecondaryContainer else lightOnSecondaryContainer
    fun onTertiaryContainer(d: Boolean) = if (d) darkOnTertiaryContainer else lightOnTertiaryContainer
}

// ═══════════════════════════════════════════════════════════════
//  Reusable DSL helpers
// ═══════════════════════════════════════════════════════════════

fun com.developerstring.ketoy.dsl.KUniversalScope.quickAction(
    icon: String, label: String, bgColor: String, iconColor: String,
    isDark: Boolean = false,
    actionId: String? = null,
    onClick: () -> Unit
) {
    KColumn(verticalArrangement = KArrangements.spacedBy(8), horizontalAlignment = KAlignments.CenterHorizontally) {
        KCard(modifier = kModifier(size = 64), containerColor = bgColor, shape = KShapes.Rounded20, elevation = 0) {
            KBox(modifier = kModifier(fillMaxSize = 1f), contentAlignment = KAlignments.Center) {
                KIconButton(icon = icon, iconColor = iconColor, iconSize = 28, onClick = onClick, actionId = actionId) {}
            }
        }
        KText(label, fontSize = 12, fontWeight = KFontWeights.Medium, color = AppColors.onSurfaceVariant(isDark))
    }
}

fun com.developerstring.ketoy.dsl.KUniversalScope.statCard(
    modifier: com.developerstring.ketoy.model.KModifier, icon: String, label: String, value: String, trend: String,
    iconBg: String, iconColor: String, trendColor: String,
    isDark: Boolean = false
) {
    KCard(modifier = modifier, containerColor = AppColors.cardBg(isDark), shape = KShapes.Rounded16, elevation = 1) {
        KColumn(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)), verticalArrangement = KArrangements.spacedBy(10)) {
            KRow(modifier = kModifier(fillMaxWidth = 1f), horizontalArrangement = KArrangements.SpaceBetween, verticalAlignment = KAlignments.CenterVertically) {
                KBox(modifier = kModifier(size = 40, background = iconBg, shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                    KIcon(icon = icon, size = 22, color = iconColor)
                }
                KText(trend, fontSize = 12, fontWeight = KFontWeights.Bold, color = trendColor)
            }
            KText(label, fontSize = 12, color = AppColors.onSurfaceVariant(isDark))
            KText(value, fontSize = 20, fontWeight = KFontWeights.Bold, color = AppColors.onSurface(isDark))
        }
    }
}

fun com.developerstring.ketoy.dsl.KUniversalScope.walletCard(
    cardName: String, lastFour: String,
    balance: String, expiry: String,
    gradientColors: List<String>,
    isDark: Boolean = false
) {
    KCard(modifier = kModifier(fillMaxWidth = 1f), shape = KShapes.Rounded24, elevation = 6) {
        KColumn(modifier = kModifier(fillMaxWidth = 1f, gradient = KGradients.linear(gradientColors, KGradients.Directions.Diagonal))) {
            KColumn(modifier = kModifier(fillMaxWidth = 1f, padding = com.developerstring.ketoy.model.KPadding(all = 24)), verticalArrangement = KArrangements.spacedBy(8)) {
                KRow(modifier = kModifier(fillMaxWidth = 1f), horizontalArrangement = KArrangements.SpaceBetween, verticalAlignment = KAlignments.CenterVertically) {
                    KText(cardName, fontSize = 16, fontWeight = KFontWeights.SemiBold, color = "#FFFFFF")
                    KIcon(icon = KIcons.CreditCard, size = 28, color = "#FFFFFFBB")
                }
                KSpacer(height = 16)
                KText(lastFour, fontSize = 22, fontWeight = KFontWeights.Bold, color = "#FFFFFF")
                KSpacer(height = 8)
                KRow(modifier = kModifier(fillMaxWidth = 1f), horizontalArrangement = KArrangements.SpaceBetween) {
                    KColumn {
                        KText("Balance", fontSize = 11, color = "#FFFFFFAA")
                        KText(balance, fontSize = 18, fontWeight = KFontWeights.Bold, color = "#FFFFFF")
                    }
                    KColumn(horizontalAlignment = KAlignments.End) {
                        KText("Expires", fontSize = 11, color = "#FFFFFFAA")
                        KText(expiry, fontSize = 14, fontWeight = KFontWeights.Medium, color = "#FFFFFF")
                    }
                }
            }
        }
    }
}

fun com.developerstring.ketoy.dsl.KUniversalScope.profileItem(
    icon: String, label: String, bgColor: String, iconColor: String,
    isDark: Boolean = false,
    actionId: String? = null,
    onClick: () -> Unit
) {
    KCard(modifier = kModifier(fillMaxWidth = 1f), containerColor = bgColor, shape = KShapes.Rounded16, elevation = 0,
        onClick = onClick, actionId = actionId) {
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KRow(horizontalArrangement = KArrangements.spacedBy(14), verticalAlignment = KAlignments.CenterVertically) {
                KBox(modifier = kModifier(size = 42, background = AppColors.primaryContainer(isDark), shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                    KIcon(icon = icon, size = 22, color = iconColor)
                }
                KText(label, fontSize = 15, fontWeight = KFontWeights.Medium, color = AppColors.onSurface(isDark))
            }
            KIcon(icon = KIcons.ChevronRight, size = 20, color = AppColors.outline(isDark))
        }
    }
}

fun com.developerstring.ketoy.dsl.KUniversalScope.categoryRow(
    icon: String, category: String, amount: String,
    iconBg: String, iconColor: String,
    isDark: Boolean = false
) {
    KCard(modifier = kModifier(fillMaxWidth = 1f), containerColor = AppColors.cardBg(isDark), shape = KShapes.Rounded16, elevation = 0) {
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14)),
            horizontalArrangement = KArrangements.SpaceBetween, verticalAlignment = KAlignments.CenterVertically
        ) {
            KRow(horizontalArrangement = KArrangements.spacedBy(12), verticalAlignment = KAlignments.CenterVertically) {
                KBox(modifier = kModifier(size = 42, background = iconBg, shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                    KIcon(icon = icon, size = 22, color = iconColor)
                }
                KText(category, fontSize = 15, fontWeight = KFontWeights.Medium, color = AppColors.onSurface(isDark))
            }
            KText(amount, fontSize = 15, fontWeight = KFontWeights.Bold, color = AppColors.onSurfaceVariant(isDark))
        }
    }
}
