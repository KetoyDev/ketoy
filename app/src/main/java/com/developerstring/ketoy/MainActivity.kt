package com.developerstring.ketoy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.developerstring.ketoy.components.AppCustomComponents
import com.developerstring.ketoy.components.AppSideDrawer
import com.developerstring.ketoy.components.TimedKetoyScreen
import com.developerstring.ketoy.devtools.KetoyDevWrapper
import com.developerstring.ketoy.navigation.*
import com.developerstring.ketoy.screens.*
import com.developerstring.ketoy.theme.KetoyThemeMode
import com.developerstring.ketoy.theme.KetoyThemeProvider
import com.developerstring.ketoy.ui.theme.KetoyTheme
import com.developerstring.ketoy.util.resolveIcon
import com.developerstring.ketoy.viewmodel.MainViewModel

/**
 * Main entry point — wires Ketoy SDK, ViewModel, custom components,
 * function registry, navigation, and theme together.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ── SDK initialisation ────────────────────────────
        Ketoy.initialize(context = applicationContext)

        // ── Custom composable components ──────────────────
        AppCustomComponents.registerAll()

        setContent {
            val vm: MainViewModel = viewModel()

            // Register ViewModel functions with the SDK
            LaunchedEffect(Unit) { vm.registerFunctions() }

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
                    bottomNavItems.forEach { item ->
                        val selected = backStackEntry?.destination?.let { dest ->
                            when (item.route) {
                                is HomeRoute -> dest.hasRoute<HomeRoute>()
                                is AnalyticsRoute -> dest.hasRoute<AnalyticsRoute>()
                                is CardsRoute -> dest.hasRoute<CardsRoute>()
                                is HistoryRoute -> dest.hasRoute<HistoryRoute>()
                                is ProfileRoute -> dest.hasRoute<ProfileRoute>()
                                else -> false
                            }
                        } ?: false

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val iconRef = if (selected) item.selectedIcon else item.icon
                                val imageVector = resolveIcon(iconRef)
                                if (imageVector != null) {
                                    Icon(
                                        imageVector = imageVector,
                                        contentDescription = item.label
                                    )
                                }
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->

            // ── Navigation host ───────────────────────────
            KetoyNavHost(
                startRoute = HomeRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                navController = navController
            ) {

                screen<HomeRoute> {
                    TimedKetoyScreen("Home") {
                        buildHomeScreen(
                            userName = vm.userName,
                            totalBalance = "\$${"%,.2f".format(vm.totalBalance)}",
                            income = "\$${"%,.2f".format(vm.income)}",
                            expenses = "\$${"%,.2f".format(vm.expenses)}",
                            savings = "\$${"%,.2f".format(vm.savings)}",
                            notificationCount = vm.notificationCount,
                            isDark = isDark,
                            transactions = vm.transactions.map {
                                Triple(it.title, it.subtitle, it.amount)
                            }
                        )
                    }
                }

                screen<AnalyticsRoute> {
                    TimedKetoyScreen("Analytics") {
                        buildAnalyticsScreen(
                            income = "\$${"%,.2f".format(vm.income)}",
                            expenses = "\$${"%,.2f".format(vm.expenses)}",
                            savings = "\$${"%,.2f".format(vm.savings)}",
                            isDark = isDark
                        )
                    }
                }

                screen<CardsRoute> {
                    TimedKetoyScreen("Cards") {
                        buildCardsScreen(
                            selectedCardIndex = vm.selectedCardIndex,
                            isDark = isDark
                        )
                    }
                }

                screen<HistoryRoute> {
                    TimedKetoyScreen("History") {
                        buildHistoryScreen(
                            transactions = vm.transactions.map {
                                Triple(it.title, it.subtitle, it.amount)
                            },
                            isDark = isDark
                        )
                    }
                }

                screen<ProfileRoute> {
                    TimedKetoyScreen("Profile") {
                        buildProfileScreen(
                            userName = vm.userName,
                            isDark = isDark
                        )
                    }
                }
            }
        }

        // ── Side drawer overlay ───────────────────────────
        AppSideDrawer(
            visible = drawerVisible,
            currentRoute = backStackEntry?.destination?.route,
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
