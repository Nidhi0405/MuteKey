package com.bbm.applock.presentation.navigation

import kotlinx.serialization.Serializable


object MainScreens {

    @Serializable
    data object InstalledAppListScreenRoute

    @Serializable
    data object ControlledAppListScreenRoute

    @Serializable
    data object ScheduleScreenRoute

}

