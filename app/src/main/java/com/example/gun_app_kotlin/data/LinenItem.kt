package com.example.gun_app_kotlin.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.gson.annotations.SerializedName

/**
 * This class defines the structure of the 'linens' table in the local Room database.
 * It also defines how JSON data from the server is mapped to Kotlin objects.
 */
@Entity(tableName = "linens")
data class LinenItem(
    @PrimaryKey
    @SerializedName("LINEN_ID")
    val epc: String,

    @SerializedName("LINEN_TYPE")
    val linenType: String,

    @SerializedName("LINEN_STATUS")
    val status: String
)
