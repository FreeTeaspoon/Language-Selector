package vegabobo.languageselector.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vegabobo.languageselector.dao.AppInfoDb
import vegabobo.languageselector.data.apps.AppRepository
import vegabobo.languageselector.data.locales.LocaleRepository
import vegabobo.languageselector.domain.apps.AppInfo
import javax.inject.Inject

data class HistoryScreenState(
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryScreenVm @Inject constructor(
    private val appRepository: AppRepository,
    private val localeRepository: LocaleRepository,
    appInfoDb: AppInfoDb
) : ViewModel() {
    private val dao = appInfoDb.appInfoDao()
    private val _uiState = MutableStateFlow(HistoryScreenState())
    val uiState: StateFlow<HistoryScreenState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = dao.getHistory().map { entity ->
                async { runCatching { appRepository.loadApp(entity.pkg) }.getOrNull() }
            }.awaitAll().filterNotNull()
            val appsWithTags = apps.map { app ->
                async { app.copy(modifiedState = localeRepository.getModifiedState(app.pkg)) }
            }.awaitAll()
            _uiState.value = HistoryScreenState(apps = appsWithTags, isLoading = false)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.cleanLastSelectedAll()
            _uiState.update { it.copy(apps = emptyList(), isLoading = false) }
        }
    }
}
