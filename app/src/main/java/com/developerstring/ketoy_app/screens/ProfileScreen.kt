package com.developerstring.ketoy_app.screens

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.export.ketoyExport
import com.developerstring.ketoy.model.KNode
import com.developerstring.ketoy.model.KScrollConfig
import com.developerstring.ketoy.screen.KetoyContent
import com.developerstring.ketoy.screen.ProvideKetoyScreen
import com.developerstring.ketoy.util.*

/**
 * Export definition for the Profile screen.
 */
val profileExport = ketoyExport("profile", displayName = "Profile", description = "User profile and settings") {
    content {
        buildProfileScreen(
            userName = KData.user("name"),
            darkModeIcon = KData.user("darkModeIcon"),
            darkModeLabel = KData.user("darkModeLabel"),
            darkModeToggleBg = KData.user("darkModeToggleBg"),
            darkModeToggleTextColor = KData.user("darkModeToggleTextColor"),
        )
    }
}

/**
 * Profile screen composable — wraps the DSL builder as a `@KScreen`
 * with a single `KetoyContent` child for cloud / hot-reload support.
 */
@Composable
fun ProfileScreen(
    userName: String,
    darkModeIcon: String,
    darkModeLabel: String,
    darkModeToggleBg: String,
    darkModeToggleTextColor: String,
) {
    ProvideKetoyScreen(screenName = "profile") {
        KetoyContent(
            nodeBuilder = {
                buildProfileScreen(
                    userName = userName,
                    darkModeIcon = darkModeIcon,
                    darkModeLabel = darkModeLabel,
                    darkModeToggleBg = darkModeToggleBg,
                    darkModeToggleTextColor = darkModeToggleTextColor,
                )
            }
        )
    }
}

/**
 * Profile screen DSL builder — returns a [com.developerstring.ketoy.model.KNode] tree with avatar,
 * account settings, preferences, and sign-out.
 */
fun buildProfileScreen(
    userName: String,
    darkModeIcon: String,
    darkModeLabel: String,
    darkModeToggleBg: String,
    darkModeToggleTextColor: String,
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    KColumn(
        modifier = kModifier(
            fillMaxSize = 1f,
            padding = kPadding(top = 10),
            background = KColors.Background,
            verticalScroll = KScrollConfig.Default
        ),
        verticalArrangement = KArrangements.spacedBy(0)
    ) {

        // ── Avatar + name ─────────────────────────────────
        KColumn(
            modifier = kModifier(fillMaxWidth = 1f),
            horizontalAlignment = KAlignments.CenterHorizontally,
            verticalArrangement = KArrangements.spacedBy(12)
        ) {
            KComponent("AvatarBadge", mapOf("initials" to userName, "badgeCount" to 0, "size" to 80))
            KText(userName, fontSize = 22, fontWeight = KFontWeights.Bold, color = KColors.OnSurface)
            KText("$userName@ketoy.dev", fontSize = 14, color = KColors.OnSurfaceVariant)
        }

        KSpacer(height = 28)

        // ── Account section ───────────────────────────────
        KText(
            "Settings",
            fontSize = 23,
            fontWeight = KFontWeights.SemiBold,
            color = KColors.OnSurfaceVariant,
            modifier = kModifier(padding = kPadding(horizontal = 20, bottom = 8))
        )

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            verticalArrangement = KArrangements.spacedBy(8)
        ) {

        }

        KSpacer(height = 20)

        // ── Preferences section ───────────────────────────────
        KText(
            "Preferences",
            fontSize = 13,
            fontWeight = KFontWeights.SemiBold,
            color = KColors.OnSurfaceVariant,
            modifier = kModifier(padding = kPadding(horizontal = 20, bottom = 8))
        )

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            verticalArrangement = KArrangements.spacedBy(8)
        ) {
            // Dark mode toggle
            KCard(
                modifier = kModifier(fillMaxWidth = 1f),
                containerColor = KColors.SurfaceContainerLow,
                shape = KShapes.Rounded16,
                elevation = 0,
                onClickAction = KFunctionAction("toggleDarkMode"),
                actionId = "profile_dark_mode"
            ) {
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14)),
                    horizontalArrangement = KArrangements.SpaceBetween,
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KRow(horizontalArrangement = KArrangements.spacedBy(14), verticalAlignment = KAlignments.CenterVertically) {
                        KBox(modifier = kModifier(size = 42, background = KColors.PrimaryContainer, shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                            KIcon(icon = darkModeIcon, size = 22, color = KColors.Primary)
                        }
                        KText("Dark Mode", fontSize = 15, fontWeight = KFontWeights.Medium, color = KColors.OnSurface)
                    }
                    // Show current state indicator
                    KCard(
                        modifier = kModifier(height = 28),
                        containerColor = darkModeToggleBg,
                        shape = KShapes.rounded(50),
                        elevation = 0
                    ) {
                        KBox(modifier = kModifier(fillMaxHeight = 1f, padding = kPadding(horizontal = 12)), contentAlignment = KAlignments.Center) {
                            KText(darkModeLabel, fontSize = 11, fontWeight = KFontWeights.Bold, color = darkModeToggleTextColor)
                        }
                    }
                }
            }

            profileItem(
                KIcons.Language, "Language", KColors.SurfaceContainerLow, KColors.Primary,
                actionId = "profile_language",
                onClickAction = KFunctionAction("showToast", "message" to "Language settings")
            )

            profileItem(
                KIcons.Lock, "Privacy", KColors.SurfaceContainerLow, KColors.Primary,
                actionId = "profile_privacy",
                onClickAction = KFunctionAction("showToast", "message" to "Privacy settings")
            )
        }

        KSpacer(height = 28)

        // ── Sign out ──────────────────────────────────────
        KBox(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20))) {
            KCard(
                modifier = kModifier(fillMaxWidth = 1f),
                containerColor = KColors.ErrorContainer,
                shape = KShapes.Rounded16,
                elevation = 0,
                onClickAction = KFunctionAction("logout"),
                actionId = "profile_logout"
            ) {
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)),
                    horizontalArrangement = KArrangements.Center,
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KRow(horizontalArrangement = KArrangements.spacedBy(8), verticalAlignment = KAlignments.CenterVertically) {
                        KIcon(icon = KIcons.ExitToApp, size = 20, color = KColors.OnErrorContainer)
                        KText("Sign Out", fontSize = 15, fontWeight = KFontWeights.SemiBold, color = KColors.OnErrorContainer)
                    }
                }
            }
        }

        KSpacer(height = 20)
    }
}

