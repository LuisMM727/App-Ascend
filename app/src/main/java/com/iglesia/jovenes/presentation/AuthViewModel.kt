package com.iglesia.jovenes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iglesia.jovenes.domain.AuthRepository
import com.iglesia.jovenes.domain.AuthUser
import com.iglesia.jovenes.domain.OnboardingParams
import com.iglesia.jovenes.domain.RegisterParams
import com.iglesia.jovenes.domain.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object LoggedOut : AuthState
    data class OnboardingRequired(val user: AuthUser) : AuthState
    data class LoggedInUser(val user: AuthUser) : AuthState
    data class LoggedInAdmin(val user: AuthUser) : AuthState
}

enum class AuthMode { LOGIN, REGISTER }

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _mode = MutableStateFlow(AuthMode.LOGIN)
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setMode(newMode: AuthMode) {
        _mode.value = newMode
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.login(email, password)
                .onSuccess { user ->
                    _isLoading.value = false
                    when {
                        user.role == UserRole.ADMIN -> {
                            _authState.value = AuthState.LoggedInAdmin(user)
                        }
                        !user.isProfileComplete -> {
                            _authState.value = AuthState.OnboardingRequired(user)
                        }
                        else -> {
                            _authState.value = AuthState.LoggedInUser(user)
                        }
                    }
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.message ?: "Ocurrió un error al iniciar sesión."
                }
        }
    }

    fun register(name: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val params = RegisterParams(name, lastName, email, password)
            authRepository.register(params)
                .onSuccess { user ->
                    _isLoading.value = false
                    // Al registrarse exitosamente, pasa directo al onboarding
                    _authState.value = AuthState.OnboardingRequired(user)
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.message ?: "Ocurrió un error al registrarse."
                }
        }
    }

    fun completeOnboarding(userId: Int, church: String, age: Int, phone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val params = OnboardingParams(userId, church, age, phone)
            authRepository.completeProfile(params)
                .onSuccess { updatedUser ->
                    _isLoading.value = false
                    _authState.value = AuthState.LoggedInUser(updatedUser)
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _errorMessage.value = error.message ?: "Ocurrió un error al guardar los datos."
                }
        }
    }

    fun logout() {
        _authState.value = AuthState.LoggedOut
        _errorMessage.value = null
        _mode.value = AuthMode.LOGIN
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuthViewModel(authRepository) as T
}
