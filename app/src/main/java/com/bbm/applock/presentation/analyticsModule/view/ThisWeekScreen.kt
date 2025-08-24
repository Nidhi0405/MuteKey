package com.bbm.applock.presentation.analyticsModule.view

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.util.toReadableDuration
import com.bbm.applock.R
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.AquaBlueLight
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextPrimaryGradient
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.util.RoundedSlicesPieChartRenderer
import com.bbm.applock.util.getAppIconDrawable
import com.bbm.applock.util.noRippleClickable
import com.bbm.applock.util.toDayOrdinalAndDayName
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.time.LocalDate
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun ThisWeekScreen(
    vm: AnalyticsVm,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topApps by vm.topAppsForUsageTab.collectAsState()
    val usageMillis = vm.totalUsageTimeForUsageTab.collectAsState().value

    var usageMode by remember { mutableStateOf("Weekly") } // "Weekly" | "Daily"
    val cacheReady by vm.isCacheReady.collectAsState()
    // Mon=0 .. Sun=6 for current week
    val todayIndex = remember { todayIndexInWeek() }
    var selectedDayOffset by remember { mutableIntStateOf(todayIndex) }

    // 0 = current week, 1 = previous week (ONLY these two)
    var weekOffset by remember { mutableIntStateOf(0) }

    // initial load
    LaunchedEffect(Unit) {
        vm.primeDailyCache(days = 21) {
            vm.showWeekChunk(0)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        AnalyticsHeader(
            onBackPress = onBackPress,
            headerTitle = R.string.this_week,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ModeDropdown(selectedMode = usageMode) { newMode ->
                    usageMode = newMode
                    selectedDayOffset = if (weekOffset == 0) todayIndexInWeek() else 0
                    if (!cacheReady) return@ModeDropdown
                    if (newMode == "Weekly") vm.showWeekChunk(weekOffset)
                    else vm.showDay(dateForCell(weekOffset, selectedDayOffset))
                }
            }

            Spacer(Modifier.height(22.dp))

            WeekStripLazyRow(
                usageMode = usageMode,
                weekOffset = weekOffset,
                selectedOffset = selectedDayOffset,
                onWeekChanged = { newOffset ->
                    weekOffset = newOffset.coerceIn(0, 1)
                    selectedDayOffset = if (weekOffset == 0) todayIndexInWeek() else 0
                    if (!cacheReady) return@WeekStripLazyRow
                    if (usageMode == "Weekly") {
                        vm.showWeekChunk(weekOffset)
                    } else {
                        vm.showDay(dateForCell(weekOffset, selectedDayOffset))
                    }
                },
                onDaySelected = { newOffset ->
                    selectedDayOffset = newOffset
                    if (!cacheReady) return@WeekStripLazyRow
                    if (usageMode == "Daily") vm.showDay(dateForCell(weekOffset, selectedDayOffset))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            if (topApps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    UsageSummaryCard(
                        topApps = topApps,
                        usageMillis = usageMillis,
                        selectedDay = if (usageMode == "Daily") {
                            dateForCell(weekOffset, selectedDayOffset)
                        } else null,
                        isCurrentWeek = weekOffset == 0,
                        modifier = Modifier
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppUsageList(
                        topApps = topApps,
                        modifier = Modifier,
                        isWeekly = (usageMode == "Weekly")   // pass this flag
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
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    )
                }
            }
        }
    }

}

@Composable
fun UsageSummaryCard(
    topApps: List<AppUsageInfo>,
    usageMillis: Long,
    modifier: Modifier = Modifier,
    selectedDay: LocalDate?,
    isCurrentWeek: Boolean
) {
    var appUsageInfo by remember { mutableStateOf<AppUsageInfo?>(null) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 2.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
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
                        this.setOnChartValueSelectedListener(
                            object : OnChartValueSelectedListener {
                                override fun onValueSelected(
                                    e: Entry?,
                                    h: Highlight?
                                ) {
                                    appUsageInfo = (e?.data as? AppUsageInfo)
                                }

                                override fun onNothingSelected() {
                                    appUsageInfo = null
                                }
                            }
                        )
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
                    appUsageInfo = null
                    pieChart.highlightValues(null)
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
                    text = appUsageInfo?.name ?: "Total Time",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400,
                        color = TextPrimary
                    )
                )
                Text(
                    text = (appUsageInfo?.usageTimeInMillis?.toReadableDuration())
                        ?: usageMillis.toReadableDuration(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W600,
                        brush = TextPrimaryGradient
                    )
                )
                Text(
                    text = selectedDay?.toDayOrdinalAndDayName
                        ?: if (isCurrentWeek)
                            "This week!"
                        else
                            "Last week!",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400,
                        color = TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun AppUsageList(
    topApps: List<AppUsageInfo>,
    modifier: Modifier = Modifier,
    isWeekly: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = if (isWeekly) "Hour/Week" else "Hour/Day",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(topApps.size) { index ->
            AppUsageItem(app = topApps[index])
        }
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
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500
                )
            )
        }
        Text(
            text = app.usageTimeInMillis.toReadableDuration(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                color = TextPrimary
            )
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WeekStripLazyRow(
    usageMode: String,                  // "Weekly" | "Daily"
    weekOffset: Int,                    // 0=this week, 1=last week
    selectedOffset: Int,                // 0..6 (Mon..Sun) for the visible week
    onWeekChanged: (Int) -> Unit,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = weekOffset)
    val fling = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { idx ->
            onWeekChanged(idx.coerceIn(0, 1))
        }
    }
    LaunchedEffect(weekOffset) {
        listState.animateScrollToItem(weekOffset)
    }

    LazyRow(
        state = listState,
        flingBehavior = fling,
        userScrollEnabled = true,
        reverseLayout = true, // swipe left to go to previous week
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ComposeColor.White)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .heightIn(min = 76.dp)
    ) {
        items(count = 2) { page -> // 0=this week, 1=last week
            Box(
                modifier = Modifier.fillParentMaxWidth()
            ) {
                WeekRowMonSun(
                    page = page,
                    usageMode = usageMode,
                    isActive = (page == weekOffset),
                    selectedOffset = if (page == weekOffset) selectedOffset else -1,
                    onDaySelected = onDaySelected
                )
            }
        }
    }
}

