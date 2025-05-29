package com.bbm.applock

import androidx.multidex.MultiDexApplication
import com.applock.core.LogUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        LogUtil.isLogEnabled = BuildConfig.DEBUG
    }
}