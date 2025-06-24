package com.bbm.applock

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.applock.core.LogUtil
import com.bbm.applock.util.AppLifecycleObserver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : MultiDexApplication() {

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        init()
    }

    private fun init() {
        LogUtil.isLogEnabled = BuildConfig.DEBUG
    }
}