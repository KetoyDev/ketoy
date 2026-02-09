package com.developerstring.ketoy.screens

import com.developerstring.ketoy.dsl.*
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.util.*

/**
 * Shared colour tokens used across all screen builders.
 */
object AppColors {
    const val primary = "#6750A4"
    const val onPrimary = "#FFFFFF"
    const val onSurface = "#1D1B20"
    const val onSurfaceVariant = "#49454F"
    const val outline = "#79747E"
    const val primaryContainer = "#EADDFF"
    const val secondaryContainer = "#E8DEF8"
    const val tertiaryContainer = "#FFD8E4"
    const val errorContainer = "#F9DEDC"
    const val onErrorContainer = "#410E0B"
    const val surfaceContainerLow = "#F7F2FA"
    const val green = "#1B7D46"
    const val greenContainer = "#A8F5C4"
    const val red = "#BA1A1A"
}

// ═══════════════════════════════════════════════════════════════
//  REUSABLE DSL HELPERS shared by all screens
// ═══════════════════════════════════════════════════════════════

fun KUniversalScope.quickAction(
    icon: String, label: String, bgColor: String, iconColor: String,
    onClick: () -> Unit
) {
    KColumn(verticalArrangement = "spacedBy_8", horizontalAlignment = KAlignments.CenterHorizontally) {
        KCard(modifier = kModifier(size = 64), containerColor = bgColor, shape = KShapes.Rounded20, elevation = 0) {
            KBox(modifier = kModifier(fillMaxSize = 1f), contentAlignment = KAlignments.Center) {
                KIconButton(icon = icon, iconColor = iconColor, iconSize = 28, onClick = onClick) {}
            }
        }
        KText(label, fontSize = 12, fontWeight = KFontWeights.Medium, color = "#49454F")
    }
}

fun KUniversalScope.statCard(
    modifier: KModifier, icon: String, label: String, value: String, trend: String,
    iconBg: String, iconColor: String, trendColor: String
) {
    KCard(modifier = modifier, containerColor = "#FFFFFF", shape = KShapes.Rounded16, elevation = 1) {
        KColumn(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)), verticalArrangement = "spacedBy_10") {
            KRow(modifier = kModifier(fillMaxWidth = 1f), horizontalArrangement = KArrangements.SpaceBetween, verticalAlignment = KAlignments.CenterVertically) {
                KBox(modifier = kModifier(size = 40, background = iconBg, shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                    KIcon(icon = icon, size = 22, color = iconColor)
                }
                KText(trend, fontSize = 12, fontWeight = KFontWeights.Bold, color = trendColor)
            }
            KText(label, fontSize = 12, color = "#49454F")
            KText(value, fontSize = 20, fontWeight = KFontWeights.Bold, color = "#1D1B20")
        }
    }
}

fun KUniversalScope.transactionItem(
    icon: String, iconBg: String, iconColor: String,
    title: String, subtitle: String,
    amount: String, amountColor: String, cardBg: String,
    onClick: () -> Unit
) {
    KCard(modifier = kModifier(fillMaxWidth = 1f), containerColor = cardBg, shape = KShapes.Rounded16, elevation = 0) {
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14), clickable = true),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KRow(horizontalArrangement = "spacedBy_14", verticalAlignment = KAlignments.CenterVertically) {
                KBox(modifier = kModifier(size = 46, background = iconBg, shape = KShapes.Circle), contentAlignment = KAlignments.Center) {
                    KIcon(icon = icon, size = 24, color = iconColor)
                }
                KColumn(verticalArrangement = "spacedBy_2") {
                    KText(title, fontSize = 15, fontWeight = KFontWeights.SemiBold, color = "#1D1B20")
                    KText(subtitle, fontSize = 12, color = "#79747E")
                }
            }
            KColumn(horizontalAlignment = KAlignments.End, verticalArrangement = "spacedBy_2") {
                KText(amount, fontSize = 15, fontWeight = KFontWeights.Bold, color = amountColor)
                KIcon(icon = KIcons.ChevronRight, size = 16, color = "#79747E")
            }
        }
    }
}

fun KUniversalScope.categoryRow(
    icon: String, category: String, amount: String,
    iconBg: String, iconColor: String
) {
    KCard(modifier = kModifier(fillMaxWidth = 1f), containerColor = "#FFFFFF", shape = KShapes.Rounded16, elevation = 0) {
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KRow(horizontalArrangement = "spacedBy_12", verticalAlignment = KAlignments.CenterVertically) {
                KBox(modifier = kModifier(size = 42, background = iconBg, shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                    KIcon(icon = icon, size = 22, color = iconColor)
                }
                KText(category, fontSize = 15, fontWeight = KFontWeights.Medium, color = "#1D1B20")
            }
            KText(amount, fontSize = 15, fontWeight = KFontWeights.Bold, color = "#49454F")
        }
    }
}

fun KUniversalScope.walletCard(
    cardName: String, lastFour: String,
    balance: String, expiry: String,
    gradientColors: List<String>
) {
    KCard(modifier = kModifier(fillMaxWidth = 1f), shape = KShapes.Rounded24, elevation = 6) {
        KColumn(
            modifier = kModifier(
                fillMaxWidth = 1f,
                gradient = KGradients.linear(gradientColors, KGradients.Directions.Diagonal)
            ),
        ) {

            KColumn(
                modifier = kModifier(fillMaxWidth = 1f, padding = KPadding(all = 24),),
                verticalArrangement = "spacedBy_8"
            ) {

                KRow(
                    modifier = kModifier(fillMaxWidth = 1f),
                    horizontalArrangement = KArrangements.SpaceBetween,
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KText(
                        cardName,
                        fontSize = 16,
                        fontWeight = KFontWeights.SemiBold,
                        color = "#FFFFFF"
                    )
                    KIcon(icon = KIcons.CreditCard, size = 28, color = "#FFFFFFBB")
                }
                KSpacer(height = 16)
                KText(lastFour, fontSize = 22, fontWeight = KFontWeights.Bold, color = "#FFFFFF")
                KSpacer(height = 8)
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f),
                    horizontalArrangement = KArrangements.SpaceBetween
                ) {
                    KColumn {
                        KText("Balance", fontSize = 11, color = "#FFFFFFAA")
                        KText(
                            balance,
                            fontSize = 18,
                            fontWeight = KFontWeights.Bold,
                            color = "#FFFFFF"
                        )
                    }
                    KColumn(horizontalAlignment = KAlignments.End) {
                        KText("Expires", fontSize = 11, color = "#FFFFFFAA")
                        KText(
                            expiry,
                            fontSize = 14,
                            fontWeight = KFontWeights.Medium,
                            color = "#FFFFFF"
                        )
                    }
                }
            }
        }
    }
}

fun KUniversalScope.profileItem(
    icon: String, label: String, bgColor: String, iconColor: String,
    onClick: () -> Unit
) {
    KCard(modifier = kModifier(fillMaxWidth = 1f), containerColor = bgColor, shape = KShapes.Rounded16, elevation = 0) {
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14), clickable = true),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KRow(horizontalArrangement = "spacedBy_14", verticalAlignment = KAlignments.CenterVertically) {
                KBox(modifier = kModifier(size = 42, background = "#EADDFF", shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                    KIcon(icon = icon, size = 22, color = iconColor)
                }
                KText(label, fontSize = 15, fontWeight = KFontWeights.Medium, color = "#1D1B20")
            }
            KIcon(icon = KIcons.ChevronRight, size = 20, color = "#79747E")
        }
    }
}
