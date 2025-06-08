package com.bbm.applock.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bbm.applock.presentation.analyticsModule.view.AnalyticsScreen
import com.bbm.applock.presentation.mainModule.view.InstalledAppListScreen
import com.bbm.applock.presentation.mainModule.vm.InstalledAppVM
import com.bbm.applock.presentation.profileModule.view.ProfileScreen
import com.bbm.applock.presentation.scheduleModule.view.ScheduleScreen
import com.bbm.applock.util.ScreenSurface


@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MainScreens.InstalledAppListScreenRoute,
        modifier = modifier
    ) {
        initAppScreens()
    }
}

private fun NavGraphBuilder.initAppScreens() {
    composable<MainScreens.InstalledAppListScreenRoute> {
        val vm = hiltViewModel<InstalledAppVM>()
        ScreenSurface {
            InstalledAppListScreen(vm)
        }
    }
    composable<MainScreens.ScheduleScreen> {
        ScreenSurface {
            ScheduleScreen()
        }
    }

    composable<MainScreens.AnalyticsScreen> {
        ScreenSurface {
            AnalyticsScreen()
        }
    }

    composable<MainScreens.ProfileScreen> {
        ScreenSurface {
            ProfileScreen()
        }
    }
}

