package com.iglesia.jovenes.domain

interface HomeRepository {
    suspend fun getHome(userId: Int): Result<HomeData>
    suspend fun enrollInActivity(userId: Int, activityId: Int): Result<Unit>
    suspend fun cancelEnrollment(userId: Int, activityId: Int): Result<Unit>
}
