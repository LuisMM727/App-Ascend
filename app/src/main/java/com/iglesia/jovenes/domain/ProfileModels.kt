package com.iglesia.jovenes.domain

/**
 * Modelo de una Insignia o Medalla representada en la cuadrícula del perfil.
 */
data class ProfileBadgeItem(
    val badgeId: Int,
    val name: String,
    val description: String,
    val isUnlocked: Boolean,
    val conditionValue: Int,
)

/**
 * Modelo de datos del perfil completo del usuario.
 */
data class UserProfileData(
    val userId: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val church: String?,
    val age: Int?,
    val phone: String?,
    val roleName: String,
    val accumulatedPoints: Int,
    val levelName: String,
    val levelOrder: Int,
    val requiredPoints: Int,
    val completedActivitiesCount: Int,
    val badges: List<ProfileBadgeItem>,
) {
    /** Nombre completo del usuario */
    val fullName: String get() = "$name $lastName".trim()

    /** Progreso normalizado de experiencia (XP) hacia el objetivo del siguiente nivel */
    val progress: Float
        get() {
            if (requiredPoints <= 0) return 0f
            val raw = accumulatedPoints.toFloat() / requiredPoints
            return if (raw.isNaN() || raw.isInfinite()) 0f else raw.coerceIn(0f, 1f)
        }
}