@Composable
private fun WeekRowMonSun(
    page: Int,                          // 0=this week, 1=last week
    usageMode: String,
    isActive: Boolean,
    selectedOffset: Int,                // 0..6 or -1 (no highlight if not active)
    onDaySelected: (Int) -> Unit
) {
    val today = java.time.LocalDate.now()
    val weekStart = mondayOfWeek(today).minusWeeks(page.toLong())
    val weekDates = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val isCurrentWeek = page == 0
    val dayChar = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        weekDates.forEachIndexed { index, date ->
            val isToday = date == today
            val isFuture = isCurrentWeek && date.isAfter(today)
            val isWeekend = date.dayOfWeek == java.time.DayOfWeek.SATURDAY ||
                    date.dayOfWeek == java.time.DayOfWeek.SUNDAY
            val isSelected = isActive && usageMode == "Daily" && index == selectedOffset

            val baseColor = when {
                isSelected -> Red
                isToday -> AquaBlueLight
                isWeekend -> Red.copy(alpha = 0.6f)
                else -> AquaBlue
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayChar[index],
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFuture && isCurrentWeek) ComposeColor.LightGray.copy(alpha = 0.4f)
                            else baseColor
                        )
                        .then(
                            if (!isFuture && usageMode == "Daily" && isActive)
                                Modifier.clickable { onDaySelected(index) }
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFuture && isCurrentWeek) ComposeColor.LightGray.copy(alpha = 0.6f)
                                else baseColor
                            )
                            .then(
                                if (isSelected) Modifier.border(
                                    1.dp,
                                    ComposeColor.White,
                                    CircleShape
                                )
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${date.dayOfMonth}",
                            color = ComposeColor.White,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 14.sp, fontWeight = FontWeight.W600
                            )
                        )
                    }
                }
            }
        }
    }
}

// Helpers
private fun mondayOfWeek(date: java.time.LocalDate): java.time.LocalDate {
    val dow = date.dayOfWeek.value // Mon=1 .. Sun=7
    return date.minusDays((dow - 1).toLong())
}

fun todayIndexInWeek(): Int = java.time.LocalDate.now().dayOfWeek.value - 1

private fun dateForCell(weekOffset: Int, dayIndex: Int): java.time.LocalDate {
    val baseMon = mondayOfWeek(java.time.LocalDate.now())
    return baseMon.minusWeeks(weekOffset.toLong()).plusDays(dayIndex.toLong())
}

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
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
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
                            text = it,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 14.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.W600
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
            this.data = app
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
