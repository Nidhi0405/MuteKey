package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.R
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextPrimaryGradient
import com.bbm.applock.ui.theme.TextSecondary
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
@Composable
fun AnalyticsHeader(
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes headerTitle : Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_back_colored),
            contentDescription = "Back",
            modifier = Modifier
                .size(24.dp)
                .noRippleClickable(onBackPress)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = stringResource(headerTitle), // <-- new title
            style = MaterialTheme.typography.titleMedium.copy(
                brush = TextPrimaryGradient,
                fontSize = 22.sp,
                fontWeight = FontWeight.W700
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun LastSevenScreen(vm: AnalyticsVm,onBackPress: () -> Unit) {
    LaunchedEffect(Unit) { vm.syncAndGetInstalledApps(7) }

    val parsedChartData by vm.parsedChartData.collectAsState()
    val labels = parsedChartData.first
    val series = parsedChartData.second

    var selectedApp by remember { mutableStateOf<String?>(null) }
    val rSeries = remember(series) { mutableStateOf(series) }
    AnalyticsHeader(onBackPress = onBackPress, headerTitle = R.string.last_7_days)
    Spacer(Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(Modifier.height(8.dp))

        if (labels.isNotEmpty() && rSeries.value.isNotEmpty()) {
            CurvedLineChartView(
                labels = labels,
                series = rSeries.value,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No journey data available.",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        WeeklyTopAppsInteractive(
            vm = vm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            selectedApp = selectedApp,
            onAppTapped = { pkg ->
                if (selectedApp == pkg) {
                    selectedApp = null
                    rSeries.value = series
                } else {
                    selectedApp = pkg
                    rSeries.value = series.filter { it.first == pkg }
                }
            }
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun WeeklyTopAppsInteractive(
    vm: AnalyticsVm,
    modifier: Modifier = Modifier,
    selectedApp: String?,
    onAppTapped: (String) -> Unit
) {
    val weeklyTopApps by vm.topAppsForUsageTab.collectAsState()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "hour/week",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.W400,
                    color = TextPrimary
                )
            )
        }

        val items = weeklyTopApps.take(5)
        if (items.isEmpty()) {
            Text(
                "No weekly usage data.",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            )
        } else {
            items.forEach { app ->
                AppUsageItem(
                    app = app,
                    isSelected = (selectedApp == app.packageName),
                    onClick = { onAppTapped(app.packageName) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun WeeklyUsageList(
    vm: AnalyticsVm,
    modifier: Modifier = Modifier
) {
    val weeklyTopApps by vm.topAppsForUsageTab.collectAsState()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "hour/week",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.W400,
                    color = TextPrimary
                )
            )
        }

        val items = weeklyTopApps.take(5)
        if (items.isEmpty()) {
            Text(
                "No weekly usage data.",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            )
        } else {
            items.forEach { app ->
                AppUsageItem(app = app)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AppUsageItem(
    app: AppUsageInfo,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) Modifier.noRippleClickable { onClick() } else Modifier
            )
            .then(
                if (isSelected) Modifier.border(1.dp, AquaBlue, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.W500
                )
            )
        }
        Text(
            text = "%.2f h".format(app.usageTimeInMillis / 1000f / 60f / 60f),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.W500,
                color = TextPrimary
            )
        )
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
        chart.data = null
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
    chart.invalidate()
}
fun generateColor(index: Int): Int {
    val hue = (index * 47f) % 360f
    return Color.HSVToColor(180, floatArrayOf(hue, 0.8f, 0.95f))
}


