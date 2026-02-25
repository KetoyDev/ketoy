package com.developerstring.ketoy_app.screens

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.screen.KetoyContent
import com.developerstring.ketoy.screen.ProvideKetoyScreen
import com.developerstring.ketoy.util.*
import com.developerstring.ketoy_app.viewmodel.MainViewModel

/**
 * Profile screen composable — wraps the DSL builder as a `@KScreen`
 * with a single `KetoyContent` child for cloud / hot-reload support.
 */
@Composable
fun ProfileScreen(
    userName: String,
    isDark: Boolean,
) {
    ProvideKetoyScreen(screenName = "profile") {
        KetoyContent(
            nodeBuilder = {
                buildProfileScreen(userName = userName, isDark = isDark)
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
    isDark: Boolean
): com.developerstring.ketoy.model.KNode = ketoyRoot {

    val c = AppColors

    KColumn(
        modifier = kModifier(
            fillMaxSize = 1f,
            padding = kPadding(top = 16),
            background = if (isDark) "#1C1B1F" else "#FFFBFE",
            verticalScroll = true
        ),
        verticalArrangement = KArrangements.spacedBy(0)
    ) {

        // ── Avatar + name ─────────────────────────────────
        KColumn(
            modifier = kModifier(fillMaxWidth = 1f),
            horizontalAlignment = KAlignments.CenterHorizontally,
            verticalArrangement = KArrangements.spacedBy(12)
        ) {
            KComponent("AvatarBadge", mapOf("initials" to userName.take(2), "badgeCount" to 0, "size" to 80))
            KText(userName, fontSize = 22, fontWeight = KFontWeights.Bold, color = c.onSurface(isDark))
            KText("$userName@ketoy.dev", fontSize = 14, color = c.onSurfaceVariant(isDark))
        }

        KSpacer(height = 28)

        // ── Account section ───────────────────────────────
        KText(
            "Prakher",
            fontSize = 23,
            fontWeight = KFontWeights.SemiBold,
            color = c.onSurfaceVariant(isDark),
            modifier = kModifier(padding = kPadding(horizontal = 20, bottom = 8))
        )

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            verticalArrangement = KArrangements.spacedBy(8)
        ) {

        }

        KSpacer(height = 20)

        // ── Preferences section ───────────────────────────
        KText(
            "Preferences",
            fontSize = 13,
            fontWeight = KFontWeights.SemiBold,
            color = c.onSurfaceVariant(isDark),
            modifier = kModifier(padding = kPadding(horizontal = 20, bottom = 8))
        )

        KColumn(
            modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20)),
            verticalArrangement = KArrangements.spacedBy(8)
        ) {
            // Dark mode toggle
            KCard(
                modifier = kModifier(fillMaxWidth = 1f),
                containerColor = c.surfaceContainerLow(isDark),
                shape = KShapes.Rounded16,
                elevation = 0,
                onClick = { KFunctionCall("toggleDarkMode") },
                actionId = "profile_dark_mode"
            ) {
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 16, vertical = 14)),
                    horizontalArrangement = KArrangements.SpaceBetween,
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KRow(horizontalArrangement = KArrangements.spacedBy(14), verticalAlignment = KAlignments.CenterVertically) {
                        KBox(modifier = kModifier(size = 42, background = c.primaryContainer(isDark), shape = KShapes.Rounded12), contentAlignment = KAlignments.Center) {
                            KIcon(icon = if (isDark) KIcons.DarkMode else KIcons.LightMode, size = 22, color = c.primary(isDark))
                        }
                        KText("Dark Mode", fontSize = 15, fontWeight = KFontWeights.Medium, color = c.onSurface(isDark))
                    }
                    // Show current state indicator
                    KCard(
                        modifier = kModifier(height = 28),
                        containerColor = if (isDark) c.primary(isDark) else c.outline(isDark),
                        shape = KShapes.rounded(50),
                        elevation = 0
                    ) {
                        KBox(modifier = kModifier(fillMaxHeight = 1f, padding = kPadding(horizontal = 12)), contentAlignment = KAlignments.Center) {
                            KText(if (isDark) "ON" else "OFF", fontSize = 11, fontWeight = KFontWeights.Bold, color = if (isDark) c.onPrimary(isDark) else "#FFFFFF")
                        }
                    }
                }
            }

            profileItem(
                KIcons.Language, "Language", c.surfaceContainerLow(isDark), c.primary(isDark),
                isDark = isDark, actionId = "profile_language"
            ) { KFunctionCall("showToast", "message" to "Language settings") }

            profileItem(
                KIcons.Lock, "Privacy", c.surfaceContainerLow(isDark), c.primary(isDark),
                isDark = isDark, actionId = "profile_privacy"
            ) { KFunctionCall("showToast", "message" to "Privacy settings") }
        }

        KSpacer(height = 28)

        // ── Sign out ──────────────────────────────────────
        KBox(modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(horizontal = 20))) {
            KCard(
                modifier = kModifier(fillMaxWidth = 1f),
                containerColor = c.errorContainer(isDark),
                shape = KShapes.Rounded16,
                elevation = 0,
                onClick = { KFunctionCall("logout") },
                actionId = "profile_logout"
            ) {
                KRow(
                    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(all = 16)),
                    horizontalArrangement = KArrangements.Center,
                    verticalAlignment = KAlignments.CenterVertically
                ) {
                    KRow(horizontalArrangement = KArrangements.spacedBy(8), verticalAlignment = KAlignments.CenterVertically) {
                        KIcon(icon = KIcons.ExitToApp, size = 20, color = c.onErrorContainer(isDark))
                        KText("Sign Out", fontSize = 15, fontWeight = KFontWeights.SemiBold, color = c.onErrorContainer(isDark))
                    }
                }
            }
        }

        KSpacer(height = 20)
    }
}
