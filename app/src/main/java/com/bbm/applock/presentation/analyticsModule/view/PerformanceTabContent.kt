package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.ui.theme.White
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.bbm.applock.R
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.util.IconLineChartRenderer
import com.bbm.applock.util.getAppIconDrawable
import com.bbm.applock.util.getAppNameFromPackage
import com.bbm.applock.util.noRippleClickable
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import java.util.*
import kotlin.collections.map

@Composable
fun PerformanceTabContent(
    vm: AnalyticsVm,
    series: List<Pair<String, List<Float>>>,
    hourlyUsageMap: Map<String, List<Long>>
) {
    var selectedApp by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var usageMode by remember { mutableStateOf("Daily") }
    var dynamicLabels by remember { mutableStateOf(emptyList<String>()) }
    var filteredSeries by remember { mutableStateOf(series) }
    var headerText by remember { mutableStateOf("Today") }


    Log.d("PerformanceTab", "Original Series Data: $series")
    Log.d("PerformanceTab", "Filtered Series Data: $filteredSeries")

    LaunchedEffect(usageMode) {
        when (usageMode) {
            "Daily" -> vm.loadUsage("DAILY")
            "Weekly" -> {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfWeekInMillis = calendar.timeInMillis
                vm.loadUsage("WEEKLY")
                vm.debugWeeklyUsage(context, startOfWeekInMillis, System.currentTimeMillis())
            }

            "Monthly" -> vm.loadUsage("MONTHLY")
        }
    }
    LaunchedEffect(hourlyUsageMap, usageMode) {
        val (labels, series) = vm.getAlignedSeriesFromHourlyMap(context, hourlyUsageMap, usageMode)
        dynamicLabels = labels
        filteredSeries = series

        headerText = when (usageMode) {
            "Weekly" -> {
                val totalWeeks = labels.size
                "Last $totalWeeks weeks"
            }

            "Monthly" -> {
                val totalMonths = labels.size
                "Last $totalMonths months"
            }

            else -> "Today"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(headerText, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ModeDropdown(selectedMode = usageMode) { newMode ->
                    usageMode = newMode
                    when (newMode) {
                        "Daily" -> vm.loadUsage("DAILY")
                        "Weekly" -> vm.loadUsage("WEEKLY")
                        "Monthly" -> vm.loadUsage("MONTHLY")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Chart Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (dynamicLabels.isNotEmpty() && filteredSeries.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Log.d("PerformanceTab", "Passing filtered data to chart: $filteredSeries")
                    val displaySeries = if (selectedApp != null) {
                        filteredSeries.filter { it.first == selectedApp }
                    } else {
                        filteredSeries
                    }
                    CurvedLineChartView(
                        vm = vm,
                        labels = dynamicLabels,
                        series = displaySeries,
                        selectedApp = selectedApp,
                        modifier = Modifier
                            .fillMaxWidth(),
                        usageMode = usageMode
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No performance data available")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // App List Section
        val usageList = filteredSeries.map { (pkg, values) ->
            val totalUsage = when (usageMode) {
                "Daily" -> values.sum()
                "Weekly" -> values.sum()
                "Monthly" -> values.sum()
                else -> values.sum()
            }
            pkg to totalUsage
        }
            .sortedByDescending { it.second }
            .take(5)

        usageList.forEach { (pkg, totalMinutes) ->
            val icon = getAppIconDrawable(context, pkg)
            val appName = getAppNameFromPackage(context, pkg)

            val formattedTime = when (usageMode) {
                "Daily" -> {
                    val hours = totalMinutes.toInt() / 60
                    val minutes = totalMinutes.toInt() % 60
                    if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                }

                "Weekly" -> {
                    val hours = totalMinutes.toInt() / 60
                    val minutes = totalMinutes.toInt() % 60
                    val str = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                    Log.d("WeeklyUsageList", "Weekly [$usageMode] $pkg formatted time: $str")
                    str
                }

                "Monthly" -> {
                    val hours = totalMinutes.toInt() / 60
                    val minutes = totalMinutes.toInt() % 60
                    val str = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                    Log.d("MonthlyUsageList", "Monthly [$usageMode] $pkg formatted time: $str")
                    str
                }

                else -> "${totalMinutes.toInt()} min"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(
                        if (selectedApp == pkg)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else
                            Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .noRippleClickable {
                        selectedApp = if (selectedApp == pkg) null else pkg
                    }
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let {
                        Image(
                            bitmap = it.toBitmap().asImageBitmap(),
                            contentDescription = appName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = appName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ModeDropdown(selectedMode: String, onModeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(White)
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
                color = TextPrimary,
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
            listOf("Daily", "Weekly", "Monthly").forEach {
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

/*@Composable
fun ChartLegendRow(
    selectedApp: String?,
    apps: List<String>,
    onAppClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        apps.forEach { pkg ->
            val icon = remember(pkg) { getAppIconDrawable(context, pkg) }
            val isSelected = selectedApp == pkg
            val label = getAppNameFromPackage(context, pkg)
            Column(
                modifier = Modifier.noRippleClickable { onAppClick(pkg) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                icon?.let {
                    Image(
                        bitmap = it.toBitmap().asImageBitmap(),
                        contentDescription = label,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .border(2.dp, color = AquaBlue, shape = CircleShape)
                                        .padding(4.dp)
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}*/

@Composable
fun CurvedLineChartView(
    vm: AnalyticsVm,
    labels: List<String>,
    series: List<Pair<String, List<Float>>>,
    selectedApp: String?,
    modifier: Modifier = Modifier,
    usageMode: String
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                setupLineChart(vm, this, labels, series, ctx, usageMode, selectedApp)
                renderer = IconLineChartRenderer(this, animator, viewPortHandler)
            }
        },
        update = { chart ->
            setupLineChart(vm, chart, labels, series, context, usageMode, selectedApp)
            chart.invalidate()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(12.dp)
    )
}

fun setupLineChart(
    vm: AnalyticsVm,
    chart: LineChart,
    labels: List<String>,
    series: List<Pair<String, List<Float>>>,
    context: Context,
    usageMode: String,
    selectedApp: String?,
) {
    val dataSets = ArrayList<ILineDataSet>()

    val seriesWithTotal = series.map { (pkg, values) ->
        Triple(pkg, values, values.sum())
    }.sortedByDescending { it.third }.take(5)

    val globalMax = series.flatMap { it.second }.maxOrNull()?.coerceAtLeast(1f) ?: 10f
    chart.axisLeft.axisMaximum = globalMax * 1.1f
    chart.axisLeft.axisMinimum = 0f

    seriesWithTotal.forEachIndexed { index, triple ->
        val pkg = triple.first
        val values = triple.second
        val appName = getAppNameFromPackage(context, pkg)
        val iconDrawable = getAppIconDrawable(context, pkg)
        val isSelected = selectedApp == pkg
        val isDimmed = selectedApp != null && selectedApp != pkg

        val maxY = values.maxOrNull() ?: 1f

        val safeValues = values.map { it.coerceAtLeast(0f) }

        Log.d(
            "PerformanceTab",
            "Setting up chart for $pkg: Total Entries = ${safeValues.size}, Values: $safeValues"
        )

        Log.d("PerformanceTab", "Safe Values: $safeValues")
        val entries = safeValues.mapIndexed { i, y ->
            Entry(i.toFloat(), y)
        }
        if (usageMode == "Monthly" && entries.isNotEmpty() && iconDrawable != null) {
            val lastIndex = entries.lastIndex
            val y = entries[lastIndex].y
            if (y < globalMax * 0.05f) {
                entries[lastIndex].y = globalMax * 0.05f
            }
            entries[lastIndex].icon = iconDrawable
        }

        if (usageMode != "Monthly" && iconDrawable != null) {
            val maxY = safeValues.maxOrNull() ?: 1f
            val maxIndex = safeValues.indexOf(maxY)
            entries[maxIndex].icon = iconDrawable
        }
        Log.d("PerformanceTab", "Entries: $entries")

        if (entries.isEmpty()) {
            Log.d("PerformanceTab", "No entries for dataset $pkg, skipping.")
            return@forEachIndexed
        }

        val dataSet = LineDataSet(entries, appName).apply {
            //color = vm.generateColor(index)
            val baseColor = vm.generateColor(index)
            color = if (isDimmed) baseColor and 0x55FFFFFF else baseColor
            lineWidth = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(false)
            setDrawValues(false)
            setDrawIcons(true)
            iconsOffset = MPPointF(0f, -30f)
            if (isDimmed) {
                setDrawFilled(false)
                enableDashedLine(10f, 10f, 0f)
            }
        }

        dataSets.add(dataSet)
    }

    if (dataSets.isEmpty()) {
        Log.d("PerformanceTab", "No valid datasets available, chart will not render.")
        chart.data = null
    } else {
        chart.data = LineData(dataSets)
    }

    chart.data = if (dataSets.isNotEmpty()) LineData(dataSets) else null
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.axisRight.isEnabled = false
    chart.axisLeft.axisMinimum = 0f
    chart.axisLeft.setDrawGridLines(false)

    chart.xAxis.apply {
        labelCount = labels.size
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = IndexAxisValueFormatter(labels)
        granularity = 1f
        setDrawGridLines(false)
    }

    chart.setTouchEnabled(false)
    chart.setScaleEnabled(false)
    chart.isDragEnabled = false

    Log.d("PerformanceTab", "Setting chart data: ${chart.data?.dataSetCount ?: 0} datasets")

    chart.invalidate()
}