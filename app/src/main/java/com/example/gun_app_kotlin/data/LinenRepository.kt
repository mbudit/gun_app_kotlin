package com.example.gun_app_kotlin.data

import com.example.gun_app_kotlin.network.ApiService

// This class is the single source of truth for linen data for the rest of the app.
class LinenRepository(
    private val linenDao: LinenDao,
    private val apiService: ApiService // Add ApiService for network calls
) {

    // This is the ONLY function the ViewModel will call to get data.
    // It queries the FAST local Room database.
    suspend fun findLinenByEpc(epc: String): LinenItem? {
        return linenDao.findByEpc(epc)
    }

    /**
     * Fetches all linen data from the network API, clears the local
     * database, and inserts the fresh data.
     */
    suspend fun refreshLinens() {
        try {
            // 1. Fetch fresh data from the API service.
            val freshLinens = apiService.getAllLinens()
            // 2. Insert the new data into the Room database.
            //    The `onConflict = REPLACE` strategy handles everything.
            linenDao.insertAll(freshLinens)
        } catch (e: Exception) {
            // In a real app, handle network errors here (e.g., log them)
            e.printStackTrace()
        }
    }
}
