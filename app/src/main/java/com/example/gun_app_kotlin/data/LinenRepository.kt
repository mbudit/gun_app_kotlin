package com.example.gun_app_kotlin.data

import com.example.gun_app_kotlin.network.ApiClient
import com.example.gun_app_kotlin.network.ApiService
import com.example.gun_app_kotlin.network.BatchOutRequest
import com.example.gun_app_kotlin.network.BatchUsageRequest
import com.example.gun_app_kotlin.network.LoginRequest
import com.example.gun_app_kotlin.network.LoginResponse
import com.example.gun_app_kotlin.network.RegisterRequest
import com.example.gun_app_kotlin.network.StorageOutRequest
import java.io.IOException

// This class is the single source of truth for linen data for the rest of the app.
class LinenRepository(
    private val linenDao: LinenDao,
    private val batchInDao: BatchInDao,
    private val batchInDetailDao: BatchInDetailDao,
    @Suppress("UNUSED_PARAMETER") apiService: ApiService, // kept for caller compat; dynamic property below is used
    private val batchUsageDao: BatchUsageDao,
    private val batchUsageDetailDao: BatchUsageDetailDao
) {
    // Always use the latest ApiService from ApiClient so URL changes take effect immediately
    private val apiService: ApiService get() = ApiClient.apiService

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

    suspend fun registerUser(username: String, password: String, name: String): Result<Unit> {
        return try {
            // Pass the new 'name' field into the request
            val request = RegisterRequest(username, password, name)
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
            // 1. Fetch ALL data from API service.
            val freshLinens = apiService.getAllLinens()
            val freshBatchIn = apiService.getBatchIn()
            val freshBatchInDetails = apiService.getBatchInDetails()
            val freshBatchUsage = apiService.getBatchUsage()
            val freshBatchUsageDetails = apiService.getBatchUsageDetails()

            // --- FILTERING FOR NULL PRIMARY KEYS ---
            val validBatchUsage = freshBatchUsage.filter { !it.batchUsageId.isNullOrEmpty() }
            val validBatchUsageDetails = freshBatchUsageDetails.filter { !it.batchUsageId.isNullOrEmpty() && !it.epc.isNullOrEmpty() }

            // --- THIS IS THE FIX ---
            // Filter 'freshLinens' by its own primary key, which is 'epc'.
            val validLinens = freshLinens.filter { !it.epc.isNullOrEmpty() }
            // -----------------------

            // Sorting (optional but good practice)
            val sortedFreshBatchIn = freshBatchIn.sortedByDescending { it.batchInId }
            val sortedFreshBatchInDetails = freshBatchInDetails.sortedByDescending { it.batchInId }
            val sortedFreshBatchUsage = validBatchUsage.sortedByDescending { it.batchUsageDateTime }
            val sortedFreshBatchUsageDetails = validBatchUsageDetails.sortedByDescending { it.batchUsageId }


            // 2. Clear existing local data
            linenDao.clearAll()
            batchInDao.clearAll()
            batchInDetailDao.clearAll()
            batchUsageDao.clearAll()
            batchUsageDetailDao.clearAll()

            // 3. Insert new, sorted, fresh data into Room
            // Use the corrected 'validLinens' list for insertion
            linenDao.insertAll(validLinens)
            batchInDao.insertAll(sortedFreshBatchIn)
            batchInDetailDao.insertAll(sortedFreshBatchInDetails)
            batchUsageDao.insertAll(sortedFreshBatchUsage)
            batchUsageDetailDao.insertAll(sortedFreshBatchUsageDetails)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    suspend fun batchOutItems(batchOutId: String, epcs: List<String>, storageType: String, petugasName: String) {
        try {
            val request = BatchOutRequest(
                batchOutId = batchOutId,
                epcs = epcs,
                storageType = storageType,
                petugasName = petugasName
            )
            apiService.executeBatchOut(request)
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real app, handle this error more gracefully
        }
    }

    suspend fun storageOutTags(epcs: List<String>, petugasName: String) {
        try {
            val request = StorageOutRequest(epcs = epcs, petugasName = petugasName)
            apiService.executeStorageOut(request)
        } catch (e: Exception) {
            // Handle network errors, maybe throw so the ViewModel can catch it
            e.printStackTrace()
            throw e
        }
    }

    suspend fun createBatchUsage(
        batchUsageId: String,
        epcs: List<String>,
        petugasName: String,
        receiverName: String,
        receiverLocation: String
    ) {
        try {
            val request = BatchUsageRequest(
                batchUsageId = batchUsageId,
                epcs = epcs,
                petugasName = petugasName,
                receiverName = receiverName,
                receiverLocation = receiverLocation
            )
            apiService.executeBatchUsage(request)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Re-throw so the ViewModel can handle the error
        }
    }

    suspend fun clearCache() {
        linenDao.clearAll()
        batchInDao.clearAll()
        batchInDetailDao.clearAll()
        batchUsageDao.clearAll()
        batchUsageDetailDao.clearAll()
    }

    fun getAllBatchUsages(): kotlinx.coroutines.flow.Flow<List<BatchUsage>> {
        return batchUsageDao.getAllBatchUsages()
    }

    fun getAllBatchUsageDetails(): kotlinx.coroutines.flow.Flow<List<BatchUsageDetail>> {
        return batchUsageDetailDao.getAllBatchUsageDetails()
    }

    fun getDetailsForBatch(batchId: String): kotlinx.coroutines.flow.Flow<List<BatchUsageDetail>> {
        return batchUsageDetailDao.getDetailsForBatch(batchId)
    }

    fun getAllLinens(): kotlinx.coroutines.flow.Flow<List<LinenItem>> {
        return linenDao.getAllLinens()
    }

    fun getBatchUsageById(batchId: String): kotlinx.coroutines.flow.Flow<BatchUsage?> {
        return batchUsageDao.getBatchUsageById(batchId)
    }
}
