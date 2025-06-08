package com.bbm.applock.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bbm.applock.R

sealed class BottomNavItem<T : Any>(
    val screen: T,
    @DrawableRes val icon: Int,
    @StringRes val label: Int
) {
    data object Apps : BottomNavItem<MainScreens.InstalledAppListScreenRoute>(
        MainScreens.InstalledAppListScreenRoute,
        R.drawable.ic_apps,
        R.string.nav_item_apps
    )

    data object Schedule : BottomNavItem<MainScreens.ScheduleScreenRoute>(
        MainScreens.ScheduleScreenRoute,
        R.drawable.ic_schedule,
        R.string.nav_item_schedule
    )

    data object Analytics : BottomNavItem<MainScreens.AnalyticsScreenRoute>(
        MainScreens.AnalyticsScreenRoute,
        R.drawable.ic_analytics,
        R.string.nav_item_analytics
    )

    data object Profile : BottomNavItem<MainScreens.ProfileScreenRoute>(
        MainScreens.ProfileScreenRoute,
        R.drawable.ic_profile,
        R.string.nav_item_profile
    )
}


