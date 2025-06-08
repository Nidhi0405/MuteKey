package com.bbm.applock.presentation.navigation

import kotlinx.serialization.Serializable


object MainScreens {

    @Serializable
    data object InstalledAppListScreenRoute

    @Serializable
    data object ControlledAppListScreen

    @Serializable
    data object ScheduleScreen

    @Serializable
    data object AnalyticsScreen

    @Serializable
    data object ProfileScreen
}


