package com.bbm.applock.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bbm.applock.presentation.navigation.BottomNavBar
import com.bbm.applock.presentation.navigation.BottomNavItem
import com.bbm.applock.presentation.navigation.NavigationGraph
import com.bbm.applock.ui.theme.AppLockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val navItems = listOf(
        BottomNavItem.Apps,
        BottomNavItem.Schedule,
        BottomNavItem.Analytics,
        BottomNavItem.Profile
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppLockTheme {
                val navController = rememberNavController()
                //val currentRoute = navBackStackEntry?.destination?.route
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(
                            navController = navController,
                            items = navItems
                        )
                    }
                ) { innerPadding ->
                    val navController = navController
                    NavigationGraph(
                        navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}