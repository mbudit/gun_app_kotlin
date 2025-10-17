package com.example.gun_app_kotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext))
) {
    val uiState by homeViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title
        Text(
            text = "RFID Scanner",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier.weight(1f), // This Box will now expand to fill all available space
            contentAlignment = Alignment.Center // Center the InfoCard within the expanded Box
        ) {
            InfoCard()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(visible = uiState.isSyncing) {
                CircularProgressIndicator()
            }

            // Masuk gudang button
            GoToScanButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("second_screen") },
                isEnabled = !uiState.isSyncing
            )

            GoToThirdScreenButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("third_screen") },
                isEnabled = !uiState.isSyncing
            )
            Button(
                onClick = { navController.navigate("epc_scan_screen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Info, "Info Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Identifikasi EPC")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SyncDataButton(
                    modifier = Modifier.weight(1f),
                    onClick = { homeViewModel.syncData() },
                    isEnabled = !uiState.isSyncing
                )

                ClearDataButton(
                    modifier = Modifier.weight(1f),
                    onClick = { homeViewModel.clearData() },
                    isEnabled = !uiState.isSyncing
                )
            }
        }
    }
}

@Composable
fun InfoCard() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Scanner Icon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Alat Pemindai RFID",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Halo petugas laundry~!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GoToScanButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Arrow Forward Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Masuk Gudang")
    }
}

@Composable
fun GoToThirdScreenButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    FilledTonalButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Arrow Forward Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Keluar Gudang")
    }
}

@Composable
fun SyncDataButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(Icons.Default.Sync, "Sync Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Sinkron Data")
    }
}

@Composable
fun ClearDataButton(modifier: Modifier = Modifier, onClick: () -> Unit, isEnabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(Icons.Default.Delete, "Clear Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Clear Cache")
    }
}
