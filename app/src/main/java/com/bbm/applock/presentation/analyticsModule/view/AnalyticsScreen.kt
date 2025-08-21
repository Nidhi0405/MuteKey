package com.bbm.applock.presentation.analyticsModule.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalyticsScreen(
    onOpenToday: () -> Unit,
    onOpenLastSeven: () -> Unit,
    onOpenThisWeek: () -> Unit,
) {
    val entries = listOf(
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                }
            }
        }
    }
}
