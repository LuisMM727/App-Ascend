package com.iglesia.jovenes.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iglesia.jovenes.data.NotificationHelper
import com.iglesia.jovenes.domain.HomeData
import com.iglesia.jovenes.domain.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estados UI de la pantalla principal */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * ViewModel que gestiona la carga de la vista principal del joven y el proceso de inscripción a eventos.
 */
class HomeViewModel(
    private val repository: HomeRepository,
    private val userId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Recarga los datos del encabezado, misiones y próximos eventos */
    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeUiState.Loading
            repository.getHome(userId)
                .onSuccess { _state.value = HomeUiState.Success(it) }
                .onFailure {
                    _state.value = HomeUiState.Error(
                        it.message ?: "No fue posible cargar el inicio",
                    )
                }
        }
    }

    /** Inscribe al joven en una actividad y dispara la notificación de confirmación */
    fun enrollInActivity(activityId: Int, context: Context? = null, title: String? = null, location: String? = null) {
        viewModelScope.launch {
            repository.enrollInActivity(userId, activityId)
                .onSuccess {
                    if (context != null && !title.isNullOrBlank() && !location.isNullOrBlank()) {
                        NotificationHelper.sendEnrollmentNotification(context, title, location)
                    }
                    refresh()
                }
                .onFailure { refresh() }
        }
    }

    /** Cancela la inscripción del joven a la actividad y libera el cupo */
    fun cancelEnrollment(activityId: Int) {
        viewModelScope.launch {
            repository.cancelEnrollment(userId, activityId)
                .onSuccess { refresh() }
                .onFailure { refresh() }
        }
    }
}

/** Fábrica de instanciación para HomeViewModel */
class HomeViewModelFactory(
    private val repository: HomeRepository,
    private val userId: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
