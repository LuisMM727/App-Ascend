package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.iglesia.jovenes.domain.FeaturedMission
import com.iglesia.jovenes.domain.HomeActivity
import com.iglesia.jovenes.domain.HomeData
import com.iglesia.jovenes.domain.LeaderboardEntry
import com.iglesia.jovenes.domain.UserHeader

@Dao
abstract class HomeDao {
    @Query(
        """
        SELECT u.ID_Usuarios AS userId,
               TRIM(u.Nombre || ' ' || u.Apellido) AS fullName,
               COALESCE(up.Puntos_acumulados, 0) AS accumulatedPoints,
               COALESCE(n.Nombre, 'Líder Juvenil') AS levelName,
               COALESCE(n.Orden, 1) AS levelOrder,
               COALESCE(n.Puntos_requeridos, 100) AS requiredPoints
        FROM USUARIOS u
        LEFT JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        LEFT JOIN NIVELES n ON n.ID_Niveles = up.FK_Nivel
        WHERE u.ID_Usuarios = :userId AND u.Estado = 'ACTIVO'
        LIMIT 1
        """
    )
    abstract suspend fun userHeader(userId: Int): UserHeader?

    @Query(
        """
        SELECT m.ID_Misiones AS missionId, m.Nombre AS name,
               m.Descripcion AS description, m.Puntos_bonus AS bonusPoints,
               COUNT(DISTINCT CASE WHEN i.Estado_asistencia IN ('COMPLETADA', 'ASISTIO')
                     THEN ma.FK_Actividades END) AS completedActivities,
               COUNT(DISTINCT ma.FK_Actividades) AS totalActivities,
               m.Fecha_inicio AS startsAt,
               m.Fecha_fin AS endsAt
        FROM MISIONES m
        LEFT JOIN MISION_ACTIVIDADES ma ON ma.FK_Mision = m.ID_Misiones
        LEFT JOIN INSCRIPCIONES i ON i.FK_Actividades = ma.FK_Actividades
             AND i.FK_Usuarios = :userId
        WHERE m.Estado = 'ACTIVA'
        GROUP BY m.ID_Misiones
        ORDER BY m.Puntos_bonus DESC, m.Fecha_fin ASC
        """
    )
    abstract suspend fun activeMissions(userId: Int): List<FeaturedMission>

    @Query(
        """
        SELECT a.ID_Actividades AS activityId, a.Titulo AS title,
               a.Lugar AS location, a.Fecha AS date,
               a.Cupo_actual AS currentCapacity, a.Cupo_maximo AS maximumCapacity,
               (a.Puntos_base + a.Puntos_bonificacion) AS totalPoints,
               i.Estado_asistencia AS enrollmentStatus
        FROM ACTIVIDADES a
        LEFT JOIN INSCRIPCIONES i ON i.FK_Actividades = a.ID_Actividades
             AND i.FK_Usuarios = :userId
        WHERE a.Estado = 'ACTIVA'
        ORDER BY a.Fecha ASC LIMIT 10
        """
    )
    abstract suspend fun upcomingActivities(userId: Int): List<HomeActivity>

    @Query(
        """
        SELECT (
                   SELECT COUNT(*) + 1
                   FROM USUARIO_PUNTOS up2
                   WHERE up2.Puntos_acumulados > up.Puntos_acumulados
               ) AS position,
               u.ID_Usuarios AS userId,
               TRIM(u.Nombre || ' ' || u.Apellido) AS fullName,
               up.Puntos_acumulados AS points
        FROM USUARIOS u
        JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        WHERE u.Estado = 'ACTIVO'
        ORDER BY up.Puntos_acumulados DESC, u.ID_Usuarios ASC
        LIMIT 3
        """
    )
    abstract suspend fun leaderboard(): List<LeaderboardEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEnrollment(enrollment: EnrollmentEntity)

    @Query("DELETE FROM INSCRIPCIONES WHERE FK_Usuarios = :userId AND FK_Actividades = :activityId")
    abstract suspend fun deleteEnrollment(userId: Int, activityId: Int)

    @Query("UPDATE ACTIVIDADES SET Cupo_actual = Cupo_actual + 1 WHERE ID_Actividades = :activityId AND Cupo_actual < Cupo_maximo")
    abstract suspend fun incrementActivityCapacity(activityId: Int)

    @Query("UPDATE ACTIVIDADES SET Cupo_actual = MAX(0, Cupo_actual - 1) WHERE ID_Actividades = :activityId")
    abstract suspend fun decrementActivityCapacity(activityId: Int)

    @Transaction
    open suspend fun enrollUserInActivity(userId: Int, activityId: Int) {
        val enrollment = EnrollmentEntity(
            id = 0,
            userId = userId,
            activityId = activityId,
            enrolledAt = System.currentTimeMillis(),
            attendanceStatus = "INSCRITO",
            earnedPoints = 0,
        )
        insertEnrollment(enrollment)
        incrementActivityCapacity(activityId)
    }

    @Transaction
    open suspend fun cancelUserEnrollment(userId: Int, activityId: Int) {
        deleteEnrollment(userId, activityId)
        decrementActivityCapacity(activityId)
    }

    @Transaction
    open suspend fun home(userId: Int, now: Long): HomeData? {
        val header = userHeader(userId) ?: return null
        val missionsList = activeMissions(userId)
        return HomeData(
            header = header,
            featuredMission = missionsList.firstOrNull(),
            missions = missionsList,
            activities = upcomingActivities(userId),
            leaderboard = leaderboard(),
        )
    }
}
