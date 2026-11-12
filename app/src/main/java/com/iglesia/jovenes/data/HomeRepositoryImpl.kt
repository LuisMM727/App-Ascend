package com.iglesia.jovenes.data

import com.iglesia.jovenes.data.local.HomeDao
import com.iglesia.jovenes.domain.HomeData
import com.iglesia.jovenes.domain.HomeRepository

class HomeRepositoryImpl(
    private val homeDao: HomeDao,
) : HomeRepository {
    override suspend fun getHome(userId: Int): Result<HomeData> = runCatching {
        homeDao.home(userId, System.currentTimeMillis())
            ?: error("No se encontraron datos para el usuario activo")
    }

    override suspend fun enrollInActivity(userId: Int, activityId: Int): Result<Unit> = runCatching {
        homeDao.enrollUserInActivity(userId, activityId)
    }

    override suspend fun cancelEnrollment(userId: Int, activityId: Int): Result<Unit> = runCatching {
        homeDao.cancelUserEnrollment(userId, activityId)
    }
}
