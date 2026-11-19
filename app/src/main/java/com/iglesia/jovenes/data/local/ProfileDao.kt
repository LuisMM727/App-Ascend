package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Query

/**
 * Mapeo de datos crudos del perfil del usuario, incluyendo nivel dinámico y meta de XP.
 */
data class UserProfileRaw(
    val userId: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val church: String?,
    val age: Int?,
    val phone: String?,
    val roleName: String,
    val accumulatedPoints: Int,
    val levelName: String,
    val levelOrder: Int,
    val requiredPoints: Int,
)

/**
 * Mapeo de datos de una Insignia/Logro con su estado de desbloqueo según la experiencia acumulada.
 */
data class BadgeRaw(
    val badgeId: Int,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val conditionType: String,
    val conditionValue: Int,
    val isUnlocked: Boolean,
)

/**
 * DAO para la gestión del Perfil del usuario, historial de actividades e insignias desbloqueadas.
 */
@Dao
interface ProfileDao {

    /**
     * Consulta detallada del perfil del usuario calculando su nivel actual y el objetivo del siguiente nivel.
     */
    @Query(
        """
        SELECT u.ID_Usuarios AS userId,
               u.Nombre AS name,
               u.Apellido AS lastName,
               u.Correo AS email,
               u.Iglesia AS church,
               u.Edad AS age,
               u.Telefono AS phone,
               COALESCE(r.Nombre, 'JUVENIL') AS roleName,
               COALESCE(up.Puntos_acumulados, 0) AS accumulatedPoints,
               COALESCE(
                   (SELECT n.Nombre FROM NIVELES n WHERE COALESCE(up.Puntos_acumulados, 0) >= n.Puntos_requeridos ORDER BY n.Orden DESC LIMIT 1),
                   'Líder Juvenil'
               ) AS levelName,
               COALESCE(
                   (SELECT n.Orden FROM NIVELES n WHERE COALESCE(up.Puntos_acumulados, 0) >= n.Puntos_requeridos ORDER BY n.Orden DESC LIMIT 1),
                   1
               ) AS levelOrder,
               COALESCE(
                   (SELECT n.Puntos_requeridos FROM NIVELES n WHERE n.Orden > (
                       SELECT COALESCE(n2.Orden, 1) FROM NIVELES n2 WHERE COALESCE(up.Puntos_acumulados, 0) >= n2.Puntos_requeridos ORDER BY n2.Orden DESC LIMIT 1
                   ) ORDER BY n.Orden ASC LIMIT 1),
                   (SELECT MAX(n3.Puntos_requeridos) FROM NIVELES n3),
                   100
               ) AS requiredPoints
        FROM USUARIOS u
        LEFT JOIN ROLES r ON r.ID_Rol = u.FK_Rol
        LEFT JOIN USUARIO_PUNTOS up ON up.FK_Usuarios = u.ID_Usuarios
        WHERE u.ID_Usuarios = :userId
        LIMIT 1
        """
    )
    suspend fun getUserProfileDetails(userId: Int): UserProfileRaw?

    /**
     * Conteo total de actividades completadas con asistencia confirmada.
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM INSCRIPCIONES
        WHERE FK_Usuarios = :userId AND Estado_asistencia IN ('COMPLETADA', 'ASISTIO')
        """
    )
    suspend fun getCompletedActivitiesCount(userId: Int): Int

    /**
     * Obtiene todas las insignias registradas indicando si el usuario ha alcanzado la meta de XP requerida.
     */
    @Query(
        """
        SELECT b.ID_Insignias AS badgeId,
               b.Nombre AS name,
               b.Descripcion AS description,
               b.Imagen_url AS imageUrl,
               b.Condicion_tipo AS conditionType,
               b.Condicion_valor AS conditionValue,
               CASE WHEN :userPoints >= b.Condicion_valor THEN 1 ELSE 0 END AS isUnlocked
        FROM INSIGNIAS b
        ORDER BY b.Condicion_valor ASC
        """
    )
    suspend fun getAllBadgesWithStatus(userPoints: Int): List<BadgeRaw>

    /**
     * Actualiza la información personal del usuario desde la pantalla de edición de perfil.
     */
    @Query("UPDATE USUARIOS SET Iglesia = :church, Edad = :age, Telefono = :phone WHERE ID_Usuarios = :userId")
    suspend fun updateUserPersonalDetails(userId: Int, church: String, age: Int, phone: String?)
}
