package com.bbm.applock.presentation.installedControlledAppsModule.vm

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.core.logE
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.PermissionInfo
import com.applock.domain.usecase.AddControlledAppUseCase
import com.applock.domain.usecase.DeleteControlledAppUseCase
import com.applock.domain.usecase.PermissionUseCase
import com.applock.domain.usecase.SyncInstalledAppsUseCase
import com.bbm.applock.R
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.base.BaseVM
import com.bbm.applock.service.AppBlockAccessibilityService
import com.bbm.applock.util.AppLifecycleObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstalledAppVM @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase,
    private val addControlledAppUseCase: AddControlledAppUseCase,
    private val deleteControlledAppUseCase: DeleteControlledAppUseCase,
    private val permissionUseCase: PermissionUseCase,
    private val appLifecycleObserver: AppLifecycleObserver,
    val imageLoader: ImageLoader,
) : BaseVM() {

    enum class SortType(@StringRes val value: Int) {
        NAME(R.string.sort_by_name), USAGE(R.string.sort_by_usage)
    }

    init {
        viewModelScope.launch(dispatchers.default) {
            appLifecycleObserver.isAppInForeground.collect { isForeground ->
                if (isForeground) {
                    checkPermission()
                }
            }
        }
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
        .flowOn(dispatchers.default)
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
                    syncAndGetInstalledApps()
                    _permissionInfo.value =
                        permissionInfo.copy(permissionInfo.permissions.filterNot { it.isGranted })
                }
            }
        }
    }

    fun syncAndGetInstalledApps(days: Int = 7) {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            val endTime = System.currentTimeMillis()
            val startTime = endTime - days * 24 * 60 * 60 * 1000L // Convert days to milliseconds
            val list = syncInstalledAppsUseCase.invoke(startTime, endTime)
            _installedApps.value = list.sortedByDescending { it.usageTimeInMillis }
            _state.emit(UiState.Success(list, "success"))
        }
    }

    fun onSearchTextChange(value: String) {
        _searchText.value = value
    }

    fun onAddOrRemoveControlledApp(app: AppUsageInfo) {
        viewModelScope.launch {
            val currentList = _installedApps.value.toMutableList()
            val index = currentList.indexOfFirst { it.packageName == app.packageName }
                .takeIf { it >= 0 } ?: return@launch
            val controlledApp = app.copy(isControlledApp = !app.isControlledApp)
            currentList[index] = controlledApp
            _installedApps.value = currentList
            if (app.isControlledApp) {
                deleteControlledAppUseCase.invoke(controlledApp).fold(
                    onSuccess = {},
                    onFailure = {
                        it.stackTraceToString().logE()
                    }
                )
            } else {
                addControlledAppUseCase.invoke(controlledApp).fold(
                    onSuccess = {},
                    onFailure = {
                        it.stackTraceToString().logE()
                    }
                )
            }
        }
    }

    fun sortAppList(type: SortType) {
        viewModelScope.launch(dispatchers.default) {
            _state.emit(UiState.Loading)
            _installedApps.update {
                when (type) {
                    SortType.NAME -> it.sortedBy { it.name }
                    SortType.USAGE -> it.sortedByDescending { it.usageTimeInMillis }
                }
            }
            _state.emit(UiState.Success(Unit, "sorted by $type"))
        }
    }
}