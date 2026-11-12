package com.iglesia.jovenes.domain

interface HomeRepository {
    suspend fun getHome(userId: Int): Result<HomeData>
}
