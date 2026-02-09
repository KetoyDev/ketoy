package com.developerstring.ketoy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.developerstring.ketoy.components.AppSideDrawer
import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.dsl.KScaffold
import com.developerstring.ketoy.dsl.KUniversalScope
import com.developerstring.ketoy.model.KModifier as KMod
import com.developerstring.ketoy.model.KTopAppBarColors
import com.developerstring.ketoy.navigation.*
import com.developerstring.ketoy.renderer.JSONStringToUI
import com.developerstring.ketoy.screens.*
import com.developerstring.ketoy.ui.theme.KetoyTheme
import com.developerstring.ketoy.util.KColors
import com.developerstring.ketoy.util.KIcons
import com.developerstring.ketoy.util.KTopAppBarType

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

    @Composable
    fun MainApp() {
        val innerNavController = rememberNavController()
        var showDrawer by remember { mutableStateOf(false) }

        // Track current route for bottom-nav selection
        val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // ── Build Ketoy TopAppBar via DSL ─────────────────────
        val topBarJson = remember {
            KUniversalScope().apply {
                KTopAppBar(
                    type = KTopAppBarType.CenterAligned,
                    colors = KTopAppBarColors(
                        containerColor = KColors.Primary,
                        titleContentColor = KColors.OnPrimary,
                        navigationIconContentColor = KColors.OnPrimary,
                        actionIconContentColor = KColors.OnPrimary
                    ),
                    title = {
                        KText(text = "Ketoy Demo")
                    },
                    navigationIcon = {
                        KAppBarAction(onClick = { showDrawer = !showDrawer }) {
                            KIcon(icon = KIcons.Menu)
                        }
                    },
                    actions = {
                        KAppBarAction(onClick = { toast("Notifications") }) {
                            KIcon(icon = KIcons.Notifications, style = KIcons.STYLE_OUTLINED)
                        }
                        KAppBarAction(onClick = { toast("Search") }) {
                            KIcon(icon = KIcons.Search, style = KIcons.STYLE_OUTLINED)
                        }
                    }
                )
            }.children.first().toJson()
        }

        // ── Build Ketoy NavigationBar via DSL (reactive to route) ──
        val bottomBarJson = KUniversalScope().apply {
            KNavigationBar {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute?.contains(
                        item.route::class.simpleName ?: ""
                    ) == true
                    KNavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            innerNavController.navigate(item.route) {
                                popUpTo(innerNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            KIcon(icon = item.icon)
                        },
                        selectedIcon = {
                            KIcon(icon = item.selectedIcon)
                        },
                        label = {
                            KText(text = item.label)
                        }
                    )
                }
            }
        }.children.first().toJson()

        // ── Build Ketoy FAB via DSL ───────────────────────────
        val fabJson = remember {
            KUniversalScope().apply {
                KFloatingActionButton(
                    onClick = { toast("New Transaction") },
                    containerColor = KColors.Primary,
                    contentColor = KColors.OnPrimary
                ) {
                    KIcon(icon = KIcons.Add)
                }
            }.children.first().toJson()
        }

        Box(modifier = Modifier.fillMaxSize()) {

            // ── Main Scaffold (using Ketoy-rendered slots) ──
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { JSONStringToUI(topBarJson) },
                bottomBar = { JSONStringToUI(bottomBarJson) },
                floatingActionButton = {
                    if (!showDrawer) {
                        JSONStringToUI(fabJson)
                    }
                }
            ) { innerPadding ->

                // ── KetoyNavHost with @Serializable type-safe routes ──
                KetoyNavHost(
                    startRoute = HomeRoute,
                    navController = innerNavController,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    screen<HomeRoute>      { _ -> HomeScreen() }
                    screen<AnalyticsRoute> { _ -> AnalyticsScreen() }
                    screen<CardsRoute>     { _ -> CardsScreen() }
                    screen<ProfileRoute>   { _ -> ProfileScreen() }
                }
            }
            
            // ── Custom side drawer overlay ──────────────
            AppSideDrawer(
                visible = showDrawer,
                currentRoute = currentRoute,
                onDismiss = { showDrawer = false },
                onNavigate = { route ->
                    innerNavController.navigate(route) {
                        popUpTo(innerNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
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
