package com.iglesia.jovenes.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(params: RegisterParams): Result<AuthUser>
    suspend fun completeProfile(params: OnboardingParams): Result<AuthUser>
}
