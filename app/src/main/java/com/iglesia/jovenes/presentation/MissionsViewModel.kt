package com.iglesia.jovenes.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iglesia.jovenes.data.NotificationHelper
import com.iglesia.jovenes.domain.HomeRepository
import com.iglesia.jovenes.domain.MissionsRepository
import com.iglesia.jovenes.domain.MissionsScreenData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MissionsFilter { ALL, ACTIVE, COMPLETED }

sealed interface MissionsUiState {
    data object Loading : MissionsUiState
    data class Success(val data: MissionsScreenData) : MissionsUiState
    data class Error(val message: String) : MissionsUiState
}

class MissionsViewModel(
    private val missionsRepository: MissionsRepository,
    private val homeRepository: HomeRepository,
    private val userId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MissionsUiState>(MissionsUiState.Loading)
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MissionsFilter.ALL)
    val selectedFilter: StateFlow<MissionsFilter> = _selectedFilter.asStateFlow()

    init {
        loadMissions()
    }

    fun setFilter(filter: MissionsFilter) {
        _selectedFilter.value = filter
    }

    fun loadMissions() {
        viewModelScope.launch {
            _uiState.value = MissionsUiState.Loading
            missionsRepository.getMissionsScreenData(userId)
                .onSuccess { _uiState.value = MissionsUiState.Success(it) }
                .onFailure { _uiState.value = MissionsUiState.Error(it.message ?: "Error al cargar misiones.") }
        }
    }

    fun enrollInActivity(activityId: Int, context: Context? = null, title: String? = null, location: String? = null) {
        viewModelScope.launch {
            homeRepository.enrollInActivity(userId, activityId)
                .onSuccess {
                    if (context != null && !title.isNullOrBlank() && !location.isNullOrBlank()) {
                        NotificationHelper.sendEnrollmentNotification(context, title, location)
                    }
                    loadMissions()
                }
                .onFailure { loadMissions() }
        }
    }

    fun cancelEnrollment(activityId: Int) {
        viewModelScope.launch {
            homeRepository.cancelEnrollment(userId, activityId)
                .onSuccess { loadMissions() }
                .onFailure { loadMissions() }
        }
    }
}

class MissionsViewModelFactory(
    private val missionsRepository: MissionsRepository,
    private val homeRepository: HomeRepository,
    private val userId: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MissionsViewModel(missionsRepository, homeRepository, userId) as T
}
