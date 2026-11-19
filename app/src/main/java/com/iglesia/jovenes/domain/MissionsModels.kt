package com.iglesia.jovenes.domain

/**
 * Modelo de una tarea o actividad comunitaria asociada a una misión.
 */
data class MissionActivityTask(
    val activityId: Int,
    val title: String,
    val description: String,
    val date: Long,
    val location: String,
    val totalPoints: Int,
    val enrollmentStatus: String?,
) {
    /** Indica si la tarea se ha completado con asistencia confirmada */
    val isCompleted: Boolean get() = enrollmentStatus == "COMPLETADA" || enrollmentStatus == "ASISTIO"

    /** Indica si el usuario está inscrito a esta tarea */
    val isEnrolled: Boolean get() = enrollmentStatus != null
}

/**
 * Modelo detallado de una misión con su desglose de actividades vinculadas.
 */
data class MissionDetailItem(
    val missionId: Int,
    val name: String,
    val description: String,
    val bonusPoints: Int,
    val status: String,
    val startsAt: Long,
    val endsAt: Long,
    val completedActivities: Int,
    val totalActivities: Int,
    val activities: List<MissionActivityTask> = emptyList(),
) {
    /** Indica si la misión ha sido completada en su totalidad */
    val isCompleted: Boolean get() = (totalActivities > 0 && completedActivities >= totalActivities) || status == "COMPLETADA"

    /** Barra de progreso normalizada (0.0f a 1.0f) de la misión */
    val progress: Float
        get() {
            if (totalActivities <= 0) return 0f
            val raw = completedActivities.toFloat() / totalActivities
            return if (raw.isNaN() || raw.isInfinite()) 0f else raw.coerceIn(0f, 1f)
        }
}

/**
 * Contenedor de datos para la pantalla de misiones.
 */
data class MissionsScreenData(
    val missions: List<MissionDetailItem>,
)
