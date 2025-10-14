package com.example.gun_app_kotlin.network

import com.example.gun_app_kotlin.data.LinenItem
import retrofit2.http.GET

interface ApiService {
    // This function will call your http://.../api/linens endpoint
    @GET("api/linens")
    suspend fun getAllLinens(): List<LinenItem>
}
