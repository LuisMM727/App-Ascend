package com.iglesia.jovenes.domain

interface LeaderboardRepository {
    suspend fun getLeaderboardData(userId: Int): Result<LeaderboardScreenData>
}
