package com.developerstring.ketoy.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.developerstring.ketoy.components.TimedKetoyScreen
import com.developerstring.ketoy.dsl.*
import com.developerstring.ketoy.model.*
import com.developerstring.ketoy.screens.AppColors.green
import com.developerstring.ketoy.screens.AppColors.greenContainer
import com.developerstring.ketoy.screens.AppColors.onErrorContainer
import com.developerstring.ketoy.screens.AppColors.onPrimary
import com.developerstring.ketoy.screens.AppColors.onSurface
import com.developerstring.ketoy.screens.AppColors.onSurfaceVariant
import com.developerstring.ketoy.screens.AppColors.primary
import com.developerstring.ketoy.screens.AppColors.primaryContainer
import com.developerstring.ketoy.screens.AppColors.red
import com.developerstring.ketoy.screens.AppColors.secondaryContainer
import com.developerstring.ketoy.screens.AppColors.surfaceContainerLow
import com.developerstring.ketoy.screens.AppColors.tertiaryContainer
import com.developerstring.ketoy.screens.AppColors.errorContainer
import com.developerstring.ketoy.util.*

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    TimedKetoyScreen(screenName = "Home") { buildHomeUI { toast(it) } }
}

private fun buildHomeUI(toast: (String) -> Unit): KNode {
    return KColumn(modifier = kModifier(fillMaxSize = 1f)) {
        KLazyColumn(
            modifier = kModifier(fillMaxSize = 1f),
            verticalArrangement = "spacedBy_16",
            contentPadding = kPadding(horizontal = 16, vertical = 16)
        ) {
            item {
                // ── Greeting
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f),
                    horizontalArrangement = KArrangements.SpaceBetween,
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KColumn(verticalArrangement = "spacedBy_2") {
                        KText("Welcome back", fontSize = 14, color = onSurfaceVariant)
                        KText(
                            "Aditya",
                            fontSize = 24,
                            fontWeight = KFontWeights.Bold,
                            color = onSurface
                        )
                    }
                    KBox(
                        modifier = kModifier(
                            size = 48,
                            background = primaryContainer,
                            shape = KShapes.Circle
                        ),
                        contentAlignment = KAlignments.Center
                    ) {
                        KIcon(icon = KIcons.AccountCircle, size = 36, color = primary)
                    }
                }

                KSpacer(height = 8)

                // ── Balance card — no containerColor so the gradient child isn't layered
                KCard(
                    modifier = kModifier(fillMaxWidth = 1f),
                    shape = KShapes.Rounded24,
                    elevation = 6,
                ) {
                    KColumn(
                        modifier = kModifier(
                            fillMaxWidth = 1f,
                            gradient = KGradients.linear(
                                listOf("#6750A4", "#7F67BE", "#9A82DB"),
                                KGradients.Directions.Diagonal
                            ),
                        ),
                    ) {

                        KColumn(
                            modifier = kModifier(fillMaxWidth = 1f, padding = KPadding(all = 24),),
                            verticalArrangement = "spacedBy_4"
                        ) {

                            KRow(
                                modifier = kModifier(fillMaxWidth = 1f),
                                horizontalArrangement = KArrangements.SpaceBetween,
                                verticalAlignment = KAlignments.CenterVertically
                            ) {
                                KText("Total Balance", fontSize = 14, color = "#D0BCFF")
                                KIcon(icon = KIcons.Visibility, size = 20, color = "#D0BCFF")
                            }
                            KText(
                                "$24,562.80",
                                fontSize = 38,
                                fontWeight = KFontWeights.Bold,
                                color = onPrimary
                            )
                            KSpacer(height = 12)

                            KRow(
                                modifier = kModifier(fillMaxWidth = 1f),
                                horizontalArrangement = KArrangements.SpaceBetween
                            ) {
                                KRow(
                                    horizontalArrangement = "spacedBy_6",
                                    verticalAlignment = KAlignments.CenterVertically
                                ) {
                                    KBox(
                                        modifier = kModifier(
                                            size = 32,
                                            background = "#FFFFFF20",
                                            shape = KShapes.Circle
                                        ), contentAlignment = KAlignments.Center
                                    ) {
                                        KIcon(
                                            icon = KIcons.TrendingUp,
                                            size = 18,
                                            color = "#A8F5C4"
                                        )
                                    }
                                    KColumn {
                                        KText("Income", fontSize = 11, color = "#D0BCFF")
                                        KText(
                                            "+$8,240",
                                            fontSize = 15,
                                            fontWeight = KFontWeights.Bold,
                                            color = onPrimary
                                        )
                                    }
                                }
                                KRow(
                                    horizontalArrangement = "spacedBy_6",
                                    verticalAlignment = KAlignments.CenterVertically
                                ) {
                                    KBox(
                                        modifier = kModifier(
                                            size = 32,
                                            background = "#FFFFFF20",
                                            shape = KShapes.Circle
                                        ), contentAlignment = KAlignments.Center
                                    ) {
                                        KIcon(
                                            icon = KIcons.TrendingDown,
                                            size = 18,
                                            color = "#FFB4AB"
                                        )
                                    }
                                    KColumn {
                                        KText("Expenses", fontSize = 11, color = "#D0BCFF")
                                        KText(
                                            "-$3,820",
                                            fontSize = 15,
                                            fontWeight = KFontWeights.Bold,
                                            color = onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                KSpacer(height = 4)

                // ── Quick actions
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f),
                    horizontalArrangement = KArrangements.SpaceEvenly
                ) {
                    quickAction(
                        KIcons.Send,
                        "Send",
                        primaryContainer,
                        primary
                    ) { toast("Send Money") }
                    quickAction(
                        KIcons.CallReceived,
                        "Receive",
                        secondaryContainer,
                        "#4A4458"
                    ) { toast("Receive Money") }
                    quickAction(
                        KIcons.Receipt,
                        "Bills",
                        tertiaryContainer,
                        "#7D5260"
                    ) { toast("Pay Bills") }
                    quickAction(
                        KIcons.CreditCard,
                        "Top Up",
                        greenContainer,
                        green
                    ) { toast("Top Up") }
                }

                KSpacer(height = 4)

                // ── Stats
                KText(
                    "Quick Stats",
                    fontSize = 18,
                    fontWeight = KFontWeights.SemiBold,
                    color = onSurface
                )
                KSpacer(height = 8)
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f),
                    horizontalArrangement = "spacedBy_12"
                ) {
                    statCard(
                        kModifier(weight = 1f),
                        KIcons.Savings,
                        "Savings",
                        "$18,240",
                        "+12%",
                        primaryContainer,
                        primary,
                        green
                    )
                    statCard(
                        kModifier(weight = 1f),
                        KIcons.ShoppingCart,
                        "This Month",
                        "$2,380",
                        "-8%",
                        errorContainer,
                        onErrorContainer,
                        red
                    )
                }

                KSpacer(height = 4)

                // ── Recent transactions header
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f),
                    horizontalArrangement = KArrangements.SpaceBetween,
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KText(
                        "Recent Transactions",
                        fontSize = 18,
                        fontWeight = KFontWeights.SemiBold,
                        color = onSurface
                    )
                    KButton(
                        containerColor = KColors.Transparent,
                        onClick = { toast("See all transactions") }) {
                        KText(
                            "See All",
                            fontSize = 13,
                            fontWeight = KFontWeights.Medium,
                            color = primary
                        )
                        KIcon(icon = KIcons.ChevronRight, size = 18, color = primary)
                    }
                }
                KSpacer(height = 4)

                // ── Transactions
                transactionItem(
                    KIcons.ShoppingCart,
                    "#E8DEF8",
                    primary,
                    "Grocery Store",
                    "Today, 2:30 PM",
                    "- $45.20",
                    red,
                    surfaceContainerLow
                ) { toast("Grocery details") }
                transactionItem(
                    KIcons.AttachMoney,
                    greenContainer,
                    green,
                    "Salary Deposit",
                    "Yesterday",
                    "+ $3,200",
                    green,
                    surfaceContainerLow
                ) { toast("Salary details") }
                transactionItem(
                    KIcons.LocalCafe,
                    tertiaryContainer,
                    "#7D5260",
                    "Coffee Shop",
                    "Yesterday",
                    "- $6.50",
                    red,
                    surfaceContainerLow
                ) { toast("Coffee details") }
                transactionItem(
                    KIcons.Movie,
                    errorContainer,
                    red,
                    "Netflix Subscription",
                    "Feb 1",
                    "- $15.99",
                    red,
                    surfaceContainerLow
                ) { toast("Netflix details") }
                transactionItem(
                    KIcons.Phone,
                    secondaryContainer,
                    "#4A4458",
                    "App Store",
                    "Jan 30",
                    "- $9.99",
                    red,
                    surfaceContainerLow
                ) { toast("App Store details") }
                transactionItem(
                    KIcons.SwapHoriz,
                    greenContainer,
                    green,
                    "Transfer from Alex",
                    "Jan 28",
                    "+ $150.00",
                    green,
                    surfaceContainerLow
                ) { toast("Transfer details") }

                KSpacer(height = 72)
            }
        }
    }
}
