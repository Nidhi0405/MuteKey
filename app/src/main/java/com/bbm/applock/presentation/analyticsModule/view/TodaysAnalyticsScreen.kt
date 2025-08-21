package com.bbm.applock.presentation.analyticsModule.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.bbm.applock.ui.theme.AccentPink
import com.bbm.applock.ui.theme.AquaBlueBorder
import com.bbm.applock.ui.theme.AquaBlueLight
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.util.getAppNameFromPackage
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun TodaysScreen(vm: AnalyticsVm, onBackPress: () -> Unit) {
    LaunchedEffect(Unit) {
        vm.syncAndGetInstalledApps(1)
        vm.syncHourlyUsage(days = 1)
    }

    var isPm by remember {
        mutableStateOf(Calendar.getInstance().get(Calendar.AM_PM) == Calendar.PM)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        AnalyticsHeader(
            onBackPress = onBackPress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            headerTitle = R.string.todays_analytics
        )

        Spacer(Modifier.height(22.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
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
            .padding(16.dp)
    ) {
        this
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
        if (items.isEmpty()) {
            Text(
                "No ${if (isPm) "PM" else "AM"} usage data.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 18.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.W600
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )
        } else {
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400,
                        color = TextPrimary
                    )
                )
            }
            items.forEach { app ->
                AppUsageItem(app = app)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
