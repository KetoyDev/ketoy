package com.developerstring.ketoy.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.developerstring.ketoy.registry.KComponentRegistry

/**
 * Custom Compose components registered with [KComponentRegistry]
 * for server-driven rendering. Each component receives its props
 * as `Map<String, Any>` from the JSON engine.
 */
object AppCustomComponents {

    /**
     * Register all custom components. Call during app init.
     */
    fun registerAll() {
        registerAvatarBadge()
        registerBalanceDisplay()
        registerTransactionRow()
        registerGradientBanner()
    }

    // ── AvatarBadge ─────────────────────────────────────────

    private fun registerAvatarBadge() {
        KComponentRegistry.register(
            name = "AvatarBadge",
            renderer = { props ->
                AvatarBadge(
                    initials = props["initials"] as? String ?: "?",
                    badgeCount = props["badgeCount"] as? Int ?: 0,
                    size = props["size"] as? Int ?: 48
                )
            },
            parameterTypes = mapOf(
                "initials" to "String",
                "badgeCount" to "Int",
                "size" to "Int"
            )
        )
    }

    @Composable
    fun AvatarBadge(initials: String, badgeCount: Int = 0, size: Int = 48) {
        Box {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.take(2).uppercase(),
                    fontSize = (size / 2.5).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (badgeCount > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text("$badgeCount", fontSize = 10.sp)
                }
            }
        }
    }

    // ── BalanceDisplay ───────────────────────────────────────

    private fun registerBalanceDisplay() {
        KComponentRegistry.register(
            name = "BalanceDisplay",
            renderer = { props ->
                BalanceDisplay(
                    label = props["label"] as? String ?: "Balance",
                    amount = props["amount"] as? String ?: "$0.00",
                    trend = props["trend"] as? String ?: "",
                    trendPositive = props["trendPositive"] as? Boolean ?: true
                )
            },
            parameterTypes = mapOf(
                "label" to "String",
                "amount" to "String",
                "trend" to "String",
                "trendPositive" to "Boolean"
            )
        )
    }

    @Composable
    fun BalanceDisplay(label: String, amount: String, trend: String = "", trendPositive: Boolean = true) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = amount,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (trend.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (trendPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (trendPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = trend,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (trendPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // ── TransactionRow ──────────────────────────────────────

    private fun registerTransactionRow() {
        KComponentRegistry.register(
            name = "TransactionRow",
            renderer = { props ->
                TransactionRow(
                    title = props["title"] as? String ?: "",
                    subtitle = props["subtitle"] as? String ?: "",
                    amount = props["amount"] as? String ?: "",
                    isIncome = props["isIncome"] as? Boolean ?: false
                )
            },
            parameterTypes = mapOf(
                "title" to "String",
                "subtitle" to "String",
                "amount" to "String",
                "isIncome" to "Boolean"
            )
        )
    }

    @Composable
    fun TransactionRow(title: String, subtitle: String, amount: String, isIncome: Boolean) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isIncome) Color(0xFF4CAF50).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = amount,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // ── GradientBanner ──────────────────────────────────────

    private fun registerGradientBanner() {
        KComponentRegistry.register(
            name = "GradientBanner",
            renderer = { props ->
                GradientBanner(
                    title = props["title"] as? String ?: "",
                    subtitle = props["subtitle"] as? String ?: "",
                    actionLabel = props["actionLabel"] as? String ?: ""
                )
            },
            parameterTypes = mapOf(
                "title" to "String",
                "subtitle" to "String",
                "actionLabel" to "String"
            )
        )
    }

    @Composable
    fun GradientBanner(title: String, subtitle: String, actionLabel: String = "") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    if (actionLabel.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(actionLabel, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
