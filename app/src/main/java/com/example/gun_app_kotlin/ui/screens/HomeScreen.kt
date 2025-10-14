package com.example.gun_app_kotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.gun_app_kotlin.ui.theme.GunAppTheme

@Composable
fun HomeScreen(
    navController: NavHostController,
    // Get an instance of the ScanViewModel to access the sync function
    scanViewModel: ScanViewModel = viewModel(factory = ScanViewModelFactory(LocalContext.current.applicationContext))
) {
    // Observe the UI state to know if syncing is in progress
    val uiState by scanViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title
        Text(
            text = "Simtech RFID Scanner",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp),
            textAlign = TextAlign.Center
        )

        // Descriptive card
        InfoCard()

        // Container for the action buttons at the bottom
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // NEW: Show a progress indicator and disable buttons while syncing
            AnimatedVisibility(visible = uiState.isSyncing) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // NEW: Sync Button
                SyncDataButton(
                    modifier = Modifier.weight(1f),
                    onClick = { scanViewModel.syncData() },
                    isEnabled = !uiState.isSyncing // Disable button while syncing
                )
                // Existing Scan Button
                GoToScanButton(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("second") },
                    isEnabled = !uiState.isSyncing // Disable button while syncing
                )
            }
        }
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
fun GoToScanButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    // We use a regular Button now for better side-by-side layout
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Arrow Forward Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Scan")
    }
}

// NEW: A dedicated composable for the Sync button
@Composable
fun SyncDataButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    // OutlinedButton provides a secondary action style
    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(Icons.Default.Star, "Sync Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Sync")
    }
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
