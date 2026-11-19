package com.iglesia.jovenes.data

import com.iglesia.jovenes.data.local.ActivityEntity
import com.iglesia.jovenes.data.local.AdminDao
import com.iglesia.jovenes.data.local.MissionEntity
import com.iglesia.jovenes.domain.AdminActivityItem
import com.iglesia.jovenes.domain.AdminLevelItem
import com.iglesia.jovenes.domain.AdminMissionItem
import com.iglesia.jovenes.domain.AdminRepository
import com.iglesia.jovenes.domain.AdminStats
import com.iglesia.jovenes.domain.AdminUserItem
import com.iglesia.jovenes.domain.CreateActivityParams
import com.iglesia.jovenes.domain.CreateMissionParams
import com.iglesia.jovenes.domain.EnrolledUserItem

/**
 * Implementación del repositorio del panel de Administrador.
 * Maneja la gestión de usuarios, creación de misiones y eventos, control de asistencia y configuración de niveles.
 */
class AdminRepositoryImpl(
    private val adminDao: AdminDao,
) : AdminRepository {

    /**
     * Consulta las métricas generales del sistema (total de usuarios, misiones activas y próximos eventos).
     */
    override suspend fun getStats(): Result<AdminStats> = runCatching {
        AdminStats(
            totalUsers = adminDao.getUserCount(),
            activeMissions = adminDao.getActiveMissionsCount(),
            upcomingActivities = adminDao.getUpcomingActivitiesCount(),
        )
    }

    /**
     * Obtiene el listado completo de jóvenes registrados con sus puntos XP acumulados.
     */
    override suspend fun getUsers(): Result<List<AdminUserItem>> = runCatching {
        adminDao.getAllUsersWithPoints().map { raw ->
            AdminUserItem(
                id = raw.userId,
                name = raw.name,
                lastName = raw.lastName,
                email = raw.email,
                status = raw.status,
                church = raw.church,
                age = raw.age,
                phone = raw.phone,
                roleName = raw.roleName,
                points = raw.points,
            )
        }
    }

    /**
     * Activa o desactiva la cuenta de un usuario registrado.
     */
    override suspend fun toggleUserStatus(userId: Int, currentStatus: String): Result<Unit> = runCatching {
        val newStatus = if (currentStatus == "ACTIVO") "INACTIVO" else "ACTIVO"
        adminDao.updateUserStatus(userId, newStatus)
    }

    /**
     * Obtiene el catálogo de misiones registradas en el sistema.
     */
    override suspend fun getMissions(): Result<List<AdminMissionItem>> = runCatching {
        adminDao.getAllMissions().map { entity ->
            AdminMissionItem(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                bonusPoints = entity.bonusPoints,
                status = entity.status,
                startsAt = entity.startsAt,
                endsAt = entity.endsAt,
            )
        }
    }

    /**
     * Crea una nueva misión comunitaria con sus puntos bonus y plazo de vigencia.
     */
    override suspend fun createMission(params: CreateMissionParams): Result<Unit> = runCatching {
        if (params.name.isBlank()) error("El nombre de la misión no puede estar vacío.")
        if (params.bonusPoints <= 0) error("Los puntos bonus deben ser mayores a 0.")

        val now = System.currentTimeMillis()
        val durationMillis = params.durationDays * 24L * 60L * 60L * 1000L

        val entity = MissionEntity(
            id = 0,
            name = params.name.trim(),
            description = params.description.trim(),
            bonusPoints = params.bonusPoints,
            startsAt = now,
            endsAt = now + durationMillis,
            status = "ACTIVA",
        )
        adminDao.insertMission(entity)
    }

    /**
     * Cambia el estado de una misión entre ACTIVA e INACTIVA.
     */
    override suspend fun toggleMissionStatus(missionId: Int, currentStatus: String): Result<Unit> = runCatching {
        val newStatus = if (currentStatus == "ACTIVA") "INACTIVA" else "ACTIVA"
        adminDao.updateMissionStatus(missionId, newStatus)
    }

    /**
     * Obtiene todas las actividades o eventos comunitarios.
     */
    override suspend fun getActivities(): Result<List<AdminActivityItem>> = runCatching {
        adminDao.getAllActivities().map { entity ->
            AdminActivityItem(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                date = entity.date,
                location = entity.location,
                maxCapacity = entity.maxCapacity,
                currentCapacity = entity.currentCapacity,
                basePoints = entity.basePoints,
                bonusPoints = entity.bonusPoints,
                status = entity.status,
            )
        }
    }

    /**
     * Crea una nueva actividad comunitaria y la vincula de forma automática a las misiones activas.
     */
    override suspend fun createActivity(creatorUserId: Int, params: CreateActivityParams): Result<Unit> = runCatching {
        if (params.title.isBlank()) error("El título de la actividad no puede estar vacío.")
        if (params.location.isBlank()) error("El lugar no puede estar vacío.")
        if (params.maxCapacity <= 0) error("El cupo máximo debe ser mayor a 0.")

        val now = System.currentTimeMillis()
        val defaultDate = now + (7L * 24 * 60 * 60 * 1000)

        val entity = ActivityEntity(
            id = 0,
            title = params.title.trim(),
            description = params.description.trim(),
            date = defaultDate,
            location = params.location.trim(),
            maxCapacity = params.maxCapacity,
            currentCapacity = 0,
            basePoints = params.basePoints,
            bonusPoints = params.bonusPoints,
            status = "ACTIVA",
            publishedAt = now,
            creatorUserId = creatorUserId,
        )
        val newId = adminDao.insertActivity(entity).toInt()
        adminDao.linkActivityToActiveMissions(newId)
    }

    /**
     * Cambia el estado de una actividad entre ACTIVA e INACTIVA.
     */
    override suspend fun toggleActivityStatus(activityId: Int, currentStatus: String): Result<Unit> = runCatching {
        val newStatus = if (currentStatus == "ACTIVA") "INACTIVA" else "ACTIVA"
        adminDao.updateActivityStatus(activityId, newStatus)
    }

    /**
     * Consulta los jóvenes inscritos a una actividad dada para la toma de asistencia.
     */
    override suspend fun getEnrolledUsersForActivity(activityId: Int): Result<List<EnrolledUserItem>> = runCatching {
        adminDao.getEnrolledUsersForActivity(activityId).map { raw ->
            EnrolledUserItem(
                userId = raw.userId,
                name = raw.name,
                lastName = raw.lastName,
                email = raw.email,
                attendanceStatus = raw.attendanceStatus,
                earnedPoints = raw.earnedPoints,
            )
        }
    }

    /**
     * Marca la asistencia confirmada de un joven y le acredita los puntos XP ganados.
     */
    override suspend fun markAttendance(userId: Int, activityId: Int, points: Int): Result<Unit> = runCatching {
        adminDao.markAttendanceAndGrantPoints(userId, activityId, points)
    }

    /**
     * Obtiene los niveles configurados en la aplicación con sus puntos XP requeridos.
     */
    override suspend fun getLevels(): Result<List<AdminLevelItem>> = runCatching {
        adminDao.getAllLevels().map { entity ->
            AdminLevelItem(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                requiredPoints = entity.requiredPoints,
                order = entity.order,
            )
        }
    }

    /**
     * Actualiza la cantidad de puntos XP requeridos para alcanzar un nivel determinado.
     */
    override suspend fun updateLevelPoints(levelId: Int, points: Int): Result<Unit> = runCatching {
        if (points <= 0) error("Los puntos requeridos deben ser mayores a 0.")
        adminDao.updateLevelPoints(levelId, points)
    }
}
