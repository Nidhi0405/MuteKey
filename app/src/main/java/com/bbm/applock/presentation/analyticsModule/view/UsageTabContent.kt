package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.applock.data.model.DayInfo
import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.util.RoundedSlicesPieChartRenderer
import com.bbm.applock.util.getAppIconDrawable
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun UsageTabContent(modifier: Modifier = Modifier) {
    val vm = hiltViewModel<AnalyticsVm>()
    val appsData by vm.installedApps.collectAsState()
    var usageMode by remember { mutableStateOf("Weekly") }
    var selectedDayOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(usageMode, selectedDayOffset) {
        if (usageMode == "Weekly") {
            vm.syncAndGetInstalledApps(7)
        } else {
            vm.syncAndGetInstalledApps(selectedDayOffset + 1)
        }
    }

    val topApps = appsData
        .filter { it.usageTimeInMillis > 0L }
        .sortedByDescending { it.usageTimeInMillis }
        .take(7)

    val totalTimeMillis = topApps.sumOf { it.usageTimeInMillis }
    val hours = TimeUnit.MILLISECONDS.toHours(totalTimeMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis) % 60
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
    ) {

        if (topApps.isNotEmpty()) {
            Column(
                modifier = modifier
                    .verticalScroll(
                        rememberScrollState(),
                        reverseScrolling = false
                    )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    ModeDropdown(selectedMode = usageMode) {
                        usageMode = it
                        selectedDayOffset = 0 // reset on switch
                    }
                }
                // 🔵 Add this:
                WeekDaysBar(
                    modifier = Modifier
                        .fillMaxWidth(), usageMode = usageMode,
                    selectedOffset = selectedDayOffset,
                    onDaySelected = { newOffset ->
                        val day = (6 - newOffset)
                        Log.e("TAG", "UsageTabContent click: $day )")
                        selectedDayOffset = day
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 12.dp,
                                vertical = 24.dp
                            )// Outer horizontal padding for the card
                            // 1. Shadow: Applied BEFORE clip and background for correct rendering
                            .shadow(
                                elevation = 6.dp, // Adjust for desired shadow strength (e.g., 4.dp, 8.dp)
                                shape = RoundedCornerShape(28.dp),
                                // Softer, more diffused shadow colors
                                ambientColor = androidx.compose.ui.graphics.Color(0x1F000000), // Very light black/transparent
                                spotColor = androidx.compose.ui.graphics.Color(0x33000000) // Light black/transparent
                            )
                            // 2. Clip: Clips the content (including background) to the rounded shape
                            .clip(RoundedCornerShape(28.dp))
                            // 3. Background: The white background of the card
                            .background(MaterialTheme.colorScheme.surface) // Uses theme's surface color for white background
                            // REMOVED: .border(1.dp, ComposeColor.White, RoundedCornerShape(28.dp))
                            // The original design does NOT have a visible border line, relies on shadow.
                            // 4. Internal Padding: Padding for content INSIDE the card
                            .padding(
                                vertical = 2.dp,
                                horizontal = 20.dp
                            ), // Increased horizontal padding
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Target Time\n23 hours Max",
                            color = MaterialTheme.colorScheme.primary, // Use theme's primary color (ensure it's the vibrant blue)
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

                                        // ✅ Adjust these two for thickness balance
                                        holeRadius = 78f
                                        transparentCircleRadius = 78f

                                        // ✅ Important for design consistency
                                        setDrawRoundedSlices(true)
                                        setDrawSlicesUnderHole(true)

                                        legend.isEnabled = false
                                        renderer = RoundedSlicesPieChartRenderer(
                                            this,
                                            animator,
                                            viewPortHandler
                                        )
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

                            // 💬 Center stats
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
                                    text = "${hours} h ${minutes} m",
                                    color = MaterialTheme.colorScheme.error, // Use theme's error color (ensure it's the vibrant red)
                                    style = MaterialTheme.typography.headlineMedium // Ensure your theme's typography makes this bold/large
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ){
                    Text(
                        text = "Hour / Week",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                topApps.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // App icon (already available if you're using Coil + AppUsageInfo)
                            AsyncImage(
                                model = getAppIconDrawable(context = LocalContext.current, packageName = app.packageName), // from AppUsageInfo
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
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading Pie chart...", color = androidx.compose.ui.graphics.Color.DarkGray)
            }
        }
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


    // Generate last 7 days
    val daysList = (0..6).map { offset ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset - 6) }
        DayInfo(
            offsetFromToday = offset,
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),  // For day character
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH), // For number
            monthYearTodayTriple = Triple(
                cal.get(Calendar.MONTH),
                cal.get(Calendar.YEAR),
                offset == 6
            ) // For today check
        )
    }

    val dayCharMap = mapOf(
        Calendar.SUNDAY to "S",
        Calendar.MONDAY to "M",
        Calendar.TUESDAY to "T",
        Calendar.WEDNESDAY to "W",
        Calendar.THURSDAY to "T",
        Calendar.FRIDAY to "F",
        Calendar.SATURDAY to "S"
    )

    val weekendDays = listOf(Calendar.SATURDAY, Calendar.SUNDAY)

    Box(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, ComposeColor(0xFF2196F3), RoundedCornerShape(20.dp))
            .background(ComposeColor.White)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            daysList.forEach { it ->
                val (month, year, isTodayGuaranteed) = it.monthYearTodayTriple
                val isWeekend = it.dayOfWeek in weekendDays
                val isToday =
                    isTodayGuaranteed && (it.dayOfMonth == todayDate) && (month == todayMonth) && (year == todayYear)

                val isSelected = when (usageMode) {
                    "Weekly" -> isToday
                    "Daily" -> it.offsetFromToday == (6 - selectedOffset)
                    else -> false
                }

                val bgColor = when {
                    isSelected || isWeekend -> ComposeColor(0xFFE53935) // 🔴 Red
                    else -> ComposeColor(0xFF00ACC1) // 🔵 Blue
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayCharMap[it.dayOfWeek] ?: "?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .then(
                                if (isSelected) Modifier.border(2.dp, ComposeColor.Red, CircleShape)
                                else Modifier
                            )
                            .clip(CircleShape)
                            .clickable(enabled = usageMode == "Daily") {
                                onDaySelected(it.offsetFromToday) // Pass the day's offset
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
                                text = "${it.dayOfMonth}",
                                color = ComposeColor.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewWeekDaysBar() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(ComposeColor(0xFFF0F8FF)) // Light Aqua backdrop
        ) {
        }
    }
}

