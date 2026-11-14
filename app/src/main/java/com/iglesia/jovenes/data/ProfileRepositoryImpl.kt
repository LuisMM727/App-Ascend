package com.iglesia.jovenes.data

import com.iglesia.jovenes.data.local.ProfileDao
import com.iglesia.jovenes.domain.ProfileBadgeItem
import com.iglesia.jovenes.domain.ProfileRepository
import com.iglesia.jovenes.domain.UserProfileData

class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
) : ProfileRepository {

    override suspend fun getProfile(userId: Int): Result<UserProfileData> = runCatching {
        val raw = profileDao.getUserProfileDetails(userId)
            ?: error("No se pudo cargar la información del usuario.")

        val completedActivities = profileDao.getCompletedActivitiesCount(userId)
        val badges = profileDao.getAllBadgesWithStatus(raw.accumulatedPoints).map { b ->
            ProfileBadgeItem(
                badgeId = b.badgeId,
                name = b.name,
                description = b.description,
                isUnlocked = b.isUnlocked,
                conditionValue = b.conditionValue,
            )
        }

        UserProfileData(
            userId = raw.userId,
            name = raw.name,
            lastName = raw.lastName,
            email = raw.email,
            church = raw.church,
            age = raw.age,
            phone = raw.phone,
            roleName = raw.roleName,
            accumulatedPoints = raw.accumulatedPoints,
            levelName = raw.levelName,
            levelOrder = raw.levelOrder,
            requiredPoints = raw.requiredPoints,
            completedActivitiesCount = completedActivities,
            badges = badges,
        )
    }

    override suspend fun updateProfileDetails(userId: Int, church: String, age: Int, phone: String): Result<Unit> = runCatching {
        if (church.isBlank()) error("La iglesia no puede estar vacía.")
        if (age <= 0) error("Ingresa una edad válida.")

        profileDao.updateUserPersonalDetails(
            userId = userId,
            church = church.trim(),
            age = age,
            phone = phone.trim().takeIf { it.isNotEmpty() },
        )
    }
}
