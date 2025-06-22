package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.util.IconLineChartRenderer
import com.bbm.applock.util.getAppIconDrawable
import com.bbm.applock.util.getAppNameFromPackage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun AnalyticsScreen() {
    val vm = hiltViewModel<AnalyticsVm>()
    val chartJson by vm.chartJson.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        vm.fetchAndGenerateChartJson(7)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor(0xFFE0F7FA)) // light background
    ) {
        // Tab Selector (Always on top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(ComposeColor(0xFFE0F7FA))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                listOf("Journey", "Usage").forEachIndexed { index, label ->
                    val isSelected = index == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(
                                    listOf(
                                        ComposeColor(0xFF4DD0E1),
                                        ComposeColor(0xFF0097A7)
                                    )
                                ) else SolidColor(ComposeColor.Transparent)
                            )
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) ComposeColor.Black else ComposeColor.DarkGray,
                        )
                    }
                }
            }
        }

        // Tab Content (Properly pushed below)
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (chartJson.isNotBlank()) {
                when (selectedTab) {
                    0 -> JourneyTabContent(chartJson, vm)
                    1 -> UsageTabContent(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading chart...", color = ComposeColor.DarkGray)
                }
            }
        }
    }
}


@Composable
fun JourneyTabContent(chartJson: String, vm: AnalyticsVm) {
    val parsed = remember(chartJson) { parseChartJson(chartJson) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        HourlyUsageBarChart(vm)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {
            CurvedLineChartView(
                labels = parsed.first,
                series = parsed.second
            )
            ChartLegendRow(parsed.second.map { it.first })
        }
    }
}

@Composable
fun ChartLegendRow(apps: List<String>) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        apps.forEach { pkg ->
            val icon = remember(pkg) { getAppIconDrawable(context, pkg) }
            val label = getAppNameFromPackage(context, pkg)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                icon?.let {
                    Image(
                        bitmap = it.toBitmap().asImageBitmap(),
                        contentDescription = label,
                        modifier = Modifier.size(48.dp)
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
}


@Composable
fun CurvedLineChartView(
    labels: List<String>,
    series: List<Pair<String, List<Float>>>
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                setupLineChart(this, labels, series, context)
                renderer = IconLineChartRenderer(this, animator, viewPortHandler)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    )
}

fun setupLineChart(
    chart: LineChart,
    labels: List<String>,
    series: List<Pair<String, List<Float>>>,
    context: Context
) {
    val dataSets = ArrayList<ILineDataSet>()
    series.forEachIndexed { index, (label, values) ->
        val appName = getAppNameFromPackage(context, label)
        val isZeroLine = values.all { it == 0f }
        val safeValues = if (isZeroLine) List(values.size) { 0.01f } else values
        val iconDrawable = getAppIconDrawable(context, label)
        val maxIndex = safeValues.indexOf(safeValues.maxOrNull() ?: 0f)
        val entries = safeValues.mapIndexed { i, y ->
            Entry(i.toFloat(), y).apply {
                icon = if (i == maxIndex && y > 0f) iconDrawable else null
            }
        }
        val color = generateColor(index)
        val dataSet = LineDataSet(entries, appName).apply {
            this.color = color
            valueTextColor = Color.DKGRAY
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            lineWidth = 5f
            circleHoleColor = Color.WHITE
            setDrawCircles(false)
            setDrawCircleHole(false)
            circleRadius = 4f
            circleHoleRadius = 1.5f
            valueTextSize = 0f
            setDrawValues(true)
            setDrawIcons(true)
            iconsOffset = MPPointF(0f, -24f)
            if (isZeroLine) {
                enableDashedLine(6f, 3f, 0f)
            }
        }
        dataSets.add(dataSet)
    }
    chart.data = LineData(dataSets)
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.axisRight.isEnabled = false
    chart.legend.textColor = Color.DKGRAY
    chart.xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = 1f
        valueFormatter = IndexAxisValueFormatter(labels)
        textColor = Color.DKGRAY
    }
    chart.axisLeft.textColor = Color.DKGRAY
    chart.axisLeft.axisMinimum = 0f
    chart.setScaleEnabled(false)
    chart.setPinchZoom(false)
    chart.isDoubleTapToZoomEnabled = false
    chart.isDragEnabled = false
    chart.isHighlightPerTapEnabled = false
    chart.invalidate()
}

fun generateColor(index: Int): Int {
    val hue = (index * 47f) % 360f
    return Color.HSVToColor(180, floatArrayOf(hue, 0.8f, 0.95f))
}

fun parseChartJson(json: String): Pair<List<String>, List<Pair<String, List<Float>>>> {
    val jsonObject = JSONObject(json)
    val datesArray = jsonObject.getJSONArray("dates")
    val labels = List(datesArray.length()) { i -> datesArray.getString(i) }
    val seriesArray = jsonObject.getJSONArray("series")
    val seriesList = mutableListOf<Pair<String, List<Float>>>()
    for (i in 0 until seriesArray.length()) {
        val seriesObj = seriesArray.getJSONObject(i)
        val name = seriesObj.getString("name")
        val dataArray = seriesObj.getJSONArray("data")
        val dataList = List(dataArray.length()) { j ->
            dataArray.optDouble(j, 0.0).toFloat()
        }
        seriesList.add(name to dataList)
    }
    val top5 = seriesList
        .sortedByDescending { it.second.sum() }
        .take(5)
    return labels to top5
}

@Composable
fun HourlyUsageBarChart(
    vm: AnalyticsVm,
    modifier: Modifier = Modifier
) {
    val usageByHour = vm.hourlyUsageMap.collectAsState()
    LaunchedEffect(usageByHour) {
        vm.syncHourlyUsage(days = 1) // Today's 24-hour usage
    }
    val hourlyData = remember(usageByHour.value) {
        val combined = MutableList(24) { 0L }

        usageByHour.value.values.forEach { appList ->
            appList.forEachIndexed { hour, millis ->
                combined[hour] += millis
            }
        }
        Log.e("CombinedChart", "HourlyCombined: ${combined.joinToString()}")
        combined
    }
    val maxUsage = (60 * 60 * 1000L).toFloat()
    val barWidth = 12.dp
    LaunchedEffect(hourlyData) {
        Log.e("ChartData", "HourlyData: ${hourlyData.joinToString()}")
    }
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .wrapContentSize()
    ) {
        Text(
            text = "Hourly Usage",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
    Row(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Y-Axis Labels Column
        Column(
            modifier = Modifier
                .padding(end = 4.dp)
                .wrapContentSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val ySteps = 4
            for (i in ySteps downTo 0) {
                val label = ((maxUsage * i) / ySteps).toLong().let {
                    "${TimeUnit.MILLISECONDS.toMinutes(it)} min"
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.height(150.dp / ySteps)
                )
            }
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                hourlyData.forEachIndexed { hour, millis ->
                    val heightRatio = millis / maxUsage
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .height((heightRatio * 150).dp.coerceAtLeast(4.dp))
                                .width(barWidth)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ComposeColor.Cyan)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${hour}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Each bar = time spent between that hour (in minutes)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}





