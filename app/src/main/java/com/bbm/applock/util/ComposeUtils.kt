package com.bbm.applock.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.bbm.applock.R
import com.bbm.applock.ui.theme.AppLockTheme


@Composable
fun ScreenSurface(
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(R.drawable.bg_app),
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier) {
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
    isLoading: Boolean,
    backgroundColor: Color = Color.White.copy(alpha = 0.8f),
    loaderColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        content.invoke(this)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .zIndex(1f)
                    .noRippleClickable {

                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = loaderColor)
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(32.dp)
            .height(82.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "Search",
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(8.dp))

            BasicTextField(
                value = query,
                onValueChange = onTextChange,
                textStyle = MaterialTheme.typography
                    .labelMedium
                    .copy(fontSize = 14.sp, fontWeight = FontWeight.W200),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .weight(1f),
            ) { innerTextField ->
                if (query.isEmpty())
                    Text(
                        stringResource(R.string.hint_search),
                        style = MaterialTheme.typography
                            .labelMedium
                            .copy(fontSize = 14.sp, fontWeight = FontWeight.W200)
                    )
                innerTextField()
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .noRippleClickable(onClick = onSearchClick)
                    .background(Color(0xFF099ABB))
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.search),
                    style = MaterialTheme.typography
                        .labelMedium
                        .copy(fontSize = 10.sp, color = Color.White),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@MultiDevicePreview
@Composable
private fun SearchBarPreview() {
    AppLockTheme {
        SearchBar(
            "Instagram",
            {},
            {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}