package com.iglesia.jovenes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.iglesia.jovenes.presentation.OnboardingScreen
import com.iglesia.jovenes.presentation.YouthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as YouthApplication
        setContent {
            YouthTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(app.authRepository),
                )

                val authState by authViewModel.authState.collectAsStateWithLifecycle()
                val authMode by authViewModel.mode.collectAsStateWithLifecycle()
                val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()

                when (val currentAuthState = authState) {
                    is AuthState.LoggedOut -> {
                        AuthScreen(
                            viewModel = authViewModel,
                            mode = authMode,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                        )
                    }

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

                    is AuthState.LoggedInUser -> {
                        val homeViewModel: HomeViewModel = viewModel(
                            key = "home_user_${currentAuthState.user.id}",
                            factory = HomeViewModelFactory(
                                repository = app.homeRepository,
                                userId = currentAuthState.user.id,
                            ),
                        )
                        HomeRoute(
                            viewModel = homeViewModel,
                            onLogout = { authViewModel.logout() },
                        )
                    }
                }
            }
        }
    }
}
