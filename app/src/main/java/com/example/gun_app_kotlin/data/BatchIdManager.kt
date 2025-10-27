package com.example.gun_app_kotlin.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatchIdManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("BatchIdPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val LAST_DATE_KEY = "last_date"
        private const val LAST_NUMBER_KEY = "last_number"
    }

    // This is the main function the ViewModel will call
    @Synchronized // Ensures thread safety when getting and updating the number
    fun getNextBatchId(): String {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString(LAST_DATE_KEY, "")

        val currentNumber = if (todayStr == lastDate) {
            // Same day, increment the number
            prefs.getInt(LAST_NUMBER_KEY, 0) + 1
        } else {
            // New day, reset to 1
            1
        }

        // Save the new state
        prefs.edit()
            .putString(LAST_DATE_KEY, todayStr)
            .putInt(LAST_NUMBER_KEY, currentNumber)
            .apply()

        // Format the number to a 6-digit string (e.g., 1 -> "000001")
        val formattedNumber = String.format("%06d", currentNumber)

        return "BATCH-$todayStr-$formattedNumber"
    }
}
