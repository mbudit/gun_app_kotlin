package com.example.gun_app_kotlin.data

import androidx.room.*

@Dao
interface LinenDao {
    // The column is now named 'epc', so we query it directly.
    @Query("SELECT * FROM linens WHERE epc = :epc")
    suspend fun findByEpc(epc: String): LinenItem?

    // ... insertAll and clearAll are fine
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(linens: List<LinenItem>)

    @Query("DELETE FROM linens")
    suspend fun clearAll()

    @Query("SELECT * FROM linens")
    fun getAllLinens(): kotlinx.coroutines.flow.Flow<List<LinenItem>>
}

