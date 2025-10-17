package com.example.gun_app_kotlin.network

import com.google.gson.annotations.SerializedName
import com.example.gun_app_kotlin.data.LinenItem
import com.example.gun_app_kotlin.data.BatchIn
import com.example.gun_app_kotlin.data.BatchInDetail
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

data class UpdateStorageRequest(
    val epcs: List<String>,
    val location: String
)

data class UpdateStatusRequest(
    val epcs: List<String>,
    val status: String
)

data class SetIntransitRequest(
    val epcs: List<String>,
    val petugas: String
)

interface ApiService {
    @GET("api/linens")
    suspend fun getAllLinens(): List<LinenItem>

    @POST("api/linens/update-storage")
    suspend fun updateLinenStorage(@Body request: UpdateStorageRequest)

    @POST("api/linens/set-intransit")
    suspend fun setLinenIntransit(@Body request: SetIntransitRequest)

    @GET("api/batch-in")
    suspend fun getBatchIn(): List<BatchIn>

    @GET("api/batch-in-details")
    suspend fun getBatchInDetails(): List<BatchInDetail>
}
