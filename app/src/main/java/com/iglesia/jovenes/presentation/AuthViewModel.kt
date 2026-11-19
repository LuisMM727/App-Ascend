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

/**
 * Estados posibles de la sesión de usuario en la aplicación.
 */
sealed interface AuthState {
    /** Sesión no iniciada (Pantalla de Login / Registro) */
    data object LoggedOut : AuthState

    /** Usuario registrado pero con datos de iglesia/edad pendientes */
    data class OnboardingRequired(val user: AuthUser) : AuthState

    /** Joven autenticado listo para ingresar a la vista principal */
    data class LoggedInUser(val user: AuthUser) : AuthState

    /** Administrador autenticado listo para ingresar al panel de gestión */
    data class LoggedInAdmin(val user: AuthUser) : AuthState
}

/** Modo del formulario de autenticación (Inicio de sesión o Registro de cuenta nueva) */
enum class AuthMode { LOGIN, REGISTER }

/**
 * ViewModel encargado del flujo de autenticación, validación de credenciales, registro y onboarding.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    /** Flujo observable del estado actual de autenticación */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _mode = MutableStateFlow(AuthMode.LOGIN)
    /** Modo actual del formulario de autenticación */
    val mode: StateFlow<AuthMode> = _mode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** Indicador de estado de carga para los formularios */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    /** Mensaje de error para mostrar en la interfaz */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Cambia entre el modo de Inicio de Sesión y Registro */
    fun setMode(newMode: AuthMode) {
        _mode.value = newMode
        _errorMessage.value = null
    }

    /** Limpia los mensajes de error activos */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Inicia sesión con el correo y contraseña especificados.
     */
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

    /**
     * Registra una nueva cuenta de joven en el sistema.
     */
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

    /**
     * Guarda la información adicional del perfil obtenida en el onboarding.
     */
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

    /** Cierra la sesión activa del usuario */
    fun logout() {
        _authState.value = AuthState.LoggedOut
        _errorMessage.value = null
        _mode.value = AuthMode.LOGIN
    }
}

/** Fábrica de instanciación para AuthViewModel */
class AuthViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
