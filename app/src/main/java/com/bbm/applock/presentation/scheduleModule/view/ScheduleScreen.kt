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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applock.domain.model.Schedule
import com.bbm.applock.R
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.scheduleModule.vm.SchedulesScreenVM
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.util.MultiDevicePreview
import com.bbm.applock.util.ScreenSurface
import com.bbm.applock.util.noRippleClickable


@Composable
fun ScheduleScreen(
    vm: SchedulesScreenVM,
    onScheduleClick: (Schedule) -> Unit
) {
    val state = vm.state.collectAsState()
    val scheduleList = vm.schedulesList.collectAsState()
    val scheduleSettingsDialog = vm.isScheduleSettingsDialogVisible.collectAsState()
    val newScheduleName = vm.scheduleName.collectAsState()
    val isCreateScheduleDialogVisible = vm.isCreateScheduleDialogVisible.collectAsState()

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
            vm.toggleCreateScheduleDialog()
        },
        onNewScheduleDismiss = {
            vm.toggleCreateScheduleDialog()
        },
        onCreateScheduleClick = {
            vm.createSchedule()
        },
        onToggleSchedule = {
            vm.toggleSchedule(it)
        },
        onScheduleSettingsClick = {
            vm.toggleScheduleSettingsDialog(it)
        },
        onScheduleDeleteClick = {
            vm.toggleScheduleSettingsDialog(null)
            vm.deleteSchedule(it)
        },
        onScheduleAnalyticsClick = {
            vm.toggleScheduleSettingsDialog(null)
        },
        onScheduleClick = {
            onScheduleClick.invoke(it)
        },
        onScheduleSettingsDismissClick = {
            vm.toggleScheduleSettingsDialog(null)
        },
        onNewScheduleNameChange = {
            vm.onNewScheduleNameChange(it)
        },
        isScheduleSettingsDialogVisible = scheduleSettingsDialog.value.first,
        selectedScheduleForSetting = scheduleSettingsDialog.value.second,
        newScheduleName = newScheduleName.value,
        schedules = scheduleList.value,
        isCreateScheduleDialogVisible = isCreateScheduleDialogVisible.value
    )
}

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
    onScheduleClick: (schedule: Schedule) -> Unit,
    isCreateScheduleDialogVisible: Boolean,
    isScheduleSettingsDialogVisible: Boolean,
    selectedScheduleForSetting: Schedule?,
    newScheduleName: String,
    onNewScheduleNameChange: (String) -> Unit,
    schedules: List<Schedule>,
    modifier: Modifier = Modifier,
) {
    if (isCreateScheduleDialogVisible) {
        CreateScheduleDialog(
            name = newScheduleName,
            onCreateClick = onCreateScheduleClick,
            onDismiss = onNewScheduleDismiss,
            onTextChange = onNewScheduleNameChange
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
    val horizontalPadding = 14.dp
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        HeaderSection(
            onAddSchedule = {
                onAddNewSchedule.invoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        )
        Spacer(Modifier.height(22.dp))
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
                        onScheduleClick.invoke(schedules[index])
                    },
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
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
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0XFF0C9BBB),
                            Color(0XFF6BD1CD)
                        ),
                        startY = 0.0f,
                        endY = 100.0f
                    ),
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
            .height(48.dp)
            .noRippleClickable(onScheduleClick)
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
            Text(
                text = schedule.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row {
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
                Spacer(Modifier.width(6.dp))
                Image(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Schedule Settings",
                    modifier = Modifier
                        .size(26.dp)
                        .noRippleClickable(onSettingsClick)
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
                schedule = Schedule(name = "Lunch Time", isActive = false),
                onToggleSchedule = {},
                onSettingsClick = {},
                onScheduleClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            ScheduleItemRow(
                schedule = Schedule(name = "Lunch Time", isActive = true),
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
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0XFF0C9BBB),
                            Color(0XFF6BD1CD)
                        )
                    ),
                    fontSize = 18.sp,
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
                        onValueChange = onTextChange,
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
                            .weight(1f),
                    ) { innerTextField ->
                        if (name.isEmpty())
                            Text(
                                stringResource(R.string.enter_schedule_name),
                                style = MaterialTheme.typography
                                    .labelMedium
                                    .copy(fontSize = 14.sp, fontWeight = FontWeight.W200)
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
                            color = TextSecondary
                        )
                    )
                }
                TextButton(
                    onClick = {
                        onCreateClick.invoke()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.length in 3..20
                ) {
                    Text(
                        stringResource(R.string.btn_create_schedule),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = TextSecondary
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
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0XFF0C9BBB),
                            Color(0XFF6BD1CD)
                        )
                    ),
                    fontSize = 18.sp,
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
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_delete_schedule),
                        style = MaterialTheme
                            .typography
                            .labelMedium
                            .copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W600,
                                color = TextSecondary
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
                        painter = painterResource(R.drawable.ic_analytics),
                        contentDescription = "Analytics",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_check_analytics),
                        style = MaterialTheme
                            .typography
                            .labelMedium
                            .copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W600,
                                color = TextSecondary
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
            schedule = Schedule(name = "Lunch Time", isActive = true),
            onDeleteClick = {},
            onCheckAnalyticsClick = {},
            onDismiss = {}
        )
    }
}

@MultiDevicePreview
@Composable
fun ScheduleScreenContentPreview() {
    AppLockTheme {
        val isDialogVisible = remember { mutableStateOf(false) }
        ScreenSurface(painter = painterResource(R.drawable.bg_schedule_screen)) {
            ScheduleScreenContent(
                onAddNewSchedule = {
                    isDialogVisible.value = !isDialogVisible.value
                },
                isCreateScheduleDialogVisible = isDialogVisible.value,
                onToggleSchedule = {},
                onCreateScheduleClick = {},
                onScheduleSettingsClick = {},
                onScheduleClick = {},
                onScheduleSettingsDismissClick = {},
                onNewScheduleNameChange = {},
                newScheduleName = "",
                onNewScheduleDismiss = {
                    isDialogVisible.value = false
                },
                onScheduleDeleteClick = {},
                onScheduleAnalyticsClick = {},
                isScheduleSettingsDialogVisible = false,
                selectedScheduleForSetting = null,
                modifier = Modifier,
                schedules = listOf(
                    Schedule(
                        name = "Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time",
                        isActive = true
                    ),
                    Schedule(
                        name = "Work Time",
                        isActive = false
                    ),
                    Schedule(
                        name = "Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time",
                        isActive = true
                    ),
                    Schedule(
                        name = "Work Time",
                        isActive = false
                    ),
                    Schedule(
                        name = "Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time",
                        isActive = true
                    ),
                    Schedule(
                        name = "Work Time",
                        isActive = false
                    ),
                    Schedule(
                        name = "Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time",
                        isActive = true
                    ),
                    Schedule(
                        name = "Work Time",
                        isActive = false
                    ),
                    Schedule(
                        name = "Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time Lunch Time",
                        isActive = true
                    ),
                    Schedule(
                        name = "Work Time",
                        isActive = false
                    ),
                )
            )
        }
    }
}