package com.developerstring.ketoy_app.screens

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.export.ketoyExport
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
            padding = kPadding(top = 100),
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
