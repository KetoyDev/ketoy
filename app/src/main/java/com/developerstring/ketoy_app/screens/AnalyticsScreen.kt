package com.developerstring.ketoy_app.screens

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.export.ketoyExport
import com.developerstring.ketoy.screen.KetoyContent
import com.developerstring.ketoy.screen.ProvideKetoyScreen
import com.developerstring.ketoy.util.*

/**
 * Export definition for the Analytics screen.
 */
val analyticsExport = ketoyExport("analytics", displayName = "Analytics", description = "Income, expenses, and spending breakdown") {
    content {
        buildAnalyticsScreen(
            income = KData.analytics("income"),
            expenses = KData.analytics("expenses"),
            savings = KData.analytics("savings"),
        )
    }
}

/**
 * Analytics screen composable — wraps the DSL builder as a `@KScreen`
 * with a single `KetoyContent` child for cloud / hot-reload support.
 */
@Composable
fun AnalyticsScreen(
    income: String,
    expenses: String,
    savings: String,
) {
    ProvideKetoyScreen(screenName = "analytics") {
        KetoyContent(
            nodeBuilder = {
                buildAnalyticsScreen(
                    income = income,
                    expenses = expenses,
                    savings = savings,
                )
            }
        )
    }
}

/**
 * Analytics screen DSL builder — returns a [KNode] tree with spending
 * breakdown, category rows, stat cards, and savings overview.
 */
fun buildAnalyticsScreen(
    income: String,
    expenses: String,
    savings: String,
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    KColumn(
        modifier = kModifier(
            fillMaxSize = 1f,
            padding = kPadding(top = 16),
            background = KColors.Background,
        ),
        verticalArrangement = KArrangements.spacedBy(0)
    ) {

        // ── Header ────────────────────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("Analytics", fontSize = 24, fontWeight = KFontWeights.Bold, color = KColors.OnSurface)
            KIconButton(
                icon = KIcons.DateRange,
                iconColor = KColors.OnSurfaceVariant,
                iconSize = 24,
                onClickAction = KFunctionAction("showToast", "message" to "Date filter coming soon"),
                actionId = "analytics_calendar"
            ) {}
        }

        KSpacer(height = 20)

        // ── Overview stat cards ───────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.spacedBy(12)
        ) {
            statCard(
                modifier = kModifier(weight = 1f),
                icon = KIcons.TrendingUp, label = "Income", value = income, trend = "+8.2%",
                iconBg = KColors.SuccessContainer, iconColor = KColors.Success,
                trendColor = KColors.Success
            )
            statCard(
                modifier = kModifier(weight = 1f),
                icon = KIcons.TrendingDown, label = "Expenses", value = expenses, trend = "-3.1%",
                iconBg = KColors.ErrorContainer, iconColor = KColors.Error,
                trendColor = KColors.Error
            )
        }

        KSpacer(height = 12)

        // Savings card
        KBox(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20))) {
            statCard(
                modifier = kModifier(fillMaxWidth = 1f),
                icon = KIcons.Savings, label = "Total Savings", value = savings, trend = "+15.4%",
                iconBg = KColors.PrimaryContainer, iconColor = KColors.Primary,
                trendColor = KColors.Success
            )
        }

        KSpacer(height = 24)

        // ── Spending by Category ──────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("Spending by Category", fontSize = 17, fontWeight = KFontWeights.SemiBold, color = KColors.OnSurface)
            KButton(
                containerColor = "#00000000", elevation = 0,
                onClickAction = KFunctionAction("showToast", "message" to "Month filter"),
                actionId = "analytics_month_filter"
            ) {
                KText("This Month", fontSize = 13, fontWeight = KFontWeights.Medium, color = KColors.Primary)
            }
        }

        KSpacer(height = 12)

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)),
            verticalArrangement = KArrangements.spacedBy(8)
        ) {
            categoryRow(KIcons.ShoppingCart, "Groceries", "\$420.50", KColors.PrimaryContainer, KColors.Primary)
            categoryRow(KIcons.Restaurant, "Dining", "\$185.30", KColors.SecondaryContainer, KColors.OnSecondaryContainer)
            categoryRow(KIcons.DirectionsCar, "Transport", "\$98.00", KColors.TertiaryContainer, KColors.OnTertiaryContainer)
            categoryRow(KIcons.Movie, "Entertainment", "\$65.99", KColors.ErrorContainer, KColors.Error)
            categoryRow(KIcons.LocalHospital, "Health", "\$250.00", KColors.SuccessContainer, KColors.Success)
            categoryRow(KIcons.Settings, "Utilities", "\$135.20", KColors.SurfaceContainerLow, KColors.OnSurfaceVariant)
        }

        KSpacer(height = 24)

        // ── Budget progress hint ──────────────────────────
        KCard(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            containerColor = KColors.PrimaryContainer,
            shape = KShapes.Rounded20,
            elevation = 0
        ) {
            KRow(
                modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)),
                horizontalArrangement = KArrangements.spacedBy(12),
                verticalAlignment = KAlignments.CenterVertically
            ) {
                KIcon(icon = KIcons.BarChart, size = 28, color = KColors.Primary)
                KColumn(modifier = kModifier(weight = 1f), verticalArrangement = KArrangements.spacedBy(2)) {
                    KText("Budget Goal", fontSize = 14, fontWeight = KFontWeights.SemiBold, color = KColors.OnSurface)
                    KText("You've used 68% of your monthly budget", fontSize = 12, color = KColors.OnSurfaceVariant)
                }
            }
        }

        KSpacer(height = 20)
    }
}
