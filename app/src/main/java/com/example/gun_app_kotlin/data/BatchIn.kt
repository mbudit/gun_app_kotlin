package com.example.gun_app_kotlin.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.gson.annotations.SerializedName

// 1. Entity for the 'batch_in' table
@Entity(tableName = "batch_in")
data class BatchIn(
    @PrimaryKey
    @SerializedName("BATCH_IN_ID")
    val batchInId: String,

    @SerializedName("BATCH_IN_DATETIME")
    val batchInDateTime: String
)

// 2. Entity for the 'batch_in_details' table
//    Since this table has a composite primary key in SQL, we can use one as a primary key for Room
//    or, for simplicity, use an auto-generated one if duplicates are not expected or are handled.
@Entity(tableName = "batch_in_details", primaryKeys = ["batchInId", "epc"])
data class BatchInDetail(
    @SerializedName("BATCH_IN_ID")
    val batchInId: String,

    // The server sends the EPC under the key "LINEN_ID"
    @SerializedName("LINEN_ID")
    val epc: String
)

// 3. DAO for BatchIn
@Dao
interface BatchInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batchIns: List<BatchIn>)

    @Query("DELETE FROM batch_in")
    suspend fun clearAll()
}

// 4. DAO for BatchInDetail
@Dao
interface BatchInDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batchInDetails: List<BatchInDetail>)

    @Query("DELETE FROM batch_in_details")
    suspend fun clearAll()

    // Custom query to find BATCH_IN_ID by LINEN_ID
    @Query("SELECT batchInId FROM batch_in_details WHERE epc = :epc LIMIT 1")
    suspend fun findBatchIdByLinenId(epc: String): String?
}
