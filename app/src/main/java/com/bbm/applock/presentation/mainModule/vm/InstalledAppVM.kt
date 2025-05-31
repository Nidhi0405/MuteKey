package com.bbm.applock.presentation.mainModule.vm

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _permissionInfo = MutableStateFlow<PermissionInfo?>(null)
    val permissionInfo: StateFlow<PermissionInfo?> = _permissionInfo

    private val _installedApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())

    val installedAppList = _searchText
        .combine(_installedApps) { query, apps ->
            if (query.trim().isBlank()) apps
            else apps.filter { it.name.contains(query.trim(), ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

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

    fun syncAndGetInstalledApps() {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            val list = syncInstalledAppsUseCase.invoke(7)
            _installedApps.value = list
            _state.emit(UiState.Success(list, "success"))
        }
    }

    fun onSearchTextChange(value: String) {
        _searchText.value = value
    }

    fun onSearchClick() {
        if (_searchText.value.isEmpty()) return
        viewModelScope.launch {
            // todo filter on query
        }
    }

    fun onAddOrRemoveControlledApp(app: AppUsageInfo) {
        viewModelScope.launch {
            val currentList = _installedApps.value.toMutableList()
            val index = currentList.indexOfFirst { it.packageName == app.packageName }
                .takeIf { it >= 0 } ?: return@launch
            val controlledApp = app.copy(isControlledApp = !app.isControlledApp)
            currentList[index] = controlledApp
            _installedApps.value = currentList
            if (app.isControlledApp)
                deleteControlledAppUseCase.invoke(controlledApp)
            else
                addControlledAppUseCase.invoke(controlledApp)
        }
    }
}