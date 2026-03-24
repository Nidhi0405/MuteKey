package com.bbm.applock.presentation.analyticsModule.view

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.util.getAppIconDrawable
import com.bbm.applock.util.getAppNameFromPackage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bbm.applock.presentation.analyticsModule.uiState.CalendarUiState
import com.bbm.applock.presentation.analyticsModule.vm.toUi
import com.bbm.applock.ui.theme.AccentPink
import com.bbm.applock.ui.theme.AquaBlueBorder
import com.bbm.applock.ui.theme.AquaBlueLight
import com.bbm.applock.ui.theme.DisableColor
import com.bbm.applock.ui.theme.LightBlue
import com.bbm.applock.ui.theme.Pink
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.ui.theme.White
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Composable
fun AnalyticsScreen(vm: AnalyticsVm) {
    val todayHourlyMap by vm.todayHourlyUsageMap.collectAsState()
    val todayHourlyData by vm.todayHourlyUsage.collectAsState()
    val usageUiState by vm.uiState.collectAsState()
    val hourlyUsageMap by vm.hourlyUsageMap.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val selectedTab = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()
    var showCalendar by remember { mutableStateOf(false) }
    val chartData by vm.parsedChartData.collectAsState(
        initial = Pair(emptyList(), emptyList())
    )
    val visibleMonth by vm.visibleMonth.collectAsState()
    val selectedHourlyData by vm.combinedHourlyUsage.collectAsState()
    val selectedHourlyMap by vm.hourlyUsageMap.collectAsState()

    val series = chartData.second

    val viewMode by vm.viewMode.collectAsState()
    val radarSelectedDateMillis by vm.radarSelectedDateMillis.collectAsState()
    val usageSelectedDateMillis by vm.usageSelectedDateMillis.collectAsState()
    val context = LocalContext.current
    val firstDataCalendar = remember {
        vm.getStartDate(context)
    }
    val firstDataDate = firstDataCalendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()


    val actualSelectedDateMillis by vm.radarSelectedDateMillis.collectAsState()
    val isDateSelected = Calendar.getInstance().apply { timeInMillis = actualSelectedDateMillis }
        .let { selected ->
            val today = Calendar.getInstance()
            selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    selected.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }

    val displayedRadarData = remember { mutableStateOf<List<Long>?>(null) }
    val displayedRadarMap = remember { mutableStateOf<Map<String, List<Long>>?>(null) }
    val displayedDateMillis = remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(todayHourlyData, selectedHourlyData, todayHourlyMap, selectedHourlyMap) {
        val radarData = if (isDateSelected) todayHourlyData else selectedHourlyData
        val radarMap = if (isDateSelected) todayHourlyMap else selectedHourlyMap

        if ((radarData.isNotEmpty() || radarMap.isNotEmpty()) &&
            (displayedRadarData.value != radarData || displayedRadarMap.value != radarMap)
        ) {
            displayedRadarData.value = radarData
            displayedRadarMap.value = radarMap
            displayedDateMillis.value = actualSelectedDateMillis
        }
    }

    LaunchedEffect(Unit) {
        vm.syncUsageForSelectedDate()
    }

    LaunchedEffect(selectedTab, viewMode) {
        if (selectedTab == 2) {
            showCalendar = false
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            vm.setViewMode(AnalyticsVm.CalendarViewMode.MONTH)
            vm.syncUsageForSelectedDate()
        } else if (selectedTab == 0) {
            vm.setViewMode(AnalyticsVm.CalendarViewMode.DAY)
            vm.syncUsageForSelectedDate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlue)
            .padding(top = 16.dp)
    ) {
        TabSelector(selectedTab) {
            coroutineScope.launch {
                pagerState.scrollToPage(it)
            }
        }

        Spacer(Modifier.height(12.dp))

        val headerTitle = when (selectedTab) {
            0 -> "Daily Analytics"
            1 -> "Weekly Analytics"
            else -> ""
        }

        val headerDateText = if (selectedTab == 0) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(radarSelectedDateMillis))
        } else {
            if (viewMode == AnalyticsVm.CalendarViewMode.MONTH) {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(visibleMonth.time)
            } else {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(usageSelectedDateMillis))
            }
        }

        if (selectedTab != 2) {
            AnalyticsHeader(title = headerTitle, headerDateText = headerDateText, onMonthClick = {
                showCalendar = !showCalendar
            }, onPrevMonth = {
                val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                vm.setVisibleMonth(newMonth)
            }, onNextMonth = {
                val newMonth = (visibleMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                vm.setVisibleMonth(newMonth)
            })
        }

        //Spacer(Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState, modifier = Modifier.weight(1f), userScrollEnabled = false
        ) {
            key(it) {
                when (it) {
                    0 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Log.d(
                            "RADAR_CALENDAR_STATE",
                            "showCalendar=$showCalendar tab=$selectedTab mode=$viewMode"
                        )
                        if (showCalendar)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = White
                            ) {
                                UsageCalendar(
                                    state = CalendarUiState(
                                        selectedDateMillis = radarSelectedDateMillis,
                                        viewMode = AnalyticsVm.CalendarViewMode.DAY.toUi(),
                                        visibleMonth = visibleMonth
                                    ),
                                    firstDataDate = firstDataDate,
                                    onEvent = { event ->
                                        when (event) {
                                            is AnalyticsEvent.OnDateSelected -> vm.onRadarDateTapped(
                                                event.date
                                            )

                                            AnalyticsEvent.OnPrevMonth -> vm.setVisibleMonth(
                                                (vm.visibleMonth.value.clone() as Calendar).apply {
                                                    add(
                                                        Calendar.MONTH,
                                                        -1
                                                    )
                                                }
                                            )

                                            AnalyticsEvent.OnNextMonth -> vm.setVisibleMonth(
                                                (vm.visibleMonth.value.clone() as Calendar).apply {
                                                    add(
                                                        Calendar.MONTH,
                                                        1
                                                    )
                                                }
                                            )

                                            is AnalyticsEvent.OnViewModeChanged -> vm.setViewMode(
                                                event.viewMode
                                            )
                                        }
                                    }
                                )
                            }

                        displayedRadarData.value?.let { radarData ->
                            displayedRadarMap.value?.let { radarMap ->
                                Radar24HrScreen(
                                    hourlyData = radarData,
                                    hourlyUsageMap = radarMap,
                                    selectedDateMillis = displayedDateMillis.value ?: actualSelectedDateMillis
                                )
                            }
                        }
                    }

                    1 -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Log.d(
                            "USAGE_CALENDAR_STATE",
                            "showCalendar=$showCalendar tab=$selectedTab mode=$viewMode"
                        )
                        if (showCalendar) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 10.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = White,
                            ) {
                                UsageCalendar(
                                    state = CalendarUiState(
                                        selectedDateMillis = usageSelectedDateMillis,
                                        viewMode = viewMode.toUi(),
                                        visibleMonth = visibleMonth
                                    ),
                                    firstDataDate = firstDataDate,
                                    onEvent = { event ->
                                        when (event) {
                                            is AnalyticsEvent.OnDateSelected -> vm.onUsageDateTapped(
                                                event.date
                                            )

                                            AnalyticsEvent.OnPrevMonth -> vm.setVisibleMonth(
                                                (vm.visibleMonth.value.clone() as Calendar).apply {
                                                    add(
                                                        Calendar.MONTH,
                                                        -1
                                                    )
                                                }
                                            )

                                            AnalyticsEvent.OnNextMonth -> vm.setVisibleMonth(
                                                (vm.visibleMonth.value.clone() as Calendar).apply {
                                                    add(
                                                        Calendar.MONTH,
                                                        1
                                                    )
                                                }
                                            )

                                            is AnalyticsEvent.OnViewModeChanged -> vm.setViewMode(
                                                event.viewMode
                                            )
                                        }
                                    },
                                )
                            }
                        }

                        UsageTabContent(
                            state = usageUiState,
                            onRefresh = { vm.syncUsageForSelectedDate() }
                        )
                    }

                    2 -> PerformanceTabContent(
                        vm = vm, series = series, hourlyUsageMap = hourlyUsageMap
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsHeader(
    title: String,
    headerDateText: String,
    onMonthClick: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AquaBlue
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "<",
                fontSize = 16.sp,
                color = White,
                modifier = Modifier
                    .clickable { onPrevMonth() }
                    .padding(horizontal = 8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AquaBlue)
                    .clickable { onMonthClick() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(text = headerDateText, color = White, fontSize = 14.sp)
            }

            Text(
                text = ">",
                fontSize = 16.sp,
                color = White,
                modifier = Modifier
                    .clickable { onNextMonth() }
                    .padding(horizontal = 8.dp))
        }
    }
}

