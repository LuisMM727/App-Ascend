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

/** Criterios de filtrado para las misiones (Todas, Activas, Completadas) */
enum class MissionsFilter { ALL, ACTIVE, COMPLETED }

/** Estados UI de la pantalla de misiones */
sealed interface MissionsUiState {
    data object Loading : MissionsUiState
    data class Success(val data: MissionsScreenData) : MissionsUiState
    data class Error(val message: String) : MissionsUiState
}

/**
 * ViewModel para la pantalla de misiones.
 * Permite filtrar misiones, desplegar tareas asociadas e inscribirse a eventos directamente desde cada tarjeta.
 */
class MissionsViewModel(
    private val missionsRepository: MissionsRepository,
    private val homeRepository: HomeRepository,
    private val userId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MissionsUiState>(MissionsUiState.Loading)
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MissionsFilter.ALL)
    /** Filtro de misiones seleccionado en la barra superior */
    val selectedFilter: StateFlow<MissionsFilter> = _selectedFilter.asStateFlow()

    init {
        loadMissions()
    }

    /** Cambia el filtro activo de misiones */
    fun setFilter(filter: MissionsFilter) {
        _selectedFilter.value = filter
    }

    /** Carga el catálogo de misiones y el progreso de tareas del usuario */
    fun loadMissions() {
        viewModelScope.launch {
            _uiState.value = MissionsUiState.Loading
            missionsRepository.getMissionsScreenData(userId)
                .onSuccess { _uiState.value = MissionsUiState.Success(it) }
                .onFailure { _uiState.value = MissionsUiState.Error(it.message ?: "Error al cargar misiones.") }
        }
    }

    /** Inscribe al usuario a la actividad asociada dentro de la misión desplegada */
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

    /** Cancela la inscripción del usuario a la actividad asociada */
    fun cancelEnrollment(activityId: Int) {
        viewModelScope.launch {
            homeRepository.cancelEnrollment(userId, activityId)
                .onSuccess { loadMissions() }
                .onFailure { loadMissions() }
        }
    }
}

/** Fábrica de instanciación para MissionsViewModel */
class MissionsViewModelFactory(
    private val missionsRepository: MissionsRepository,
    private val homeRepository: HomeRepository,
    private val userId: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MissionsViewModel::class.java)) {
            return MissionsViewModel(missionsRepository, homeRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
