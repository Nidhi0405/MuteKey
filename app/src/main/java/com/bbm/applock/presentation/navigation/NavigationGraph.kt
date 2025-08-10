package com.bbm.applock.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bbm.applock.R
import com.bbm.applock.presentation.analyticsModule.view.AnalyticsScreen
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.presentation.installedControlledAppsModule.view.InstalledAppListScreen
import com.bbm.applock.presentation.installedControlledAppsModule.vm.InstalledAppVM
import com.bbm.applock.presentation.profileModule.view.ProfileScreen
import com.bbm.applock.presentation.scheduleModule.view.ScheduleDetailScreen
import com.bbm.applock.presentation.scheduleModule.view.ScheduleScreen
import com.bbm.applock.presentation.scheduleModule.vm.CreateOrUpdateScheduleVM
import com.bbm.applock.presentation.scheduleModule.vm.ScheduleDetailScreenVM
import com.bbm.applock.presentation.scheduleModule.vm.SchedulesScreenVM
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
        initAppScreens(navController)
    }
}

private fun NavGraphBuilder.initAppScreens(navController: NavHostController) {
    composable<MainScreens.InstalledAppListScreenRoute> {
        val vm = hiltViewModel<InstalledAppVM>()
        ScreenSurface(modifier = Modifier.fillMaxSize()) {
            InstalledAppListScreen(vm)
        }
    }

    composable<MainScreens.ScheduleScreenRoute> {
        val vm = hiltViewModel<SchedulesScreenVM>()
        val scheduleVM = hiltViewModel<CreateOrUpdateScheduleVM>(key = "ScheduleScreen")
        ScreenSurface(
            Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.bg_schedule_screen)
        ) {
            ScheduleScreen(
                vm,
                scheduleVM,
                onScheduleClick = {
                    navController.navigate(
                        MainScreens.ScheduleDetailScreenRoute(scheduleId = it.id)
                    )
                }
            )
        }
    }

    composable<MainScreens.AnalyticsScreenRoute> {
        ScreenSurface(modifier = Modifier.fillMaxSize()) {
            val vm = hiltViewModel<AnalyticsVm>()
            AnalyticsScreen(vm)
        }
    }

    composable<MainScreens.ProfileScreenRoute> {
        ScreenSurface(modifier = Modifier.fillMaxSize()) {
            ProfileScreen()
        }
    }

    composable<MainScreens.ScheduleDetailScreenRoute> {
        val vm = hiltViewModel<ScheduleDetailScreenVM>()
        val scheduleVM = hiltViewModel<CreateOrUpdateScheduleVM>(key = "ScheduleDetailScreen")
        ScreenSurface {
            ScheduleDetailScreen(
                onBackPress = {
                    navController.popBackStack()
                },
                vm = vm,
                scheduleVM = scheduleVM
            )
        }
    }
}