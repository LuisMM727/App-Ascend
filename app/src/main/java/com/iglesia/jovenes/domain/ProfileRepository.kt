package com.iglesia.jovenes.domain

interface ProfileRepository {
    suspend fun getProfile(userId: Int): Result<UserProfileData>
    suspend fun updateProfileDetails(userId: Int, church: String, age: Int, phone: String): Result<Unit>
}
