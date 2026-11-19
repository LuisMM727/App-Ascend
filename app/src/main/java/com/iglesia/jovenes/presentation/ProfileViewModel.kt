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

/** Estados de interfaz para la pantalla de perfil del joven */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profileData: UserProfileData) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

/**
 * ViewModel que administra el perfil de usuario, insignias desbloqueadas y edición de información.
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val userId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    /** Controla la visibilidad del diálogo de edición de información personal */
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    /** Estado de guardado durante la edición del perfil */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadProfile()
    }

    /** Carga las estadísticas, XP, nivel e insignias del usuario */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.getProfile(userId)
                .onSuccess { _uiState.value = ProfileUiState.Success(it) }
                .onFailure { _uiState.value = ProfileUiState.Error(it.message ?: "Error al cargar perfil.") }
        }
    }

    /** Abre el diálogo modal de edición de perfil */
    fun openEditDialog() {
        _showEditDialog.value = true
        _errorMessage.value = null
    }

    /** Cierra el diálogo modal de edición de perfil */
    fun closeEditDialog() {
        _showEditDialog.value = false
        _errorMessage.value = null
    }

    /** Guarda los cambios realizados en la información personal (Iglesia, Edad, Teléfono) */
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

/** Fábrica de instanciación para ProfileViewModel */
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
