package com.developerstring.ketoy.screens

import com.developerstring.ketoy.util.*

/**
 * Home screen — the main dashboard showing balance overview,
 * quick-actions, custom components (AvatarBadge, BalanceDisplay),
 * KetoyFunctionRegistry calls, and recent transactions.
 */
fun buildHomeScreen(
    userName: String,
    totalBalance: String,
    income: String,
    expenses: String,
    savings: String,
    notificationCount: Int,
    isDark: Boolean,
    transactions: List<Triple<String, String, String>> // title, subtitle, amount+(+/-)
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    val c = AppColors

    // ── Top bar: avatar + greeting + notifications ─────────
    KColumn(
        modifier = kModifier(
            fillMaxSize = 1f,
            padding = kPadding(top = 16),
            background = if (isDark) "#1C1B1F" else "#FFFBFE"
        ),
        verticalArrangement = "spacedBy_0"
    ) {

        // Header row
        KRow(
            modifier = kModifier(
                fillMaxWidth = 1f,
                padding = kPadding(horizontal = 20)
            ),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KRow(
                horizontalArrangement = "spacedBy_14",
                verticalAlignment = KAlignments.CenterVertically
            ) {
                KComponent("AvatarBadge", mapOf("initials" to userName.take(2), "badgeCount" to notificationCount, "size" to 48))
                KColumn(verticalArrangement = "spacedBy_2") {
                    KText("Good morning,", fontSize = 13, color = c.onSurfaceVariant(isDark))
                    KText(userName, fontSize = 20, fontWeight = KFontWeights.Bold, color = c.onSurface(isDark))
                }
            }

            KIconButton(
                icon = KIcons.NotificationsNone,
                iconColor = c.onSurface(isDark),
                iconSize = 26,
                onClick = { KFunctionCall("clearNotifications") },
                actionId = "home_clear_notifs"
            ) {}
        }

        KSpacer(height = 20)

        // ── Balance card (custom component) ───────────────
        KBox(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20))) {
            KComponent(
                "BalanceDisplay",
                mapOf(
                    "label" to "Total Balance",
                    "amount" to totalBalance,
                    "trend" to "+12.5% this month",
                    "trendPositive" to true
                )
            )
        }

        KSpacer(height = 20)

        // ── Quick actions row ─────────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceEvenly
        ) {
            quickAction(
                KIcons.Send, "Send", c.primaryContainer(isDark), c.primary(isDark),
                isDark = isDark, actionId = "home_send"
            ) { KFunctionCall("sendMoney", "amount" to 50.0, "recipient" to "Alex") }

            quickAction(
                KIcons.Download, "Receive", c.secondaryContainer(isDark), c.onSecondaryContainer(isDark),
                isDark = isDark, actionId = "home_receive"
            ) { KFunctionCall("showToast", "message" to "Receive link copied") }

            quickAction(
                KIcons.SwapHoriz, "Swap", c.tertiaryContainer(isDark), c.onTertiaryContainer(isDark),
                isDark = isDark, actionId = "home_swap"
            ) { KFunctionCall("showToast", "message" to "Swap feature coming soon") }

            quickAction(
                KIcons.MoreHoriz, "More", c.surfaceContainerLow(isDark), c.onSurfaceVariant(isDark),
                isDark = isDark, actionId = "home_more"
            ) { KFunctionCall("showToast", "message" to "More options") }
        }

        KSpacer(height = 24)

        // ── Stat cards row (income / expenses) ────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = "spacedBy_12"
        ) {
            statCard(
                modifier = kModifier(weight = 1f),
                icon = KIcons.TrendingUp, label = "Income", value = income,
                trend = "+8.2%",
                iconBg = c.greenContainer(isDark), iconColor = c.green(isDark),
                trendColor = c.green(isDark), isDark = isDark
            )
            statCard(
                modifier = kModifier(weight = 1f),
                icon = KIcons.TrendingDown, label = "Expenses", value = expenses,
                trend = "-3.1%",
                iconBg = c.errorContainer(isDark), iconColor = c.red(isDark),
                trendColor = c.red(isDark), isDark = isDark
            )
        }

        KSpacer(height = 24)

        // ── Recent transactions header ────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("Recent Transactions", fontSize = 17, fontWeight = KFontWeights.SemiBold, color = c.onSurface(isDark))
            KButton(
                containerColor = "#00000000", elevation = 0,
                onClick = { KFunctionCall("showToast", "message" to "View all transactions") },
                actionId = "home_see_all"
            ) {
                KText("See All", fontSize = 13, fontWeight = KFontWeights.Medium, color = c.primary(isDark))
            }
        }

        KSpacer(height = 8)

        // ── Transaction list (custom components) ──────────
        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)),
            verticalArrangement = "spacedBy_8"
        ) {
            for ((title, subtitle, amount) in transactions.take(5)) {
                val isIncome = amount.startsWith("+")
                KComponent(
                    "TransactionRow",
                    mapOf(
                        "title" to title,
                        "subtitle" to subtitle,
                        "amount" to amount,
                        "isIncome" to isIncome
                    )
                )
            }
        }
    }
}
