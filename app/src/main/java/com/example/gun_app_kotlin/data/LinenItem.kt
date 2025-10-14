package com.example.gun_app_kotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "linens") // This defines the table for the local Room database
data class LinenItem(
    // @SerializedName maps the JSON key from your API to this variable name.
    // @PrimaryKey tells Room this is the unique ID for the local table.
    @SerializedName("LINEN_ID")
    @PrimaryKey
    val epc: String,

    @SerializedName("LINEN_TYPE")
    val linenType: String,

    @SerializedName("LINEN_STATUS")
    val status: String,

    @SerializedName("LINEN_DESCRIPTION")
    val description: String,

    // You can add the other fields here if you need them in the app
    // @SerializedName("LINEN_MAX_CYCLE")
    // val maxCycle: Int
)
