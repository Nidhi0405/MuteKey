package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.presentation.analyticsModule.uiState.CalendarViewMode
import com.bbm.applock.presentation.analyticsModule.uiState.UsageUiState
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.LightBlue
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.White
import com.bbm.applock.util.RoundedSlicesPieChartRenderer
import com.bbm.applock.util.getAppIconDrawable
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun UsageTabContent(
    state: UsageUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate = remember(state.selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = state.selectedDateMillis }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBlue)
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            UsageSummaryCard(
                topApps = state.chartApps,
                hours = state.totalHours,
                minutes = state.totalMinutes,
                selectedDate = selectedDate,
                viewMode = state.viewMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.appUsageList.isNotEmpty()) {
                AppUsageList(
                    topApps = state.appUsageList,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else {
                EmptyState(state.emptyMessage)
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TextPrimary
        )
    }
}

@Composable
fun UsageSummaryCard(
    topApps: List<AppUsageInfo>,
    hours: Long,
    minutes: Long,
    modifier: Modifier = Modifier,
    selectedDate: Calendar,
    viewMode: CalendarViewMode
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(White)
            .padding(vertical = 40.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {

        val chartSize = 250.dp

        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {

            AndroidView(
                factory = { ctx ->
                    PieChart(ctx).apply {
                        description.isEnabled = false
                        isDrawHoleEnabled = true
                        setHoleColor(Color.TRANSPARENT)
                        setDrawEntryLabels(false)

                        holeRadius = 80f
                        transparentCircleRadius = 80f

                        legend.isEnabled = false
                        renderer = RoundedSlicesPieChartRenderer(this, animator, viewPortHandler)
                        data = generatePieData(ctx, topApps)
                        animateY(1200, Easing.EaseInOutExpo)
                    }
                },
                update = { pieChart ->
                    pieChart.data = generatePieData(pieChart.context, topApps)
                    pieChart.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Time",
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (hours > 0)
                        "${hours}h ${minutes}m"
                    else
                        "${minutes}m",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AquaBlue
                )

                Text(
                    text = when (viewMode) {
                        CalendarViewMode.MONTH -> "This Month"
                        CalendarViewMode.DAY -> "${formatDate(selectedDate)}"
                        CalendarViewMode.WEEK -> "This Week"
                    },
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

fun formatDate(calendar: Calendar): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

@Composable
fun AppUsageList(
    topApps: List<AppUsageInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(topApps) { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val context = LocalContext.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = getAppIconDrawable(context, app.packageName),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = app.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                Text(
                    text = formatUsageTime(app.usageTimeInMillis),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun formatUsageTime(millis: Long): String {
    val totalMinutes = millis / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

data class DayInfo(
    val offsetFromToday: Int,
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int
)

fun generatePieData(context: Context, apps: List<AppUsageInfo>): PieData {
    val entries = apps.mapIndexed { index, app ->
        val adjusted = if (app.usageTimeInMillis < 10 * 60 * 1000L) {
            10 * 60 * 1000f
        } else app.usageTimeInMillis.toFloat()

        PieEntry(adjusted, app.name).apply {
            icon = getAppIconDrawable(context, app.packageName)
                ?.toBitmap(96, 96)
                ?.toDrawable(context.resources)
        }
    }

    val vibrantColors = entries.indices.map { index ->
        val goldenRatioConjugate = 0.618034f
        val hue = ((index * goldenRatioConjugate) % 1.0f) * 360f
        val saturation = 0.75f
        val brightness = 0.95f
        Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }

    val dataSet = PieDataSet(entries, "").apply {
        setDrawIcons(false)
        sliceSpace = 4f
        selectionShift = 4f
        colors = vibrantColors
    }

    return PieData(dataSet).apply {
        setDrawValues(false)
    }
}