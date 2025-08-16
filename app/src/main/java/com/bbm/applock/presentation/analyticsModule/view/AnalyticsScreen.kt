package com.bbm.applock.presentation.analyticsModule.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm

@Composable
fun AnalyticsScreen(
    onOpenToday: () -> Unit,
    onOpenLastSeven: () -> Unit,
    onOpenThisWeek: () -> Unit,
) {

    var entries = listOf(
        "Today’s" to onOpenToday,
        "Last 7 days" to onOpenLastSeven,
        "This Week" to onOpenThisWeek
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries) { (title, onClick) ->
            Card(
                onClick = onClick,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text("›")
                }
            }
        }
    }
}