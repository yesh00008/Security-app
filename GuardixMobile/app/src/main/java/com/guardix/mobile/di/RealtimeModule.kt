package com.guardix.mobile.di

import com.guardix.mobile.data.realtime.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    @Provides
    @Singleton
    fun provideRealtimeRepository(
        webSocketClient: WebSocketClient,
        @ApplicationScope coroutineScope: CoroutineScope
    ): RealtimeRepository {
        return RealtimeRepositoryImpl(webSocketClient, coroutineScope)
    }
}
