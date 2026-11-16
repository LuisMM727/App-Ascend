package com.iglesia.jovenes.data

import com.iglesia.jovenes.data.local.LeaderboardDao
import com.iglesia.jovenes.domain.LeaderboardEntryItem
import com.iglesia.jovenes.domain.LeaderboardRepository
import com.iglesia.jovenes.domain.LeaderboardScreenData

class LeaderboardRepositoryImpl(
    private val leaderboardDao: LeaderboardDao,
) : LeaderboardRepository {

    override suspend fun getLeaderboardData(userId: Int): Result<LeaderboardScreenData> = runCatching {
        val rawList = leaderboardDao.getFullLeaderboardRaw()

        val items = rawList.map { raw ->
            LeaderboardEntryItem(
                position = raw.position,
                userId = raw.userId,
                fullName = raw.fullName,
                church = raw.church,
                levelName = raw.levelName,
                points = raw.points,
                isCurrentUser = raw.userId == userId,
            )
        }

        val top3 = items.take(3)
        val fullList = if (items.size > 3) items.drop(3) else emptyList()

        val currentUserRaw = leaderboardDao.getUserRankRaw(userId)
        val currentUserEntry = currentUserRaw?.let { raw ->
            LeaderboardEntryItem(
                position = raw.position,
                userId = raw.userId,
                fullName = raw.fullName,
                church = raw.church,
                levelName = raw.levelName,
                points = raw.points,
                isCurrentUser = true,
            )
        } ?: items.firstOrNull { it.isCurrentUser }

        LeaderboardScreenData(
            top3 = top3,
            fullList = fullList,
            currentUserEntry = currentUserEntry,
        )
    }
}
