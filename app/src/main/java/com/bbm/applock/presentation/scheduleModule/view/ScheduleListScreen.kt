package com.bbm.applock.presentation.scheduleModule.view

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import coil3.ImageLoader
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.Schedule
import com.bbm.applock.R
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.UiState.Ideal.consumeOnce
import com.bbm.applock.presentation.scheduleModule.view.component.CreateOrUpdateScheduleBottomSheet
import com.bbm.applock.presentation.scheduleModule.vm.CreateOrUpdateScheduleVM
import com.bbm.applock.presentation.scheduleModule.vm.SchedulesScreenVM
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.ui.theme.TextButtonColor
import com.bbm.applock.ui.theme.TextPrimaryGradient
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.util.LifeCycleEvent
import com.bbm.applock.util.MultiDevicePreview
import com.bbm.applock.util.ScreenSurface
import com.bbm.applock.util.noRippleClickable
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    vm: SchedulesScreenVM,
    scheduleVM: CreateOrUpdateScheduleVM,
    onScheduleClick: (Schedule) -> Unit
) {
    val state = vm.state.collectAsState()
    val scheduleList = vm.schedulesList.collectAsState()
    val scheduleSettingsDialog = vm.isScheduleSettingsDialogVisible.collectAsState()

    val isCreateScheduleDialogVisible = scheduleVM.isCreateScheduleDialogVisible.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
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
    val onCreateScheduleClick = scheduleVM::createSchedule
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            // Prevent dismissal by user interaction (swipe down, tap outside)
            newState != SheetValue.Hidden
        }
    )
    LifeCycleEvent {
        if (it == Lifecycle.Event.ON_RESUME) {
            scheduleVM.clear()
        }
    }
    LaunchedEffect(Unit) {
        launch {
            vm.errorAlertMsg.collect { message ->
                snackbarHostState.showSnackbar(message = context.getString(message))
            }
        }
        launch {
            scheduleVM.state.collect {
                when (it) {
                    UiState.Ideal -> {}
                    UiState.Loading -> {}
                    is UiState.Success<*> -> {}
                    is UiState.ValidationError -> {
                        launch {
                            it.consumeOnce()?.message?.let {
                                snackbarHostState.showSnackbar(message = context.getString(it))
                            }
                        }
                    }

                    is UiState.Failure<*> -> {
                        it.consumeOnce()?.message?.let { message ->
                            snackbarHostState.showSnackbar(message = message)
                        }
                    }
                }
            }
        }
    }

    when (state.value) {
        is UiState.Failure<*> -> {

        }

        UiState.Ideal -> {

        }

        UiState.Loading -> {

        }

        is UiState.Success<*> -> {

        }

        is UiState.ValidationError -> {

        }
    }
    ScheduleScreenContent(
        onAddNewSchedule = {
            scheduleVM.toggleCreateScheduleDialog()
        },
        onNewScheduleDismiss = {
            scheduleVM.toggleCreateScheduleDialog()
        },
        onToggleSchedule = {
            vm.toggleSchedule(it)
        },
        onScheduleSettingsClick = {
            vm.toggleScheduleSettingsDialog(it)
        },
        onScheduleDeleteClick = {
            vm.deleteSchedule(it)
        },
        onScheduleAnalyticsClick = {
            vm.toggleScheduleSettingsDialog(null)
        },
        onScheduleRowClick = {
            onScheduleClick.invoke(it)
        },
        onScheduleSettingsDismissClick = {
            vm.toggleScheduleSettingsDialog(null)
        },
        isScheduleSettingsDialogVisible = scheduleSettingsDialog.value.first,
        selectedScheduleForSetting = scheduleSettingsDialog.value.second,
        schedules = scheduleList.value,
        isCreateScheduleDialogVisible = isCreateScheduleDialogVisible.value,
        imageLoader = imageLoader,
        sheetState = sheetState,

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
        onCreateScheduleClick = onCreateScheduleClick,

        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    )

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenContent(
    onAddNewSchedule: () -> Unit,
    onNewScheduleDismiss: () -> Unit,
    onCreateScheduleClick: () -> Unit,
    onToggleSchedule: (schedule: Schedule) -> Unit,
    onScheduleSettingsClick: (schedule: Schedule) -> Unit,
    onScheduleSettingsDismissClick: () -> Unit,
    onScheduleDeleteClick: (Schedule) -> Unit,
    onScheduleAnalyticsClick: (Schedule) -> Unit,
    onScheduleRowClick: (schedule: Schedule) -> Unit,
    isCreateScheduleDialogVisible: Boolean,
    isScheduleSettingsDialogVisible: Boolean,
    selectedScheduleForSetting: Schedule?,
    newScheduleName: String,
    onNewScheduleNameChange: (String) -> Unit,
    schedules: List<Schedule>,
    imageLoader: ImageLoader,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    controlledApps: List<AppUsageInfo>,
    onControlledAppSelect: (AppUsageInfo) -> Unit,
    selectedApps: Set<AppUsageInfo>,
    selectedRepeatDays: Set<DayOfWeek>,
    onRepeatDaysChangeClick: (DayOfWeek) -> Unit,
    onSelectTimeSlot: (LocalTime, LocalTime) -> Unit,
    startTime: LocalTime,
    endTime: LocalTime,
) {
    if (isCreateScheduleDialogVisible) {
        CreateOrUpdateScheduleBottomSheet(
            schedule = null,
            scheduleName = newScheduleName,
            onScheduleNameChange = onNewScheduleNameChange,
            selectedDays = selectedRepeatDays,
            onDaySelected = onRepeatDaysChangeClick,
            controlledApps = controlledApps,
            selectedApps = selectedApps,
            onAppSelectToggleClick = onControlledAppSelect,
            startTime = startTime,
            endTime = endTime,
            onDismiss = onNewScheduleDismiss,
            onAddOrUpdateClick = {
                // will be create only
                onCreateScheduleClick.invoke()
            },
            onDeleteOrDismiss = {
                // no delete from here
                onNewScheduleDismiss.invoke()
            },
            onSelectTimeSlot = onSelectTimeSlot,
            imageLoader = imageLoader,
            sheetState = sheetState,
        )
    }
    if (isScheduleSettingsDialogVisible && selectedScheduleForSetting != null) {
        ScheduleSettingsDialog(
            schedule = selectedScheduleForSetting,
            onDeleteClick = {
                onScheduleDeleteClick.invoke(selectedScheduleForSetting)
            },
            onCheckAnalyticsClick = {
                onScheduleAnalyticsClick.invoke(selectedScheduleForSetting)
            },
            onDismiss = onScheduleSettingsDismissClick
        )
    }
    val horizontalPadding = 16.dp
    Column(
        modifier = modifier
    ) {
        HeaderSection(
            onAddSchedule = {
                onAddNewSchedule.invoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        )
        Spacer(Modifier.height(22.dp))
        if (schedules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = 12.dp
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.no_schedules_yet),
                    style = MaterialTheme.typography.titleLarge.copy(
                        brush = TextPrimaryGradient,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W600
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.create_your_first_schedule_to_start_managing_your_app_usage_throughout_the_day),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 18.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.W400
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(schedules.size) { index ->
                    ScheduleItemRow(
                        schedule = schedules[index],
                        onToggleSchedule = {
                            onToggleSchedule.invoke(schedules[index])
                        },
                        onSettingsClick = {
                            onScheduleSettingsClick.invoke(schedules[index])
                        },
                        onScheduleClick = {
                            onScheduleRowClick.invoke(schedules[index])
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    onAddSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(42.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Image(
                painter = painterResource(R.drawable.ic_calender),
                contentDescription = "Calender Icon",
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.my_schedules),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_add_gradient),
            contentDescription = "Create Schedule",
            modifier = Modifier
                .size(38.dp)
                .noRippleClickable(onAddSchedule)
        )
    }
}

@Preview
@Composable
private fun HeaderSectionPreview() {
    AppLockTheme {
        HeaderSection(
            onAddSchedule = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun ScheduleItemRow(
    schedule: Schedule,
    onSettingsClick: () -> Unit,
    onToggleSchedule: () -> Unit,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .noRippleClickable(onScheduleClick)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0XFF6BD1CD))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                Text(
                    text = schedule.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W500
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = schedule.isActive,
                    onCheckedChange = {
                        onToggleSchedule.invoke()
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
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .padding(2.dp)
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_analog_clock),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${schedule.startTimeFormat} - ${schedule.endTimeFormat}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .padding(2.dp)
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_calender_month),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    schedule.onWhichDay,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .padding(2.dp)
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_apps),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                val scheduledText =
                    if (schedule.apps.size > 1)
                        "${schedule.apps.size} Apps Scheduled"
                    else
                        "${schedule.apps.size} App Scheduled"
                Text(
                    text = scheduledText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun ScheduleItemRowPreview() {
    AppLockTheme {
        Column {
            ScheduleItemRow(
                schedule = Schedule(
                    name = "Lunch Time", isActive = false,
                    id = 1,
                    startTime = LocalTime.now(),
                    endTime = LocalTime.now().plusHours(5),
                    repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                    apps = emptyList()
                ),
                onToggleSchedule = {},
                onSettingsClick = {},
                onScheduleClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            ScheduleItemRow(
                schedule = Schedule(
                    name = "Lunch Time", isActive = false,
                    id = 1,
                    startTime = LocalTime.now(),
                    endTime = LocalTime.now().plusHours(5),
                    repeatDays = DayOfWeek.entries.toList(),
                    apps = emptyList()
                ),
                onToggleSchedule = {},
                onSettingsClick = {},
                onScheduleClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CreateScheduleDialog(
    name: String,
    onTextChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        }
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0XFFF1FCFF))
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.new_schedule),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W600
                )
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(Color.White),
                border = BorderStroke(1.5.dp, Color(0XFF6BD1CD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = {
                            if (it.length <= 20)
                                onTextChange.invoke(it)
                        },
                        textStyle = MaterialTheme.typography
                            .labelMedium
                            .copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W200
                            ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 1.dp)
                            .weight(1f),
                    ) { innerTextField ->
                        if (name.isEmpty())
                            Text(
                                stringResource(R.string.enter_schedule_name),
                                style = MaterialTheme.typography
                                    .labelMedium
                                    .copy(fontSize = 14.sp, fontWeight = FontWeight.W400),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 1.dp)
                                    .weight(1f)
                            )
                        innerTextField()
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                TextButton(
                    onClick = {
                        onDismiss.invoke()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(R.string.btn_cancel),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = TextButtonColor
                        )
                    )
                }
                TextButton(
                    onClick = {
                        onCreateClick.invoke()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.length >= 3
                ) {
                    Text(
                        stringResource(R.string.btn_create_schedule),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = TextButtonColor
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CreateScheduleDialogPreview() {
    AppLockTheme {
        CreateScheduleDialog(
            name = "",
            onCreateClick = {

            },
            onDismiss = {

            },
            onTextChange = {

            },

            )
    }
}

@Composable
fun ScheduleSettingsDialog(
    schedule: Schedule,
    onDeleteClick: () -> Unit,
    onCheckAnalyticsClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0XFFF1FCFF))
                .padding(8.dp)
        ) {
            Text(
                text = schedule.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W600
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.btn_delete_schedule),
                        style = MaterialTheme
                            .typography
                            .labelMedium
                            .copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W600,
                                color = TextButtonColor
                            )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onCheckAnalyticsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_schedule_analytics),
                        contentDescription = "Analytics",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.btn_check_analytics),
                        style = MaterialTheme
                            .typography
                            .labelMedium
                            .copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W600,
                                color = TextButtonColor
                            )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ScheduleSettingsDialogPreview() {
    AppLockTheme {
        ScheduleSettingsDialog(
            schedule = Schedule(
                name = "Lunch Time",
                isActive = false,
                id = 1,
                startTime = LocalTime.now(),
                endTime = LocalTime.now().plusHours(5),
                repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                apps = emptyList()
            ),
            onDeleteClick = {},
            onCheckAnalyticsClick = {},
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@MultiDevicePreview
@Composable
fun ScheduleScreenContentPreview() {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    AppLockTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            ScreenSurface(painter = painterResource(R.drawable.bg_schedule_screen)) {
                ScheduleScreenContent(
                    onAddNewSchedule = {},
                    onNewScheduleDismiss = {},
                    onCreateScheduleClick = {},
                    onToggleSchedule = {},
                    onScheduleSettingsClick = {},
                    onScheduleSettingsDismissClick = {},
                    onScheduleDeleteClick = {},
                    onScheduleAnalyticsClick = {},
                    onScheduleRowClick = {},
                    isCreateScheduleDialogVisible = false,
                    isScheduleSettingsDialogVisible = false,
                    selectedScheduleForSetting = null,
                    newScheduleName = "",
                    onNewScheduleNameChange = {},
                    schedules = listOf(
                        Schedule(
                            id = 1,
                            name = "Lunch Time",
                            isActive = false,
                            startTime = LocalTime.now(),
                            endTime = LocalTime.now().plusHours(5),
                            repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                            apps = listOf(
                                Schedule.App(
                                    id = 1,
                                    appId = "com.bbm.applock",
                                    appName = "App Lock"
                                )
                            )
                        ),
                        Schedule(
                            id = 1,
                            name = "Work Time",
                            isActive = false,
                            startTime = LocalTime.now(),
                            endTime = LocalTime.now().plusHours(5),
                            repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                            apps = listOf(
                                Schedule.App(
                                    id = 1,
                                    appId = "com.bbm.applock",
                                    appName = "App Lock"
                                ),
                                Schedule.App(
                                    id = 1,
                                    appId = "com.bbm.applock",
                                    appName = "App Lock"
                                ),
                            )
                        ),
                    ),
                    imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                    sheetState = sheetState,
                    controlledApps = emptyList(),
                    onControlledAppSelect = { },
                    selectedApps = emptySet(),
                    selectedRepeatDays = emptySet(),
                    onRepeatDaysChangeClick = {},
                    onSelectTimeSlot = { _, _ -> },
                    startTime = LocalTime.now(),
                    endTime = LocalTime.now().plusHours(1),
                    modifier = Modifier,
                )
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@MultiDevicePreview
//@Composable
//fun ScheduleScreenContentPreview2() {
//    val sheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = true
//    )
//    LaunchedEffect(Unit) {
//        sheetState.show()
//    }
//    AppLockTheme {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .imePadding()
//        ) {
//            ScreenSurface(painter = painterResource(R.drawable.bg_schedule_screen)) {
//                ScheduleScreenContent(
//                    onAddNewSchedule = {},
//                    onNewScheduleDismiss = {},
//                    onCreateScheduleClick = {},
//                    onToggleSchedule = {},
//                    onScheduleSettingsClick = {},
//                    onScheduleSettingsDismissClick = {},
//                    onScheduleDeleteClick = {},
//                    onScheduleAnalyticsClick = {},
//                    onScheduleClick = {},
//                    isCreateScheduleDialogVisible = true,
//                    isScheduleSettingsDialogVisible = false,
//                    selectedScheduleForSetting = null,
//                    newScheduleName = "",
//                    onNewScheduleNameChange = {},
//                    schedules = listOf(
//                        Schedule(
//                            id = 1,
//                            name = "Lunch Time",
//                            isActive = false,
//                            startTime = LocalTime.now(),
//                            endTime = LocalTime.now().plusHours(5),
//                            repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
//                            apps = listOf(
//                                Schedule.App(
//                                    id = 1,
//                                    appId = "com.bbm.applock",
//                                    appName = "App Lock"
//                                )
//                            )
//                        ),
//                        Schedule(
//                            id = 1,
//                            name = "Work Time",
//                            isActive = false,
//                            startTime = LocalTime.now(),
//                            endTime = LocalTime.now().plusHours(5),
//                            repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
//                            apps = listOf(
//                                Schedule.App(
//                                    id = 1,
//                                    appId = "com.bbm.applock",
//                                    appName = "App Lock"
//                                ),
//                                Schedule.App(
//                                    id = 1,
//                                    appId = "com.bbm.applock",
//                                    appName = "App Lock"
//                                ),
//                            )
//                        ),
//                    ),
//                    imageLoader = ImageLoader.Builder(LocalContext.current).build(),
//                    sheetState = sheetState
//                )
//            }
//        }
//    }
//}