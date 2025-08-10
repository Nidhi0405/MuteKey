package com.applock.domain.util

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


inline fun <T> result(block: () -> T): Result<T> {
    return try {
        val result = block()
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun Long.toReadableDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

val LocalTime.formate: String
    get() {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        return this.format(formatter)
    }

/**
 * Converts a list of [DayOfWeek] to a user-friendly string representation.
 *
 * Examples:
 * - Empty list: ""
 * - Single day (e.g., Monday): "Monday"
 * - Two days (e.g., Monday and Tuesday): "Monday and Tuesday"
 * - Multiple days (e.g., Monday, Wednesday, Friday): "Mon, Wed & Fri"
 * - All weekdays: "Mon, Tue, Wed, Thu & Fri"
 */
val List<DayOfWeek>.shortString: String
    get() {
        if (isEmpty()) return ""

        val sortedDays = this.sortedBy { it.value }

        return when (sortedDays.size) {
            1 -> sortedDays.first().getDisplayName(TextStyle.FULL, Locale.getDefault())

            2 -> { // Special handling for two days
                val firstDay =
                    sortedDays.first().getDisplayName(TextStyle.FULL, Locale.getDefault())
                val secondDay =
                    sortedDays.last().getDisplayName(TextStyle.FULL, Locale.getDefault())
                "$firstDay and $secondDay" // Example: "Monday and Tuesday"
            }

            else -> { // More than two days
                val shortDayNames =
                    sortedDays.map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                shortDayNames.dropLast(1)
                    .joinToString(", ") + " & " + shortDayNames.last()
            }
        }
    }