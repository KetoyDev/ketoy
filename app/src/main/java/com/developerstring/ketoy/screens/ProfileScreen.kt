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
import com.developerstring.ketoy.screens.AppColors.surfaceContainerLow
import com.developerstring.ketoy.util.*

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    TimedKetoyScreen(screenName = "Profile") { buildProfileUI { toast(it) } }
}

private fun buildProfileUI(toast: (String) -> Unit): KNode {
    return KColumn(modifier = kModifier(fillMaxSize = 1f)) {
        KLazyColumn(
            modifier = kModifier(fillMaxSize = 1f),
            verticalArrangement = "spacedBy_8",
            contentPadding = kPadding(horizontal = 16, vertical = 16)
        ) {
            item {
                // Profile header
                KColumn(
                    modifier = kModifier(fillMaxWidth = 1f, padding = kPadding(vertical = 16)),
                    horizontalAlignment = KAlignments.CenterHorizontally,
                    verticalArrangement = "spacedBy_8"
                ) {
                    KBox(
                        modifier = kModifier(size = 96, background = "#EADDFF", shape = KShapes.Circle),
                        contentAlignment = KAlignments.Center
                    ) {
                        KIcon(icon = KIcons.Person, size = 56, color = primary)
                    }
                    KText("Aditya", fontSize = 22, fontWeight = KFontWeights.Bold, color = onSurface)
                    KText("aditya@ketoy.dev", fontSize = 14, color = onSurfaceVariant)
                }

                KSpacer(height = 8)

                // Settings list
                profileItem(KIcons.Person, "Edit Profile", surfaceContainerLow, primary) { toast("Edit Profile") }
                profileItem(KIcons.Notifications, "Notifications", surfaceContainerLow, primary) { toast("Notifications") }
                profileItem(KIcons.Lock, "Security", surfaceContainerLow, primary) { toast("Security") }
                profileItem(KIcons.Language, "Language", surfaceContainerLow, primary) { toast("Language") }
                profileItem(KIcons.DarkMode, "Dark Mode", surfaceContainerLow, primary) { toast("Dark Mode") }
                profileItem(KIcons.HelpOutline, "Help & Support", surfaceContainerLow, primary) { toast("Help") }
                profileItem(KIcons.Info, "About", surfaceContainerLow, primary) { toast("About") }

                KSpacer(height = 16)

                KButton(
                    modifier = kModifier(fillMaxWidth = 1f),
                    containerColor = "#F9DEDC",
                    contentColor = "#410E0B",
                    shape = KShapes.Rounded16,
                    onClick = { toast("Logged out") }
                ) {
                    KIcon(icon = KIcons.ExitToApp, size = 20, color = "#BA1A1A")
                    KSpacer(width = 8)
                    KText("Log Out", fontWeight = KFontWeights.SemiBold, color = "#BA1A1A")
                }

                KSpacer(height = 72)
            }
        }
    }
}
