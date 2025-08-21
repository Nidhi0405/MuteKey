package com.bbm.applock.presentation.analyticsModule.view

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.R
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.ui.theme.*
import com.bbm.applock.util.getAppNameFromPackage
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun TodaysScreen(vm: AnalyticsVm,onBackPress : ()-> Unit) {
    LaunchedEffect(Unit) {
        vm.syncAndGetInstalledApps(1)
        vm.syncHourlyUsage(days = 1)
    }

    var isPm by remember {
        mutableStateOf(Calendar.getInstance().get(Calendar.AM_PM) == Calendar.PM)
    }
    AnalyticsHeader(onBackPress = onBackPress, headerTitle = R.string.todays_analytics)
    Spacer(Modifier.height(8.dp))
    LazyColumn(
        modifier = Modifier.padding(top = 20.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            HourlyUsageClockHeatmap(
                vm = vm,
                isPm = isPm,
                onToggle = { isPm = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            DailyUsageListAmPm(
                vm = vm,
                isPm = isPm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun HourlyUsageClockHeatmap(
    vm: AnalyticsVm,
    isPm: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val usageByHour by vm.hourlyUsageMap.collectAsState(initial = emptyMap())

    val hourlyData = remember(usageByHour) {
        val combined = MutableList(24) { 0L }
        usageByHour.values.forEach { appList ->
            appList.forEachIndexed { hour, millis ->
                if (hour < combined.size) combined[hour] += millis
            }
        }
        combined
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = ComposeColor.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Hourly Usage",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            AmPmToggle(
                isPm = isPm,
                onToggle = onToggle,
                modifier = Modifier.fillMaxWidth(0.5f)
            )

            Spacer(Modifier.height(16.dp))
            ClockHeatmap(hourlyData = hourlyData, isPm = isPm)
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Time zone: - ${if (isPm) "PM" else "AM"}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )
        }
    }
}

@Composable
fun AmPmToggle(
    isPm: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ComposeColor(0xFFF1F9FA))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AM button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (!isPm) AquaBlueLight else ComposeColor.Transparent)
                .clickable { onToggle(false) }
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AM",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = if (!isPm) ComposeColor.White else TextSecondary
                )
            )
        }

        Spacer(Modifier.width(6.dp))

        // PM button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isPm) AquaBlueLight else ComposeColor.Transparent)
                .clickable { onToggle(true) }
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PM",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = if (isPm) ComposeColor.White else TextSecondary
                )
            )
        }
    }
}

@Composable
fun ClockHeatmap(
    hourlyData: List<Long>,
    isPm: Boolean,
    modifier: Modifier = Modifier
) {
    val bgLight = LightBlue
    val outlineOuter = AquaBlueBorder
    val outlineInner = AquaBlueLight
    val highUsageCol = AccentPink
    val mediumUsageCol = AquaBlueLight

    val base = if (isPm) 12 else 0
    val usages = remember(hourlyData, isPm) {
        (0..11).map { hourlyData.getOrElse(base + it) { 0L } }
    }
    val maxUsageMs = 3_600_000f

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgLight, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {this
        val density = LocalDensity.current
        val boxPx = with(density) { maxWidth.coerceAtMost(maxHeight).toPx() }
        val outline = with(density) { 2.dp.toPx() }
        val ringW = with(density) { 24.dp.toPx() }

        val outerR = boxPx / 2f - outline / 2
        val ringR = outerR - ringW / 2f
        val innerR = ringR - ringW / 2f - outline

        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)

            usages.forEachIndexed { idx, u ->
                val ratio = u.toFloat() / maxUsageMs
                val colour = when {
                    ratio > 0.5f -> highUsageCol
                    ratio > 0.1f -> mediumUsageCol
                    else -> ComposeColor.Transparent
                }
                if (colour != ComposeColor.Transparent) {
                    drawArc(
                        color = colour,
                        startAngle = idx * 30f - 90f,
                        sweepAngle = 30f,
                        useCenter = false,
                        style = Stroke(width = ringW, cap = StrokeCap.Butt),
                        topLeft = Offset(c.x - ringR, c.y - ringR),
                        size = Size(ringR * 2, ringR * 2)
                    )
                }
            }

            drawCircle(outlineOuter, outerR, c, style = Stroke(outline))
            drawCircle(outlineInner, innerR, c, style = Stroke(outline))
        }

        for (h in 1..12) {
            val angle = (h * 30f - 90f).toDouble()
            val labelR = innerR * 0.88f
            val x = (cos(Math.toRadians(angle)) * labelR).toFloat()
            val y = (sin(Math.toRadians(angle)) * labelR).toFloat()

            Text(
                text = h.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = TextPrimary
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = with(density) { x.toDp() },
                        y = with(density) { y.toDp() }
                    )
            )
        }
    }
}

@Composable
fun DailyUsageList(
    vm: AnalyticsVm,
    modifier: Modifier = Modifier
) {
    val topAppsToday by vm.topAppsForUsageTab.collectAsState()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hour / Day",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )
        }

        if (topAppsToday.isEmpty()) {
            Text(
                "No usage data for today.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )
        } else {
            topAppsToday.forEach { app ->
                AppUsageItem(app = app)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DailyUsageListAmPm(
    vm: AnalyticsVm,
    isPm: Boolean,
    modifier: Modifier = Modifier
) {
    val usageByHour by vm.hourlyUsageMap.collectAsState(initial = emptyMap())
    val context = LocalContext.current

    val items = remember(usageByHour, isPm) {
        val range = if (isPm) 12..23 else 0..11
        usageByHour.map { (pkg, hours) ->
            val sum = range.sumOf { h -> hours.getOrElse(h) { 0L } }
            AppUsageInfo(
                name = getAppNameFromPackage(context, pkg),
                packageName = pkg,
                usageTimeInMillis = sum
            )
        }
            .filter { it.usageTimeInMillis > 0L }
            .sortedByDescending { it.usageTimeInMillis }
            .take(5)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hour / ${if (isPm) "PM" else "AM"}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )
        }

        if (items.isEmpty()) {
            Text(
                "No ${if (isPm) "PM" else "AM"} usage data.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
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
