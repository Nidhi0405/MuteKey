package com.bbm.applock.hiltmodule

import coil3.ImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ComponentActivityInjectModule {
    fun imageLoader(): ImageLoader
}