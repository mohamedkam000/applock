package dev.muhammad.applock.features.applist.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.muhammad.applock.data.repository.AppLockRepository
import dev.muhammad.applock.features.applist.domain.AppSearchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appSearchManager = AppSearchManager(application)
    private val appLockRepository = AppLockRepository(application)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lockedApps = MutableStateFlow<Set<String>>(emptySet())

    private val _debouncedQuery = MutableStateFlow("")

    val filteredApps: StateFlow<List<ApplicationInfo>> =
        combine(_allApps, _debouncedQuery) { apps, query ->
            if (query.isBlank()) {
                apps
            } else {
                apps.filter { appInfo ->
                    appInfo.loadLabel(getApplication<Application>().packageManager).toString()
                        .contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        loadAllApplications()
        loadLockedApps()

        viewModelScope.launch {
            _searchQuery
                .debounce(100L)
                .collect { query ->
                    _debouncedQuery.value = query
                }
        }
    }

    private fun loadAllApplications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apps = withContext(Dispatchers.IO) {
                    appSearchManager.loadApps()
                }
                _allApps.value = apps
            } catch (e: Exception) {
                e.printStackTrace()
                _allApps.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadLockedApps() {
        _lockedApps.value = appLockRepository.getLockedApps()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppLock(appInfo: ApplicationInfo, shouldLock: Boolean) {
        val packageName = appInfo.packageName

        val currentLockedApps = _lockedApps.value.toMutableSet()
        if (shouldLock) {
            currentLockedApps.add(packageName)
            appLockRepository.addLockedApp(packageName)
        } else {
            currentLockedApps.remove(packageName)
            appLockRepository.removeLockedApp(packageName)
        }
        _lockedApps.value = currentLockedApps
    }

    fun isAppLocked(packageName: String): Boolean {
        return _lockedApps.value.contains(packageName)
    }
}
