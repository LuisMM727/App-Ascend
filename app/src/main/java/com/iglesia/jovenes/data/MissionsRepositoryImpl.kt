package com.iglesia.jovenes.data

import com.iglesia.jovenes.data.local.MissionsDao
import com.iglesia.jovenes.domain.MissionActivityTask
import com.iglesia.jovenes.domain.MissionDetailItem
import com.iglesia.jovenes.domain.MissionsRepository
import com.iglesia.jovenes.domain.MissionsScreenData

class MissionsRepositoryImpl(
    private val missionsDao: MissionsDao,
) : MissionsRepository {

    override suspend fun getMissionsScreenData(userId: Int): Result<MissionsScreenData> = runCatching {
        val rawMissions = missionsDao.getAllMissionsWithProgress(userId)

        val missionItems = rawMissions.map { raw ->
            val tasks = missionsDao.getActivitiesForMission(raw.missionId, userId).map { taskRaw ->
                MissionActivityTask(
                    activityId = taskRaw.activityId,
                    title = taskRaw.title,
                    description = taskRaw.description,
                    date = taskRaw.date,
                    location = taskRaw.location,
                    totalPoints = taskRaw.totalPoints,
                    enrollmentStatus = taskRaw.enrollmentStatus,
                )
            }

            MissionDetailItem(
                missionId = raw.missionId,
                name = raw.name,
                description = raw.description,
                bonusPoints = raw.bonusPoints,
                status = raw.status,
                startsAt = raw.startsAt,
                endsAt = raw.endsAt,
                completedActivities = raw.completedActivities,
                totalActivities = raw.totalActivities,
                activities = tasks,
            )
        }

        MissionsScreenData(missions = missionItems)
    }
}
