package com.example.gun_app_kotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.gun_app_kotlin.ui.theme.GunAppTheme

@Composable
fun HomeScreen(navController: NavHostController) {
    // Use a Column to arrange elements vertically with padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes content apart
    ) {
        // App Title at the top
        Text(
            text = "Simtech RFID Scanner",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp)
        )

        // A descriptive card in the middle
        InfoCard()

        // The main action button at the bottom
        GoToScanButton(
            onClick = { navController.navigate("second") }
        )
    }
}

@Composable
fun InfoCard() {
    // OutlinedCard provides a nice border and container for content
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Adds space between items
        ) {
            // An icon to make it more visual
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Scanner Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "High-Performance Scanning",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Press the button below to start the real-time RFID inventory tool.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GoToScanButton(onClick: () -> Unit) {
    // ExtendedFloatingActionButton is prominent and good for primary actions
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(56.dp),
        icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Arrow Forward Icon") },
        text = { Text("Begin Scanning", style = MaterialTheme.typography.labelLarge) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}

// A preview function to see your new design in Android Studio
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GunAppTheme {
        // We use a "dummy" NavController for the preview to work
        HomeScreen(navController = rememberNavController())
    }
}
