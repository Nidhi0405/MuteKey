// com.bbm.applock.lifecycle.AppLifecycleObserver.kt
package com.bbm.applock.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor() : DefaultLifecycleObserver {

    private val _isAppInForeground = MutableSharedFlow<Boolean>(replay = 1)
    val isAppInForeground: SharedFlow<Boolean> = _isAppInForeground.asSharedFlow()
    private var isFirstStart = true

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (isFirstStart) {
            _isAppInForeground.tryEmit(true)
            isFirstStart = false
        } else {
            _isAppInForeground.tryEmit(true)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        _isAppInForeground.tryEmit(false)
    }
}