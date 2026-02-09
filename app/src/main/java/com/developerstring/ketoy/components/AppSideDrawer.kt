package com.developerstring.ketoy.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.developerstring.ketoy.navigation.*
import kotlinx.serialization.Serializable

/**
 * A custom side-drawer that occupies ~2/3 of the screen width.
 *
 * Features a gradient profile header, grouped menu sections,
 * and a scrim that dismisses the drawer on tap.
 */
@Composable
fun AppSideDrawer(
    visible: Boolean,
    currentRoute: String?,
    onDismiss: () -> Unit,
    onNavigate: (route: Any) -> Unit,
    onAction: (label: String) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        // Scrim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.72f),    // ← ~2/3 of screen width
            shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ─── Gradient profile header ────────────────────
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
                        .padding(horizontal = 24.dp, vertical = 36.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.End),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White.copy(alpha = 0.85f)
                            )
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        Text("Aditya", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        Text("aditya@ketoy.dev", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ─── Main navigation ────────────────────────────
                DrawerSectionTitle("Navigation")
                DrawerMenuItem(Icons.Outlined.Home, "Home", selected = currentRoute?.contains("HomeRoute") == true) { onNavigate(HomeRoute) }
                DrawerMenuItem(Icons.Outlined.Insights, "Analytics", selected = currentRoute?.contains("AnalyticsRoute") == true) { onNavigate(AnalyticsRoute) }
                DrawerMenuItem(Icons.Outlined.CreditCard, "Cards", selected = currentRoute?.contains("CardsRoute") == true) { onNavigate(CardsRoute) }
                DrawerMenuItem(Icons.Outlined.Person, "Profile", selected = currentRoute?.contains("ProfileRoute") == true) { onNavigate(ProfileRoute) }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

                // ─── Quick actions ──────────────────────────────
                DrawerSectionTitle("Quick Actions")
                DrawerMenuItem(Icons.Outlined.Add, "New Transaction") { onAction("New Transaction") }
                DrawerMenuItem(Icons.AutoMirrored.Outlined.Send, "Send Money") { onAction("Send Money") }
                DrawerMenuItem(Icons.Outlined.CreditCard, "Top Up") { onAction("Top Up") }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

                // ─── Preferences ────────────────────────────────
                DrawerSectionTitle("Preferences")
                DrawerMenuItem(Icons.Outlined.Notifications, "Notifications") { onAction("Notifications") }
                DrawerMenuItem(Icons.Outlined.DarkMode, "Dark Mode") { onAction("Dark Mode") }
                DrawerMenuItem(Icons.Outlined.Settings, "Settings") { onAction("Settings") }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

                // ─── Footer ─────────────────────────────────────
                DrawerMenuItem(Icons.Outlined.Info, "About") { onAction("About") }
                DrawerMenuItem(Icons.AutoMirrored.Filled.ExitToApp, "Log Out", tint = MaterialTheme.colorScheme.error) { onAction("Log Out") }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Private composable helpers ──────────────────────────────────

@Composable
private fun DrawerSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 28.dp, top = 12.dp, bottom = 6.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    tint: Color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp), tint = tint)
        Text(label, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = tint)
    }
}
