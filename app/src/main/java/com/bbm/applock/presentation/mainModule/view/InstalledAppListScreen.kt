package com.bbm.applock.presentation.mainModule.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.PermissionInfo
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.mainModule.vm.InstalledAppVM
import com.bbm.applock.ui.theme.AppLockTheme
import com.bbm.applock.util.LifeCycleEvent
import com.bbm.applock.util.formatUsageTime
import com.bbm.applock.util.icon

@Composable
fun InstalledAppListScreen(vm: InstalledAppVM) {
    val state = vm.state.collectAsState(UiState.Ideal)
    val permission = vm.permissionInfo.collectAsState(null)
    val context = LocalContext.current

    when (state.value) {
        is UiState.Failure<*> -> {
        }

        UiState.Ideal -> {

        }

        UiState.Loading -> {

        }

        is UiState.Success<*> -> {

        }
    }

    LifeCycleEvent {
        if (it == Lifecycle.Event.ON_RESUME) {
            vm.checkPermission()
            vm.syncAndGetInstalledApps()
        }
    }
    InstalledAppListScreenContent(
        isLoading = state.value is UiState.Loading,
        appList = vm.installedAppList,
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
                            component = ComponentName(it.packageName, it.className)
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
    appList: List<AppUsageInfo>,
    permission: PermissionInfo?,
    onClickPermission: (PermissionInfo.Permission) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        PermissionContent(
            permission,
            click = onClickPermission,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(appList.size) {
                AppUsageRow(appList[it])
            }
        }
    }
}

@Composable
private fun PermissionContent(
    permissions: PermissionInfo?,
    click: (PermissionInfo.Permission) -> Unit
) {
    permissions ?: return
    for (permission in permissions.permissions) {
        PermissionRow(
            title = permission.permissionType.name,
            enableClick = { click.invoke(permission) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    enableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Button(
            onClick = enableClick,
            modifier = Modifier
                .wrapContentWidth()
                .height(40.dp),
            content = {
                Text("Enable")
            }
        )
    }
}

@Preview
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


@Composable
fun AppUsageRow(info: AppUsageInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAppIcon(
                packageName = info.packageName,
                context = LocalContext.current
            )!!,
            contentDescription = info.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = info.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formatUsageTime(info.usageTime),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun rememberAppIcon(
    packageName: String,
    context: Context = LocalContext.current
): Painter? {
    val cachedBitmap = remember { packageName.icon }

    return remember(cachedBitmap) {
        if (cachedBitmap != null) {
            BitmapPainter(cachedBitmap.asImageBitmap())
        } else {
            try {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                val bmp = drawable.toBitmap()
                packageName.icon = bmp
                BitmapPainter(bmp.asImageBitmap())
            } catch (e: Exception) {
                null
            }
        }
    }
}

@Preview
@Composable
private fun InstalledAppListScreenPreview() {
    AppLockTheme {

    }
}