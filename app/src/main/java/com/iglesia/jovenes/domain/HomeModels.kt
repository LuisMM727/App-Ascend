package com.iglesia.jovenes.domain

data class UserHeader(
    val userId: Int,
    val fullName: String,
    val accumulatedPoints: Int,
    val levelName: String,
    val levelOrder: Int,
    val requiredPoints: Int,
) {
    val progress: Float
        get() {
            if (requiredPoints <= 0) return 0f
            val raw = accumulatedPoints.toFloat() / requiredPoints
            return if (raw.isNaN() || raw.isInfinite()) 0f else raw.coerceIn(0f, 1f)
        }
}

data class FeaturedMission(
    val missionId: Int,
    val name: String,
    val description: String,
    val bonusPoints: Int,
    val completedActivities: Int,
    val totalActivities: Int,
    val startsAt: Long = 0L,
    val endsAt: Long = 0L,
)

data class HomeActivity(
    val activityId: Int,
    val title: String,
    val location: String,
    val date: Long,
    val currentCapacity: Int,
    val maximumCapacity: Int,
    val totalPoints: Int,
    val enrollmentStatus: String?,
)

data class LeaderboardEntry(
    val position: Int,
    val userId: Int,
    val fullName: String,
    val points: Int,
)

data class HomeData(
    val header: UserHeader,
    val featuredMission: FeaturedMission?,
    val missions: List<FeaturedMission> = emptyList(),
    val activities: List<HomeActivity>,
    val leaderboard: List<LeaderboardEntry>,
)
