package com.bbm.applock.presentation.scheduleModule.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.Schedule
import com.bbm.applock.R
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.scheduleModule.view.component.CreateOrUpdateScheduleBottomSheet
import com.bbm.applock.presentation.scheduleModule.view.component.TimeSelectionCard
import com.bbm.applock.presentation.scheduleModule.view.component.WeekDaySelectionRow
import com.bbm.applock.presentation.scheduleModule.vm.CreateOrUpdateScheduleVM
import com.bbm.applock.presentation.scheduleModule.vm.ScheduleDetailScreenVM
import com.bbm.applock.ui.theme.ActiveGreen
import com.bbm.applock.ui.theme.DisableColor
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextPrimaryGradient
import com.bbm.applock.util.AppIcon
import com.bbm.applock.util.noRippleClickable
import com.bbm.applock.util.toHourMinute
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    onBackPress: () -> Unit,
    vm: ScheduleDetailScreenVM,
    scheduleVM: CreateOrUpdateScheduleVM,
) {
    val schedule = vm.schedule.collectAsState()
    val imageLoader = vm.imageLoader

    // Create UPDATE
    val newScheduleName = scheduleVM.scheduleName.collectAsState()
    val onScheduleNameChange = scheduleVM::onScheduleNameChange
    val controlledApps = scheduleVM.controlledApps.collectAsState().value
    val selectedApps = scheduleVM.selectedControlledApps.collectAsState().value
    val onControlledAppSelect = scheduleVM::onAppSelectToggleClick
    val selectedRepeatDays = scheduleVM.selectedDays.collectAsState().value
    val onRepeatDaysChangeClick = scheduleVM::onDaySelectToggleClick
    val (startTime, endTime) = scheduleVM.selectedTimeSlot.collectAsState().value
    val onSelectTimeSlot = scheduleVM::onSelectTimeSlot
    val onUpdateClick = scheduleVM::updateSchedule
    val isCreateScheduleDialogVisible =
        scheduleVM.isCreateScheduleDialogVisible.collectAsState().value
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            // Prevent dismissal by user interaction (swipe down, tap outside)
            newState != SheetValue.Hidden
        }
    )
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        val stateBlock = { message: UiState ->
            when (message) {
                is UiState.Success<*> -> {
                    if (message.data is CreateOrUpdateScheduleVM.DeleteSuccess) {
                        onBackPress.invoke()
                    }
                }

                is UiState.Failure<*> -> {
                    launch {
                        snackbarHostState.showSnackbar(message = message.message)
                    }
                }

                UiState.Ideal -> {}
                UiState.Loading -> {}
                is UiState.ValidationError -> {}
            }
        }
        launch {
            (scheduleVM.state).collect { message ->
                stateBlock.invoke(message)
            }
        }
        launch {
            (vm.state).collect { message ->
                stateBlock.invoke(message)
            }
        }
    }
    BackHandler {
        scheduleVM.clear()
        onBackPress.invoke()
    }
    schedule.value?.let { schedule ->
        ScheduleDetailScreenContent(
            schedule = schedule,
            imageLoader = imageLoader,
            onBackPress = onBackPress,
            onToggleActiveStatus = {
                scheduleVM.toggleActiveStatusSchedule(schedule)
            },
            // createParams
            newScheduleName = newScheduleName.value,
            onNewScheduleNameChange = onScheduleNameChange,
            controlledApps = controlledApps,
            onControlledAppSelect = onControlledAppSelect,
            selectedApps = selectedApps,
            selectedRepeatDays = selectedRepeatDays,
            onRepeatDaysChangeClick = onRepeatDaysChangeClick,
            onSelectTimeSlot = onSelectTimeSlot,
            startTime = startTime,
            endTime = endTime,
            onCreateScheduleClick = {
                onUpdateClick.invoke(schedule)
            },
            sheetState = sheetState,
            isUpdateScheduleDialogVisible = isCreateScheduleDialogVisible,
            modifier = Modifier.fillMaxSize(),
            toggleDialogVisible = {
                if (!isCreateScheduleDialogVisible) {
                    scheduleVM.fillUpdateScheduleFields(schedule)
                }
                scheduleVM.toggleCreateScheduleDialog()
            },
            onUpdateDialogDismiss = {
                scheduleVM.toggleCreateScheduleDialog()
            },
            onDeleteSchedule = {
                scheduleVM.deleteSchedule(schedule)
            }
        )
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreenContent(
    schedule: Schedule,
    onBackPress: () -> Unit,
    imageLoader: ImageLoader,
    onToggleActiveStatus: () -> Unit,
    // Update Params
    isUpdateScheduleDialogVisible: Boolean,
    newScheduleName: String,
    onNewScheduleNameChange: (String) -> Unit,
    controlledApps: List<AppUsageInfo>,
    onControlledAppSelect: (AppUsageInfo) -> Unit,
    selectedApps: Set<AppUsageInfo>,
    selectedRepeatDays: Set<DayOfWeek>,
    onRepeatDaysChangeClick: (DayOfWeek) -> Unit,
    onSelectTimeSlot: (LocalTime, LocalTime) -> Unit,
    startTime: LocalTime,
    endTime: LocalTime,
    onCreateScheduleClick: () -> Unit,
    toggleDialogVisible: () -> Unit,
    onUpdateDialogDismiss: () -> Unit,
    onDeleteSchedule: () -> Unit,
    sheetState: SheetState,
    ////
    modifier: Modifier = Modifier,
) {
    if (isUpdateScheduleDialogVisible) {
        CreateOrUpdateScheduleBottomSheet(
            schedule = schedule,
            scheduleName = newScheduleName,
            onScheduleNameChange = onNewScheduleNameChange,
            selectedDays = selectedRepeatDays,
            onDaySelected = onRepeatDaysChangeClick,
            controlledApps = controlledApps,
            selectedApps = selectedApps,
            onAppSelectToggleClick = onControlledAppSelect,
            startTime = startTime,
            endTime = endTime,
            onDismiss = onUpdateDialogDismiss,
            onAddOrUpdateClick = {
                // will be create only
                onCreateScheduleClick.invoke()
            },
            onDeleteOrDismiss = {
                // delete from here as schedule is already created
                onDeleteSchedule.invoke()
            },
            onSelectTimeSlot = onSelectTimeSlot,
            imageLoader = imageLoader,
            sheetState = sheetState,
        )
    }

    val horizontalPadding = remember { 16.dp }
    Box(modifier = modifier) {
        Column {
            HeaderSection(
                onBackPress = onBackPress,
                onEditClick = {
                    toggleDialogVisible.invoke()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )
            Spacer(Modifier.height(30.dp))
            ScheduleNameBox(
                schedule = schedule,
                onToggleActiveStatus = onToggleActiveStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .height(68.dp)
            ) {
                TimeSelectionCard(
                    heading = "Start",
                    time = schedule.startTime.toHourMinute,
                    onClick = {
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TimeSelectionCard(
                    heading = "End",
                    time = schedule.endTime.toHourMinute,
                    onClick = {
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.repeat),
                modifier = Modifier.padding(horizontal = 26.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500
                )
            )

            Spacer(Modifier.height(4.dp))

            WeekDaySelectionRow(
                selectedDays = schedule.repeatDays.toSet(),
                onDaySelected = {},
                isViewing = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.scheduled_apps),
                modifier = Modifier.padding(horizontal = 26.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500
                )
            )

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    horizontal = 22.dp,
                    vertical = 8.dp
                )
            ) {
                items(count = schedule.apps.size) {
                    val app = schedule.apps[it]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = if (LocalInspectionMode.current)
                                painterResource(R.drawable.ic_check_circle)
                            else
                                rememberAsyncImagePainter(
                                    AppIcon(app.appId),
                                    imageLoader = imageLoader
                                ),
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W600,
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleNameBox(
    schedule: Schedule,
    onToggleActiveStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0XFF6BD1CD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (schedule.isActive)
                            ActiveGreen
                        else
                            DisableColor
                    )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = schedule.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = schedule.isActive,
                onCheckedChange = {
                    onToggleActiveStatus.invoke()
                },
                thumbContent = {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .size(20.dp)
                            .background(Color.White)
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0XFF5BBD00),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                ),
                modifier = Modifier.scale(0.7f)
            )
        }
    }
}

//
//@Composable
//fun ScheduleDetailScreen(
//    schedule: Schedule,
//    vm: ScheduleDetailScreenVM,
//    onBackPress: () -> Unit,
//) {
//    val imageLoader = vm.imageLoader
//    val currentMonth = vm.currentMonth
//    val startMonth = vm.startMonth
//    val endMonth = vm.endMonth
//
//    val currentDate = vm.currentDate
//    val startDate = vm.startDate
//    val endDate = vm.endDate
//
//    val selectedDay = vm.selectedDay.collectAsState().value
//
//    val calenderViewType = vm.calenderViewType.collectAsState().value
//
//    val controlledApps = vm.controlledApps.collectAsState().value
//    val selectedApps = vm.selectedControlledApps.collectAsState().value
//    val selectedTimeSlot = vm.selectedTimeSlot.collectAsState().value
//
//    val currentSelectedDateData = vm.currentSelectedDateData.collectAsState().value
//    val scheduleDatesMap = vm.scheduleDatesMap.collectAsState().value
//    val totalTimeBlockedForSelectedDate = vm.totalBlockedDuration.collectAsState().value
//    val currentUpdatingTimeSlot = vm.currentUpdatingTimeSlot.collectAsState().value
//
//    ScheduleDetailScreenContent(
//        schedule = schedule,
//        currentSelectedDateData = currentSelectedDateData,
//        currentMonth = currentMonth,
//        startMonth = startMonth,
//        endMonth = endMonth,
//        currentDate = currentDate,
//        startDate = startDate,
//        endDate = endDate,
//        selectedDay = selectedDay,
//        calenderViewType = calenderViewType,
//        controlledApps = controlledApps,
//        selectedApps = selectedApps,
//        selectedTimeSlot = selectedTimeSlot,
//        totalTimeBlockedForSelectedDate = totalTimeBlockedForSelectedDate,
//        currentUpdatingTimeSlot = currentUpdatingTimeSlot,
//        onAppSelectToggleClick = vm::onAppSelectToggleClick,
//        onSelectTimeSlot = vm::onSelectTimeSlot,
//        onSelectedDayChanged = vm::onSelectedDayChanged,
//        onChangeCalenderViewType = vm::onChangeCalenderViewType,
//        onDismissBsd = vm::clearNewTimeSlotData,
//        onClickExistingTimeSlot = vm::selectCurrentUpdatingTimeSlot,
//        onCreateTimeSlot = vm::onCreateOrUpdateTimeSlot,
//        onDeleteTimeSlot = vm::onDeleteTimeSlot,
//        onBackPress = onBackPress,
//        shouldShowIndicatorOnDay = {
//            !scheduleDatesMap[it]?.timeSlots.isNullOrEmpty()
//        },
//        painter = {
//            rememberAsyncImagePainter(model = AppIcon(it.packageName), imageLoader)
//        },
//        painterForBlockedApp = {
//            rememberAsyncImagePainter(model = AppIcon(it.packageName), imageLoader)
//        },
//        modifier = Modifier
//            .fillMaxSize()
//            .imePadding()
//    )
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ScheduleDetailScreenContent(
//    schedule: Schedule,
//    currentSelectedDateData: ScheduleWithDates.DateItem?,
//    currentMonth: YearMonth,
//    startMonth: YearMonth,
//    endMonth: YearMonth,
//    currentDate: LocalDate,
//    startDate: LocalDate,
//    endDate: LocalDate,
//    selectedDay: LocalDate,
//    calenderViewType: CalenderViewType,
//    controlledApps: List<AppUsageInfo>,
//    selectedApps: Set<AppUsageInfo>,
//    selectedTimeSlot: Pair<LocalTime, LocalTime>,
//    totalTimeBlockedForSelectedDate: Duration,
//    currentUpdatingTimeSlot: Schedule.DateInput.TimeSlotsInput?,
//    onAppSelectToggleClick: (AppUsageInfo) -> Unit,
//    onSelectTimeSlot: (LocalTime, LocalTime) -> Unit,
//    onClickExistingTimeSlot: (ScheduleWithDates.DateItem.TimeSlotItem) -> Unit,
//    onSelectedDayChanged: (LocalDate) -> Unit,
//    onChangeCalenderViewType: (CalenderViewType) -> Unit,
//    onCreateTimeSlot: () -> Unit,
//    onDeleteTimeSlot: () -> Unit,
//    onDismissBsd: () -> Unit,
//    onBackPress: () -> Unit,
//    shouldShowIndicatorOnDay: (LocalDate) -> Boolean,
//    painter: @Composable (AppUsageInfo) -> Painter,
//    painterForBlockedApp: @Composable (Schedule.DateInput.TimeSlotsInput.BlockedAppsInput) -> Painter,
//    modifier: Modifier,
//) {
//    val horizontalPadding = remember { 16.dp }
//    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
//    val monthState = rememberCalendarState(
//        startMonth = startMonth,
//        endMonth = endMonth,
//        firstVisibleMonth = currentMonth,
//        firstDayOfWeek = firstDayOfWeek
//    )
//    val weekState = rememberWeekCalendarState(
//        startDate = startDate,
//        endDate = endDate,
//        firstDayOfWeek = firstDayOfWeek
//    )
//    var currentMonthTitle by remember { mutableStateOf(currentMonth.month) }
//    val sheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = true
//    )
//    LaunchedEffect(selectedDay) {
//        weekState.scrollToWeek(selectedDay)
//        monthState.animateScrollToMonth(YearMonth.from(selectedDay))
//    }
//    var showCreateOrUpdateTimeSlotSheet by rememberSaveable { mutableStateOf(false) }
//    var showActiveTimeSlotUpdateError by remember { mutableStateOf(false) }
//    var showPastTimeSlotUpdateError by remember { mutableStateOf(false) }
//
//    currentMonthTitle = if (calenderViewType == CalenderViewType.WEEKLY) {
//        weekState.firstVisibleWeek.days[0].date.month
//    } else {
//        monthState.lastVisibleMonth.yearMonth.month
//    }
//
//    Box(modifier = modifier) {
//        Column {
//            HeaderSection(
//                schedule,
//                onBackPress = onBackPress,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = horizontalPadding)
//            )
//
//            Spacer(Modifier.height(14.dp))
//
//            CalenderSection(
//                currentDate = currentDate,
//                monthState = monthState,
//                weekState = weekState,
//                currentMonthTitle = currentMonthTitle,
//                calenderViewType = calenderViewType,
//                onChangeCalenderViewType = onChangeCalenderViewType,
//                selectedDay = selectedDay,
//                onSelectedDayChanged = onSelectedDayChanged,
//                shouldShowIndicatorOnDay = shouldShowIndicatorOnDay,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = horizontalPadding)
//            )
//
//            Spacer(Modifier.height(24.dp))
//
//            SelectedDateDetailCard(
//                currentSelectedDateData = currentSelectedDateData,
//                totalTimeBlockedForSelectedDate = totalTimeBlockedForSelectedDate,
//                currentDate = currentDate,
//                selectedDate = selectedDay,
//                painter = painterForBlockedApp,
//                onAddClick = {
//                    showCreateOrUpdateTimeSlotSheet = true
//                },
//                onTimeSlotClick = {
//                    val now = LocalTime.now()
//                    val start = it.timeSlot.start
//                    val end = it.timeSlot.end
//
//                    val isInSlot = if (end.isAfter(start)) {
//                        // Normal case: same day
//                        !now.isBefore(start) && !now.isAfter(end)
//                    } else {
//                        // Crosses midnight
//                        !now.isBefore(start) || !now.isAfter(end)
//                    }
//                    when {
//                        selectedDay < currentDate -> {
//                            showPastTimeSlotUpdateError = true
//                        }
//
//                        schedule.isActive && selectedDay == currentDate && isInSlot -> {
//                            "Can not update current time slot".logE()
//                            showActiveTimeSlotUpdateError = true
//                        }
//
//                        else -> {
//                            onClickExistingTimeSlot.invoke(it)
//                            showCreateOrUpdateTimeSlotSheet = true
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = horizontalPadding)
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            PrevNextDayButtonSection(
//                onNextClick = {
//                    onSelectedDayChanged.invoke(selectedDay.plusDays(1))
//                },
//                onPrevClick = {
//                    onSelectedDayChanged.invoke(selectedDay.minusDays(1))
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = horizontalPadding + 4.dp)
//            )
//        }
//
//        if (showCreateOrUpdateTimeSlotSheet) {
//            AddScheduleTimeRangeBottomSheet(
//                currentUpdatingTimeSlot = currentUpdatingTimeSlot,
//                selectedDate = selectedDay,
//                controlledApps = controlledApps,
//                selectedApps = selectedApps,
//                onAppSelectToggleClick = onAppSelectToggleClick,
//                sheetState = sheetState,
//                startTime = selectedTimeSlot.first,
//                endTime = selectedTimeSlot.second,
//                onSelectTimeSlot = onSelectTimeSlot,
//                onDismiss = {
//                    showCreateOrUpdateTimeSlotSheet = false
//                    onDismissBsd.invoke()
//                },
//                onAddOrUpdateClick = {
//                    onCreateTimeSlot.invoke()
//                    showCreateOrUpdateTimeSlotSheet = false
//                },
//                onDelete = {
//                    showCreateOrUpdateTimeSlotSheet = false
//                    onDeleteTimeSlot.invoke()
//                },
//                painter = painter
//            )
//        }
//
//        if (showActiveTimeSlotUpdateError) {
//            AlertDialogWithOneAction(
//                title = stringResource(R.string.unable_to_update_time_slot),
//                description = stringResource(R.string.you_can_t_update_this_time_slot_because_the_schedule_is_currently_active),
//                onDismiss = {
//                    showActiveTimeSlotUpdateError = false
//                }
//            )
//        }
//
//        if (showPastTimeSlotUpdateError) {
//            AlertDialogWithOneAction(
//                title = stringResource(R.string.action_not_allowed),
//                description = stringResource(R.string.past_time_slots_cannot_be_updated_or_deleted),
//            ) {
//                showPastTimeSlotUpdateError = false
//            }
//        }
//    }
//}
//
@Composable
private fun HeaderSection(
    onBackPress: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(45.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_back_colored),
            contentDescription = "Back",
            modifier = Modifier
                .size(24.dp)
                .noRippleClickable(onBackPress)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.schedule_detail),
            style = MaterialTheme.typography.titleMedium.copy(
                brush = TextPrimaryGradient,
                fontSize = 22.sp,
                fontWeight = FontWeight.W700
            ),
            modifier = Modifier.weight(1f),
        )

        Image(
            painterResource(R.drawable.ic_edit),
            contentDescription = "Edit",
            modifier = Modifier
                .size(24.dp)
                .noRippleClickable(onEditClick)
        )
    }
}
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@Composable
//fun CalenderSection(
//    currentDate: LocalDate,
//    monthState: CalendarState,
//    weekState: WeekCalendarState,
//    currentMonthTitle: Month,
//    calenderViewType: CalenderViewType,
//    onChangeCalenderViewType: (CalenderViewType) -> Unit,
//    selectedDay: LocalDate,
//    onSelectedDayChanged: (LocalDate) -> Unit,
//    shouldShowIndicatorOnDay: (LocalDate) -> Boolean,
//    modifier: Modifier,
//) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        modifier = modifier
//            .wrapContentHeight()
//    ) {
//        Spacer(Modifier.height(12.dp))
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 12.dp),
//        ) {
//            Text(
//                text = currentMonthTitle.getDisplayName(
//                    TextStyle.FULL,
//                    Locale.getDefault()
//                ),
//                style = MaterialTheme.typography.labelLarge.copy(
//                    fontSize = 22.sp,
//                    color = TextSecondary
//                ),
//            )
//            Spacer(Modifier.weight(1f))
//
//            if (selectedDay != currentDate) {
//                Image(
//                    painter = painterResource(R.drawable.ic_refresh),
//                    contentDescription = "Jump to current date",
//                    modifier = Modifier
//                        .size(24.dp)
//                        .noRippleClickable {
//                            onSelectedDayChanged.invoke(currentDate)
//                        }
//                )
//                Spacer(Modifier.width(8.dp))
//            }
//            Image(
//                painter = painterResource(
//                    if (calenderViewType == CalenderViewType.MONTHLY)
//                        R.drawable.ic_calender_month
//                    else
//                        R.drawable.ic_calender_week
//                ),
//                contentDescription = "Calender View Type",
//                modifier = Modifier
//                    .size(24.dp)
//                    .noRippleClickable {
//                        if (calenderViewType == CalenderViewType.MONTHLY) {
//                            onChangeCalenderViewType.invoke(CalenderViewType.WEEKLY)
//                        } else {
//                            onChangeCalenderViewType.invoke(CalenderViewType.MONTHLY)
//                        }
//                    }
//            )
//        }
//
//        Spacer(Modifier.height(8.dp))
//
//        AnimatedVisibility(calenderViewType == CalenderViewType.MONTHLY) {
//            BoxWithConstraints(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                HorizontalCalendar(
//                    modifier = Modifier.padding(horizontal = 10.dp),
//                    state = monthState,
//                    dayContent = { day ->
//                        val cellWidth = this@BoxWithConstraints.maxWidth / 7
//                        MonthDayComponent(
//                            day = day,
//                            cellWidth = cellWidth,
//                            selectedDay = selectedDay,
//                            currentDate = currentDate,
//                            shouldShowIndicator = shouldShowIndicatorOnDay(day.date),
//                            onSelectedDayChanged = onSelectedDayChanged,
//                        )
//                    },
//                    monthHeader = { month ->
//                        val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
//                        DaysOfWeekTitle(daysOfWeek)
//                    }
//                )
//            }
//        }
//
//        AnimatedVisibility(calenderViewType == CalenderViewType.WEEKLY) {
//            BoxWithConstraints(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                val cellWidth = this.maxWidth / 7
//                WeekCalendar(
//                    modifier = Modifier.padding(horizontal = 10.dp),
//                    state = weekState,
//                    dayContent = { day ->
//                        WeekDayComponent(
//                            day,
//                            cellWidth,
//                            selectedDay,
//                            currentDate,
//                            shouldShowIndicator = shouldShowIndicatorOnDay(day.date),
//                            onSelectedDayChanged,
//                        )
//                    },
//                )
//            }
//        }
//
//        Spacer(Modifier.height(12.dp))
//    }
//}
//
//@Composable
//fun PrevNextDayButtonSection(
//    onNextClick: () -> Unit,
//    onPrevClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Row(
//            modifier = Modifier.noRippleClickable(onPrevClick),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Image(
//                painter = painterResource(R.drawable.ic_prev_circle),
//                contentDescription = "Prev",
//                modifier = Modifier.size(22.dp)
//            )
//            Spacer(Modifier.width(4.dp))
//            Text(
//                text = stringResource(R.string.prev),
//                style = MaterialTheme.typography.labelSmall.copy(
//                    color = TextSecondary,
//                    fontSize = 14.sp
//                )
//            )
//        }
//        Row(
//            modifier = Modifier.noRippleClickable(onNextClick),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = stringResource(R.string.next),
//                style = MaterialTheme.typography.labelSmall.copy(
//                    color = TextSecondary,
//                    fontSize = 14.sp
//                )
//            )
//            Spacer(Modifier.width(4.dp))
//            Image(
//                painter = painterResource(R.drawable.ic_next_circle),
//                contentDescription = "Next",
//                modifier = Modifier.size(22.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun SelectedDateDetailCard(
//    currentSelectedDateData: ScheduleWithDates.DateItem?,
//    totalTimeBlockedForSelectedDate: Duration,
//    currentDate: LocalDate,
//    selectedDate: LocalDate,
//    painter: @Composable (Schedule.DateInput.TimeSlotsInput.BlockedAppsInput) -> Painter,
//    onAddClick: () -> Unit,
//    onTimeSlotClick: (ScheduleWithDates.DateItem.TimeSlotItem) -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    val containerSize = LocalWindowInfo.current.containerSize
//    val density = LocalDensity.current.density
//
//    val screenHeight = containerSize.height.dp / density
//    val containerHeight = (screenHeight.value * 0.50).dp
//    Box(
//        modifier = modifier
//            .height(containerHeight)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 2.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(AquaBlueLight.copy(alpha = .4f))
//        )
//
//        Card(
//            modifier = Modifier
//                .padding(top = 8.dp)
//                .fillMaxSize(),
//            colors = CardDefaults.cardColors(WhiteColor)
//        ) {
//            Column(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                Spacer(Modifier.height(16.dp))
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp)
//                ) {
//                    Column(
//                        modifier = Modifier.align(Alignment.Center),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text(
//                            text = selectedDate.toDayDateMonth,
//                            style = MaterialTheme.typography.titleMedium.copy(
//                                color = AquaBlue,
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.W600,
//                            )
//                        )
//                        Spacer(Modifier.height(2.dp))
//                        Text(
//                            text = totalTimeBlockedForSelectedDate.readable,
//                            style = MaterialTheme.typography.titleMedium.copy(
//                                color = TextSecondary,
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.W400,
//                            )
//                        )
//                    }
//
//                    if (selectedDate >= currentDate) {
//                        Image(
//                            painter = painterResource(R.drawable.ic_add_gradient),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .size(20.dp)
//                                .align(Alignment.TopEnd)
//                                .padding(top = 2.dp)
//                                .noRippleClickable(onAddClick)
//                        )
//                    }
//                }
//
//                Spacer(Modifier.height(16.dp))
//
//                if (currentSelectedDateData != null && currentSelectedDateData.timeSlots.isNotEmpty()) {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 22.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        items(count = currentSelectedDateData.timeSlots.size) {
//                            TimeSlotsWithApps(
//                                timeSlots = currentSelectedDateData.timeSlots[it],
//                                onClick = {
//                                    onTimeSlotClick.invoke(currentSelectedDateData.timeSlots[it])
//                                },
//                                painter = painter,
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                        }
//                    }
//                } else {
//                    Text(
//                        "No Data",
//                        modifier = Modifier.fillMaxSize(),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TimeSlotsWithApps(
//    timeSlots: ScheduleWithDates.DateItem.TimeSlotItem,
//    onClick: () -> Unit,
//    painter: @Composable (Schedule.DateInput.TimeSlotsInput.BlockedAppsInput) -> Painter,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier,
//        elevation = CardDefaults.elevatedCardElevation(2.dp),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(WhiteColor),
//        onClick = onClick
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(15.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            FlowRow(
//                modifier = Modifier.weight(1f),
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                timeSlots.blockedApps.forEach {
//                    Image(
//                        painter = painter(it),
//                        contentDescription = it.name,
//                        modifier = Modifier
//                            .size(24.dp)
//                            .clip(CircleShape)
//                    )
//                }
//            }
//            Spacer(Modifier.width(8.dp))
//            Text(
//                text = "${timeSlots.timeSlot.start.toHourMinute} - ${timeSlots.timeSlot.end.toHourMinute}",
//                style = MaterialTheme.typography.labelSmall.copy(
//                    color = TextSecondary,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.W400,
//                )
//            )
//        }
//    }
//}
//
//
//@MultiDevicePreview
//@Composable
//private fun ScheduleDetailScreenContentPreview() {
//    val currentMonth = YearMonth.now()
//    val startMonth = currentMonth.minusMonths(50)
//    val endMonth = currentMonth.plusMonths(50)
//
//    val currentDate = LocalDate.now()
//    val startDate = currentDate
//    val endDate = currentDate.plusDays(100)
//
//    val selectedDay = remember { mutableStateOf<LocalDate>(currentDate.plusDays(1)) }
//
//    val calenderViewType = remember { mutableStateOf(CalenderViewType.WEEKLY) }
//
//    AppLockTheme {
//        ScreenSurface {
//            ScheduleDetailScreenContent(
//                schedule = Schedule(0, "Work Mode", true),
//                currentSelectedDateData = null,
//                currentMonth = currentMonth,
//                startMonth = startMonth,
//                endMonth = endMonth,
//                currentDate = currentDate,
//                startDate = startDate,
//                endDate = endDate,
//                selectedDay = selectedDay.value,
//                calenderViewType = calenderViewType.value,
//                controlledApps = emptyList(),
//                selectedApps = emptySet(),
//                selectedTimeSlot = LocalTime.now() to LocalTime.now().plusHours(1),
//                totalTimeBlockedForSelectedDate = Duration.ZERO,
//                onAppSelectToggleClick = {},
//                onSelectTimeSlot = { _, _ -> },
//                onClickExistingTimeSlot = {},
//                onSelectedDayChanged = {
//                    selectedDay.value = it
//                },
//                onChangeCalenderViewType = {
//                    calenderViewType.value = it
//                },
//                onCreateTimeSlot = {},
//                onDismissBsd = {},
//                onBackPress = {},
//                shouldShowIndicatorOnDay = { false },
//                painter = {
//                    painterResource(R.drawable.ic_launcher_background)
//                },
//                painterForBlockedApp = { painterResource(R.drawable.ic_launcher_background) },
//                currentUpdatingTimeSlot = null,
//                onDeleteTimeSlot = {},
//                modifier = Modifier.fillMaxSize(),
//            )
//        }
//    }
//}