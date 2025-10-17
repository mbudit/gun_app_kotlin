package com.example.gun_app_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.gun_app_kotlin.ui.theme.GunAppTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gun_app_kotlin.ui.screens.HomeScreen
import com.example.gun_app_kotlin.ui.screens.SecondScreen
import com.example.gun_app_kotlin.ui.screens.ThirdScreen
import com.example.gun_app_kotlin.ui.screens.EpcScanScreen


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

    NavHost(navController = navController, startDestination = "home") {
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
