package com.guardix.mobile.data.remote

import com.guardix.mobile.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object EnhancedGuardixApiClient {
    
    private const val BASE_URL = BuildConfig.API_BASE_URL
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    // Moshi JSON adapter
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // Network interceptor for debugging
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // Custom error handling interceptor
    private val errorInterceptor: Interceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Log network errors in debug builds
        if (BuildConfig.DEBUG && !response.isSuccessful) {
            println("Network Error: ${response.code} - ${response.message}")
        }
        
        response
    }

    // Network timeout interceptor
    private val timeoutInterceptor: Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Connection", "close") // Force connection close to prevent hanging
            .build()
        chain.proceed(request)
    }

    // OkHttp client with enhanced configuration
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(timeoutInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Enhanced Retrofit instance
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // API service instance
    val service: GuardixApiService by lazy {
        retrofit.create(GuardixApiService::class.java)
    }
}