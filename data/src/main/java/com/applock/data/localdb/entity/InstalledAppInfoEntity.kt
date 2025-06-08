package com.applock.data.localdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * [com.applock.data.localdb.entity.InstalledAppInfoEntity]
 * to store the list of installed apps
 * */
@Entity(tableName = "installed_app_info_table")
data class InstalledAppInfoEntity(
    @PrimaryKey val packageName: String,
    val name: String,
    val isControlledApp: Boolean = false
)