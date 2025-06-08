package com.bbm.applock.presentation.installedControlledAppsModule.view

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.PermissionInfo
import com.bbm.applock.R
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.installedControlledAppsModule.vm.InstalledAppVM
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.util.AppIcon
import com.bbm.applock.util.AppIconFetcher
import com.bbm.applock.util.LifeCycleEvent
import com.bbm.applock.util.MultiDevicePreview
import com.bbm.applock.util.ScreenSurface
import com.bbm.applock.util.formatUsageTime
import com.bbm.applock.util.noRippleClickable

@Composable
fun InstalledAppListScreen(vm: InstalledAppVM) {
    val state = vm.state.collectAsState(UiState.Ideal)
    val permission = vm.permissionInfo.collectAsState(null)
    val context = LocalContext.current
    val searchText = vm.searchText.collectAsState()
    val appList = vm.installedAppList.collectAsState()

    when (state.value) {
        is UiState.Failure<*> -> {
        }

        UiState.Ideal -> {
        }

        UiState.Loading -> {
        }

        is UiState.Success<*> -> {
        }

        is UiState.ValidationError -> {

        }
    }

    LifeCycleEvent {
        if (it == Lifecycle.Event.ON_RESUME) {
            vm.checkPermission()
        }
    }
    InstalledAppListScreenContent(
        isLoading = state.value is UiState.Loading,
        searchText = searchText.value,
        onTextChange = {
            vm.onSearchTextChange(it)
        },
        onSearchClick = {
            // nothing to do
        },
        appList = appList.value,
        painter = {
            val imageLoader = remember(it.packageName) {
                ImageLoader.Builder(context)
                    .components {
                        add(AppIconFetcher.Factory(context))
                    }
                    .build()
            }
            rememberAsyncImagePainter(
                model = AppIcon(it.packageName),
                imageLoader = imageLoader
            )
        },
        onAddOrRemoveControlledApp = {
            vm.onAddOrRemoveControlledApp(it)
        },
        permission = permission.value,
        onClickPermission = {
            when (it.permissionType) {
                PermissionInfo.PermissionType.NOTIFICATION -> {

                }

                PermissionInfo.PermissionType.USAGE -> {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }

                PermissionInfo.PermissionType.ACCESSIBILITY -> {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }

                PermissionInfo.PermissionType.AUTO_START -> {
                    it.intent?.let {
                        val intent = Intent().apply {
                            component = ComponentName(
                                it.packageName,
                                it.className
                            )
                        }
                        context.startActivity(intent)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun InstalledAppListScreenContent(
    isLoading: Boolean,
    searchText: String,
    onTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    appList: List<AppUsageInfo>,
    painter: @Composable (AppUsageInfo) -> Painter,
    onAddOrRemoveControlledApp: (AppUsageInfo) -> Unit,
    permission: PermissionInfo?,
    onClickPermission: (PermissionInfo.Permission) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(64.dp))
        PermissionContent(
            permission,
            click = onClickPermission,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        SearchBar(
            query = searchText,
            onTextChange = onTextChange,
            onSearchClick = onSearchClick,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp, end = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Most Used Apps",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                )
            )
            Text(
                "Hours/Week",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                )
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(appList.size) {
                AppUsageRow(
                    appList[it],
                    painter.invoke(appList[it]),
                    onAddToControlledApp = {
                        onAddOrRemoveControlledApp(appList[it])
                    },
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun PermissionContent(
    permissions: PermissionInfo?,
    click: (PermissionInfo.Permission) -> Unit,
    modifier: Modifier = Modifier
) {
    permissions ?: return
    Column(
        modifier = modifier
    ) {
        for ((index, permission) in permissions.permissions.withIndex()) {
            PermissionRow(
                title = permission.permissionType.name,
                allowClick = { click.invoke(permission) },
                modifier = Modifier.fillMaxWidth()
            )
            if (index != permissions.permissions.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    allowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .border(
                width = 1.dp,
                color = Color(0xFF099ABB),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography
                .labelMedium
                .copy(fontSize = 12.sp),
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .noRippleClickable(allowClick)
                .background(Color(0xFF099ABB))
                .wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.allow),
                style = MaterialTheme.typography
                    .labelMedium
                    .copy(fontSize = 12.sp, color = Color.White),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@MultiDevicePreview
@Composable
private fun PermissionRowPreview() {
    AppLockTheme {
        PermissionRow(
            "Accessibility",
            {},
            Modifier
                .fillMaxWidth()
                .background(Color.White)
        )
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AppUsageRow(
    info: AppUsageInfo,
    appIcon: Painter,
    onAddToControlledApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = if (info.isControlledApp) painterResource(R.drawable.ic_check_circle)
            else painterResource(R.drawable.ic_add_circle),
            contentDescription = "Add to Controlled App",
            modifier = Modifier
                .size(18.dp)
                .noRippleClickable(onAddToControlledApp)
        )

        Spacer(Modifier.width(8.dp))

        Image(
            painter = appIcon,
            contentDescription = info.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = info.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                val totalScreenTime =
                    info.totalScreenTime?.timeInMillis?.takeIf { it > 0 }
                        ?: (7 * 24 * 60 * 60 * 1000L)
                val width =
                    maxWidth * (info.usageTimeInMillis / totalScreenTime.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .width(width)
                        .fillMaxHeight()
                        .background(Color(0xFF099ABB))
                )
            }
        }
        Text(
            text = formatUsageTime(info.usageTimeInMillis),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@MultiDevicePreview
@Composable
private fun AppUsageRowPreview() {
    AppLockTheme {
        Column {
            AppUsageRow(
                AppUsageInfo(
                    name = "Instagram",
                    packageName = "com.media.instagra",
                    usageTimeInMillis = 22,
                    isControlledApp = false
                ),
                appIcon = painterResource(R.drawable.ic_launcher_background),
                onAddToControlledApp = {},
                modifier = Modifier.padding(12.dp)
            )
            AppUsageRow(
                AppUsageInfo(
                    name = "Youtube",
                    packageName = "com.media.instagra",
                    usageTimeInMillis = 22,
                    isControlledApp = true
                ),
                appIcon = painterResource(R.drawable.ic_launcher_background),
                onAddToControlledApp = {},
                modifier = Modifier.padding(12.dp)
            )
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

@MultiDevicePreview
@Composable
private fun InstalledAppListScreenPreview() {
    AppLockTheme {
        ScreenSurface {
            InstalledAppListScreenContent(
                isLoading = false,
                searchText = "Instagram",
                onTextChange = {},
                onSearchClick = {},
                appList = listOf(
                    AppUsageInfo(
                        name = "Instagram",
                        packageName = "com.media.instagra",
                        usageTimeInMillis = 99990000,
                        isControlledApp = false
                    ),
                    AppUsageInfo(
                        name = "Youtube",
                        packageName = "com.media.instagra",
                        usageTimeInMillis = 90000000,
                        isControlledApp = true
                    ),
                    AppUsageInfo(
                        name = "Gmail",
                        packageName = "com.media.instagra",
                        usageTimeInMillis = 40000000,
                        isControlledApp = false
                    )
                ),
                onAddOrRemoveControlledApp = {},
                permission = PermissionInfo(
                    permissions = listOf(
                        PermissionInfo.Permission(
                            permissionType = PermissionInfo.PermissionType.ACCESSIBILITY,
                            intent = null,
                            isGranted = true,
                            isOptional = false
                        ),
                        PermissionInfo.Permission(
                            permissionType = PermissionInfo.PermissionType.NOTIFICATION,
                            intent = null,
                            isGranted = true,
                            isOptional = false
                        ),
                        PermissionInfo.Permission(
                            permissionType = PermissionInfo.PermissionType.USAGE,
                            intent = null,
                            isGranted = true,
                            isOptional = false
                        ),
                    ),
                    allGranted = false,
                    shouldAskPermission = true
                ),
                painter = { painterResource(R.drawable.ic_launcher_background) },
                onClickPermission = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}