package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.bbm.applock.R
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.WhiteColor
import com.bbm.applock.util.IconLineChartRenderer
import com.bbm.applock.util.getAppIconDrawable
import com.bbm.applock.util.getAppNameFromPackage
import com.bbm.applock.util.noRippleClickable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun AnalyticsScreen(vm: AnalyticsVm) {
    val parsedChartData by vm.parsedChartData.collectAsState()
    val combinedHourlyUsage by vm.combinedHourlyUsage.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor(0xFFE0F7FA)) // light background
    ) {
        TabSelector(selectedTab) {
            selectedTab = it
            coroutineScope.launch {
                pagerState.scrollToPage(it)
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) {
                when (it) {
                    0 -> JourneyTabContent(
                        labels = parsedChartData.first,
                        series = parsedChartData.second,
                        hourlyData = combinedHourlyUsage
                    )

                    1 -> UsageTabContent(
                        vm,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (selectedTab == 0 && (parsedChartData.first.isEmpty() || combinedHourlyUsage.isEmpty())) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading chart...", color = ComposeColor.DarkGray)
                }
            } else if (selectedTab == 1 && vm.installedApps.collectAsState().value.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading apps data...", color = ComposeColor.DarkGray)
                }
            }
        }
    }
}

@Composable
fun TabSelector(selectedTab: Int, onTabSelected: (Int) -> Unit) {
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
                            if (isSelected)
                                Brush.horizontalGradient(
                                    listOf(
                                        ComposeColor(0xFF4DD0E1),
                                        ComposeColor(0xFF4DD0E1),
                                        ComposeColor(0xFF0097A7)
                                    )
                                )
                            else
                                SolidColor(ComposeColor.White)
                        )
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = if (isSelected) ComposeColor.Black else ComposeColor.DarkGray,
                        )
                    )
                }
                if (index == 0)
                    Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun JourneyTabContent(
    labels: List<String>,
    series: List<Pair<String, List<Float>>>,
    hourlyData: List<Long>
) {
    var selectedApp by remember {
        mutableStateOf<String?>(null)
    }
    val rSeries = remember(series) {
        mutableStateOf(series)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))
        HourlyUsageBarChart(
            hourlyData = hourlyData,
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteColor)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (labels.isNotEmpty() && series.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.last_7_days_top_5_app_engagement),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
                CurvedLineChartView(
                    labels = labels,
                    series = rSeries.value,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                ChartLegendRow(
                    selectedApp = selectedApp,
                    apps = series.map { it.first },
                    onAppClick = { pkg ->
                        if (selectedApp == pkg) {
                            selectedApp = null
                            rSeries.value = series
                        } else {
                            selectedApp = pkg
                            rSeries.value = series.filter { it.first == pkg }
                        }
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No journey data available.", color = ComposeColor.DarkGray)
                }
            }
        }
    }
}

@Composable
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
}

@Composable
fun CurvedLineChartView(
    labels: List<String>,
    series: List<Pair<String, List<Float>>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                setupLineChart(this, labels, series, ctx)
                renderer = IconLineChartRenderer(this, animator, viewPortHandler)
            }
        },
        update = { chart ->
            setupLineChart(chart, labels, series, context)
            chart.invalidate()
        },
        modifier = modifier
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
                icon = if (i == maxIndex && y > 0f && !isZeroLine) iconDrawable else null
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
            } else {
                disableDashedLine()
            }
        }
        dataSets.add(dataSet)
    }

    if (dataSets.isNotEmpty()) {
        chart.data = LineData(dataSets)
    } else {
        chart.data = null // Clear data if no datasets
    }

    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.axisRight.isEnabled = false
    chart.legend.textColor = Color.DKGRAY
    chart.xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = 1f
        valueFormatter = IndexAxisValueFormatter(labels)
        textColor = Color.DKGRAY
        setDrawGridLines(false)
    }
    chart.axisLeft.textColor = Color.DKGRAY
    chart.axisLeft.axisMinimum = 0f
    chart.axisLeft.setDrawGridLines(false)
    chart.setScaleEnabled(false)
    chart.setPinchZoom(false)
    chart.isDoubleTapToZoomEnabled = false
    chart.isDragEnabled = false
    chart.isHighlightPerTapEnabled = false
    chart.invalidate() // Refresh chart
}

fun generateColor(index: Int): Int {
    val hue = (index * 47f) % 360f
    return Color.HSVToColor(180, floatArrayOf(hue, 0.8f, 0.95f))
}

@Composable
fun HourlyUsageBarChart(
    hourlyData: List<Long>,
    modifier: Modifier = Modifier
) {
    val maxUsage = (60 * 60 * 1000L).toFloat()
    val barWidth = 12.dp

    Column(
        modifier = modifier.wrapContentSize()
    ) {
        Text(
            text = stringResource(R.string.today_s_hourly_usage),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            // Y-Axis Labels Column
            Column(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .height(180.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                val ySteps = 4 // Number of labels on Y-axis
                for (i in ySteps downTo 0) {
                    val label = ((maxUsage * i) / ySteps).toLong().let {
                        "${TimeUnit.MILLISECONDS.toMinutes(it)} min"
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = ComposeColor.DarkGray,
                        modifier = Modifier.height(180.dp / (ySteps))
                    )
                }
            }
            // Bar Chart Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    hourlyData.forEachIndexed { hour, millis ->
                        val heightRatio =
                            (millis / maxUsage).coerceIn(0f, 1f)
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
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(AquaBlue)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$hour",
                                style = MaterialTheme.typography.labelSmall,
                                color = ComposeColor.DarkGray
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
        Spacer(Modifier.height(8.dp))
    }
}