package com.iglesia.jovenes.domain

interface MissionsRepository {
    suspend fun getMissionsScreenData(userId: Int): Result<MissionsScreenData>
}
