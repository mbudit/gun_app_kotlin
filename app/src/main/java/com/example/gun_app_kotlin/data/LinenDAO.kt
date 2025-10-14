package com.example.gun_app_kotlin.data

import androidx.room.*

@Dao
interface LinenDao {
    // Finds a single linen item by its EPC (LINEN_ID) from the local cache
    @Query("SELECT * FROM linens WHERE epc = :epc")
    suspend fun findByEpc(epc: String): LinenItem?

    // Inserts a list of linens, replacing any duplicates. This is for syncing.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(linens: List<LinenItem>)
}
