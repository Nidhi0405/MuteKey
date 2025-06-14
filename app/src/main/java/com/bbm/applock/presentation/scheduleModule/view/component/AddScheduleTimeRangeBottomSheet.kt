package com.bbm.applock.presentation.scheduleModule.view.component

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.R
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.ui.theme.AquaBlue
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextSecondary
import com.bbm.applock.ui.theme.WhiteColor
import com.bbm.applock.util.ScreenSurface
import com.bbm.applock.util.noRippleClickable
import com.bbm.applock.util.toDayDateMonth
import com.bbm.applock.util.toHourMinute
import java.time.LocalDate
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleTimeRangeBottomSheet(
    selectedDate: LocalDate,
    controlledApps: List<AppUsageInfo>,
    selectedApps: Set<AppUsageInfo>,
    onAppSelectToggleClick: (AppUsageInfo) -> Unit,
    startTime: LocalTime,
    endTime: LocalTime,
    onDismiss: () -> Unit,
    onTimeRangeSelectionClick: () -> Unit,
    onAddOrUpdateClick: () -> Unit,
    painter: @Composable (AppUsageInfo) -> Painter,
    sheetState: SheetState,
) {
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current.density
    val screenHeight = containerSize.height.dp / density
    val containerHeight = (screenHeight.value * 0.60).dp

    ModalBottomSheet(
        dragHandle = null,
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss.invoke()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight)
                .background(WhiteColor)
        ) {
            Spacer(Modifier.height(22.dp))

            Text(
                text = selectedDate.toDayDateMonth,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = AquaBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                ),
                modifier = Modifier.padding(horizontal = 26.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .height(80.dp)
            ) {
                TimeSelectionCard(
                    heading = "Start",
                    time = startTime.toHourMinute,
                    onClick = {
                        onTimeRangeSelectionClick.invoke()
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TimeSelectionCard(
                    heading = "End",
                    time = endTime.toHourMinute,
                    onClick = {
                        onTimeRangeSelectionClick.invoke()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 22.dp)
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
                            painter = painter.invoke(app),
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
                        painter = painter.invoke(selectedApps.elementAt(it)),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                onClick = onAddOrUpdateClick,
                colors = ButtonDefaults.buttonColors(containerColor = AquaBlue)
            ) {
                Text(
                    "Add Or Update",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WhiteColor,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun TimeSelectionCard(
    heading: String,
    time: String,
    onClick: () -> Unit,
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
            Spacer(Modifier.height(12.dp))
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
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AddScheduleTimeRangeBottomSheetPreview() {
    AppLockTheme {
        ScreenSurface {
            AddScheduleTimeRangeBottomSheet(
                selectedDate = LocalDate.now(),
                startTime = LocalTime.now(),
                endTime = LocalTime.now().plusHours(1),
                onDismiss = {},
                sheetState = rememberModalBottomSheetState().apply {
                },
                onTimeRangeSelectionClick = {},
                onAddOrUpdateClick = {},
                selectedApps = emptySet(),
                controlledApps = emptyList(),
                onAppSelectToggleClick = {},
                painter = {
                    painterResource(R.drawable.ic_launcher_background)
                }
            )
        }
    }
}