package com.developerstring.ketoy.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.developerstring.ketoy.components.TimedKetoyScreen
import com.developerstring.ketoy.dsl.*
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.screens.AppColors.green
import com.developerstring.ketoy.screens.AppColors.greenContainer
import com.developerstring.ketoy.screens.AppColors.onSurface
import com.developerstring.ketoy.screens.AppColors.onSurfaceVariant
import com.developerstring.ketoy.screens.AppColors.primary
import com.developerstring.ketoy.screens.AppColors.primaryContainer
import com.developerstring.ketoy.screens.AppColors.red
import com.developerstring.ketoy.screens.AppColors.errorContainer
import com.developerstring.ketoy.util.*

@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    TimedKetoyScreen(screenName = "Analytics") { buildAnalyticsUI { toast(it) } }
}

private fun buildAnalyticsUI(toast: (String) -> Unit): KNode {
    return KColumn(modifier = kModifier(fillMaxSize = 1f)) {
        KLazyColumn(
            modifier = kModifier(fillMaxSize = 1f),
            verticalArrangement = "spacedBy_16",
            contentPadding = kPadding(horizontal = 16, vertical = 16)
        ) {
            item {
                KText("Analytics", fontSize = 24, fontWeight = KFontWeights.Bold, color = onSurface)
                KText("Your spending overview", fontSize = 14, color = onSurfaceVariant)
                KSpacer(height = 12)

                // ── Summary cards
                KRow(modifier = kModifier(fillMaxWidth = 1f), horizontalArrangement = "spacedBy_12") {
                    statCard(kModifier(weight = 1f), KIcons.TrendingUp, "Income", "$8,240", "+18%", greenContainer, green, green)
                    statCard(kModifier(weight = 1f), KIcons.TrendingDown, "Expenses", "$3,820", "+5%", errorContainer, red, red)
                }
                KSpacer(height = 8)
                KRow(modifier = kModifier(fillMaxWidth = 1f), horizontalArrangement = "spacedBy_12") {
                    statCard(kModifier(weight = 1f), KIcons.Savings, "Saved", "$4,420", "+32%", primaryContainer, primary, green)
                    statCard(kModifier(weight = 1f), KIcons.AccountBalance, "Investments", "$12,800", "+8%", "#E8DEF8", "#4A4458", green)
                }

                KSpacer(height = 16)

                // ── Top spending categories
                KText("Top Categories", fontSize = 18, fontWeight = KFontWeights.SemiBold, color = onSurface)
                KSpacer(height = 8)

                categoryRow(KIcons.ShoppingCart, "Shopping", "$980", "#E8DEF8", primary)
                categoryRow(KIcons.Restaurant, "Food & Dining", "$640", "#FFD8E4", "#7D5260")
                categoryRow(KIcons.DirectionsCar, "Transport", "$420", greenContainer, green)
                categoryRow(KIcons.Movie, "Entertainment", "$310", errorContainer, red)
                categoryRow(KIcons.LocalHospital, "Health", "$180", "#D6E3FF", "#2C5EA0")

                KSpacer(height = 72)
            }
        }
    }
}
