package com.babeli.network

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.babeli.network.service.NetworkMonitorService
import com.babeli.network.ui.screens.*
import com.babeli.network.ui.theme.*
import com.babeli.network.viewmodel.NetworkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: NetworkViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.recheckPermission() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
        startService(Intent(this, NetworkMonitorService::class.java))

        setContent {
            BabeliTheme {
                val state by viewModel.state.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgPrimary)
                ) {
                    val navController = rememberNavController()

                    Scaffold(
                        containerColor = BgPrimary,
                        bottomBar = {
                            BabeliNavBar(navController = navController)
                        },
                        contentWindowInsets = WindowInsets(0),
                    ) { padding ->
                        NavHost(
                            navController    = navController,
                            startDestination = "dashboard",
                            modifier         = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .systemBarsPadding(),
                            enterTransition  = { fadeIn() + slideInVertically { it / 10 } },
                            exitTransition   = { fadeOut() },
                        ) {
                            composable("dashboard") {
                                DashboardScreen(
                                    state          = state,
                                    onResetSession = viewModel::resetSession,
                                )
                            }
                            composable("apps") {
                                AppsScreen(
                                    state          = state,
                                    onPeriodChange = viewModel::loadTopApps,
                                )
                            }
                            composable("networks") {
                                NetworksScreen(
                                    state     = state,
                                    onRefresh = viewModel::refreshNetworks,
                                )
                            }
                            composable("speed") {
                                SpeedScreen(state = state)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.recheckPermission()
    }

    private fun requestPermissions() {
        val perms = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_WIFI_STATE)
            add(Manifest.permission.CHANGE_WIFI_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.READ_PHONE_STATE)
            }
        }.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (perms.isNotEmpty()) permissionLauncher.launch(perms.toTypedArray())
    }
}

data class NavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun BabeliNavBar(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        NavItem("dashboard", Icons.Rounded.Home,      "Home"),
        NavItem("speed",     Icons.Rounded.Speed,     "Speed"),
        NavItem("apps",      Icons.Rounded.Apps,      "Apps"),
        NavItem("networks",  Icons.Rounded.Wifi,      "Networks"),
    )

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    NavigationBar(
        containerColor = BgSurface,
        tonalElevation = 0.dp,
        modifier       = Modifier.height(68.dp),
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Cyan.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector        = item.icon,
                            contentDescription = item.label,
                            tint               = if (selected) Cyan else TextHint,
                        )
                    }
                },
                label = {
                    Text(
                        text  = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) Cyan else TextHint,
                        ),
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
            )
        }
    }
}
