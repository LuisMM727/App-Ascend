package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Query

/**
 * Mapeo de datos crudos de una Misión con su conteo de actividades completadas.
 */
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

/**
 * Mapeo de datos de una Actividad/Tarea vinculada a una Misión específica.
 */
data class MissionActivityTaskRaw(
    val activityId: Int,
    val title: String,
    val description: String,
    val date: Long,
    val location: String,
    val totalPoints: Int,
    val enrollmentStatus: String?,
)

/**
 * DAO para consultas de la pantalla completa de Misiones y desglose de tareas asociadas.
 */
@Dao
interface MissionsDao {

    /**
     * Obtiene todas las misiones registradas calculando cuántas actividades ha completado el usuario.
     */
    @Query(
        """
        SELECT m.ID_Misiones AS missionId,
               COALESCE(m.Nombre, 'Misión') AS name,
               COALESCE(m.Descripcion, '') AS description,
               COALESCE(m.Puntos_bonus, 0) AS bonusPoints,
               COALESCE(m.Estado, 'ACTIVA') AS status,
               COALESCE(m.Fecha_inicio, 0) AS startsAt,
               COALESCE(m.Fecha_fin, 0) AS endsAt,
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

    /**
     * Obtiene la lista de actividades comunitarias vinculadas a una misión dada, agrupadas por ID único.
     */
    @Query(
        """
        SELECT a.ID_Actividades AS activityId,
               COALESCE(a.Titulo, 'Actividad') AS title,
               COALESCE(a.Descripcion, '') AS description,
               COALESCE(a.Fecha, 0) AS date,
               COALESCE(a.Lugar, 'Iglesia') AS location,
               (COALESCE(a.Puntos_base, 0) + COALESCE(a.Puntos_bonificacion, 0)) AS totalPoints,
               MAX(i.Estado_asistencia) AS enrollmentStatus
        FROM MISION_ACTIVIDADES ma
        JOIN ACTIVIDADES a ON a.ID_Actividades = ma.FK_Actividades
        LEFT JOIN INSCRIPCIONES i ON i.FK_Actividades = a.ID_Actividades AND i.FK_Usuarios = :userId
        WHERE ma.FK_Mision = :missionId AND a.Estado = 'ACTIVA'
        GROUP BY a.ID_Actividades
        ORDER BY a.Fecha ASC
        """
    )
    suspend fun getActivitiesForMission(missionId: Int, userId: Int): List<MissionActivityTaskRaw>
}
