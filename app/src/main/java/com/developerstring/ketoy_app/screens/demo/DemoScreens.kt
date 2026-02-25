package com.developerstring.ketoy_app.screens.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.developerstring.ketoy.navigation.LocalKetoyNavController

// ═══════════════════════════════════════════════════════════════
//  Screen 1: Explore — landing page with live-remappable navigation
//
//  Uses action IDs from nav_demo.json:
//    nav?.navigateToRoute("go_favorites")
//
//  The action ID resolves through the remap table:
//    "go_favorites" → toRoute from JSON (default "favorites").
//  Change toRoute in nav_demo.json → navigation updates live.
// ═══════════════════════════════════════════════════════════════

@Composable
fun DemoExploreScreen() {
    val nav = LocalKetoyNavController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Explore,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Explore",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Navigate to any screen below",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // Navigation via action IDs — resolved through nav graph remaps.
        // Route defined once in DemoNavGraphs; change it in JSON → live update.
        DestinationCard(
            icon = Icons.Filled.Favorite,
            title = "Favorites",
            subtitle = "Your saved items",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            onClick = { nav?.navigateToRoute("go_favorites") }
        )

        Spacer(Modifier.height(12.dp))

        DestinationCard(
            icon = Icons.Filled.Notifications,
            title = "Notifications",
            subtitle = "Recent alerts & updates",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = { nav?.navigateToRoute("go_notifications") }
        )

        Spacer(Modifier.height(12.dp))

        DestinationCard(
            icon = Icons.Filled.Settings,
            title = "Settings",
            subtitle = "App preferences",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = { nav?.navigateToRoute("go_settings") }
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "Each card uses navigateToRoute(\"go_favorites\").\n" +
                        "Route is defined once in DemoNavGraphs / nav_demo.json.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Screen 2: Favorites
// ═══════════════════════════════════════════════════════════════

@Composable
fun DemoFavoritesScreen() {
    val nav = LocalKetoyNavController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Icon(
            Icons.Filled.Favorite,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Favorites",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Your saved items",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        val items = listOf(
            Triple("Sunset Beach Resort", "Travel", Icons.Filled.Star),
            Triple("Italian Kitchen", "Food", Icons.Filled.Star),
            Triple("Mountain Trail Hike", "Activities", Icons.Filled.Star),
            Triple("Jazz Night Concert", "Events", Icons.Filled.Star),
            Triple("Vintage Book Store", "Shopping", Icons.Filled.Star),
        )

        items.forEachIndexed { index, (title, category, icon) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Medium)
                        Text(
                            category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (index < items.lastIndex) Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = { nav?.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back")
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Screen 3: Notifications
// ═══════════════════════════════════════════════════════════════

@Composable
fun DemoNotificationsScreen() {
    val nav = LocalKetoyNavController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Recent alerts & updates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        val notifications = listOf(
            Triple("Order Shipped", "Your package is on the way!", "2 min ago"),
            Triple("New Follower", "Sarah started following you", "15 min ago"),
            Triple("Flash Sale", "50% off — ends tonight!", "1 hr ago"),
            Triple("Payment Received", "$42.00 credited to your account", "3 hrs ago"),
            Triple("App Update", "Version 2.5 is now available", "Yesterday"),
        )

        notifications.forEachIndexed { index, (title, body, time) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index < 2)
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.SemiBold)
                        Text(
                            body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            if (index < notifications.lastIndex) Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = { nav?.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back")
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Screen 4: Settings
// ═══════════════════════════════════════════════════════════════

@Composable
fun DemoSettingsScreen() {
    val nav = LocalKetoyNavController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Icon(
            Icons.Filled.Settings,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "App preferences & configuration",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        val sections = listOf(
            "Account" to listOf(
                Triple("Profile", "Edit your profile", Icons.Outlined.Person),
                Triple("Privacy", "Manage data & permissions", Icons.Outlined.Lock),
            ),
            "General" to listOf(
                Triple("Appearance", "Theme, colors, fonts", Icons.Outlined.Palette),
                Triple("Notifications", "Push alerts & sounds", Icons.Outlined.Notifications),
                Triple("Language", "English (US)", Icons.Outlined.Language),
            ),
            "About" to listOf(
                Triple("Version", "2.5.0", Icons.Outlined.Info),
                Triple("Licenses", "Open-source libraries", Icons.Outlined.Description),
            ),
        )

        sections.forEach { (sectionTitle, items) ->
            Text(
                sectionTitle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column {
                    items.forEachIndexed { index, (title, subtitle, icon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Medium)
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (index < items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { nav?.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back")
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Shared: reusable destination card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DestinationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
