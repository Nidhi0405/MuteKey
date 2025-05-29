package com.applock.data.util

import androidx.datastore.preferences.core.booleanPreferencesKey

object DataStoreConstants {
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val SHOULD_ASK_PERMISSION = booleanPreferencesKey("should_ask_permission")
    val TOGGLE_AUTO_START_PERMISSION = booleanPreferencesKey("auto_start_permission")
}