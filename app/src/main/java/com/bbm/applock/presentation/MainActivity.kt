package com.bbm.applock.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.bbm.applock.App
import com.bbm.applock.presentation.navigation.BottomNavBar
import com.bbm.applock.presentation.navigation.BottomNavItem
import com.bbm.applock.presentation.navigation.NavigationGraph
import com.bbm.applock.ui.theme.AppLockTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val lifeCycleEvent by lazy { (applicationContext as App).lifeCycleEvent }
    val navItems = listOf(
        BottomNavItem.Apps,
        BottomNavItem.Schedule,
        BottomNavItem.Analytics,
        BottomNavItem.Profile
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            lifeCycleEvent.emit(Lifecycle.Event.ON_CREATE)
        }
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
                    NavigationGraph(
                        navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            lifeCycleEvent.emit(Lifecycle.Event.ON_START)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            lifeCycleEvent.emit(Lifecycle.Event.ON_PAUSE)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            lifeCycleEvent.emit(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            lifeCycleEvent.emit(Lifecycle.Event.ON_STOP)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            lifeCycleEvent.emit(Lifecycle.Event.ON_DESTROY)
        }
    }
}