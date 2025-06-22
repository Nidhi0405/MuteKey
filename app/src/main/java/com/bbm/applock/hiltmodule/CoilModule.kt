package com.bbm.applock.hiltmodule

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.bbm.applock.util.AppIconFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoilModule {
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        appIconFetcherFactory: AppIconFetcher.Factory
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(appIconFetcherFactory)
                // Add other Coil components if needed
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // Example: 25% of app's available RAM
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .maxSizeBytes(512L * 1024 * 1024) // Example: 512MB
                    .build()
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideAppIconFetcherFactory(
        @ApplicationContext context: Context
    ): AppIconFetcher.Factory {
        return AppIconFetcher.Factory(context)
    }
}