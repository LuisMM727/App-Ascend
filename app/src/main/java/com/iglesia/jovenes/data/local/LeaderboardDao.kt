package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Query

/**
 * Mapeo de datos crudos para un puesto en la tabla de clasificación.
 */
data class LeaderboardEntryRaw(
    val position: Int,
    val userId: Int,
    val fullName: String,
    val church: String?,
    val levelName: String,
    val points: Int,
)

/**
 * DAO para consultas del Ranking y Tabla de Clasificación de líderes.
 */
@Dao
interface LeaderboardDao {

    /**
     * Obtiene el ranking completo de jóvenes ordenados por puntos XP descendente.
     */
    @Query(
        """
        SELECT (
                   SELECT COUNT(*) + 1
                   FROM USUARIO_PUNTOS up2
                   WHERE up2.Puntos_acumulados > up.Puntos_acumulados
               ) AS position,
               u.ID_Usuarios AS userId,
               TRIM(COALESCE(u.Nombre, 'Usuario') || ' ' || COALESCE(u.Apellido, '')) AS fullName,
               u.Iglesia AS church,
               COALESCE(
                   (SELECT n.Nombre FROM NIVELES n WHERE COALESCE(up.Puntos_acumulados, 0) >= n.Puntos_requeridos ORDER BY n.Orden DESC LIMIT 1),
                   'Líder Juvenil'
               ) AS levelName,
               COALESCE(up.Puntos_acumulados, 0) AS points
        FROM USUARIOS u
        JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        WHERE u.Estado = 'ACTIVO' AND u.FK_Rol = 2
        ORDER BY up.Puntos_acumulados DESC, u.ID_Usuarios ASC
        LIMIT 50
        """
    )
    suspend fun getFullLeaderboardRaw(): List<LeaderboardEntryRaw>

    /**
     * Obtiene la posición exacta del usuario actual dentro de la clasificación global.
     */
    @Query(
        """
        SELECT (
                   SELECT COUNT(*) + 1
                   FROM USUARIO_PUNTOS up2
                   WHERE up2.Puntos_acumulados > up.Puntos_acumulados
               ) AS position,
               u.ID_Usuarios AS userId,
               TRIM(COALESCE(u.Nombre, 'Usuario') || ' ' || COALESCE(u.Apellido, '')) AS fullName,
               u.Iglesia AS church,
               COALESCE(
                   (SELECT n.Nombre FROM NIVELES n WHERE COALESCE(up.Puntos_acumulados, 0) >= n.Puntos_requeridos ORDER BY n.Orden DESC LIMIT 1),
                   'Líder Juvenil'
               ) AS levelName,
               COALESCE(up.Puntos_acumulados, 0) AS points
        FROM USUARIOS u
        JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        WHERE u.ID_Usuarios = :userId AND u.Estado = 'ACTIVO'
        LIMIT 1
        """
    )
    suspend fun getUserRankRaw(userId: Int): LeaderboardEntryRaw?
}