@Composable
fun TabSelector(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LightBlue)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            listOf("24hr Radar", "Usage", "Performance").forEachIndexed { index, label ->
                val isSelected = index == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Brush.horizontalGradient(
                                listOf(
                                    AquaBlueLight, AquaBlue
                                )
                            )
                            else SolidColor(White)
                        )
                        .clickable { onTabSelected(index) }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label, style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = if (isSelected) TextPrimary else TextSecondary,
                        )
                    )
                }
                if (index < 2) Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun Radar24HrScreen(
    hourlyData: List<Long>, hourlyUsageMap: Map<String, List<Long>>, selectedDateMillis: Long
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    var selectedStartHour by remember { mutableStateOf<Int?>(null) }
    var showAM by remember { mutableStateOf(true) }
    val totalTodayMillis = hourlyData.sum()
    val totalMinutesToday = TimeUnit.MILLISECONDS.toMinutes(totalTodayMillis)
    val today = remember { java.time.LocalDate.now() }

    val selectedRange = selectedStartHour?.let { it..it }

    val filteredHourlyData = if (showAM) {
        hourlyData.take(12)
    } else {
        hourlyData.drop(12)
    }

    val totalMillis = if (selectedRange == null) {
        filteredHourlyData.sum()
    } else {
        selectedRange.sumOf { localHour ->
            val globalHour = if (showAM) localHour % 12 else (localHour % 12) + 12
            hourlyData.getOrNull(globalHour) ?: 0L
        }
    }

    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(White), contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {

                Text(
                    text = "Hourly Usage",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AquaBlue,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf("AM", "PM").forEachIndexed { index, label ->
                        val isSelected = (index == 0 && showAM) || (index == 1 && !showAM)
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) LightBlue else AquaBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) AquaBlue else LightBlue
                                )
                                .clickable { showAM = index == 0 }
                                .padding(horizontal = 12.dp, vertical = 6.dp))
                        if (index == 0) Spacer(Modifier.width(8.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                ) {
                    Interactive12HrRadar(
                        hourlyData = filteredHourlyData,
                        selectedRange = selectedRange,
                        onHourSelected = { hour ->
                            selectedStartHour = if (selectedStartHour == hour) null else hour
                            Log.d("SELECTED_HOUR", "Radar selected hour: $selectedStartHour")
                        })


                    RadarCenterContent(
                        totalMinutes = totalMinutes,
                        selectedRange = selectedRange,
                        isAM = showAM,
                        totalMinutesToday = totalMinutesToday,
                        selectedDateMillis = selectedDateMillis
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        FullAppUsageList(
            hourlyUsageMap = hourlyUsageMap, selectedRange = selectedRange, isAM = showAM
        )
    }
}

@Composable
fun Interactive12HrRadar(
    hourlyData: List<Long>, selectedRange: IntRange?, onHourSelected: (Int) -> Unit
) {
    require(hourlyData.size == 12) { "hourlyData must contain exactly 12 values." }

    val maxUsage = hourlyData.maxOrNull()?.toFloat()?.takeIf { it > 0 } ?: 1f

    Canvas(
        modifier = Modifier
            .size(260.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y

                    var angle = Math.toDegrees(
                        kotlin.math.atan2(dy.toDouble(), dx.toDouble())
                    )

                    angle = (angle + 450) % 360

                    val hour = (((angle + 15) % 360) / 30).toInt()

                    onHourSelected(hour)
                }
            }) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        val strokeWidth = 34f

        drawCircle(
            color = AquaBlueBorder, radius = radius - strokeWidth / 2, style = Stroke(width = 2f)
        )

        drawArc(
            color = LightBlue,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(strokeWidth)
        )

        drawCircle(
            color = AquaBlueBorder, radius = radius + strokeWidth / 2, style = Stroke(width = 2f)
        )

        hourlyData.forEachIndexed { hour, millis ->

            val ratio = (millis / maxUsage).coerceIn(0f, 1f)
            val isSelected = selectedRange?.contains(hour) == true

            val sliceColor = if (isSelected) {
                AccentPink
            } else {
                lerp(LightBlue, Red, ratio)
            }

            drawArc(
                color = sliceColor,
                startAngle = -90f + hour * 30f,
                sweepAngle = 28f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth, cap = StrokeCap.Round
                )
            )
        }

        hourlyData.forEachIndexed { hour, _ ->

            val angleDeg = -90f + hour * 30f
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val textRadius = radius - strokeWidth - 28f

            val x = center.x + (textRadius * kotlin.math.cos(angleRad)).toFloat()
            val y = center.y + (textRadius * kotlin.math.sin(angleRad)).toFloat()

            drawContext.canvas.nativeCanvas.apply {

                val paint = android.graphics.Paint().apply {
                    color = DisableColor.toArgb()
                    textSize = 34f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                val displayHour = if (hour == 0) 12 else hour

                val textHeight = paint.descent() + paint.ascent()

                drawText(
                    displayHour.toString(), x, y - textHeight / 2, paint
                )
            }
        }
    }
}

