package com.example.waterreminder.data.remote

import android.content.Context
import com.example.waterreminder.util.ApiConfig
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private val moshi: Moshi = Moshi.Builder().build()
    @Volatile private var cachedBaseUrl: String? = null
    @Volatile private var cachedApiKey: String? = null
    @Volatile private var cachedService: ApiService? = null

    fun apiService(context: Context): ApiService {
        val baseUrl = ApiConfig.resolveBaseUrl(context)
        val apiKey = ApiConfig.resolveApiKey(context)
        val cached = cachedService
        if (cached != null && baseUrl == cachedBaseUrl && apiKey == cachedApiKey) {
            return cached
        }

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val newRequest = if (apiKey.isNotBlank()) {
                    request.newBuilder()
                        .addHeader("X-API-Key", apiKey)
                        .build()
                } else {
                    request
                }
                chain.proceed(newRequest)
            }
            .build()

        val service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)

        cachedBaseUrl = baseUrl
        cachedApiKey = apiKey
        cachedService = service
        return service
    }
}
