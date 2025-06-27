package com.vtu.translate.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
// import okhttp3.logging.HttpLoggingInterceptor // Keep it disabled for now
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Temporarily disable the logging interceptor to debug EOFException
    // private val loggingInterceptor = HttpLoggingInterceptor().apply {
    //     level = HttpLoggingInterceptor.Level.HEADERS
    // }

    private val client = OkHttpClient.Builder()
        // .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .build()

    val api: OpenRouterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenRouterApiService::class.java)
    }
} 