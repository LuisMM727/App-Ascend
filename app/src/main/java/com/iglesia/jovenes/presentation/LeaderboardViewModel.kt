package com.iglesia.jovenes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iglesia.jovenes.domain.LeaderboardRepository
import com.iglesia.jovenes.domain.LeaderboardScreenData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LeaderboardUiState {
    data object Loading : LeaderboardUiState
    data class Success(val data: LeaderboardScreenData) : LeaderboardUiState
    data class Error(val message: String) : LeaderboardUiState
}

class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val userId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = LeaderboardUiState.Loading
            leaderboardRepository.getLeaderboardData(userId)
                .onSuccess { _uiState.value = LeaderboardUiState.Success(it) }
                .onFailure { _uiState.value = LeaderboardUiState.Error(it.message ?: "Error al cargar la clasificación.") }
        }
    }
}

class LeaderboardViewModelFactory(
    private val leaderboardRepository: LeaderboardRepository,
    private val userId: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LeaderboardViewModel(leaderboardRepository, userId) as T
}
