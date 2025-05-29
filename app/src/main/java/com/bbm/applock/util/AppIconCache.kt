package com.bbm.applock.util

import android.graphics.Bitmap
import androidx.collection.LruCache

typealias PackageName = String

private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
private val cacheSize = maxMemory / 8

private val iconCache = object : LruCache<String, Bitmap>(cacheSize) {
    override fun sizeOf(key: String, value: Bitmap): Int {
        return value.byteCount / 1024
    }
}


var PackageName.icon: Bitmap?
    @Synchronized
    get() {
        synchronized(this) {
            return iconCache[this]
        }
    }
    @Synchronized
    set(value) {
        synchronized(this) {
            value ?: return
            iconCache.put(this@icon, value)
        }
    }
