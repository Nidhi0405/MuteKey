package com.bbm.applock.hiltmodule

import com.applock.data.repo.LocalAppRepoImpl
import com.applock.data.repo.PermissionRepoImpl
import com.applock.data.repo.ScheduleRepoImpl
import com.applock.data.repo.SystemAppRepoImpl
import com.applock.domain.repo.LocalAppRepo
import com.applock.domain.repo.PermissionRepo
import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.repo.SystemAppRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides
    @Singleton
    fun provideLocalAppRepo(
        repo: LocalAppRepoImpl
    ): LocalAppRepo = repo

    @Provides
    @Singleton
    fun provideSystemAppRepo(
        repo: SystemAppRepoImpl
    ): SystemAppRepo = repo

    @Provides
    @Singleton
    fun providePermissionRepo(
        repo: PermissionRepoImpl
    ): PermissionRepo = repo

    @Provides
    @Singleton
    fun provideScheduleRepo(
        repo: ScheduleRepoImpl
    ): ScheduleRepo = repo
}