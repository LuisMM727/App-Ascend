package com.iglesia.jovenes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iglesia.jovenes.domain.ProfileRepository
import com.iglesia.jovenes.domain.UserProfileData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profileData: UserProfileData) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val userId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.getProfile(userId)
                .onSuccess { _uiState.value = ProfileUiState.Success(it) }
                .onFailure { _uiState.value = ProfileUiState.Error(it.message ?: "Error al cargar perfil.") }
        }
    }

    fun openEditDialog() {
        _showEditDialog.value = true
        _errorMessage.value = null
    }

    fun closeEditDialog() {
        _showEditDialog.value = false
        _errorMessage.value = null
    }

    fun updateProfileDetails(church: String, age: Int, phone: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            profileRepository.updateProfileDetails(userId, church, age, phone)
                .onSuccess {
                    _isSaving.value = false
                    _showEditDialog.value = false
                    loadProfile()
                }
                .onFailure {
                    _isSaving.value = false
                    _errorMessage.value = it.message
                }
        }
    }
}

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val userId: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
