package com.example.gun_app_kotlin.data

import com.example.gun_app_kotlin.network.ApiService
import com.example.gun_app_kotlin.network.BatchOutRequest
import com.example.gun_app_kotlin.network.SetIntransitRequest
import com.example.gun_app_kotlin.network.UpdateStorageRequest
import com.example.gun_app_kotlin.network.LoginRequest // Add import
import com.example.gun_app_kotlin.network.LoginResponse // Add import
import com.example.gun_app_kotlin.network.RegisterRequest
import java.io.IOException

// This class is the single source of truth for linen data for the rest of the app.
class LinenRepository(
    private val linenDao: LinenDao,
    private val batchInDao: BatchInDao,
    private val batchInDetailDao: BatchInDetailDao,
    private val apiService: ApiService
) {
    suspend fun loginUser(username: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)
            Result.success(response)
        } catch (e: Exception) {
            // Catch specific HTTP exceptions or general IO exceptions
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun registerUser(username: String, password: String): Result<Unit> {
        return try {
            val request = RegisterRequest(username, password)
            apiService.register(request)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

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

            // Sort the lists by batchInId in descending order before inserting.
            val sortedFreshBatchIn = freshBatchIn.sortedByDescending { it.batchInId }
            val sortedFreshBatchInDetails = freshBatchInDetails.sortedByDescending { it.batchInId }
            // ----------------------------------------

            // 2. Clear existing local data
            linenDao.clearAll()
            batchInDao.clearAll()
            batchInDetailDao.clearAll()

            // 3. Insert new, sorted, fresh data into Room
            linenDao.insertAll(freshLinens) // linens don't need sorting by batchId
            batchInDao.insertAll(sortedFreshBatchIn)
            batchInDetailDao.insertAll(sortedFreshBatchInDetails)

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

    suspend fun batchOutItems(batchOutId: String, epcs: List<String>) {
        try {
            val request = BatchOutRequest(batchOutId = batchOutId, epcs = epcs)
            apiService.executeBatchOut(request)
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real app, handle this error more gracefully
        }
    }

    suspend fun clearCache() {
        linenDao.clearAll()
        batchInDao.clearAll()
        batchInDetailDao.clearAll()
    }
}
