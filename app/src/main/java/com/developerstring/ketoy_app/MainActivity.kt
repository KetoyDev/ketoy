package com.developerstring.ketoy_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.developerstring.ketoy.Ketoy
import com.developerstring.ketoy.cloud.KetoyCloud
import com.developerstring.ketoy.cloud.KetoyCloudConfig
import com.developerstring.ketoy.cloud.cache.KetoyCacheConfig
import com.developerstring.ketoy.cloud.cache.KetoyCacheStrategy
import com.developerstring.ketoy_app.components.AppCustomComponents
import com.developerstring.ketoy_app.components.AppSideDrawer
import com.developerstring.ketoy.devtools.KetoyDevWrapper
import com.developerstring.ketoy.navigation.*
import com.developerstring.ketoy.theme.KetoyThemeMode
import com.developerstring.ketoy.theme.KetoyThemeProvider
import com.developerstring.ketoy_app.ui.theme.KetoyTheme
import com.developerstring.ketoy.util.KColors
import com.developerstring.ketoy.util.KIcons
import com.developerstring.ketoy.util.resolveIcon
import com.developerstring.ketoy_app.screens.AnalyticsScreen
import com.developerstring.ketoy_app.screens.AppNavGraphs
import com.developerstring.ketoy_app.screens.CardsScreen
import com.developerstring.ketoy_app.screens.DemoNavScreen
import com.developerstring.ketoy_app.screens.HistoryScreen
import com.developerstring.ketoy_app.screens.HomeScreen
import com.developerstring.ketoy_app.screens.ProfileScreen
import com.developerstring.ketoy_app.viewmodel.MainViewModel

/**
 * Main entry point — wires Ketoy SDK, ViewModel, custom components,
 * function registry, navigation, and theme together.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ── SDK initialisation ────────────────────────────
        Ketoy.initialize(
            context = applicationContext,
            cloudConfig = KetoyCloudConfig(
                apiKey = "api",
                packageName = "package",
                baseUrl = "url"
            ),
            cacheConfig = KetoyCacheConfig(
                strategy = KetoyCacheStrategy.NETWORK_FIRST,
            )
        )

        // ── Custom composable components ──────────────────
        AppCustomComponents.registerAll()

        // ── Register all nav graphs (local-first) ────────
        AppNavGraphs.registerAll()

        setContent {
            val vm: MainViewModel = viewModel()

            // Register ViewModel functions and sync data variables
            LaunchedEffect(Unit) { vm.registerFunctions() }
            // Re-sync variables whenever ViewModel state changes
            LaunchedEffect(vm.userName, vm.totalBalance, vm.income, vm.expenses, vm.savings, vm.notificationCount) {
                vm.syncVariables()
            }

            // ── Fetch cloud nav graphs (overwrites local when available) ──
            LaunchedEffect(Unit) {
                if (Ketoy.isCloudEnabled()) {
                    KetoyCloud.fetchNavGraph("nav_main")
                    KetoyCloud.fetchNavGraph("nav_demo")
                }
            }

            // Observe dark mode from ViewModel
            val isDark = vm.isDarkMode

            KetoyTheme(darkTheme = isDark) {
                KetoyThemeProvider(
                    themeMode = if (isDark) KetoyThemeMode.Dark else KetoyThemeMode.Light
                ) {
                    KetoyDevWrapper {
                        MainApp(vm, isDark)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Main scaffold with bottom nav, side drawer, and toast observer
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(vm: MainViewModel, isDark: Boolean) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // ── Toast observer ────────────────────────────────────
    val toastMsg = vm.toastMessage
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.consumeToast()
        }
    }

    // ── Side drawer state ─────────────────────────────────
    var drawerVisible by remember { mutableStateOf(false) }

    // Bottom-bar destinations from nav graph (priority: dev → cloud → local)
    val navGraph = KetoyNavDevOverrides.overrides["main"]
        ?: KetoyCloudNavOverrides.overrides["main"]
        ?: KetoyNavRegistry.get("main")
    val routeRemaps = remember(navGraph) { navGraph?.buildActionRemaps() ?: emptyMap() }
    val bottomDests = remember(navGraph) {
        navGraph?.destinations?.filter { it.route != "demo_nav" } ?: emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { drawerVisible = true }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomDests.forEach { dest ->
                        // Resolve via action remap: go_{id} → actual route
                        val targetRoute = routeRemaps["go_${dest.resolvedId}"] ?: dest.route
                        val selected = currentRoute == targetRoute

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val iconRef = if (selected) dest.selectedIcon else dest.icon
                                val imageVector = resolveIcon(iconRef)
                                if (imageVector != null) {
                                    Icon(
                                        imageVector = imageVector,
                                        contentDescription = dest.label
                                    )
                                }
                            },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->

            // ── Navigation host ───────────────────────────
            KetoyNavHost(
                startRoute = "home",
                navHostName = "main",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                navController = navController
            ) {

                composable("home") {
                    HomeScreen(
                        userName = vm.userName,
                        totalBalance = "\$${"%,.2f".format(vm.totalBalance)}",
                        income = "\$${"%,.2f".format(vm.income)}",
                        expenses = "\$${"%,.2f".format(vm.expenses)}",
                        savings = "\$${"%,.2f".format(vm.savings)}",
                        notificationCount = vm.notificationCount,
                        isDark = isDark,
                    )
                }

                composable("analytics") {
                    AnalyticsScreen(
                        income = "\$${"%,.2f".format(vm.income)}",
                        expenses = "\$${"%,.2f".format(vm.expenses)}",
                        savings = "\$${"%,.2f".format(vm.savings)}",

                    )
                }

                composable("cards") {
                    CardsScreen(
                        selectedCardIndex = vm.selectedCardIndex,
                    )
                }

                composable("history") {
                    HistoryScreen()
                }

                composable("profile") {
                    val darkMode = vm.isDarkMode
                    ProfileScreen(
                        userName = vm.userName,
                        darkModeIcon = if (darkMode) KIcons.DarkMode else KIcons.LightMode,
                        darkModeLabel = if (darkMode) "ON" else "OFF",
                        darkModeToggleBg = if (darkMode) KColors.Primary else KColors.Outline,
                        darkModeToggleTextColor = if (darkMode) KColors.OnPrimary else "#FFFFFF",
                    )
                }

                composable("demo_nav") {
                    DemoNavScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // ── Side drawer overlay ───────────────────────────
        AppSideDrawer(
            visible = drawerVisible,
            currentRoute = currentRoute,
            onDismiss = { drawerVisible = false },
            onNavigate = { route ->
                drawerVisible = false
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onAction = { label ->
                drawerVisible = false
                when (label) {
                    "Dark Mode" -> vm.isDarkMode.let { /* toggle handled by drawer */ }
                    "Log Out" -> com.developerstring.ketoy.registry.KetoyFunctionRegistry.call("logout")
                    else -> Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
