package com.example.gun_app_kotlin.ui.screens

import com.example.gun_app_kotlin.data.LinenItem

/**
 * A shared data class that represents a scanned RFID tag, enriched with
 * database information like the corresponding LinenItem and its Batch ID.
 *
 * By placing this in its own file, it can be safely used by multiple
 * ViewModels (ScanViewModel, ThirdScreenViewModel, etc.) without causing
 * "Unresolved reference" errors.
 */
data class EnrichedTag(
    val epc: String,
    var count: Int,
    val linenItem: LinenItem?,
    val batchId: String?
)
