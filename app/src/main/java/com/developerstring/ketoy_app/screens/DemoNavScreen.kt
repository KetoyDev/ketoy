package com.developerstring.ketoy_app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.developerstring.ketoy.navigation.KetoyNavHost
import com.developerstring.ketoy.navigation.KetoyNavRegistry
import com.developerstring.ketoy_app.screens.demo.*

/**
 * Self-contained demo screen for **screen-to-screen navigation**.
 *
 * The nav graph (destinations + navigation actions) is defined in
 * [DemoNavGraph] and registered on first composition. The same
 * definition is exported to `nav_demo.json` by the export test.
 *
 * ## Live route editing
 * 1. `./gradlew ketoyExport && ./gradlew ketoyDev`
 * 2. Connect the app to the dev server
 * 3. Edit `ketoy-screens/nav_demo.json`
 * 4. Change any action's `"route"` → navigation updates live
 *
 * In code, screens use:
 * ```kotlin
 * nav?.navigateToRoute(navId = "go_favorites", route = "favorites")
 * ```
 * The `navId` resolves through the JSON remap table. Change
 * `"route": "bookmarks"` in JSON → same code navigates to "bookmarks".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoNavScreen(
    onBack: () -> Unit
) {
    // Register the demo nav graph (main is already registered in MainActivity)
    remember { KetoyNavRegistry.register(AppNavGraphs.demo); true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nav Demo", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        KetoyNavHost(
            startRoute = "explore",
            navHostName = "demo",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("explore") { DemoExploreScreen() }
            composable("favorites") { DemoFavoritesScreen() }
            composable("notifications") { DemoNotificationsScreen() }
            composable("settings") { DemoSettingsScreen() }
        }
    }
}
