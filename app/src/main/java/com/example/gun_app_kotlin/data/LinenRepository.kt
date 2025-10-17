package com.example.gun_app_kotlin.data

import com.example.gun_app_kotlin.network.ApiService
import com.example.gun_app_kotlin.network.SetIntransitRequest
import com.example.gun_app_kotlin.network.UpdateStorageRequest

// This class is the single source of truth for linen data for the rest of the app.
class LinenRepository(
    private val linenDao: LinenDao,
    private val batchInDao: BatchInDao,
    private val batchInDetailDao: BatchInDetailDao,
    private val apiService: ApiService
) {

    // This is the ONLY function the ViewModel will call to get data.
    // It queries the FAST local Room database.
    suspend fun findLinenByEpc(epc: String): LinenItem? {
        return linenDao.findByEpc(epc)
    }

    /**
     * Finds the BATCH_IN_ID associated with a given LINEN_ID by querying
     * the local batch_in_details table.
     */
    suspend fun findBatchIdForLinen(linenId: String): String? {
        return batchInDetailDao.findBatchIdByLinenId(linenId)
    }

    /**
     * Fetches all linen data from the network API, clears the local
     * database, and inserts the fresh data.
     */
    suspend fun refreshLinens() {
        try {
            // 1. Fetch data from API service.
            val freshLinens = apiService.getAllLinens()
            val freshBatchIn = apiService.getBatchIn()
            val freshBatchInDetails = apiService.getBatchInDetails()

            // 2. Clear existing local data
            linenDao.clearAll()
            batchInDao.clearAll()
            batchInDetailDao.clearAll()

            // 3. Insert new fresh data into Room
            linenDao.insertAll(freshLinens)
            batchInDao.insertAll(freshBatchIn)
            batchInDetailDao.insertAll(freshBatchInDetails)

        } catch (e: Exception) {
            // In a real app, handle network errors here (e.g., log them)
            e.printStackTrace()
        }
    }

    suspend fun updateStorageForTags(epcs: List<String>, location: String) {
        try {
            val request = UpdateStorageRequest(epcs = epcs, location = location)
            apiService.updateLinenStorage(request)
        } catch (e: Exception) {
            // In a real app, you would want to handle this error more gracefully
            e.printStackTrace()
        }
    }

    suspend fun setIntransitForTags(epcs: List<String>, petugas: String) {
        try {
            val request = SetIntransitRequest(epcs = epcs, petugas = petugas)
            apiService.setLinenIntransit(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearCache() {
        linenDao.clearAll()
        batchInDao.clearAll()
        batchInDetailDao.clearAll()
    }
}
