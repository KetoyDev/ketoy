package com.developerstring.ketoy.screens

import com.developerstring.ketoy.util.*

/**
 * Analytics screen — spending breakdown with category rows,
 * stat cards, and savings overview. Uses ViewModel state and
 * KetoyFunctionRegistry calls.
 */
fun buildAnalyticsScreen(
    income: String,
    expenses: String,
    savings: String,
    isDark: Boolean
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    val c = AppColors

    KColumn(
        modifier = kModifier(
            fillMaxSize = 1f,
            padding = kPadding(top = 16),
            background = if (isDark) "#1C1B1F" else "#FFFBFE"
        ),
        verticalArrangement = "spacedBy_0"
    ) {

        // ── Header ────────────────────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("Analytics", fontSize = 24, fontWeight = KFontWeights.Bold, color = c.onSurface(isDark))
            KIconButton(
                icon = KIcons.DateRange,
                iconColor = c.onSurfaceVariant(isDark),
                iconSize = 24,
                onClick = { KFunctionCall("showToast", "message" to "Date filter coming soon") },
                actionId = "analytics_calendar"
            ) {}
        }

        KSpacer(height = 20)

        // ── Overview stat cards ───────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = "spacedBy_12"
        ) {
            statCard(
                modifier = kModifier(weight = 1f),
                icon = KIcons.TrendingUp, label = "Income", value = income, trend = "+8.2%",
                iconBg = c.greenContainer(isDark), iconColor = c.green(isDark),
                trendColor = c.green(isDark), isDark = isDark
            )
            statCard(
                modifier = kModifier(weight = 1f),
                icon = KIcons.TrendingDown, label = "Expenses", value = expenses, trend = "-3.1%",
                iconBg = c.errorContainer(isDark), iconColor = c.red(isDark),
                trendColor = c.red(isDark), isDark = isDark
            )
        }

        KSpacer(height = 12)

        // Savings card
        KBox(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20))) {
            statCard(
                modifier = kModifier(fillMaxWidth = 1f),
                icon = KIcons.Savings, label = "Total Savings", value = savings, trend = "+15.4%",
                iconBg = c.primaryContainer(isDark), iconColor = c.primary(isDark),
                trendColor = c.green(isDark), isDark = isDark
            )
        }

        KSpacer(height = 24)

        // ── Spending by Category ──────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("Spending by Category", fontSize = 17, fontWeight = KFontWeights.SemiBold, color = c.onSurface(isDark))
            KButton(
                containerColor = "#00000000", elevation = 0,
                onClick = { KFunctionCall("showToast", "message" to "Month filter") },
                actionId = "analytics_month_filter"
            ) {
                KText("This Month", fontSize = 13, fontWeight = KFontWeights.Medium, color = c.primary(isDark))
            }
        }

        KSpacer(height = 12)

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)),
            verticalArrangement = "spacedBy_8"
        ) {
            categoryRow(KIcons.ShoppingCart, "Groceries", "\$420.50", c.primaryContainer(isDark), c.primary(isDark), isDark)
            categoryRow(KIcons.Restaurant, "Dining", "\$185.30", c.secondaryContainer(isDark), c.onSecondaryContainer(isDark), isDark)
            categoryRow(KIcons.DirectionsCar, "Transport", "\$98.00", c.tertiaryContainer(isDark), c.onTertiaryContainer(isDark), isDark)
            categoryRow(KIcons.Movie, "Entertainment", "\$65.99", c.errorContainer(isDark), c.red(isDark), isDark)
            categoryRow(KIcons.LocalHospital, "Health", "\$250.00", c.greenContainer(isDark), c.green(isDark), isDark)
            categoryRow(KIcons.Settings, "Utilities", "\$135.20", c.surfaceContainerLow(isDark), c.onSurfaceVariant(isDark), isDark)
        }

        KSpacer(height = 24)

        // ── Budget progress hint ──────────────────────────
        KCard(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            containerColor = c.primaryContainer(isDark),
            shape = KShapes.Rounded20,
            elevation = 0
        ) {
            KRow(
                modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)),
                horizontalArrangement = "spacedBy_12",
                verticalAlignment = KAlignments.CenterVertically
            ) {
                KIcon(icon = KIcons.BarChart, size = 28, color = c.primary(isDark))
                KColumn(modifier = kModifier(weight = 1f), verticalArrangement = "spacedBy_2") {
                    KText("Budget Goal", fontSize = 14, fontWeight = KFontWeights.SemiBold, color = c.onSurface(isDark))
                    KText("You've used 68% of your monthly budget", fontSize = 12, color = c.onSurfaceVariant(isDark))
                }
            }
        }

        KSpacer(height = 20)
    }
}
