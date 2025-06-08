package com.bbm.applock.presentation.navigation

import kotlinx.serialization.Serializable


object MainScreens {

    @Serializable
    data object InstalledAppListScreenRoute

    @Serializable
    data object ControlledAppListScreenRoute

    @Serializable
    data object ScheduleScreenRoute

    @Serializable
    data object AnalyticsScreenRoute

    @Serializable
    data object ProfileScreenRoute

    @Serializable
    data class ScheduleDetailScreenRoute(
        val id: Int,
        val name: String,
        val isActive: Boolean
    )
}