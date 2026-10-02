package com.example.gun_app_kotlin.network

import com.google.gson.annotations.SerializedName
import com.example.gun_app_kotlin.data.LinenItem
import com.example.gun_app_kotlin.data.BatchIn
import com.example.gun_app_kotlin.data.BatchInDetail
import com.example.gun_app_kotlin.data.BatchUsage
import com.example.gun_app_kotlin.data.BatchUsageDetail
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

data class StorageOutRequest(
    @SerializedName("epcs")
    val epcs: List<String>,
    @SerializedName("petugas_name")
    val petugasName: String
)

data class BatchOutRequest(
    @SerializedName("batch_out_id")
    val batchOutId: String,

    @SerializedName("epcs")
    val epcs: List<String>,

    @SerializedName("storage_type")
    val storageType: String,

    @SerializedName("petugas_name")
    val petugasName: String
)

data class BatchUsageRequest(
    @SerializedName("batch_usage_id")
    val batchUsageId: String,
    @SerializedName("epcs")
    val epcs: List<String>,
    @SerializedName("petugas_name")
    val petugasName: String,
    @SerializedName("receiver_name")
    val receiverName: String,
    @SerializedName("receiver_location")
    val receiverLocation: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val name: String
)

data class LinenRegisterRequest(
    @SerializedName("linen_id") val linenId: String,
    @SerializedName("linen_type") val linenType: String,
    @SerializedName("linen_height") val linenHeight: Int,
    @SerializedName("linen_width") val linenWidth: Int,
    @SerializedName("linen_length") val linenLength: Int,
    @SerializedName("linen_max_cycle") val linenMaxCycle: Int,
    @SerializedName("linen_description") val linenDescription: String,
    @SerializedName("linen_created_date") val linenCreatedDate: String,
    @SerializedName("linen_size_category") val linenSizeCategory: String,
    @SerializedName("linen_weight") val linenWeight: Double,
    @SerializedName("linen_material") val linenMaterial: String,
    @SerializedName("linen_supplier") val linenSupplier: String,
    @SerializedName("linen_budget_source") val linenBudgetSource: String,
    @SerializedName("operator_username") val operatorUsername: String,
    @SerializedName("epc_list") val epcList: List<String>
)

data class LinenRegisterResponse(
    @SerializedName("inserted_epcs") val insertedEpcs: List<String>,
    @SerializedName("skipped_existing_epcs") val skippedExistingEpcs: List<String>
)

data class User(
    val username: String,
    val name: String
)


interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest)

    @GET("api/linens")
    suspend fun getAllLinens(): List<LinenItem>

    @GET("api/batch-in")
    suspend fun getBatchIn(): List<BatchIn>

    @GET("api/batch-in-details")
    suspend fun getBatchInDetails(): List<BatchInDetail>

    @POST("api/batch-out")
    suspend fun executeBatchOut(@Body request: BatchOutRequest)

    @POST("api/storage-out")
    suspend fun executeStorageOut(@Body request: StorageOutRequest)

    @GET("api/batch-usage")
    suspend fun getBatchUsage(): List<BatchUsage>

    @GET("api/batch-usage-details")
    suspend fun getBatchUsageDetails(): List<BatchUsageDetail>

    @POST("api/batch-usage")
    suspend fun executeBatchUsage(@Body request: BatchUsageRequest)

    @POST("api/linens/register-batch")
    suspend fun registerLinens(@Body request: LinenRegisterRequest): LinenRegisterResponse
}
