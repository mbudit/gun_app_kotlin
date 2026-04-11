package com.example.gun_app_kotlin.network

import com.example.gun_app_kotlin.data.ServerConfigManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    var apiService: ApiService = buildApiService(ServerConfigManager.getHttpBaseUrl())
        private set

    /**
     * Rebuild ApiService with the current URL from ServerConfigManager.
     * Call this after changing the server URL.
     */
    fun updateBaseUrl() {
        val newBaseUrl = ServerConfigManager.getHttpBaseUrl()
        apiService = buildApiService(newBaseUrl)
    }

    private fun buildApiService(baseUrl: String): ApiService {
        val r = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit = r
        return r.create(ApiService::class.java)
    }
}
