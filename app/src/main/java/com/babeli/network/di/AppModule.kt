package com.babeli.network.di

import android.content.Context
import com.babeli.network.data.AppUsageManager
import com.babeli.network.data.NetworkMonitor
import com.babeli.network.data.WifiScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideNetworkMonitor(): NetworkMonitor = NetworkMonitor()

    @Provides @Singleton
    fun provideWifiScanner(@ApplicationContext ctx: Context): WifiScanner = WifiScanner(ctx)

    @Provides @Singleton
    fun provideAppUsageManager(@ApplicationContext ctx: Context): AppUsageManager = AppUsageManager(ctx)
}
