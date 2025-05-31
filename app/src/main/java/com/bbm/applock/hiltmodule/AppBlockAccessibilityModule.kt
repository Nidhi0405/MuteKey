package com.bbm.applock.hiltmodule

import com.applock.domain.usecase.SyncInstalledAppsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppBlockAccessibilityModule {
    fun syncInstalledAppsUseCase(): SyncInstalledAppsUseCase
}