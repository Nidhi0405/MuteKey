package com.bbm.applock.hiltmodule

import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.dispatcher.RealCoroutineDispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Singleton
    fun provideDispatcher(): CoroutineDispatcherProvider = RealCoroutineDispatcherProvider()
}