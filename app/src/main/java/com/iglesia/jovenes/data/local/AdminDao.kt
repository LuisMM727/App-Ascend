package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

data class AdminUserRaw(
    val userId: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val status: String,
    val church: String?,
    val age: Int?,
    val phone: String?,
    val roleName: String,
    val points: Int,
)

@Dao
interface AdminDao {
    @Query(
        """
        SELECT u.ID_Usuarios AS userId,
               u.Nombre AS name,
               u.Apellido AS lastName,
               u.Correo AS email,
               u.Estado AS status,
               u.Iglesia AS church,
               u.Edad AS age,
               u.Telefono AS phone,
               r.Nombre AS roleName,
               COALESCE(up.Puntos_acumulados, 0) AS points
        FROM USUARIOS u
        JOIN ROLES r ON r.ID_Rol = u.FK_Rol
        LEFT JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        ORDER BY u.ID_Usuarios DESC
        """
    )
    suspend fun getAllUsersWithPoints(): List<AdminUserRaw>

    @Query("UPDATE USUARIOS SET Estado = :status WHERE ID_Usuarios = :userId")
    suspend fun updateUserStatus(userId: Int, status: String)

    @Query("SELECT * FROM MISIONES ORDER BY ID_Misiones DESC")
    suspend fun getAllMissions(): List<MissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Query("UPDATE MISIONES SET Estado = :status WHERE ID_Misiones = :missionId")
    suspend fun updateMissionStatus(missionId: Int, status: String)

    @Query("SELECT * FROM ACTIVIDADES ORDER BY Fecha DESC")
    suspend fun getAllActivities(): List<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Query("UPDATE ACTIVIDADES SET Estado = :status WHERE ID_Actividades = :activityId")
    suspend fun updateActivityStatus(activityId: Int, status: String)

    @Query("SELECT * FROM NIVELES ORDER BY Orden ASC")
    suspend fun getAllLevels(): List<LevelEntity>

    @Query("UPDATE NIVELES SET Puntos_requeridos = :points WHERE ID_Niveles = :levelId")
    suspend fun updateLevelPoints(levelId: Int, points: Int)

    @Query("SELECT COUNT(*) FROM USUARIOS WHERE FK_Rol = 2")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM MISIONES WHERE Estado = 'ACTIVA'")
    suspend fun getActiveMissionsCount(): Int

    @Query("SELECT COUNT(*) FROM ACTIVIDADES WHERE Estado = 'ACTIVA'")
    suspend fun getUpcomingActivitiesCount(): Int
}