@Composable
fun RadarCenterContent(
    totalMinutes: Long, selectedRange: IntRange?, isAM: Boolean, totalMinutesToday: Long? = null, selectedDateMillis: Long
) {
    val displayMinutes =
        if (selectedRange == null) totalMinutesToday ?: totalMinutes else totalMinutes
    val hours = displayMinutes / 60
    val minutes = displayMinutes % 60

    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(selectedDateMillis))
    }

    val title = if (selectedRange == null) {
        formattedDate
    } else {
        "${formatHour(selectedRange.first, isAM)} - ${
            formatHour((selectedRange.last + 1) % 12, isAM)
        }"
    }

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "$hours h $minutes m", color = Red, fontWeight = FontWeight.Bold, fontSize = 26.sp
            )
        }
    }
}

private fun formatHour(hour: Int, isAM: Boolean): String {
    val globalHour = if (!isAM) hour + 12 else hour
    val displayHour = when (val h = globalHour % 24) {
        0 -> 12
        in 1..11 -> h
        12 -> 12
        else -> h - 12
    }
    val amPm = if (globalHour < 12 || globalHour >= 24) "AM" else "PM"
    return "$displayHour:00 $amPm"
}

@Composable
fun FullAppUsageList(
    hourlyUsageMap: Map<String, List<Long>>, selectedRange: IntRange?, isAM: Boolean
) {
    val context = LocalContext.current
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    Log.d("FULL_LIST_DEBUG", "Selected Range: $selectedRange")

    val appUsageList = remember(hourlyUsageMap, selectedRange, isAM) {
        hourlyUsageMap.map { (packageName, usagePerHour) ->
            val totalMillis = if (selectedRange == null) {
                usagePerHour.take(24).sum()
            } else {
                selectedRange.sumOf { localHour ->
                    val targetHour = if (isAM) localHour % 12 else (localHour % 12) + 12
                    usagePerHour.getOrNull(targetHour) ?: 0L
                }
            }
            packageName to totalMillis
        }
            .filter { it.second > 0L }
            .sortedByDescending { it.second }
    }

    if (appUsageList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text("No usage in selected time")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(1f)
            .padding(vertical = 8.dp)
    ) {
        appUsageList.forEach { (pkg, millis) ->

            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            val formattedTime =
                if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    val icon = getAppIconDrawable(context, pkg)
                    icon?.let {
                        Image(
                            bitmap = it.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = getAppNameFromPackage(context, pkg),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = formattedTime,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}