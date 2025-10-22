package com.example.gun_app_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gun_app_kotlin.ui.theme.GunAppTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gun_app_kotlin.ui.screens.HomeScreen
import com.example.gun_app_kotlin.ui.screens.SecondScreen
import com.example.gun_app_kotlin.ui.screens.ThirdScreen
import com.example.gun_app_kotlin.ui.screens.EpcScanScreen
import com.example.gun_app_kotlin.ui.screens.LoginScreen
import com.example.gun_app_kotlin.ui.screens.RegisterScreen
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GunAppTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold (snackbarHost = { SnackbarHost(hostState = SnackbarHostState()) } ) { innerPadding ->
        NavHost(navController = navController, startDestination = "login", modifier = Modifier.padding(innerPadding)) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { // Pass the navigation action
                        navController.navigate("register")
                    }
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
                HomeScreen(navController)
            }
            composable("second_screen") {
                SecondScreen()
            }
            composable("third_screen") {
                ThirdScreen(onNavigateUp = { navController.navigateUp() })
            }
            composable("epc_scan_screen") {
                EpcScanScreen(onNavigateUp = { navController.navigateUp() })
            }
        }
    }
}
