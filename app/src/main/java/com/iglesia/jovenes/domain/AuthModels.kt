package com.iglesia.jovenes.domain

enum class UserRole {
    ADMIN,
    JUVENIL
}

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
    val fullName: String get() = "$name $lastName".trim()
}

data class RegisterParams(
    val name: String,
    val lastName: String,
    val email: String,
    val password: String,
)

data class OnboardingParams(
    val userId: Int,
    val church: String,
    val age: Int,
    val phone: String,
)
