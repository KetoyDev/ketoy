package com.developerstring.ketoy_app.screens

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.export.ketoyExport
import com.developerstring.ketoy.model.KScrollConfig
import com.developerstring.ketoy.screen.KetoyContent
import com.developerstring.ketoy.screen.ProvideKetoyScreen
import com.developerstring.ketoy.util.*

/**
 * Export definition for the Cards screen.
 *
 * Uses [KData.userInt] so the exported JSON contains `{{data:user:selectedCardIndex}}`
 * instead of a hardcoded `0`. At runtime the SDK resolves this to the real value
 * from [com.developerstring.ketoy.core.KetoyVariableRegistry], keeping the selected card in sync with ViewModel state.
 */
val cardsExport = ketoyExport("cards", displayName = "Cards", description = "Wallet cards and card management") {
    content {
        buildCardsScreen(selectedCardIndex = KData.userInt("selectedCardIndex"))
    }
}

/**
 * Cards screen composable — wraps the DSL builder as a `@KScreen`
 * with a single `KetoyContent` child for cloud / hot-reload support.
 *
 * [selectedCardIndex] accepts either a literal [Int] (runtime) or a
 * template string from [KData.userInt] (export). The DSL builder resolves
 * it via [com.developerstring.ketoy.core.KetoyVariableRegistry.resolveInt].
 */
@Composable
fun CardsScreen(
    selectedCardIndex: Int,
) {
    ProvideKetoyScreen(screenName = "cards") {
        KetoyContent(
            nodeBuilder = {
                buildCardsScreen(
                    selectedCardIndex = selectedCardIndex,
                )
            }
        )
    }
}

/**
 * Cards screen DSL builder — returns a [com.developerstring.ketoy.model.KNode] tree with wallet-style
 * card carousel, card actions, and recent card activity.
 *
 * @param selectedCardIndex Either a literal [Int] or a template string such as
 *   `KData.userInt("selectedCardIndex")`. The value is resolved via
 *   [com.developerstring.ketoy.core.KetoyVariableRegistry.resolveInt] so both the export path (template → JSON)
 *   and the runtime path (literal → direct use) work identically.
 */
fun buildCardsScreen(
    selectedCardIndex: Any,
): com.developerstring.ketoy.model.KNode = ketoyRoot {
    val indexForLogic = selectedCardIndex as? Int ?: 0

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
            padding = kPadding(top = 15),
            background = KColors.Background,
            verticalScroll = KScrollConfig(enabled = true)
        ),
        verticalArrangement = KArrangements.spacedBy(0)
    ) {

        // ── Header ────────────────────────────────────────
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText("My Cards", fontSize = 24, fontWeight = KFontWeights.Bold, color = KColors.OnSurface)
            KIconButton(
                icon = KIcons.Add,
                iconColor = KColors.Primary,
                iconSize = 26,
                onClickAction = KFunctionAction("showToast", "message" to "Add card flow"),
                actionId = "cards_add"
            ) {}
        }

        KSpacer(height = 20)

        KText(text = selectedCardIndex.toString(), fontSize = 30, color = KColors.White)

        // ── Card carousel ─────────────────────────────────
        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            verticalArrangement = KArrangements.spacedBy(16)
        ) {
            cards.forEachIndexed { index, card ->
                val isSelected = (index == selectedCardIndex.toString().toIntOrNull())

                walletCard(
                    cardName = card.name,
                    lastFour = card.lastFour,
                    balance = card.balance,
                    expiry = card.expiry,
                    gradientColors = if (isSelected) card.gradients else listOf("#787880", "#A0A0A8"),
                )

                // Card action buttons (only for selected)
                if (isSelected) {
                    KRow(
                        modifier = kModifier(fillMaxWidth = 1f),
                        horizontalArrangement = KArrangements.SpaceEvenly
                    ) {
                        quickAction(
                            KIcons.Lock, "Freeze", KColors.ErrorContainer, KColors.Error,
                            actionId = "cards_freeze_$index",
                            onClickAction = KFunctionAction("freezeCard", "cardName" to card.name)
                        )

                        quickAction(
                            KIcons.Settings, "Settings", KColors.SecondaryContainer, KColors.OnSecondaryContainer,
                            actionId = "cards_settings_$index",
                            onClickAction = KFunctionAction("showToast", "message" to "${card.name} settings")
                        )

                        quickAction(
                            KIcons.Visibility, "Details", KColors.PrimaryContainer, KColors.Primary,
                            actionId = "cards_details_$index",
                            onClickAction = KFunctionAction("showToast", "message" to "Card details for ${card.name}")
                        )
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
            KRow(horizontalArrangement = KArrangements.spacedBy(8)) {
                cards.forEachIndexed { index, card ->
                    val chipBg = if (index == indexForLogic) KColors.Primary else KColors.SurfaceContainerLow
                    val chipColor = if (index == indexForLogic) KColors.OnPrimary else KColors.OnSurfaceVariant

                    KCard(
                        modifier = kModifier(height = 36),
                        containerColor = chipBg,
                        shape = KShapes.rounded(50),
                        elevation = 0,
                        onClickAction = KFunctionAction("selectCard", "index" to index),
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
            KText("Card Activity", fontSize = 17, fontWeight = KFontWeights.SemiBold, color = KColors.OnSurface)
            KButton(
                containerColor = "#00000000", elevation = 0,
                onClickAction = KFunctionAction("showToast", "message" to "View all card activity"),
                actionId = "cards_view_all"
            ) {
                KText("View All", fontSize = 13, fontWeight = KFontWeights.Medium, color = KColors.Primary)
            }
        }

        KSpacer(height = 8)

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)),
            verticalArrangement = KArrangements.spacedBy(8)
        ) {
            KComponent("TransactionRow", mapOf("title" to "Apple Store", "subtitle" to "Today", "amount" to "- \$999.00", "isIncome" to false))
            KComponent("TransactionRow", mapOf("title" to "Refund - Amazon", "subtitle" to "Yesterday", "amount" to "+ \$42.50", "isIncome" to true))
            KComponent("TransactionRow", mapOf("title" to "Gas Station", "subtitle" to "Feb 2", "amount" to "- \$55.00", "isIncome" to false))
        }

        KSpacer(height = 20)
    }
}
