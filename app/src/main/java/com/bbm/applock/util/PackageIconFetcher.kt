package com.bbm.applock.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.asDrawable
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.pxOrElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class AppIcon(val packageName: String)

class AppIconFetcher(
    private val context: Context,
    private val appIcon: AppIcon,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        return withContext(Dispatchers.IO) {
            val drawable: Drawable = context.packageManager.getApplicationIcon(appIcon.packageName)
            val bitmap: Bitmap = drawable.toBitmap(
                width = options.size.width.pxOrElse { 128 },
                height = options.size.height.pxOrElse { 128 },
                config = Bitmap.Config.ARGB_8888
            )

            ImageFetchResult(
                image = bitmap.asImage(),
                isSampled = false,
                dataSource = DataSource.DISK
            )
        }
    }


    class Factory(private val context: Context) : Fetcher.Factory<AppIcon> {
        override fun create(
            data: AppIcon,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            return AppIconFetcher(context, data, options)
        }
    }
}