package com.iglesia.jovenes.domain

data class ProfileBadgeItem(
    val badgeId: Int,
    val name: String,
    val description: String,
    val isUnlocked: Boolean,
    val conditionValue: Int,
)

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
    val fullName: String get() = "$name $lastName".trim()
    val progress: Float
        get() {
            if (requiredPoints <= 0) return 0f
            val raw = accumulatedPoints.toFloat() / requiredPoints
            return if (raw.isNaN() || raw.isInfinite()) 0f else raw.coerceIn(0f, 1f)
        }
}
