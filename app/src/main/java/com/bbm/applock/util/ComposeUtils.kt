package com.bbm.applock.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.bbm.applock.R


@Composable
fun ScreenSurface(
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(R.drawable.bg_app),
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        content.invoke(this)
    }
}

@Composable
fun FullScreenLoader(
    backgroundColor: Color = Color.White.copy(alpha = 0.8f),
    loaderColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .noRippleClickable({

            })
            .zIndex(1f),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = loaderColor)
    }
}