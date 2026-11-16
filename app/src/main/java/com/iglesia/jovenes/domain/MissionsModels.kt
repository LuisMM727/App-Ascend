package com.iglesia.jovenes.domain

data class MissionActivityTask(
    val activityId: Int,
    val title: String,
    val description: String,
    val date: Long,
    val location: String,
    val totalPoints: Int,
    val enrollmentStatus: String?,
) {
    val isCompleted: Boolean get() = enrollmentStatus == "COMPLETADA" || enrollmentStatus == "ASISTIO"
    val isEnrolled: Boolean get() = enrollmentStatus != null
}

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
    val isCompleted: Boolean get() = (totalActivities > 0 && completedActivities >= totalActivities) || status == "COMPLETADA"
    val progress: Float
        get() {
            if (totalActivities <= 0) return 0f
            val raw = completedActivities.toFloat() / totalActivities
            return if (raw.isNaN() || raw.isInfinite()) 0f else raw.coerceIn(0f, 1f)
        }
}

data class MissionsScreenData(
    val missions: List<MissionDetailItem>,
)
