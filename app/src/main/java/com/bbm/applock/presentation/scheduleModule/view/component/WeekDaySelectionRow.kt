package com.bbm.applock.presentation.scheduleModule.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.ui.theme.AquaBlueBorder
import com.bbm.applock.ui.theme.WhiteColor
import com.bbm.applock.util.noRippleClickable
import java.time.DayOfWeek

@Composable
fun WeekDaySelectionRow(
    selectedDays: Set<DayOfWeek>,
    onDaySelected: (DayOfWeek) -> Unit,
    isViewing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DayOfWeek.entries.forEach { day ->
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .then(
                        if (selectedDays.contains(day)) {
                            Modifier.background(AquaBlueBorder)
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = AquaBlueBorder,
                                shape = CircleShape
                            )
                        }
                    )
                    .then(
                        if (isViewing) Modifier else
                            Modifier.noRippleClickable {
                                onDaySelected(
                                    day
                                )
                            }
                    )

            ) {
                Text(
                    text = day.name.first().toString(),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp,
                        color = if (selectedDays.contains(day)) WhiteColor else AquaBlueBorder
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun WeekDaySelectionRowPreview() {
    AppLockTheme {
        WeekDaySelectionRow(
            setOf(DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
            {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}