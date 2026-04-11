package com.example.gun_app_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import com.example.gun_app_kotlin.ui.theme.GunAppTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gun_app_kotlin.ui.screens.HomeScreen
import com.example.gun_app_kotlin.ui.screens.SecondScreen
import com.example.gun_app_kotlin.ui.screens.ThirdScreen
import com.example.gun_app_kotlin.ui.screens.EpcScanScreen
import com.example.gun_app_kotlin.ui.screens.FifthScreen
import com.example.gun_app_kotlin.ui.screens.FourthScreen
import com.example.gun_app_kotlin.ui.screens.LoginScreen
import com.example.gun_app_kotlin.ui.screens.LoginViewModel
import com.example.gun_app_kotlin.ui.screens.LoginViewModelFactory
import com.example.gun_app_kotlin.ui.screens.RegisterScreen
import com.example.gun_app_kotlin.ui.screens.SessionViewModel
import com.example.gun_app_kotlin.ui.screens.SettingsScreen
import com.example.gun_app_kotlin.ui.screens.SixthScreen
import kotlinx.coroutines.launch
import com.example.gun_app_kotlin.data.AppDatabase
import com.example.gun_app_kotlin.data.LinenRepository
import com.example.gun_app_kotlin.data.ServerConfigManager
import com.example.gun_app_kotlin.network.ApiClient
import com.example.gun_app_kotlin.network.WebSocketManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize server config first so ApiClient and WebSocket use the saved URL
        ServerConfigManager.init(applicationContext)
        ApiClient.updateBaseUrl()

        val db = AppDatabase.getDatabase(applicationContext)
        val linenRepository = LinenRepository(
            linenDao = db.linenDao(),
            batchInDao = db.batchInDao(),
            batchInDetailDao = db.batchInDetailDao(),
            apiService = ApiClient.apiService,
            batchUsageDao = db.batchUsageDao(),
            batchUsageDetailDao = db.batchUsageDetailDao()
        )
        // Initialize the WebSocketManager once.
        WebSocketManager.init(linenRepository)
        // --- END: WebSocket Initialization ---

        setContent {
            GunAppTheme {
                AppNavHost()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Clean up the connection when the app is fully closed
        WebSocketManager.close()
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sessionViewModel: SessionViewModel = viewModel()

    Scaffold (snackbarHost = { SnackbarHost(hostState = SnackbarHostState()) } ) { innerPadding ->
        NavHost(navController = navController, startDestination = "login", modifier = Modifier.padding(innerPadding)) {
            composable("login") {
                // --- 2. Pass the SessionViewModel to the LoginScreen factory ---
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(
                        LocalContext.current.applicationContext,
                        sessionViewModel
                    )
                )

                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    // Pass the created viewModel to the screen
                    loginViewModel = loginViewModel
                )
            }

            // Add the new register composable
            composable("register") {
                RegisterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRegistrationSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Registration successful! Please log in.")
                        }
                        navController.popBackStack() // Go back to the login screen
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    navController = navController,
                    sessionViewModel = sessionViewModel,
                    onNavigateToSettings = { navController.navigate("settings_screen") } // Add this line
                )
            }
            composable("second_screen") {
                SecondScreen(onNavigateUp = { navController.navigateUp() } ,sessionViewModel = sessionViewModel)
            }
            composable("third_screen") {
                ThirdScreen(onNavigateUp = { navController.navigateUp() }, sessionViewModel = sessionViewModel)
            }
            composable("fourth_screen") {
                FourthScreen(onNavigateUp = { navController.navigateUp() }, sessionViewModel = sessionViewModel)
            }
            composable("fifth_screen") {
                FifthScreen(
                    onNavigateUp = { navController.navigateUp() },
                    // --- THIS IS THE CHANGE ---
                    // Add the navigation action here
                    onBatchClicked = { batchId ->
                        navController.navigate("sixth_screen/$batchId")
                    }
                )
            }
            composable(
                route = "sixth_screen/{batchId}",
                arguments = listOf(navArgument("batchId") { type = NavType.StringType })
            ) { backStackEntry ->
                SixthScreen(
                    onNavigateUp = { navController.navigateUp() },
                    batchId = backStackEntry.arguments?.getString("batchId")
                )
            }
            composable("settings_screen") {
                SettingsScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable("epc_scan_screen") {
                EpcScanScreen(onNavigateUp = { navController.navigateUp() })
            }
        }
    }
}
