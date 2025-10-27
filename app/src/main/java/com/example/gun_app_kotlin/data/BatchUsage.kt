package com.example.gun_app_kotlin.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.gson.annotations.SerializedName

// 1. Entity for the 'batch_usage' table
@Entity(tableName = "batch_usage")
data class BatchUsage(
    @PrimaryKey
    @SerializedName("batch_usage_id")
    val batchUsageId: String,

    @SerializedName("batch_usage_pic")
    val batchUsagePic: String?,

    @SerializedName("batch_usage_receiver")
    val batchUsageReceiver: String?,

    @SerializedName("batch_usage_location")
    val batchUsageLocation: String?,

    @SerializedName("batch_usage_time_in")
    val batchUsageDateTime: String?
)

// 2. Entity for the 'batch_usage_details' table
@Entity(tableName = "batch_usage_details", primaryKeys = ["batchUsageId", "epc"])
data class BatchUsageDetail(
//    @ColumnInfo(name = "batch_usage_id")
    @SerializedName("batch_usage_id")
    val batchUsageId: String,

//    @ColumnInfo(name = "epc")
    @SerializedName("linen_id")
    val epc: String
)

// 3. DAO for BatchUsage
@Dao
interface BatchUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batchUsage: List<BatchUsage>)

    @Query("DELETE FROM batch_usage")
    suspend fun clearAll()

    @Query("SELECT * FROM batch_usage ORDER BY batchUsageDateTime DESC")
    fun getAllBatchUsages(): kotlinx.coroutines.flow.Flow<List<BatchUsage>>

    @Query("SELECT * FROM batch_usage WHERE batchUsageId = :batchId")
    fun getBatchUsageById(batchId: String): kotlinx.coroutines.flow.Flow<BatchUsage?> // Return nullable in case not found
}

@Dao
interface BatchUsageDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batchUsageDetails: List<BatchUsageDetail>)

    @Query("DELETE FROM batch_usage_details")
    suspend fun clearAll()

    // --- ADD a simple query to get all details ---
    @Query("SELECT * FROM batch_usage_details")
    fun getAllBatchUsageDetails(): kotlinx.coroutines.flow.Flow<List<BatchUsageDetail>>

    @Query("SELECT * FROM batch_usage_details WHERE batchUsageId = :batchId")
    fun getDetailsForBatch(batchId: String): kotlinx.coroutines.flow.Flow<List<BatchUsageDetail>>
}
