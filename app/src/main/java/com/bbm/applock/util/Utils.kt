package com.bbm.applock.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


inline fun Modifier.noRippleClickable(
    crossinline onClick: () -> Unit
): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

fun getAppIconDrawable(context: Context, packageName: String): Drawable? {
    return try {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        val bitmap = drawable.toBitmap(width = 128, height = 128)
        bitmap.toDrawable(context.resources)
    } catch (e: PackageManager.NameNotFoundException) {
        Log.w("AppIcon", "Icon not found for package: $packageName", e)
        null
    }
}

fun getAppNameFromPackage(context: Context, packageName: String): String {
    return try {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
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

val LocalDate.toDayOrdinalAndDayName: String
    get() {
        // Get the day of the month (e.g., 22)
        val dayOfMonth = this.dayOfMonth

        // Get the full day name (e.g., "Mon")
        val dayName = this.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

        // Determine the ordinal suffix
        val ordinalSuffix = when {
            dayOfMonth % 10 == 1 && dayOfMonth % 100 != 11 -> "st"
            dayOfMonth % 10 == 2 && dayOfMonth % 100 != 12 -> "nd"
            dayOfMonth % 10 == 3 && dayOfMonth % 100 != 13 -> "rd"
            else -> "th"
        }

        return "$dayOfMonth$ordinalSuffix $dayName"
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