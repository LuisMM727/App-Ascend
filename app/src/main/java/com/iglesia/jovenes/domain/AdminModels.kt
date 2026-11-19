package com.iglesia.jovenes.domain

/**
 * Modelo de usuario representado en el panel de administración.
 */
data class AdminUserItem(
    val id: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val status: String,
    val church: String?,
    val age: Int?,
    val phone: String?,
    val roleName: String,
    val points: Int,
) {
    /** Nombre completo concatenado del usuario */
    val fullName: String get() = "$name $lastName".trim()

    /** Indica si el estado de la cuenta es ACTIVO */
    val isActive: Boolean get() = status == "ACTIVO"
}

/**
 * Estadísticas e indicadores generales para el resumen del Administrador.
 */
data class AdminStats(
    val totalUsers: Int,
    val activeMissions: Int,
    val upcomingActivities: Int,
)

/**
 * Modelo de misión administrable.
 */
data class AdminMissionItem(
    val id: Int,
    val name: String,
    val description: String,
    val bonusPoints: Int,
    val status: String,
    val startsAt: Long,
    val endsAt: Long,
) {
    /** Indica si la misión se encuentra activa */
    val isActive: Boolean get() = status == "ACTIVA"
}

/**
 * Modelo de actividad o evento administrable.
 */
data class AdminActivityItem(
    val id: Int,
    val title: String,
    val description: String,
    val date: Long,
    val location: String,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val basePoints: Int,
    val bonusPoints: Int,
    val status: String,
) {
    /** Puntos de experiencia (XP) totales otorgados por la actividad */
    val totalPoints: Int get() = basePoints + bonusPoints

    /** Indica si la actividad se encuentra activa */
    val isActive: Boolean get() = status == "ACTIVA"
}

/**
 * Modelo de joven inscrito a un evento para la toma de asistencia por parte del Administrador.
 */
data class EnrolledUserItem(
    val userId: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val attendanceStatus: String,
    val earnedPoints: Int,
) {
    /** Nombre completo del joven inscrito */
    val fullName: String get() = "$name $lastName".trim()

    /** Indica si se le ha confirmado la asistencia */
    val isCompleted: Boolean get() = attendanceStatus == "COMPLETADA"
}

/**
 * Modelo de Nivel configurable por el Administrador.
 */
data class AdminLevelItem(
    val id: Int,
    val name: String,
    val description: String,
    val requiredPoints: Int,
    val order: Int,
)

/**
 * Parámetros para la creación de una nueva misión desde el Administrador.
 */
data class CreateMissionParams(
    val name: String,
    val description: String,
    val bonusPoints: Int,
    val durationDays: Int = 30,
)

/**
 * Parámetros para la creación de una nueva actividad desde el Administrador.
 */
data class CreateActivityParams(
    val title: String,
    val description: String,
    val location: String,
    val maxCapacity: Int,
    val basePoints: Int,
    val bonusPoints: Int,
)
