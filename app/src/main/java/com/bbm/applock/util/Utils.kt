package com.bbm.applock.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatUsageTime(ms: Long): String {
    val totalSecs = ms / 1000
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60

    return when {
        hours > 0 -> "$hours.$minutes"
        minutes > 0 -> "0.$minutes"
        else -> "0.0"
    }
}


inline fun Modifier.noRippleClickable(
    crossinline onClick: () -> Unit
): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
val LocalTime.toHourMinute: String
    get() = format(timeFormatter)

val dateFormatter = DateTimeFormatter.ofPattern("dd")

private val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ENGLISH)
val LocalDate.toDayDateMonth: String
    get() {
        return format(formatter)
    }

val Duration.readable: String
    get() {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return buildString {
            if (hours > 0) append("$hours hr ")
            if (minutes > 0) append("$minutes min ")
            if (secs > 0 || isEmpty()) append("$secs sec")
        }.trim()
    }