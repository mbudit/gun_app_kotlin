package com.example.gun_app_kotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FifthScreen(
    onNavigateUp: () -> Unit,
    onBatchClicked: (String) -> Unit,
    viewModel: FifthScreenViewModel = viewModel(factory = FifthScreenViewModelFactory(LocalContext.current.applicationContext))
) {
    // The UI now observes the entire FifthScreenState
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Serah Terima") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate back")
                    }
                },
                actions = {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.syncData() }) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Data"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.batchUsages.isEmpty() && !uiState.isSyncing) {
            EmptyState(message = "Tidak ada data riwayat serah terima.\nTekan tombol 'Sync' untuk memuat.")
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = uiState.batchUsages, key = { it.batchUsage.batchUsageId }) { uiUsage ->
                    BatchUsageItemCard(
                        uiUsage = uiUsage,
                        onClick = { onBatchClicked(uiUsage.batchUsage.batchUsageId) }
                    )
                }
            }
        }
    }
}

@Composable
fun BatchUsageItemCard(
    uiUsage: UiBatchUsage,
    onClick: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp).clickable(onClick = onClick)) {
            Text(
                text = uiUsage.batchUsage.batchUsageId,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow(
                icon = Icons.Default.Person,
                label = "PIC:",
                value = uiUsage.batchUsage.batchUsagePic ?: "N/A"
            )
            InfoRow(
                icon = Icons.Default.Person,
                label = "Penerima:",
                value = uiUsage.batchUsage.batchUsageReceiver ?: "N/A"
            )
            InfoRow(
                icon = Icons.Default.Inventory,
                label = "Lokasi:",
                value = uiUsage.batchUsage.batchUsageLocation ?: "N/A"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Jumlah Linen: ${uiUsage.linenCount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
