package com.bbm.applock.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object AppMetadataCache {
    val iconCache = object : LruCache<String, Drawable>(128) {}
    val appNameCache = object : LruCache<String, String>(256) {}
}

data class AppPackageMetadata(
    val label: String,
    val icon: Drawable?
)


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
    AppMetadataCache.iconCache.get(packageName)?.let { return it }
    return try {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        val bitmap = drawable.toBitmap(width = 128, height = 128)
        bitmap.toDrawable(context.resources).also {
            AppMetadataCache.iconCache.put(packageName, it)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Log.w("AppIcon", "Icon not found for package: $packageName", e)
        null
    }
}

fun getAppNameFromPackage(context: Context, packageName: String): String {
    AppMetadataCache.appNameCache.get(packageName)?.let { return it }
    return try {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString().also {
            AppMetadataCache.appNameCache.put(packageName, it)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}

suspend fun loadAppPackageMetadata(
    context: Context,
    imageLoader: ImageLoader,
    packageNames: Collection<String>
): Map<String, AppPackageMetadata> = withContext(Dispatchers.IO) {
    packageNames
        .distinct()
        .associateWith { packageName ->
            AppPackageMetadata(
                label = getAppNameFromPackage(context, packageName),
                icon = loadAppIconWithCoil(context, imageLoader, packageName)
            )
        }
}

private suspend fun loadAppIconWithCoil(
    context: Context,
    imageLoader: ImageLoader,
    packageName: String
): Drawable? {
    AppMetadataCache.iconCache.get(packageName)?.let { return it }
    val request = ImageRequest.Builder(context)
        .data(AppIcon(packageName))
        .build()
    return (imageLoader.execute(request) as? SuccessResult)
        ?.image
        ?.asDrawable(context.resources)
        ?.also { AppMetadataCache.iconCache.put(packageName, it) }
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
