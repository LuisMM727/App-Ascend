package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Query

data class MissionWithProgressRaw(
    val missionId: Int,
    val name: String,
    val description: String,
    val bonusPoints: Int,
    val status: String,
    val startsAt: Long,
    val endsAt: Long,
    val completedActivities: Int,
    val totalActivities: Int,
)

data class MissionActivityTaskRaw(
    val activityId: Int,
    val title: String,
    val description: String,
    val date: Long,
    val location: String,
    val totalPoints: Int,
    val enrollmentStatus: String?,
)

@Dao
interface MissionsDao {
    @Query(
        """
        SELECT m.ID_Misiones AS missionId,
               m.Nombre AS name,
               m.Descripcion AS description,
               m.Puntos_bonus AS bonusPoints,
               m.Estado AS status,
               m.Fecha_inicio AS startsAt,
               m.Fecha_fin AS endsAt,
               COUNT(DISTINCT CASE WHEN i.Estado_asistencia IN ('COMPLETADA', 'ASISTIO') THEN ma.FK_Actividades END) AS completedActivities,
               COUNT(DISTINCT ma.FK_Actividades) AS totalActivities
        FROM MISIONES m
        LEFT JOIN MISION_ACTIVIDADES ma ON ma.FK_Mision = m.ID_Misiones
        LEFT JOIN INSCRIPCIONES i ON i.FK_Actividades = ma.FK_Actividades AND i.FK_Usuarios = :userId
        GROUP BY m.ID_Misiones
        ORDER BY m.Puntos_bonus DESC, m.Fecha_fin ASC
        """
    )
    suspend fun getAllMissionsWithProgress(userId: Int): List<MissionWithProgressRaw>

    @Query(
        """
        SELECT a.ID_Actividades AS activityId,
               a.Titulo AS title,
               a.Descripcion AS description,
               a.Fecha AS date,
               a.Lugar AS location,
               (a.Puntos_base + a.Puntos_bonificacion) AS totalPoints,
               i.Estado_asistencia AS enrollmentStatus
        FROM MISION_ACTIVIDADES ma
        JOIN ACTIVIDADES a ON a.ID_Actividades = ma.FK_Actividades
        LEFT JOIN INSCRIPCIONES i ON i.FK_Actividades = a.ID_Actividades AND i.FK_Usuarios = :userId
        WHERE ma.FK_Mision = :missionId AND a.Estado = 'ACTIVA'
        ORDER BY a.Fecha ASC
        """
    )
    suspend fun getActivitiesForMission(missionId: Int, userId: Int): List<MissionActivityTaskRaw>
}
