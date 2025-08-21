package com.bbm.applock.presentation.analyticsModule.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.R
import com.bbm.applock.ui.theme.TextPrimaryGradient
import com.bbm.applock.util.noRippleClickable

@Composable
fun AnalyticsScreen(
    onOpenToday: () -> Unit,
    onOpenLastSeven: () -> Unit,
    onOpenThisWeek: () -> Unit,
) {
    val entries = remember {
        listOf(
            "Today’s" to onOpenToday,
            "Last 7 days" to onOpenLastSeven,
            "This Week" to onOpenThisWeek
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        HeaderSection()
        Spacer(Modifier.height(22.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries.size) { index ->
                Card(
                    modifier = Modifier
                        .noRippleClickable(entries[index].second)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0XFF6BD1CD))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entries[index].first,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W500
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_arrow_right),
                            modifier = Modifier
                                .size(30.dp)
                                .padding(8.dp),
                            contentDescription = entries[index].first,
                            contentScale = ContentScale.Inside
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(42.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Image(
                painter = painterResource(R.drawable.ic_schedule_analytics),
                contentDescription = "Calender Icon",
                modifier = Modifier
                    .size(38.dp)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.analytics),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}