package com.bbm.applock.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bbm.applock.ui.theme.Typography
import com.bbm.applock.ui.theme.Pink

@Composable
fun BottomNavBar(
    navController: NavHostController,
    items: List<BottomNavItem<*>>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val context = LocalContext.current
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val isSelected = currentRoute?.contains(item.screen::class.simpleName.orEmpty()) == true
            NavigationBarItem(
                selected = currentDestination?.route?.contains(item.screen::class.simpleName.orEmpty()) == true,
                onClick = {
                    navController.navigate(item.screen) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = context.getString(item.label),
                        tint = if (isSelected) Pink else Color.Black
                    )
                },
                label = {
                    Text(
                        text = context.getString(item.label),
                        style = Typography.labelSmall,
                        color = if (isSelected) Pink else Color.Black
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}