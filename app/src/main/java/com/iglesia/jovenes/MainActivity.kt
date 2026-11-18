package com.iglesia.jovenes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iglesia.jovenes.data.NotificationHelper
import com.iglesia.jovenes.presentation.AdminHomeScreen
import com.iglesia.jovenes.presentation.AdminViewModel
import com.iglesia.jovenes.presentation.AdminViewModelFactory
import com.iglesia.jovenes.presentation.AuthScreen
import com.iglesia.jovenes.presentation.AuthState
import com.iglesia.jovenes.presentation.AuthViewModel
import com.iglesia.jovenes.presentation.AuthViewModelFactory
import com.iglesia.jovenes.presentation.HomeRoute
import com.iglesia.jovenes.presentation.HomeViewModel
import com.iglesia.jovenes.presentation.HomeViewModelFactory
import com.iglesia.jovenes.presentation.LeaderboardViewModel
import com.iglesia.jovenes.presentation.LeaderboardViewModelFactory
import com.iglesia.jovenes.presentation.MissionsViewModel
import com.iglesia.jovenes.presentation.MissionsViewModelFactory
import com.iglesia.jovenes.presentation.OnboardingScreen
import com.iglesia.jovenes.presentation.ProfileViewModel
import com.iglesia.jovenes.presentation.ProfileViewModelFactory
import com.iglesia.jovenes.presentation.YouthTheme

/**
 * Actividad Principal de la aplicación Ascend.
 * Coordina la inicialización de notificaciones, permisos y la navegación en Jetpack Compose según el estado de autenticación.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar canal de notificaciones y solicitar permisos en Android 13+
        try {
            NotificationHelper.createNotificationChannel(this)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                }
            }
        } catch (t: Throwable) {
            // Protección contra excepciones al solicitar permisos en tiempo de ejecución
        }

        val app = application as YouthApplication
        setContent {
            YouthTheme {
                // ViewModel principal de Autenticación
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(app.authRepository),
                )

                val authState by authViewModel.authState.collectAsStateWithLifecycle()
                val authMode by authViewModel.mode.collectAsStateWithLifecycle()
                val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()

                // Navegación principal basada en el estado de AuthState
                when (val currentAuthState = authState) {
                    // Estado 1: Sesión no iniciada -> Mostrar pantalla de Login / Registro
                    is AuthState.LoggedOut -> {
                        AuthScreen(
                            viewModel = authViewModel,
                            mode = authMode,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                        )
                    }

                    // Estado 2: Perfil pendiente -> Mostrar formulario de completado (Iglesia, Edad)
                    is AuthState.OnboardingRequired -> {
                        OnboardingScreen(
                            user = currentAuthState.user,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onCompleteProfile = { church, age, phone ->
                                authViewModel.completeOnboarding(
                                    userId = currentAuthState.user.id,
                                    church = church,
                                    age = age,
                                    phone = phone,
                                )
                            },
                        )
                    }

                    // Estado 3: Administrador Autenticado -> Panel de Gestión Admin
                    is AuthState.LoggedInAdmin -> {
                        val adminViewModel: AdminViewModel = viewModel(
                            key = "admin_user_${currentAuthState.user.id}",
                            factory = AdminViewModelFactory(
                                adminRepository = app.adminRepository,
                                adminUserId = currentAuthState.user.id,
                            ),
                        )
                        AdminHomeScreen(
                            adminUser = currentAuthState.user,
                            viewModel = adminViewModel,
                            onLogout = { authViewModel.logout() },
                        )
                    }

                    // Estado 4: Joven / Usuario Autenticado -> Pantalla Principal (Home, Misiones, Clasificación, Perfil)
                    is AuthState.LoggedInUser -> {
                        val homeViewModel: HomeViewModel = viewModel(
                            key = "home_user_${currentAuthState.user.id}",
                            factory = HomeViewModelFactory(
                                repository = app.homeRepository,
                                userId = currentAuthState.user.id,
                            ),
                        )
                        val profileViewModel: ProfileViewModel = viewModel(
                            key = "profile_user_${currentAuthState.user.id}",
                            factory = ProfileViewModelFactory(
                                profileRepository = app.profileRepository,
                                userId = currentAuthState.user.id,
                            ),
                        )
                        val missionsViewModel: MissionsViewModel = viewModel(
                            key = "missions_user_${currentAuthState.user.id}",
                            factory = MissionsViewModelFactory(
                                missionsRepository = app.missionsRepository,
                                userId = currentAuthState.user.id,
                            ),
                        )
                        val leaderboardViewModel: LeaderboardViewModel = viewModel(
                            key = "leaderboard_user_${currentAuthState.user.id}",
                            factory = LeaderboardViewModelFactory(
                                leaderboardRepository = app.leaderboardRepository,
                                userId = currentAuthState.user.id,
                            ),
                        )
                        HomeRoute(
                            viewModel = homeViewModel,
                            profileViewModel = profileViewModel,
                            missionsViewModel = missionsViewModel,
                            leaderboardViewModel = leaderboardViewModel,
                            onLogout = { authViewModel.logout() },
                        )
                    }
                }
            }
        }
    }
}
