package com.applock.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.applock.data.util.DataStoreConstants.IS_LOGGED_IN
import com.applock.data.util.DataStoreConstants.SHOULD_ASK_PERMISSION
import com.applock.data.util.DataStoreConstants.TOGGLE_AUTO_START_PERMISSION
import com.applock.domain.repo.LocalDataStore
import kotlinx.coroutines.flow.first

class LocalDataStoreImpl(
    private val dataStore: DataStore<Preferences>
) : LocalDataStore {

    override suspend fun isLoggedIn(): Boolean {
        return dataStore.data.first()[IS_LOGGED_IN] == true
    }

    override suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit {
            it[IS_LOGGED_IN] = value
        }
    }

    override suspend fun shouldAskPermission(): Boolean {
        return dataStore.data.first()[SHOULD_ASK_PERMISSION] != false
    }

    override suspend fun doNotAskPermission(value: Boolean) {
        dataStore.edit {
            it[SHOULD_ASK_PERMISSION] = value
        }
    }

    override suspend fun toggleAutoStartPermission() {
        dataStore.edit {
            it[TOGGLE_AUTO_START_PERMISSION] =
                dataStore.data.first()[TOGGLE_AUTO_START_PERMISSION] != true
        }
    }

    override suspend fun shouldAskAutostartPermission(): Boolean {
        return dataStore.data.first()[TOGGLE_AUTO_START_PERMISSION] != false
    }
}