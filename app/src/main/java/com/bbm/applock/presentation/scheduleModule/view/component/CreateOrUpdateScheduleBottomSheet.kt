package com.bbm.applock.presentation.scheduleModule.view.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.Schedule
import com.bbm.applock.R
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.Red
import com.bbm.applock.ui.theme.TextButtonColor
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextPrimaryGradient
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.ui.theme.WhiteColor
import com.bbm.applock.util.AppIcon
import com.bbm.applock.util.ScreenSurface
import com.bbm.applock.util.noRippleClickable
import com.bbm.applock.util.toHourMinute
import java.time.DayOfWeek
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrUpdateScheduleBottomSheet(
    schedule: Schedule?,
    scheduleName: String,
    onScheduleNameChange: (String) -> Unit,
    selectedDays: Set<DayOfWeek>,
    onDaySelected: (DayOfWeek) -> Unit,
    controlledApps: List<AppUsageInfo>,
    selectedApps: Set<AppUsageInfo>,
    onAppSelectToggleClick: (AppUsageInfo) -> Unit,
    startTime: LocalTime,
    endTime: LocalTime,
    onDismiss: () -> Unit,
    onAddOrUpdateClick: () -> Unit,
    onDeleteOrDismiss: () -> Unit,
    onSelectTimeSlot: (start: LocalTime, end: LocalTime) -> Unit,
    imageLoader: ImageLoader,
    sheetState: SheetState,
) {
    var isStartTimeSelectionDialogOpen by remember {
        mutableStateOf(false)
    }
    var isEndTimeSelectionDialogOpen by remember {
        mutableStateOf(false)
    }
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current.density
    val screenHeight = containerSize.height.dp / density
    val containerHeight = (screenHeight.value * 0.70).dp

    ModalBottomSheet(
        dragHandle = null,
        sheetState = sheetState,
        onDismissRequest = {
            // only dismiss when updating else will be from [Cancel] click
            if (schedule != null)
                onDismiss.invoke()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight)
                .background(WhiteColor)
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (schedule == null)
                    stringResource(R.string.create_your_schedule)
                else stringResource(R.string.update_your_schedule),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 26.dp),
            )
            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 26.dp),
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
                        value = scheduleName,
                        onValueChange = {
                            if (it.length <= 20)
                                onScheduleNameChange.invoke(it)
                        },
                        textStyle = MaterialTheme.typography
                            .labelMedium
                            .copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W400
                            ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 1.dp)
                            .weight(1f),
                    ) { innerTextField ->
                        if (scheduleName.isEmpty())
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
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .height(68.dp)
            ) {
                TimeSelectionCard(
                    heading = "Start",
                    time = startTime.toHourMinute,
                    onClick = {
                        isStartTimeSelectionDialogOpen = true
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TimeSelectionCard(
                    heading = "End",
                    time = endTime.toHourMinute,
                    onClick = {
                        isEndTimeSelectionDialogOpen = true
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.which_days),
                modifier = Modifier.padding(horizontal = 26.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(4.dp))
            WeekDaySelectionRow(
                selectedDays = selectedDays,
                onDaySelected = onDaySelected,
                isViewing = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.controlled_apps),
                modifier = Modifier.padding(horizontal = 26.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = TextPrimaryGradient,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    horizontal = 32.dp,
                    vertical = 8.dp
                )
            ) {
                items(count = controlledApps.size) {
                    val app = controlledApps[it]
                    val isSelected = selectedApps.contains(app)
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
                                    AppIcon(app.packageName),
                                    imageLoader = imageLoader
                                ),
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W600,
                            )
                        )
                        Spacer(Modifier.weight(1f))
                        Image(
                            painter = if (isSelected)
                                painterResource(R.drawable.ic_check_circle)
                            else
                                painterResource(R.drawable.ic_add_circle),
                            contentDescription = "Add Or Remove",
                            modifier = Modifier
                                .size(20.dp)
                                .noRippleClickable {
                                    onAppSelectToggleClick.invoke(app)
                                }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 26.dp),
                horizontalArrangement = Arrangement.End,
                reverseLayout = true
            ) {
                items(count = selectedApps.size) {
                    Image(
                        painter = if (LocalInspectionMode.current)
                            painterResource(R.drawable.ic_check_circle)
                        else
                            rememberAsyncImagePainter(
                                AppIcon(selectedApps.elementAt(it).packageName),
                                imageLoader = imageLoader
                            ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    onClick = onDeleteOrDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (schedule != null)
                            Red
                        else TextButtonColor
                    )
                ) {
                    Text(
                        if (schedule != null) stringResource(R.string.delete)
                        else stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = WhiteColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W600
                        )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    onClick = onAddOrUpdateClick,
                    enabled = (selectedApps.isNotEmpty()
                            && selectedDays.isNotEmpty()
                            && scheduleName.length > 2),
                    colors = ButtonDefaults.buttonColors(containerColor = AquaBlue)
                ) {
                    Text(
                        if (schedule == null) stringResource(R.string.add)
                        else stringResource(R.string.update),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = WhiteColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W600
                        )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    if (isStartTimeSelectionDialogOpen) {
        TimeRangePickerDialog(
            title = stringResource(R.string.select_start_time),
            time = startTime,
            onConfirm = {
                isStartTimeSelectionDialogOpen = false
                onSelectTimeSlot.invoke(it, endTime)
            },
            onDismiss = {
                isStartTimeSelectionDialogOpen = false
            }
        )
    }
    if (isEndTimeSelectionDialogOpen) {
        TimeRangePickerDialog(
            title = stringResource(R.string.select_end_time),
            time = endTime,
            onConfirm = {
                isEndTimeSelectionDialogOpen = false
                onSelectTimeSlot.invoke(startTime, it)
            },
            onDismiss = {
                isEndTimeSelectionDialogOpen = false
            }
        )
    }
}

@Composable
fun TimeSelectionCard(
    heading: String,
    time: String,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = WhiteColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteColor)
        ) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = heading,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AddScheduleTimeRangeBottomSheetPreview() {
    AppLockTheme {
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val dummyApps = listOf(
            AppUsageInfo(
                "App One", "com.example.app1",
                usageTimeInMillis = 10000,
                isControlledApp = true,
                totalScreenTime = null
            ),
            AppUsageInfo(
                "App One", "com.example.app1",
                usageTimeInMillis = 10000,
                isControlledApp = true,
                totalScreenTime = null
            ),
            AppUsageInfo(
                "App One", "com.example.app1",
                usageTimeInMillis = 10000,
                isControlledApp = true,
                totalScreenTime = null
            ),
        )
        LaunchedEffect(Unit) {
            state.show()
        }
        ScreenSurface {
            CreateOrUpdateScheduleBottomSheet(
                schedule = null,
                controlledApps = dummyApps,
                selectedApps = emptySet(),
                onAppSelectToggleClick = {},
                startTime = LocalTime.now(),
                endTime = LocalTime.now().plusHours(1),
                onDismiss = {},
                onAddOrUpdateClick = {},
                onSelectTimeSlot = { _, _ -> },
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
                sheetState = state,
                onDeleteOrDismiss = {},
                scheduleName = "",
                onScheduleNameChange = {},
                selectedDays = emptySet(),
                onDaySelected = {},
            )
        }
    }
}