@Composable
fun ModeDropdown(selectedMode: String, onModeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ComposeColor.White)
            .border(1.dp, ComposeColor.Gray, RoundedCornerShape(16.dp))
            .wrapContentSize(),
        contentAlignment = Alignment.TopEnd
    ) {

        Text(
            text = "$selectedMode \uD83D\uDD3D",
            modifier = Modifier
                .padding(all = 10.dp)
                .clickable { expanded = true }
                .align(Alignment.Center),
            color = ComposeColor.DarkGray
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("Weekly", "Daily").forEach {
                DropdownMenuItem(text = { Text(text = it) }, onClick = {
                    onModeSelected(it)
                    expanded = false
                })
            }
        }
    }
}


fun generatePieData(context: Context, apps: List<AppUsageInfo>): PieData {
    val entries = apps.mapIndexed { index, app ->
        val adjusted = if (app.usageTimeInMillis < 10 * 60 * 1000L) {
            Log.w("PieFix", "Boosting tiny slice: ${app.name}")
            10 * 60 * 1000f
        } else app.usageTimeInMillis.toFloat()

        PieEntry(adjusted, app.name).apply {
            icon = getAppIconDrawable(context, app.packageName)
                ?.toBitmap(96, 96)
                ?.toDrawable(context.resources)
        }
    }

    // Generate unique vibrant colors using Golden Ratio for distinctiveness
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






