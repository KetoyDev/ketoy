package com.developerstring.ketoy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.developerstring.ketoy.components.AppSideDrawer
import com.developerstring.ketoy.navigation.Screen
import com.developerstring.ketoy.navigation.bottomNavScreens
import com.developerstring.ketoy.screens.*
import com.developerstring.ketoy.ui.theme.KetoyTheme
import androidx.navigation.NavDestination.Companion.hierarchy

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Ketoy.initialize()

        setContent {
            KetoyTheme {
                MainApp()
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainApp() {
        val navController = rememberNavController()
        var showDrawer by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {

            // ── Main scaffold ───────────────────────────
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Ketoy Demo") },
                        navigationIcon = {
                            IconButton(onClick = { showDrawer = !showDrawer }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { toast("Notifications") }) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                            }
                            IconButton(onClick = { toast("Search") }) {
                                Icon(Icons.Outlined.Search, contentDescription = "Search")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    NavigationBar {
                        bottomNavScreens.forEach { screen ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        if (isSelected) screen.selectedIcon else screen.icon,
                                        contentDescription = screen.label
                                    )
                                },
                                label = { Text(screen.label) }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (!showDrawer) {
                        FloatingActionButton(
                            onClick = { toast("New Transaction") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Home.route) { HomeScreen() }
                    composable(Screen.Analytics.route) { AnalyticsScreen() }
                    composable(Screen.Cards.route) { CardsScreen() }
                    composable(Screen.Profile.route) { ProfileScreen() }
                }
            }

            // ── Custom side drawer overlay ──────────────
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            AppSideDrawer(
                visible = showDrawer,
                currentRoute = currentRoute,
                onDismiss = { showDrawer = false },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    showDrawer = false
                },
                onAction = { label ->
                    toast(label)
                    showDrawer = false
                }
            )
        }
    }
}
