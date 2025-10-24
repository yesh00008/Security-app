package com.guardix.mobile.di

import android.content.Context
import com.guardix.mobile.data.realtime.WebSocketClient
import com.guardix.mobile.data.remote.*
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideWebSocketClient(): WebSocketClient {
        return WebSocketClient()
    }
    
    @Provides
    @Singleton
    fun provideRealtimeRepository(webSocketClient: WebSocketClient): RealtimeRepository {
        return RealtimeRepositoryImpl(webSocketClient)
    }
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return EnhancedGuardixApiClient.moshi
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return EnhancedGuardixApiClient.okHttpClient
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return EnhancedGuardixApiClient.retrofit
    }
    
    @Provides
    @Singleton
    fun provideGuardixApiService(retrofit: Retrofit): GuardixApiService {
        return retrofit.create(GuardixApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager {
        return NetworkManager(context)
    }
    
    @Provides
    @Singleton
    fun provideEnhancedGuardixRepository(
        @ApplicationContext context: Context,
        apiService: GuardixApiService,
        networkManager: NetworkManager,
        moshi: Moshi
    ): EnhancedGuardixRepository {
        return EnhancedGuardixRepository(context, apiService, networkManager, moshi)
    }
}
