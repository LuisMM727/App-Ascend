package com.iglesia.jovenes.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ROLES")
data class RoleEntity(
    @PrimaryKey @ColumnInfo(name = "ID_Rol") val id: Int,
    @ColumnInfo(name = "Nombre") val name: String,
)

@Entity(tableName = "USUARIOS", indices = [Index("Correo", unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID_Usuarios") val id: Int = 0,
    @ColumnInfo(name = "Nombre") val name: String,
    @ColumnInfo(name = "Apellido") val lastName: String,
    @ColumnInfo(name = "Correo") val email: String,
    @ColumnInfo(name = "Password") val passwordHash: String,
    @ColumnInfo(name = "Estado") val status: String = "ACTIVO",
    @ColumnInfo(name = "Intentos_fallidos") val failedAttempts: Int = 0,
    @ColumnInfo(name = "Bloqueado_hasta") val blockedUntil: Long? = null,
    @ColumnInfo(name = "Fecha_registro") val registeredAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "FK_Rol") val roleId: Int,
    @ColumnInfo(name = "Iglesia") val church: String? = null,
    @ColumnInfo(name = "Edad") val age: Int? = null,
    @ColumnInfo(name = "Telefono") val phone: String? = null,
    @ColumnInfo(name = "PerfilCompletado") val isProfileComplete: Boolean = false,
)

@Entity(tableName = "USUARIO_PUNTOS", indices = [Index("FK_Usuarios", unique = true)])
data class UserPointsEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID_Usuarios_puntos") val id: Int = 0,
    @ColumnInfo(name = "FK_Usuarios") val userId: Int,
    @ColumnInfo(name = "Puntos_acumulados") val accumulatedPoints: Int,
    @ColumnInfo(name = "FK_Nivel") val levelId: Int,
    @ColumnInfo(name = "Ultima_actualizacion") val updatedAt: Long,
)

@Entity(tableName = "NIVELES")
data class LevelEntity(
    @PrimaryKey @ColumnInfo(name = "ID_Niveles") val id: Int,
    @ColumnInfo(name = "Nombre") val name: String,
    @ColumnInfo(name = "Descripcion") val description: String,
    @ColumnInfo(name = "Puntos_requeridos") val requiredPoints: Int,
    @ColumnInfo(name = "Orden") val order: Int,
)

@Entity(tableName = "MISIONES", indices = [Index("Estado")])
data class MissionEntity(
    @PrimaryKey @ColumnInfo(name = "ID_Misiones") val id: Int,
    @ColumnInfo(name = "Nombre") val name: String,
    @ColumnInfo(name = "Descripcion") val description: String,
    @ColumnInfo(name = "Puntos_bonus") val bonusPoints: Int,
    @ColumnInfo(name = "Fecha_inicio") val startsAt: Long,
    @ColumnInfo(name = "Fecha_fin") val endsAt: Long,
    @ColumnInfo(name = "Estado") val status: String,
)

@Entity(tableName = "ACTIVIDADES", indices = [Index("Fecha"), Index("Estado")])
data class ActivityEntity(
    @PrimaryKey @ColumnInfo(name = "ID_Actividades") val id: Int,
    @ColumnInfo(name = "Titulo") val title: String,
    @ColumnInfo(name = "Descripcion") val description: String,
    @ColumnInfo(name = "Fecha") val date: Long,
    @ColumnInfo(name = "Lugar") val location: String,
    @ColumnInfo(name = "Cupo_maximo") val maxCapacity: Int,
    @ColumnInfo(name = "Cupo_actual") val currentCapacity: Int,
    @ColumnInfo(name = "Puntos_base") val basePoints: Int,
    @ColumnInfo(name = "Puntos_bonificacion") val bonusPoints: Int,
    @ColumnInfo(name = "Estado") val status: String,
    @ColumnInfo(name = "Fecha_publicacion") val publishedAt: Long,
    @ColumnInfo(name = "FK_Usuarios") val creatorUserId: Int,
)

@Entity(
    tableName = "INSCRIPCIONES",
    indices = [Index(value = ["FK_Usuarios", "FK_Actividades"], unique = true)],
)
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID_Inscripciones") val id: Int = 0,
    @ColumnInfo(name = "FK_Usuarios") val userId: Int,
    @ColumnInfo(name = "FK_Actividades") val activityId: Int,
    @ColumnInfo(name = "Fecha_inscripcion") val enrolledAt: Long,
    @ColumnInfo(name = "Estado_asistencia") val attendanceStatus: String,
    @ColumnInfo(name = "Puntos_ganados") val earnedPoints: Int,
)

@Entity(tableName = "INSIGNIAS")
data class BadgeEntity(
    @PrimaryKey @ColumnInfo(name = "ID_Insignias") val id: Int,
    @ColumnInfo(name = "Nombre") val name: String,
    @ColumnInfo(name = "Descripcion") val description: String,
    @ColumnInfo(name = "Imagen_url") val imageUrl: String?,
    @ColumnInfo(name = "Condicion_tipo") val conditionType: String,
    @ColumnInfo(name = "Condicion_valor") val conditionValue: Int,
)

@Entity(tableName = "MISION_ACTIVIDADES")
data class MissionActivityEntity(
    @PrimaryKey @ColumnInfo(name = "ID_Mision_actividades") val id: Int,
    @ColumnInfo(name = "FK_Mision") val missionId: Int,
    @ColumnInfo(name = "FK_Actividades") val activityId: Int,
)
