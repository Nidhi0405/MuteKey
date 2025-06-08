package com.bbm.applock.hiltmodule

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.applock.data.localdb.LockAppDb
import com.applock.data.repo.LocalDataStoreImpl
import com.applock.domain.repo.LocalDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideLockAppDb(
        @ApplicationContext context: Context
    ): LockAppDb {
        return Room.databaseBuilder(
            context,
            LockAppDb::class.java,
            "app_lock_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideInstalledAppLockDao(
        lockAppDb: LockAppDb
    ) = lockAppDb.installedAppDao()

    @Provides
    @Singleton
    fun provideControlledAppDao(
        lockAppDb: LockAppDb
    ) = lockAppDb.controlledAppDao()

    @Provides
    @Singleton
    fun provideScheduleDao(
        lockAppDb: LockAppDb
    ) = lockAppDb.scheduleDao()

    @Provides
    @Singleton
    fun getLocalDataStore(
        @ApplicationContext context: Context
    ): LocalDataStore = LocalDataStoreImpl(
        preferencesDataStore(name = "app-lock-data-store").getValue(
            context,
            String::javaClass
        )
    )
}