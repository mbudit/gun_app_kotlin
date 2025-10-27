package com.example.gun_app_kotlin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SixthScreen(
    onNavigateUp: () -> Unit,
    batchId: String?,
    viewModel: SixthScreenViewModel = viewModel(factory = SixthScreenViewModel.Factory)
) {
    // Get both states from the ViewModel
    val details by viewModel.enrichedDetails.collectAsState()
    val batchUsage by viewModel.batchUsage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Details for ${batchId ?: "..."}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            batchUsage?.let { usage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
//                        Text(
//                            text = "Ringkasan Batch",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold
//                        )
//                        HorizontalDivider()
                        InfoRow(label = "Penerima:", value = usage.batchUsageReceiver ?: "N/A")
                        InfoRow(label = "Lokasi:", value = usage.batchUsageLocation ?: "N/A")
                        InfoRow(label = "Total Linen:", value = details.size.toString())
                    }
                }
            }

            if (details.isEmpty()) {
                EmptyState(message = "No linens found for this batch.")
            } else {
                LazyColumn(
                ) {
                    items(items = details, key = { it.epc }) { detail ->
                        LinenDetailItem(detail = detail)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// composable for displaying each item in the list
@Composable
fun LinenDetailItem(detail: UiLinenDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show the Linen Type prominently
        Text(
            text = detail.linenType,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Show the raw EPC as smaller, secondary information
        Text(
            text = detail.epc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
