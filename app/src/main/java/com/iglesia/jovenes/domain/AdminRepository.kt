package com.iglesia.jovenes.domain

interface AdminRepository {
    suspend fun getStats(): Result<AdminStats>
    suspend fun getUsers(): Result<List<AdminUserItem>>
    suspend fun toggleUserStatus(userId: Int, currentStatus: String): Result<Unit>
    suspend fun getMissions(): Result<List<AdminMissionItem>>
    suspend fun createMission(params: CreateMissionParams): Result<Unit>
    suspend fun toggleMissionStatus(missionId: Int, currentStatus: String): Result<Unit>
    suspend fun getActivities(): Result<List<AdminActivityItem>>
    suspend fun createActivity(creatorUserId: Int, params: CreateActivityParams): Result<Unit>
    suspend fun toggleActivityStatus(activityId: Int, currentStatus: String): Result<Unit>
    suspend fun getLevels(): Result<List<AdminLevelItem>>
    suspend fun updateLevelPoints(levelId: Int, points: Int): Result<Unit>
}
