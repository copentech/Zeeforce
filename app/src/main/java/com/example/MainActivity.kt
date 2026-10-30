package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.UserRole
import com.example.data.repository.SalesForceRepository
import com.example.ui.screens.*
import com.example.ui.theme.ZayedSalesForceTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = SalesForceRepository.getInstance(applicationContext)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDark) }

            ZayedSalesForceTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SalesForceAppContent(
                        repository = repository,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = it }
                    )
                }
            }
        }
    }
}

@Composable
fun SalesForceAppContent(
    repository: SalesForceRepository,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    var currentUserRole by remember { mutableStateOf<UserRole?>(UserRole.ADMIN) }
    var currentUserEmail by remember { mutableStateOf("admin@zayed.com") }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentUserRole != null && currentRoute != "login") {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "admin_dashboard",
                        onClick = { navController.navigate("admin_dashboard") },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Admin") },
                        modifier = Modifier.testTag("nav_item_admin")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "customer_account",
                        onClick = { navController.navigate("customer_account") },
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Customer") },
                        label = { Text("Customer") },
                        modifier = Modifier.testTag("nav_item_customer")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "commission_slips",
                        onClick = { navController.navigate("commission_slips") },
                        icon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = "Commissions") },
                        label = { Text("Commissions") },
                        modifier = Modifier.testTag("nav_item_commissions")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { navController.navigate("settings") },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "admin_dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    repository = repository,
                    onLoginSuccess = { role, email ->
                        currentUserRole = role
                        currentUserEmail = email
                        val destination = when (role) {
                            UserRole.ADMIN -> "admin_dashboard"
                            UserRole.SALES_REP -> "commission_slips"
                            UserRole.CUSTOMER -> "customer_account"
                            UserRole.SUPPLIER -> "customer_account"
                        }
                        navController.navigate(destination) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("admin_dashboard") {
                AdminDashboardScreen(
                    repository = repository,
                    onNavigateToCommissions = { navController.navigate("commission_slips") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("customer_account") {
                CustomerAccountScreen(
                    repository = repository,
                    customerEmail = currentUserEmail
                )
            }

            composable("commission_slips") {
                CommissionSlipsScreen(
                    repository = repository,
                    salesRepId = "rep_101",
                    salesRepName = "Ahmad Al-Mansoor"
                )
            }

            composable("settings") {
                AppSettingsScreen(
                    repository = repository,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }
        }
    }
}
