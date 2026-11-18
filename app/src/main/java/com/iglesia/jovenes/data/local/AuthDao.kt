package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO para operaciones de autenticación, verificación de correo, registro y onboarding.
 */
@Dao
interface AuthDao {
    /**
     * Busca un usuario por correo electrónico de manera insensible a mayúsculas y minúsculas.
     */
    @Query("SELECT * FROM USUARIOS WHERE LOWER(Correo) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    /**
     * Busca un usuario por su identificador único.
     */
    @Query("SELECT * FROM USUARIOS WHERE ID_Usuarios = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    /**
     * Obtiene el nombre del rol según el ID_Rol.
     */
    @Query("SELECT Nombre FROM ROLES WHERE ID_Rol = :roleId LIMIT 1")
    suspend fun getRoleName(roleId: Int): String?

    /**
     * Inserta un nuevo usuario en la base de datos o reemplaza sus datos si ya existía.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    /**
     * Actualiza los datos personales completados durante el onboarding (Iglesia, Edad, Teléfono).
     */
    @Query("UPDATE USUARIOS SET Iglesia = :church, Edad = :age, Telefono = :phone, PerfilCompletado = 1 WHERE ID_Usuarios = :userId")
    suspend fun updateUserProfile(userId: Int, church: String, age: Int, phone: String?)

    /**
     * Asigna el registro de puntos inicial al usuario tras registrarse.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPoints(userPoints: UserPointsEntity)
}
