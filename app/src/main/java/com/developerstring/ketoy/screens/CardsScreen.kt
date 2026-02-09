package com.developerstring.ketoy.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.developerstring.ketoy.components.TimedKetoyScreen
import com.developerstring.ketoy.dsl.*
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.screens.AppColors.onSurface
import com.developerstring.ketoy.screens.AppColors.onSurfaceVariant
import com.developerstring.ketoy.screens.AppColors.primary
import com.developerstring.ketoy.util.*

@Composable
fun CardsScreen() {
    val context = LocalContext.current
    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    TimedKetoyScreen(screenName = "Cards") { buildCardsUI { toast(it) } }
}

private fun buildCardsUI(toast: (String) -> Unit): KNode {
    return KColumn(modifier = kModifier(fillMaxSize = 1f)) {
        KLazyColumn(
            modifier = kModifier(fillMaxSize = 1f),
            verticalArrangement = "spacedBy_16",
            contentPadding = kPadding(horizontal = 16, vertical = 16)
        ) {
            item {
                KText("My Cards", fontSize = 24, fontWeight = KFontWeights.Bold, color = onSurface)
                KText("Manage your payment methods", fontSize = 14, color = onSurfaceVariant)
                KSpacer(height = 12)

                // Card 1
                walletCard(
                    cardName = "Platinum Visa", lastFour = "•••• 4829",
                    balance = "$18,240.50", expiry = "12/28",
                    gradientColors = listOf("#6750A4", "#9A82DB", "#D0BCFF")
                )
                KSpacer(height = 12)

                // Card 2
                walletCard(
                    cardName = "Gold Mastercard", lastFour = "•••• 7631",
                    balance = "$6,322.30", expiry = "08/27",
                    gradientColors = listOf("#7D5260", "#B88894", "#FFD8E4")
                )

                KSpacer(height = 20)

                // Quick actions for cards
                KText("Card Actions", fontSize = 18, fontWeight = KFontWeights.SemiBold, color = onSurface)
                KSpacer(height = 8)

                KRow(modifier = kModifier(fillMaxWidth = 1f), horizontalArrangement = KArrangements.SpaceEvenly) {
                    quickAction(KIcons.Lock, "Freeze", "#E8DEF8", primary) { toast("Card frozen") }
                    quickAction(KIcons.Settings, "Limits", "#FFD8E4", "#7D5260") { toast("Set limits") }
                    quickAction(KIcons.CreditCard, "Details", "#A8F5C4", "#1B7D46") { toast("Card details") }
                    quickAction(KIcons.Add, "New Card", "#F9DEDC", "#BA1A1A") { toast("Add new card") }
                }

                KSpacer(height = 72)
            }
        }
    }
}
