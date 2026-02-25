package com.developerstring.ketoy_app.screens

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.screen.KetoyContent
import com.developerstring.ketoy.screen.ProvideKetoyScreen
import com.developerstring.ketoy.util.*

/**
 * History screen composable — wraps the DSL builder as a `@KScreen`
 * with a single `KetoyContent` child for cloud / hot-reload support.
 */
@Composable
fun HistoryScreen(
    transactions: List<Triple<String, String, String>>,
    isDark: Boolean
) {
    ProvideKetoyScreen(screenName = "history_screen") {
        KetoyContent(
            nodeBuilder = {
                buildHistoryScreen(
                    transactions = transactions,
                    isDark = isDark
                )
            }
        )
    }
}

/**
 * History screen DSL builder — returns a [com.developerstring.ketoy.model.KNode] tree with full
 * transaction list, search hint, and filter chips.
 */
fun buildHistoryScreen(
    transactions: List<Triple<String, String, String>>,
    isDark: Boolean
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    val c = AppColors

    KColumn(
        modifier = kModifier(
            fillMaxSize = 1f,
            padding = kPadding(top = 16),
            background = if (isDark) "#1C1B1F" else "#FFFBFE"
        ),
        verticalArrangement = KArrangements.spacedBy(0)
    ) {

        KText(fontSize = 20)

        // ── Header ────────────────────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("Transaction History", fontSize = 24, fontWeight = KFontWeights.Bold, color = c.onSurface(isDark))
            KIconButton(
                icon = KIcons.FilterList,
                iconColor = c.onSurfaceVariant(isDark),
                iconSize = 24,
                onClick = { KFunctionCall("showToast", "message" to "Filter options") },
                actionId = "history_filter"
            ) {}
        }

        KSpacer(height = 16)

        // ── Search hint card ──────────────────────────────
        KBox(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20))) {
            KCard(
                modifier = kModifier(fillMaxWidth = 1f),
                containerColor = c.surfaceContainerLow(isDark),
                shape = KShapes.Rounded16,
                elevation = 0,
                onClick = { KFunctionCall("showToast", "message" to "Search transactions") },
                actionId = "history_search"
            ) {
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14)),
                    horizontalArrangement = KArrangements.spacedBy(12),
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KIcon(icon = KIcons.Search, size = 22, color = c.outline(isDark))
                    KText("Search transactions...", fontSize = 15, color = c.outline(isDark))
                }
            }
        }

        KSpacer(height = 20)

        // ── Summary chips ─────────────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.spacedBy(8)
        ) {
            val chipLabels = listOf("All" to true, "Income" to false, "Expense" to false, "Pending" to false)
            for ((label, isActive) in chipLabels) {
                val bg = if (isActive) c.primary(isDark) else c.surfaceContainerLow(isDark)
                val textColor = if (isActive) c.onPrimary(isDark) else c.onSurfaceVariant(isDark)
                KCard(
                    modifier = kModifier(height = 34),
                    containerColor = bg,
                    shape = KShapes.rounded(50),
                    elevation = 0,
                    onClick = { KFunctionCall("showToast", "message" to "$label filter") },
                    actionId = "history_chip_$label"
                ) {
                    KBox(modifier = kModifier(fillMaxHeight = 1f, padding = kPadding(horizontal = 16)), contentAlignment = KAlignments.Center) {
                        KText(label, fontSize = 13, fontWeight = KFontWeights.Medium, color = textColor)
                    }
                }
            }
        }

        KSpacer(height = 16)

        // ── Transaction list ──────────────────────────────
        KText(
            "February 2025",
            fontSize = 13,
            fontWeight = KFontWeights.SemiBold,
            color = c.onSurfaceVariant(isDark),
            modifier = kModifier(padding = kPadding(horizontal = 20, bottom = 8))
        )

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)),
            verticalArrangement = KArrangements.spacedBy(8)
        ) {
            for ((title, subtitle, amount) in transactions) {
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

        // ── Add transaction FAB hint ──────────────────────
        KSpacer(height = 8)
        KBox(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)), contentAlignment = KAlignments.Center) {
            KCard(
                containerColor = c.primary(isDark),
                shape = KShapes.rounded(50),
                elevation = 4,
                onClick = {
                    KFunctionCall(
                        "addTransaction",
                        "title" to "New Purchase",
                        "amount" to 25.0,
                        "isIncome" to false
                    )
                },
                actionId = "history_add_txn"
            ) {
                KRow(
                    modifier = kModifier(padding = kPadding(horizontal = 20, vertical = 12)),
                    horizontalArrangement = KArrangements.spacedBy(8),
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KIcon(icon = KIcons.Add, size = 20, color = c.onPrimary(isDark))
                    KText("Add Transaction", fontSize = 14, fontWeight = KFontWeights.SemiBold, color = c.onPrimary(isDark))
                }
            }
        }
    }
}
