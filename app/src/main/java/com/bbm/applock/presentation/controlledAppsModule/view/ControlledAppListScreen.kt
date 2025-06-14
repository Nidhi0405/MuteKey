package com.bbm.applock.presentation.controlledAppsModule.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbm.applock.presentation.controlledAppsModule.vm.ControlledAppListVM

@Composable
fun ControlledAppListScreen(
    vm: ControlledAppListVM,
) {
    ControlledAppListScreenContent()
}

@Composable
private fun ControlledAppListScreenContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun ControlledAppRow(
    modifier: Modifier = Modifier
) {

}