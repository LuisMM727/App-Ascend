package com.iglesia.jovenes.domain

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
    val fullName: String get() = "$name $lastName".trim()
    val isActive: Boolean get() = status == "ACTIVO"
}

data class AdminStats(
    val totalUsers: Int,
    val activeMissions: Int,
    val upcomingActivities: Int,
)

data class AdminMissionItem(
    val id: Int,
    val name: String,
    val description: String,
    val bonusPoints: Int,
    val status: String,
    val startsAt: Long,
    val endsAt: Long,
) {
    val isActive: Boolean get() = status == "ACTIVA"
}

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
    val totalPoints: Int get() = basePoints + bonusPoints
    val isActive: Boolean get() = status == "ACTIVA"
}

data class EnrolledUserItem(
    val userId: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val attendanceStatus: String,
    val earnedPoints: Int,
) {
    val fullName: String get() = "$name $lastName".trim()
    val isCompleted: Boolean get() = attendanceStatus == "COMPLETADA"
}

data class AdminLevelItem(
    val id: Int,
    val name: String,
    val description: String,
    val requiredPoints: Int,
    val order: Int,
)

data class CreateMissionParams(
    val name: String,
    val description: String,
    val bonusPoints: Int,
    val durationDays: Int = 30,
)

data class CreateActivityParams(
    val title: String,
    val description: String,
    val location: String,
    val maxCapacity: Int,
    val basePoints: Int,
    val bonusPoints: Int,
)
