package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import coil3.ImageLoader
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.rememberAsyncImagePainter
import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.presentation.analyticsModule.uiState.CalendarViewMode
import com.bbm.applock.presentation.analyticsModule.uiState.UsageUiState
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.LightBlue
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.White
import com.bbm.applock.util.AppIcon
import com.bbm.applock.util.loadAppPackageMetadata
import com.bbm.applock.util.RoundedSlicesPieChartRenderer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val usageDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

@Composable
fun UsageTabContent(
    state: UsageUiState,
    imageLoader: ImageLoader,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate = remember(state.selectedDateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = state.selectedDateMillis ?: System.currentTimeMillis()
        }
    }

    val effectiveViewMode = if (state.selectedDateMillis == null) {
        CalendarViewMode.MONTH
    } else {
        state.viewMode
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LightBlue)
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            UsageSummaryCard(
                topApps = state.chartApps,
                imageLoader = imageLoader,
                hours = state.totalHours,
                minutes = state.totalMinutes,
                selectedDate = selectedDate,
                viewMode = effectiveViewMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.appUsageList.isNotEmpty()) {
                AppUsageList(
                    topApps = state.appUsageList,
                    imageLoader = imageLoader,
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
    imageLoader: ImageLoader,
    hours: Long,
    minutes: Long,
    modifier: Modifier = Modifier,
    selectedDate: Calendar,
    viewMode: CalendarViewMode
) {
    val context = LocalContext.current
    val pieData by produceState<PieData?>(initialValue = null, context, topApps) {
        value = withContext(Dispatchers.IO) {
            generatePieData(context, imageLoader, topApps)
        }
    }

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
                        data = pieData
                        animateY(1200, Easing.EaseInOutExpo)
                    }
                },
                update = { pieChart ->
                    pieChart.data = pieData
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
    return calendar.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(usageDateFormatter)
}

@Composable
fun AppUsageList(
    topApps: List<AppUsageInfo>,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        topApps.forEach { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(
                            model = AppIcon(app.packageName),
                            imageLoader = imageLoader
                        ),
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

private suspend fun generatePieData(
    context: Context,
    imageLoader: ImageLoader,
    apps: List<AppUsageInfo>
): PieData {
    val metadata = loadAppPackageMetadata(context, imageLoader, apps.map { it.packageName })
    val entries = apps.mapIndexed { index, app ->
        val adjusted = if (app.usageTimeInMillis < 10 * 60 * 1000L) {
            10 * 60 * 1000f
        } else app.usageTimeInMillis.toFloat()

        PieEntry(adjusted, app.name).apply {
            icon = metadata[app.packageName]?.icon
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
