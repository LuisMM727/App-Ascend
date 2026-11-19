package com.iglesia.jovenes.domain

/**
 * Roles de usuario dentro de la plataforma Ascend (Administrador vs Joven).
 */
enum class UserRole {
    ADMIN,
    JUVENIL
}

/**
 * Modelo de usuario autenticado en la sesión actual.
 */
data class AuthUser(
    val id: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val role: UserRole,
    val isProfileComplete: Boolean,
    val church: String? = null,
    val age: Int? = null,
    val phone: String? = null,
) {
    /** Nombre completo del usuario concatenado */
    val fullName: String get() = "$name $lastName".trim()
}

/**
 * Parámetros requeridos para registrar una nueva cuenta de joven.
 */
data class RegisterParams(
    val name: String,
    val lastName: String,
    val email: String,
    val password: String,
)

/**
 * Parámetros requeridos para completar el proceso de onboarding.
 */
data class OnboardingParams(
    val userId: Int,
    val church: String,
    val age: Int,
    val phone: String,
)