fun buildFinanceDashboard(): KNode = ketoyRoot {

    KColumn(
        modifier = kModifier(
            fillMaxSize = 1f,
            padding = kPadding(all = 16)
        ),
        verticalArrangement = KArrangements.spacedBy(20)
    ) {

        // 🔹 Header
        KRow(
            modifier = kModifier(fillMaxWidth = 1f),
            horizontalArrangement = KArrangements.SpaceBetween,
            verticalAlignment = KAlignments.CenterVertically
        ) {

            KRow(
                verticalAlignment = KAlignments.CenterVertically,
                horizontalArrangement = KArrangements.spacedBy(12)
            ) {

                KBox(
                    modifier = kModifier(
                        size = 48,
                        shape = KShapes.Circle,
                        background = KColors.Blue
                    ),
                    contentAlignment = KAlignments.Center
                ) {
                    KText(
                        text = "AD",
                        color = KColors.White,
                        fontWeight = KFontWeights.Bold
                    )
                }

                KText(
                    text = "Aditya",
                    fontSize = 20,
                    fontWeight = KFontWeights.Bold,
                    color = KColors.OnSurface
                )
            }

            KIcon(
                icon = KIcons.NotificationsNone,
                size = 24,
                color = KColors.OnSurface
            )
        }

        // 🔹 Total Balance Card
        KCard(
            modifier = kModifier(
                fillMaxWidth = 1f,
                padding = kPadding(vertical = 4)
            ),
            shape = KShapes.Rounded24,
            containerColor = KColors.SurfaceContainerHigh,
            elevation = 2
        ) {

            KColumn(
                modifier = kModifier(padding = kPadding(all = 20)),
                verticalArrangement = KArrangements.spacedBy(8)
            ) {

                KText(
                    text = "Total Balance",
                    fontSize = 14,
                    color = KColors.OnSurfaceVariant
                )

                KText(
                    text = "$24,567.80",
                    fontSize = 32,
                    fontWeight = KFontWeights.Bold,
                    color = KColors.OnSurface
                )

                KRow(
                    verticalAlignment = KAlignments.CenterVertically,
                    horizontalArrangement = KArrangements.spacedBy(6)
                ) {
                    KIcon(
                        icon = KIcons.TrendingUp,
                        size = 16,
                        color = KColors.Green
                    )
                    KText(
                        text = "+12.5% this month",
                        fontSize = 14,
                        color = KColors.Green
                    )
                }
            }
        }

        // 🔹 Actions Row
        KRow(
            modifier = kModifier(fillMaxWidth = 1f),
            horizontalArrangement = KArrangements.SpaceBetween
        ) {

            fun actionItem(icon: String, label: String, bg: String) {
                KColumn(
                    horizontalAlignment = KAlignments.CenterHorizontally,
                    verticalArrangement = KArrangements.spacedBy(8)
                ) {
                    KBox(
                        modifier = kModifier(
                            size = 64,
                            shape = KShapes.Rounded20,
                            background = bg
                        ),
                        contentAlignment = KAlignments.Center
                    ) {
                        KIcon(icon = icon, size = 24, color = KColors.White)
                    }

                    KText(
                        text = label,
                        fontSize = 14,
                        color = KColors.OnSurfaceVariant
                    )
                }
            }

            actionItem(KIcons.Send, "Send", KColors.Blue)
            actionItem(KIcons.Download, "Receive", KColors.Gray)
            actionItem(KIcons.SwapHoriz, "Swap", KColors.Purple)
            actionItem(KIcons.MoreHoriz, "More", KColors.SurfaceVariant)
        }

        // 🔹 Income Card
        KCard(
            modifier = kModifier(fillMaxWidth = 1f),
            shape = KShapes.Rounded24,
            containerColor = KColors.SurfaceContainerHigh,
            elevation = 2
        ) {

            KRow(
                modifier = kModifier(
                    padding = kPadding(all = 20)
                ),
                horizontalArrangement = KArrangements.SpaceBetween,
                verticalAlignment = KAlignments.CenterVertically
            ) {

                KColumn(
                    verticalArrangement = KArrangements.spacedBy(6)
                ) {

                    KRow(
                        verticalAlignment = KAlignments.CenterVertically,
                        horizontalArrangement = KArrangements.spacedBy(8)
                    ) {
                        KBox(
                            modifier = kModifier(
                                size = 36,
                                shape = KShapes.Rounded12,
                                background = KColors.Green
                            ),
                            contentAlignment = KAlignments.Center
                        ) {
                            KIcon(
                                icon = KIcons.TrendingUp,
                                size = 16,
                                color = KColors.White
                            )
                        }

                        KText(
                            text = "Income",
                            fontSize = 14,
                            color = KColors.OnSurfaceVariant
                        )
                    }

                    KText(
                        text = "$8,240.00",
                        fontSize = 22,
                        fontWeight = KFontWeights.Bold,
                        color = KColors.OnSurface
                    )
                }

                KText(
                    text = "+8.2%",
                    fontSize = 14,
                    color = KColors.Green
                )
            }
        }

        // 🔹 Expenses Overview Title
        KText(
            text = "Expenses Overview",
            fontSize = 18,
            fontWeight = KFontWeights.Bold,
            color = KColors.OnSurface
        )

        // 🔹 Bottom Cards
        KRow(
            modifier = kModifier(fillMaxWidth = 1f),
            horizontalArrangement = KArrangements.spacedBy(12)
        ) {

            fun statCard(
                title: String,
                amount: String,
                change: String,
                icon: String,
                color: String
            ) {
                KCard(
                    modifier = kModifier(weight = 1f),
                    shape = KShapes.Rounded20,
                    containerColor = KColors.SurfaceContainer,
                    elevation = 1
                ) {

                    KColumn(
                        modifier = kModifier(padding = kPadding(all = 16)),
                        verticalArrangement = KArrangements.spacedBy(8)
                    ) {

                        KRow(
                            horizontalArrangement = KArrangements.SpaceBetween,
                            verticalAlignment = KAlignments.CenterVertically
                        ) {
                            KIcon(icon = icon, size = 18, color = color)

                            KText(
                                text = change,
                                fontSize = 12,
                                color = color
                            )
                        }

                        KText(
                            text = title,
                            fontSize = 14,
                            color = KColors.OnSurfaceVariant
                        )

                        KText(
                            text = amount,
                            fontSize = 18,
                            fontWeight = KFontWeights.Bold,
                            color = KColors.OnSurface
                        )
                    }
                }
            }

            statCard(
                "Expenses",
                "$3,820.00",
                "-3.1%",
                KIcons.TrendingDown,
                KColors.Red
            )

            statCard(
                "Savings",
                "$18,240.00",
                "+5.4%",
                KIcons.TrendingUp,
                KColors.Green
            )
        }
    }
}
