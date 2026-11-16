package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Query

data class LeaderboardEntryRaw(
    val position: Int,
    val userId: Int,
    val fullName: String,
    val church: String?,
    val levelName: String,
    val points: Int,
)

@Dao
interface LeaderboardDao {
    @Query(
        """
        SELECT (
                   SELECT COUNT(*) + 1
                   FROM USUARIO_PUNTOS up2
                   WHERE up2.Puntos_acumulados > up.Puntos_acumulados
               ) AS position,
               u.ID_Usuarios AS userId,
               TRIM(u.Nombre || ' ' || u.Apellido) AS fullName,
               u.Iglesia AS church,
               COALESCE(n.Nombre, 'Líder Juvenil') AS levelName,
               COALESCE(up.Puntos_acumulados, 0) AS points
        FROM USUARIOS u
        JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        LEFT JOIN NIVELES n ON n.ID_Niveles = up.FK_Nivel
        WHERE u.Estado = 'ACTIVO' AND u.FK_Rol = 2
        ORDER BY up.Puntos_acumulados DESC, u.ID_Usuarios ASC
        LIMIT 50
        """
    )
    suspend fun getFullLeaderboardRaw(): List<LeaderboardEntryRaw>

    @Query(
        """
        SELECT (
                   SELECT COUNT(*) + 1
                   FROM USUARIO_PUNTOS up2
                   WHERE up2.Puntos_acumulados > up.Puntos_acumulados
               ) AS position,
               u.ID_Usuarios AS userId,
               TRIM(u.Nombre || ' ' || u.Apellido) AS fullName,
               u.Iglesia AS church,
               COALESCE(n.Nombre, 'Líder Juvenil') AS levelName,
               COALESCE(up.Puntos_acumulados, 0) AS points
        FROM USUARIOS u
        JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        LEFT JOIN NIVELES n ON n.ID_Niveles = up.FK_Nivel
        WHERE u.ID_Usuarios = :userId AND u.Estado = 'ACTIVO'
        LIMIT 1
        """
    )
    suspend fun getUserRankRaw(userId: Int): LeaderboardEntryRaw?
}
