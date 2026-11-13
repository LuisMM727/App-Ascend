package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

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

data class EnrolledUserRaw(
    val userId: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val attendanceStatus: String,
    val earnedPoints: Int,
)

@Dao
abstract class AdminDao {
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
               COALESCE(r.Nombre, 'JUVENIL') AS roleName,
               COALESCE(up.Puntos_acumulados, 0) AS points
        FROM USUARIOS u
        LEFT JOIN ROLES r ON r.ID_Rol = u.FK_Rol
        LEFT JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        ORDER BY u.ID_Usuarios DESC
        """
    )
    abstract suspend fun getAllUsersWithPoints(): List<AdminUserRaw>

    @Query("UPDATE USUARIOS SET Estado = :status WHERE ID_Usuarios = :userId")
    abstract suspend fun updateUserStatus(userId: Int, status: String)

    @Query("SELECT * FROM MISIONES ORDER BY ID_Misiones DESC")
    abstract suspend fun getAllMissions(): List<MissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMission(mission: MissionEntity): Long

    @Query("UPDATE MISIONES SET Estado = :status WHERE ID_Misiones = :missionId")
    abstract suspend fun updateMissionStatus(missionId: Int, status: String)

    @Query("SELECT * FROM ACTIVIDADES ORDER BY Fecha DESC")
    abstract suspend fun getAllActivities(): List<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertActivity(activity: ActivityEntity): Long

    @Query("UPDATE ACTIVIDADES SET Estado = :status WHERE ID_Actividades = :activityId")
    abstract suspend fun updateActivityStatus(activityId: Int, status: String)

    @Query(
        """
        SELECT u.ID_Usuarios AS userId,
               u.Nombre AS name,
               u.Apellido AS lastName,
               u.Correo AS email,
               i.Estado_asistencia AS attendanceStatus,
               i.Puntos_ganados AS earnedPoints
        FROM INSCRIPCIONES i
        JOIN USUARIOS u ON u.ID_Usuarios = i.FK_Usuarios
        WHERE i.FK_Actividades = :activityId
        ORDER BY u.Nombre ASC
        """
    )
    abstract suspend fun getEnrolledUsersForActivity(activityId: Int): List<EnrolledUserRaw>

    @Query("UPDATE INSCRIPCIONES SET Estado_asistencia = 'COMPLETADA', Puntos_ganados = :points WHERE FK_Usuarios = :userId AND FK_Actividades = :activityId")
    abstract suspend fun updateEnrollmentAttendance(userId: Int, activityId: Int, points: Int)

    @Query("UPDATE USUARIO_PUNTOS SET Puntos_acumulados = Puntos_acumulados + :points, Ultima_actualizacion = :now WHERE FK_Usuarios = :userId")
    abstract suspend fun addPointsToUser(userId: Int, points: Int, now: Long)

    @Transaction
    open suspend fun markAttendanceAndGrantPoints(userId: Int, activityId: Int, points: Int) {
        updateEnrollmentAttendance(userId, activityId, points)
        addPointsToUser(userId, points, System.currentTimeMillis())
    }

    @Query("SELECT * FROM NIVELES ORDER BY Orden ASC")
    abstract suspend fun getAllLevels(): List<LevelEntity>

    @Query("UPDATE NIVELES SET Puntos_requeridos = :points WHERE ID_Niveles = :levelId")
    abstract suspend fun updateLevelPoints(levelId: Int, points: Int)

    @Query("SELECT COUNT(*) FROM USUARIOS WHERE FK_Rol = 2")
    abstract suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM MISIONES WHERE Estado = 'ACTIVA'")
    abstract suspend fun getActiveMissionsCount(): Int

    @Query("SELECT COUNT(*) FROM ACTIVIDADES WHERE Estado = 'ACTIVA'")
    abstract suspend fun getUpcomingActivitiesCount(): Int
}
