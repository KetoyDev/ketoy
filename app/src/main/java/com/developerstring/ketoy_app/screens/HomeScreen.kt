package com.developerstring.ketoy_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.developerstring.ketoy.model.KIconImageSource
import com.developerstring.ketoy.model.KScaleType
import com.developerstring.ketoy.screen.KetoyContent
import com.developerstring.ketoy.screen.ProvideKetoyScreen
import com.developerstring.ketoy.util.*

/**
 * Home screen composable — demonstrates the **mixed Compose + DSL** pattern.
 *
 * One `@KScreen` parent with multiple `KetoyContent` children interleaved
 * with native Jetpack Compose code:
 *
 * - **Cards section** → DSL via `KetoyContent(name = "cards")`
 * - **Expenses section** → native Compose
 * - **Transactions list** → DSL via `KetoyContent(name = "transactions")`
 * - **Bottom buttons** → native Compose
 *
 * Each `KetoyContent` block can be independently hot-reloaded from the
 * dev-server or fetched from Ketoy Cloud, while the native Compose
 * sections remain unchanged.
 */
@Composable
fun HomeScreen(
    userName: String,
    totalBalance: String,
    income: String,
    expenses: String,
    savings: String,
    notificationCount: Int,
    isDark: Boolean,
    transactions: List<Triple<String, String, String>>
) {
    ProvideKetoyScreen(screenName = "home") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE))
        ) {

            // ── 1. Cards section (DSL) ────────────────────────
            KetoyContent(
                name = "cards",
                nodeBuilder = {
                    buildHomeCards(
                        userName = userName,
                        totalBalance = totalBalance,
                        income = income,
                        notificationCount = notificationCount,
                        isDark = isDark
                    )
                }
            )

            // ── 2. Expenses section (native Compose) ─────────
            HomeExpensesSection(
                expenses = expenses,
                savings = savings,
                isDark = isDark
            )

            // ── 3. Transactions list (DSL) ────────────────────
            KetoyContent(
                name = "transactions",
                nodeBuilder = {
                    buildHomeTransactions(
                        transactions = transactions,
                        isDark = isDark
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── 4. Bottom buttons (native Compose) ───────────
            HomeBottomButtons(isDark = isDark)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Native Compose sections
// ═══════════════════════════════════════════════════════════════

/**
 * Expenses overview — written in pure Jetpack Compose.
 * This section is NOT managed by the Ketoy DSL and cannot be
 * hot-reloaded from the dev-server.
 */
@Composable
private fun HomeExpensesSection(
    expenses: String,
    savings: String,
    isDark: Boolean
) {
    val textColor = if (isDark) Color(0xFFE6E0E9) else Color(0xFF1D1B20)
    val subtitleColor = if (isDark) Color(0xFFCAC4D0) else Color(0xFF49454F)
    val cardColor = if (isDark) Color(0xFF2B2930) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Expenses Overview",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Expenses card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Expenses",
                            tint = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "-3.1%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Expenses", fontSize = 12.sp, color = subtitleColor)
                    Text(expenses, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }

            // Savings card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Savings",
                            tint = if (isDark) Color(0xFF6DD58C) else Color(0xFF1B7D46),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "+5.4%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF6DD58C) else Color(0xFF1B7D46)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Savings", fontSize = 12.sp, color = subtitleColor)
                    Text(savings, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
        }
    }
}

/**
 * Bottom action buttons — written in pure Jetpack Compose.
 */
@Composable
private fun HomeBottomButtons(isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { /* TODO: Add money action */ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
            )
        ) {
            Text(
                "Add Money",
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF381E72) else Color.White
            )
        }

        OutlinedButton(
            onClick = { /* TODO: Export action */ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Export",
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  DSL builders — each one backs a KetoyContent block
// ═══════════════════════════════════════════════════════════════

/**
 * DSL builder for the "cards" KetoyContent — header, balance card,
 * quick-actions, and income stat card.
 */
fun buildHomeCards(
    userName: String,
    totalBalance: String,
    income: String,
    notificationCount: Int,
    isDark: Boolean
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    val c = AppColors

    KColumn(
        modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(top = 16)),
        verticalArrangement = KArrangements.spacedBy(0)
    ) {
        // Header row
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {
            KText(
                fontWeight = KFontWeights.Bold, textAlign = KTextAlign.Center,
                maxLines = 1, fontSize = 24, color = c.onSurface(isDark)
            )
            KRow(
                horizontalArrangement = KArrangements.spacedBy(14),
                verticalAlignment = KAlignments.CenterVertically
            ) {
                KComponent("AvatarBadge", mapOf("initials" to userName.take(2), "badgeCount" to notificationCount, "size" to 48))
                KColumn(verticalArrangement = KArrangements.spacedBy(2)) {
                    KText("Good morning, hello", fontSize = 13, color = c.onSurfaceVariant(isDark))
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

        // Balance card
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

        // Quick actions row
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

        // Income stat card
        KRow(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            horizontalArrangement = KArrangements.spacedBy(12)
        ) {
            statCard(
                modifier = kModifier(weight = 1f),
                icon = KIcons.TrendingUp, label = "Income", value = income,
                trend = "+8.2%",
                iconBg = c.greenContainer(isDark), iconColor = c.green(isDark),
                trendColor = c.green(isDark), isDark = isDark
            )
        }
    }
}

/**
 * DSL builder for the "transactions" KetoyContent — recent transaction
 * list with header and see-all button.
 */
fun buildHomeTransactions(
    transactions: List<Triple<String, String, String>>,
    isDark: Boolean
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    val c = AppColors

    KColumn(
        modifier = kModifier(fillMaxWidth = 1f),
        verticalArrangement = KArrangements.spacedBy(0)
    ) {
        // Recent transactions header
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

        // Transaction list
        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20, bottom = 16)),
            verticalArrangement = KArrangements.spacedBy(8)
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
