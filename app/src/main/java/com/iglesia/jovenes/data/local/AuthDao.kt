package com.iglesia.jovenes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuthDao {
    @Query("SELECT * FROM USUARIOS WHERE LOWER(Correo) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM USUARIOS WHERE ID_Usuarios = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT Nombre FROM ROLES WHERE ID_Rol = :roleId LIMIT 1")
    suspend fun getRoleName(roleId: Int): String?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE USUARIOS SET Iglesia = :church, Edad = :age, Telefono = :phone, PerfilCompletado = 1 WHERE ID_Usuarios = :userId")
    suspend fun updateUserProfile(userId: Int, church: String, age: Int, phone: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPoints(userPoints: UserPointsEntity)
}
