package com.developerstring.ketoy.screens

import com.developerstring.ketoy.util.*

/**
 * Cards screen — wallet-style card carousel with gradient cards,
 * card actions (freeze, select), and KetoyFunctionRegistry calls.
 */
fun buildCardsScreen(
    selectedCardIndex: Int,
    isDark: Boolean
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    val c = AppColors

    data class CardData(
        val name: String, val lastFour: String, val balance: String,
        val expiry: String, val gradients: List<String>
    )

    val cards = listOf(
        CardData("Visa Platinum", "•••• •••• •••• 4592", "\$12,450.00", "09/27",
            listOf("#6750A4", "#9A82DB")),
        CardData("Mastercard Gold", "•••• •••• •••• 7831", "\$8,320.50", "03/26",
            listOf("#7D5260", "#B58392")),
        CardData("Amex Blue", "•••• •••• •••• 1204", "\$3,792.30", "11/28",
            listOf("#1B7D46", "#4CAF50"))
    )

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
            KText("My Cards", fontSize = 24, fontWeight = KFontWeights.Bold, color = c.onSurface(isDark))
            KIconButton(
                icon = KIcons.Add,
                iconColor = c.primary(isDark),
                iconSize = 26,
                onClick = { KFunctionCall("showToast", "message" to "Add card flow") },
                actionId = "cards_add"
            ) {}
        }

        KSpacer(height = 20)

        // ── Card carousel ─────────────────────────────────
        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            verticalArrangement = "spacedBy_16"
        ) {
            cards.forEachIndexed { index, card ->
                val isSelected = index == selectedCardIndex

                walletCard(
                    cardName = card.name,
                    lastFour = card.lastFour,
                    balance = card.balance,
                    expiry = card.expiry,
                    gradientColors = if (isSelected) card.gradients else listOf("#787880", "#A0A0A8"),
                    isDark = isDark
                )

                // Card action buttons (only for selected)
                if (isSelected) {
                    KRow(
                        modifier = kModifier(fillMaxWidth = 1f),
                        horizontalArrangement = KArrangements.SpaceEvenly
                    ) {
                        quickAction(
                            KIcons.Lock, "Freeze", c.errorContainer(isDark), c.red(isDark),
                            isDark = isDark, actionId = "cards_freeze_$index"
                        ) { KFunctionCall("freezeCard", "cardName" to card.name) }

                        quickAction(
                            KIcons.Settings, "Settings", c.secondaryContainer(isDark), c.onSecondaryContainer(isDark),
                            isDark = isDark, actionId = "cards_settings_$index"
                        ) { KFunctionCall("showToast", "message" to "${card.name} settings") }

                        quickAction(
                            KIcons.Visibility, "Details", c.primaryContainer(isDark), c.primary(isDark),
                            isDark = isDark, actionId = "cards_details_$index"
                        ) { KFunctionCall("showToast", "message" to "Card details for ${card.name}") }
                    }
                }
            }
        }

        KSpacer(height = 24)

        // ── Card selector chips ───────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.Center
        ) {
            KRow(horizontalArrangement = "spacedBy_8") {
                cards.forEachIndexed { index, card ->
                    val chipBg = if (index == selectedCardIndex) c.primary(isDark) else c.surfaceContainerLow(isDark)
                    val chipColor = if (index == selectedCardIndex) c.onPrimary(isDark) else c.onSurfaceVariant(isDark)

                    KCard(
                        modifier = kModifier(height = 36),
                        containerColor = chipBg,
                        shape = KShapes.rounded(50),
                        elevation = 0,
                        onClick = { KFunctionCall("selectCard", "index" to index) },
                        actionId = "cards_select_$index"
                    ) {
                        KBox(
                            modifier = kModifier(fillMaxHeight = 1f, padding = kPadding(horizontal = 16)),
                            contentAlignment = KAlignments.Center
                        ) {
                            KText("Card ${index + 1}", fontSize = 13, fontWeight = KFontWeights.Medium, color = chipColor)
                        }
                    }
                }
            }
        }

        KSpacer(height = 24)

        // ── Recent card activity ──────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("Card Activity", fontSize = 17, fontWeight = KFontWeights.SemiBold, color = c.onSurface(isDark))
            KButton(
                containerColor = "#00000000", elevation = 0,
                onClick = { KFunctionCall("showToast", "message" to "View all card activity") },
                actionId = "cards_view_all"
            ) {
                KText("View All", fontSize = 13, fontWeight = KFontWeights.Medium, color = c.primary(isDark))
            }
        }

        KSpacer(height = 8)

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)),
            verticalArrangement = "spacedBy_8"
        ) {
            KComponent("TransactionRow", mapOf("title" to "Apple Store", "subtitle" to "Today", "amount" to "- \$999.00", "isIncome" to false))
            KComponent("TransactionRow", mapOf("title" to "Refund - Amazon", "subtitle" to "Yesterday", "amount" to "+ \$42.50", "isIncome" to true))
            KComponent("TransactionRow", mapOf("title" to "Gas Station", "subtitle" to "Feb 2", "amount" to "- \$55.00", "isIncome" to false))
        }

        KSpacer(height = 20)
    }
}
