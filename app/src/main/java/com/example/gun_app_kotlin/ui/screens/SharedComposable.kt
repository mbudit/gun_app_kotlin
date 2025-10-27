package com.example.gun_app_kotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Universal composables  ---

@Composable
fun BatchScanHeader(
    isScanning: Boolean,
    userName: String, // <-- ADD THIS PARAMETER
    uniqueCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the user's name if it's not empty
            if (userName.isNotEmpty()) {
                Text(
                    text = "Petugas: $userName",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Fallback in case name is not available
                Spacer(modifier = Modifier)
            }

            Text(
                text = if (isScanning) "SCANNING..." else "IDLE",
                color = if (isScanning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Total Unique Tags: $uniqueCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BatchListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Adjust the weights to make space for the new column
        Text(text = "Linen Type", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold)
        Text(text = "Batch ID", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold)
//      Text(text = "Status", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text(text = "Count", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun BatchListHeaderThirdScreen() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Adjust the weights to make space for the new column
        Text(text = "Linen Type", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold)
//      Text(text = "Batch ID", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold)
//      Text(text = "Status", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text(text = "Count", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun BatchListItem(
    tag: EnrichedTag,
    showBatchId: Boolean = true
) {
    val linenType = tag.linenItem?.linenType ?: "Unknown Tag"
    val batchId = tag.batchId ?: "N/A"
    val isUnknown = tag.linenItem == null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = linenType,
            modifier = Modifier.weight(2.5f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUnknown) Color.Red else LocalContentColor.current,
            fontWeight = if (isUnknown) FontWeight.Bold else FontWeight.Normal
        )

        // --- 2. CONDITIONALLY SHOW THE BATCH ID ---
        if (showBatchId) {
            Text(
                text = batchId,
                modifier = Modifier.weight(2.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        // ------------------------------------------

        Text(
            text = "x${tag.count}",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun SaveButtonBottomBar(onSave: () -> Unit, onClear: () -> Unit, isSaving: Boolean, hasItems: Boolean) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = onSave,
            enabled = !isSaving && hasItems,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(Icons.Default.Done, contentDescription = "Save")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Save to Database")
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onClear,
            enabled = !isSaving && hasItems,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear List")
        }
    }
}

@Composable
fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}
