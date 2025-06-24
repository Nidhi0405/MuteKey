package com.bbm.applock.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.bbm.applock.R
import com.bbm.applock.presentation.installedControlledAppsModule.vm.InstalledAppVM
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.ui.theme.TextButtonColor
import com.bbm.applock.ui.theme.TextPrimary
import com.bbm.applock.ui.theme.TextSecondary


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
    onSortClick: (InstalledAppVM.SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .height(42.dp),
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
                    .copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400
                    ),
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
                            .copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W400
                            )
                    )
                innerTextField()
            }

            Box {
                IconButton(
                    onClick = {
                        expanded = !expanded
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_horizontal_dots),
                        contentDescription = "Sort by name or usage time"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    InstalledAppVM.SortType.entries.forEach {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(it.value),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 18.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.W400
                                    )
                                )
                            },
                            onClick = {
                                onSortClick.invoke(it)
                                expanded = false
                            }
                        )
                    }
                }
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

@Composable
fun AlertDialogWithOneAction(
    title: String,
    description: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0XFFF1FCFF))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600,
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                )
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        color = TextButtonColor
                    )
                )
            }
        }
    }
}