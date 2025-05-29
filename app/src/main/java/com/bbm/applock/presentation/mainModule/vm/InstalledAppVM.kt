package com.bbm.applock.presentation.mainModule.vm

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.PermissionInfo
import com.applock.domain.usecase.AddControlledAppUseCase
import com.applock.domain.usecase.DeleteControlledAppUseCase
import com.applock.domain.usecase.PermissionUseCase
import com.applock.domain.usecase.SyncInstalledAppsUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.base.BaseVM
import com.bbm.applock.service.AppBlockAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstalledAppVM @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase,
    private val addControlledAppUseCase: AddControlledAppUseCase,
    private val deleteControlledAppUseCase: DeleteControlledAppUseCase,
    private val permissionUseCase: PermissionUseCase
) : BaseVM() {

    init {
        checkPermission()
    }

    private val _permissionInfo = MutableStateFlow<PermissionInfo?>(null)
    val permissionInfo: StateFlow<PermissionInfo?> = _permissionInfo
    fun checkPermission() {
        viewModelScope.launch(dispatchers.io) {
            val permissionInfo =
                permissionUseCase.checkPermission(AppBlockAccessibilityService::class.java)
            when {
                permissionInfo.allGranted -> {
                    _permissionInfo.value = null
                    syncAndGetInstalledApps()
                }

                else -> {
                    _permissionInfo.value =
                        permissionInfo.copy(permissionInfo.permissions.filterNot { it.isGranted })
                }
            }
        }
    }

    val installedAppList = mutableStateListOf<AppUsageInfo>()
    fun syncAndGetInstalledApps() {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            val list = syncInstalledAppsUseCase.invoke(7)
            installedAppList.clear()
            installedAppList.addAll(list)
            _state.emit(UiState.Success(list, "success"))
        }
    }

    fun addControlledApp(app: AppUsageInfo) {
        viewModelScope.launch {
            val index = installedAppList.indexOfFirst { it.packageName == app.packageName }
                .takeIf { it >= 0 } ?: return@launch
            val controlledApp = app.copy(isControlledApp = true)
            installedAppList[index] = controlledApp
            addControlledAppUseCase.invoke(controlledApp)
        }
    }

    fun removeControlledApp(app: AppUsageInfo) {
        viewModelScope.launch {
            val index = installedAppList.indexOfFirst { it.packageName == app.packageName }
                .takeIf { it >= 0 } ?: return@launch
            val controlledApp = app.copy(isControlledApp = false)
            installedAppList[index] = controlledApp
            deleteControlledAppUseCase.invoke(controlledApp)
        }
    }
}