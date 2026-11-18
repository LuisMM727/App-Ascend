package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Mapeo de usuario con puntos acumulados para las vistas del Administrador.
 */
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

/**
 * Mapeo de usuario inscrito a una actividad para el control de asistencia.
 */
data class EnrolledUserRaw(
    val userId: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val attendanceStatus: String,
    val earnedPoints: Int,
)

/**
 * DAO con consultas de administración para usuarios, misiones, actividades, métricas y niveles.
 */
@Dao
abstract class AdminDao {

    /**
     * Obtiene todos los usuarios registrados con su información de perfil y puntos acumulados.
     */
    @Query(
        """
        SELECT u.ID_Usuarios AS userId,
               COALESCE(u.Nombre, 'Usuario') AS name,
               COALESCE(u.Apellido, '') AS lastName,
               COALESCE(u.Correo, '') AS email,
               COALESCE(u.Estado, 'ACTIVO') AS status,
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

    /**
     * Actualiza el estado de una cuenta de usuario (ACTIVO / INACTIVO).
     */
    @Query("UPDATE USUARIOS SET Estado = :status WHERE ID_Usuarios = :userId")
    abstract suspend fun updateUserStatus(userId: Int, status: String)

    /**
     * Obtiene la lista de todas las misiones.
     */
    @Query("SELECT * FROM MISIONES ORDER BY ID_Misiones DESC")
    abstract suspend fun getAllMissions(): List<MissionEntity>

    /**
     * Inserta o reemplaza una misión en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMission(mission: MissionEntity): Long

    /**
     * Cambia el estado de una misión (ACTIVA / INACTIVA).
     */
    @Query("UPDATE MISIONES SET Estado = :status WHERE ID_Misiones = :missionId")
    abstract suspend fun updateMissionStatus(missionId: Int, status: String)

    /**
     * Obtiene la lista de todas las actividades.
     */
    @Query("SELECT * FROM ACTIVIDADES ORDER BY Fecha DESC")
    abstract suspend fun getAllActivities(): List<ActivityEntity>

    /**
     * Inserta o reemplaza una actividad comunitaria.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertActivity(activity: ActivityEntity): Long

    /**
     * Vincula automáticamente una nueva actividad con las misiones activas en el sistema.
     */
    @Query("INSERT OR IGNORE INTO MISION_ACTIVIDADES (FK_Mision, FK_Actividades) SELECT m.ID_Misiones, :activityId FROM MISIONES m WHERE m.Estado = 'ACTIVA'")
    abstract suspend fun linkActivityToActiveMissions(activityId: Int)

    /**
     * Cambia el estado de una actividad (ACTIVA / INACTIVA).
     */
    @Query("UPDATE ACTIVIDADES SET Estado = :status WHERE ID_Actividades = :activityId")
    abstract suspend fun updateActivityStatus(activityId: Int, status: String)

    /**
     * Obtiene los jóvenes inscritos a una actividad específica.
     */
    @Query(
        """
        SELECT u.ID_Usuarios AS userId,
               COALESCE(u.Nombre, 'Usuario') AS name,
               COALESCE(u.Apellido, '') AS lastName,
               COALESCE(u.Correo, '') AS email,
               COALESCE(i.Estado_asistencia, 'INSCRITO') AS attendanceStatus,
               COALESCE(i.Puntos_ganados, 0) AS earnedPoints
        FROM INSCRIPCIONES i
        JOIN USUARIOS u ON u.ID_Usuarios = i.FK_Usuarios
        WHERE i.FK_Actividades = :activityId
        ORDER BY u.Nombre ASC
        """
    )
    abstract suspend fun getEnrolledUsersForActivity(activityId: Int): List<EnrolledUserRaw>

    /**
     * Actualiza el estado de la inscripción a COMPLETADA al marcar la asistencia.
     */
    @Query("UPDATE INSCRIPCIONES SET Estado_asistencia = 'COMPLETADA', Puntos_ganados = :points WHERE FK_Usuarios = :userId AND FK_Actividades = :activityId")
    abstract suspend fun updateEnrollmentAttendance(userId: Int, activityId: Int, points: Int)

    /**
     * Suma los puntos ganados y actualiza automáticamente el nivel del usuario en una sola operación atómica.
     */
    @Query(
        """
        UPDATE USUARIO_PUNTOS 
        SET FK_Nivel = COALESCE(
            (SELECT n.ID_Niveles FROM NIVELES n WHERE (Puntos_acumulados + :points) >= n.Puntos_requeridos ORDER BY n.Orden DESC LIMIT 1),
            FK_Nivel
        ),
        Puntos_acumulados = Puntos_acumulados + :points, 
        Ultima_actualizacion = :now 
        WHERE FK_Usuarios = :userId
        """
    )
    abstract suspend fun addPointsToUser(userId: Int, points: Int, now: Long)

    /**
     * Operación transaccional completa para marcar asistencia y acreditar la experiencia (XP) al joven.
     */
    @Transaction
    open suspend fun markAttendanceAndGrantPoints(userId: Int, activityId: Int, points: Int) {
        updateEnrollmentAttendance(userId, activityId, points)
        addPointsToUser(userId, points, System.currentTimeMillis())
    }

    /**
     * Obtiene la lista de niveles configurados en el sistema.
     */
    @Query("SELECT * FROM NIVELES ORDER BY Orden ASC")
    abstract suspend fun getAllLevels(): List<LevelEntity>

    /**
     * Actualiza la meta de puntos requeridos para un nivel.
     */
    @Query("UPDATE NIVELES SET Puntos_requeridos = :points WHERE ID_Niveles = :levelId")
    abstract suspend fun updateLevelPoints(levelId: Int, points: Int)

    /**
     * Conteo total de usuarios con rol de jóvenes.
     */
    @Query("SELECT COUNT(*) FROM USUARIOS WHERE FK_Rol = 2")
    abstract suspend fun getUserCount(): Int

    /**
     * Conteo total de misiones activas.
     */
    @Query("SELECT COUNT(*) FROM MISIONES WHERE Estado = 'ACTIVA'")
    abstract suspend fun getActiveMissionsCount(): Int

    /**
     * Conteo total de eventos programados activos.
     */
    @Query("SELECT COUNT(*) FROM ACTIVIDADES WHERE Estado = 'ACTIVA'")
    abstract suspend fun getUpcomingActivitiesCount(): Int
}
