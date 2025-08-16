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
    data class ScheduleDetailScreenRoute(val scheduleId: Long = 0)

    @Serializable data object AnalyticsTodayRoute
    @Serializable data object AnalyticsLastSevenRoute
    @Serializable data object AnalyticsThisWeekRoute

}