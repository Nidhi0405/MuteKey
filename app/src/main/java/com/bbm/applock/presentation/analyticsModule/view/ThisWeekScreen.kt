package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.R
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.AquaBlueLight
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.util.RoundedSlicesPieChartRenderer
import com.bbm.applock.util.getAppIconDrawable
import com.bbm.applock.util.noRippleClickable
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun ThisWeekScreen(
    vm: AnalyticsVm,
    modifier: Modifier = Modifier
) {
    val topApps by vm.topAppsForUsageTab.collectAsState()
    val (hours, minutes) = vm.totalUsageTimeForUsageTab.collectAsState().value
    var usageMode by remember { mutableStateOf("Weekly") }
    var selectedDayOffset by remember { mutableIntStateOf(6) }

    LaunchedEffect(usageMode, selectedDayOffset) {
        val daysToSync = if (usageMode == "Weekly") 7 else (6 - selectedDayOffset) + 1
        vm.syncAndGetInstalledApps(daysToSync)
    }

    Box(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ModeDropdown(selectedMode = usageMode) { newMode ->
                    usageMode = newMode
                    val daysToSync =
                        if (newMode == "Weekly") 7 else 1
                    vm.syncAndGetInstalledApps(daysToSync)
                    selectedDayOffset = 6
                }
            }

            Spacer(Modifier.height(12.dp))

            WeekDaysBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                usageMode = usageMode,
                selectedOffset = selectedDayOffset,
                onDaySelected = { newOffset ->
                    selectedDayOffset = newOffset
                    vm.syncAndGetInstalledApps(6 - newOffset + 1)
                }
            )

            if (topApps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    UsageSummaryCard(
                        topApps = topApps,
                        hours = hours,
                        minutes = minutes,
                        modifier = Modifier
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppUsageList(
                        topApps = topApps,
                        modifier = Modifier
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No usage data available for selected period.",
                        color = ComposeColor.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun UsageSummaryCard(
    topApps: List<AppUsageInfo>,
    hours: Long,
    minutes: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 2.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Target Time\n23 hours Max",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(top = 50.dp)
        )
        val chartSize = 250.dp

        Box(
            modifier = Modifier
                .padding(vertical = 60.dp)
                .size(chartSize)
                .align(Alignment.Center)
        ) {
            AndroidView(
                factory = { ctx ->
                    PieChart(ctx).apply {
                        setUsePercentValues(false)
                        description.isEnabled = false
                        isDrawHoleEnabled = true
                        setHoleColor(Color.TRANSPARENT)
                        setDrawEntryLabels(false)
                        holeRadius = 78f
                        transparentCircleRadius = 78f
                        setDrawRoundedSlices(true)
                        setDrawSlicesUnderHole(true)
                        legend.isEnabled = false
                        renderer = RoundedSlicesPieChartRenderer(this, animator, viewPortHandler)
                        data = generatePieData(ctx, topApps)
                        animateY(1400, Easing.EaseInOutExpo)
                    }
                },
                update = { pieChart ->
                    pieChart.data = generatePieData(pieChart.context, topApps)
                    pieChart.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$hours h $minutes m",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "This week!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AppUsageList(
    topApps: List<AppUsageInfo>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "Hour / Week",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    topApps.forEach {
        AppUsageItem(app = it)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun AppUsageItem(app: AppUsageInfo) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = getAppIconDrawable(context = context, packageName = app.packageName),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "%.2f".format(app.usageTimeInMillis / 1000f / 60f / 60f) + " h",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun WeekDaysBar(
    modifier: Modifier = Modifier,
    usageMode: String,
    selectedOffset: Int,
    onDaySelected: (Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    val todayDate = calendar.get(Calendar.DAY_OF_MONTH)
    val todayMonth = calendar.get(Calendar.MONTH)
    val todayYear = calendar.get(Calendar.YEAR)

    val daysList = (0..6).map { offset ->
        val cal = Calendar.getInstance()
            .apply { add(Calendar.DAY_OF_YEAR, offset - 6) }
        DayInfo(
            offsetFromToday = offset,
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
            month = cal.get(Calendar.MONTH),
            year = cal.get(Calendar.YEAR)
        )
    }

    val dayCharMap = remember {
        mapOf(
            Calendar.SUNDAY to "S",
            Calendar.MONDAY to "M",
            Calendar.TUESDAY to "T",
            Calendar.WEDNESDAY to "W",
            Calendar.THURSDAY to "T",
            Calendar.FRIDAY to "F",
            Calendar.SATURDAY to "S"
        )
    }

    val weekendDays = listOf(Calendar.SATURDAY, Calendar.SUNDAY)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ComposeColor.White)
            .wrapContentHeight()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            daysList.forEach { dayInfo ->
                val isWeekend = dayInfo.dayOfWeek in weekendDays
                val isToday =
                    (dayInfo.dayOfMonth == todayDate) && (dayInfo.month == todayMonth) && (dayInfo.year == todayYear)

                val isSelected = when (usageMode) {
                    "Weekly" -> false
                    "Daily" -> dayInfo.offsetFromToday == selectedOffset
                    else -> false
                }

                val bgColor = when {
                    isSelected -> Red
                    isToday -> AquaBlueLight
                    isWeekend -> Red.copy(alpha = 0.6f)
                    else -> AquaBlue // Blue otherwise
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayCharMap[dayInfo.dayOfWeek] ?: "?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    ComposeColor.Red,
                                    CircleShape
                                )
                                else Modifier
                            )
                            .clip(CircleShape)
                            .clickable(enabled = usageMode == "Daily") {
                                onDaySelected(dayInfo.offsetFromToday)
                            }
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (isSelected) Modifier.border(
                                        1.dp,
                                        ComposeColor.White,
                                        CircleShape
                                    )
                                    else Modifier
                                )
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${dayInfo.dayOfMonth}",
                                color = ComposeColor.White,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W600
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DayInfo(
    val offsetFromToday: Int,
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int
)

@Composable
fun ModeDropdown(selectedMode: String, onModeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ComposeColor.White)
            .wrapContentSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .noRippleClickable {
                    expanded = true
                }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedMode,
                color = ComposeColor.DarkGray,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = TextSecondary
                )
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_drop_down),
                contentDescription = null,
                modifier = Modifier.size(8.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("Weekly", "Daily").forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = it, style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 18.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.W400
                            )
                        )
                    },
                    onClick = {
                        onModeSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun generatePieData(context: Context, apps: List<AppUsageInfo>): PieData {
    val entries = apps.mapIndexed { index, app ->
        val adjusted = if (app.usageTimeInMillis < 10 * 60 * 1000L) {
            // Apply a minimum value to very small slices for visibility
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

@Preview(showBackground = true)
@Composable
fun PreviewWeekDaysBar() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(ComposeColor(0xFFF0F8FF))
        ) {
            WeekDaysBar(
                usageMode = "Daily",
                selectedOffset = 6, // Today
                onDaySelected = {}
            )
        }
    }
}