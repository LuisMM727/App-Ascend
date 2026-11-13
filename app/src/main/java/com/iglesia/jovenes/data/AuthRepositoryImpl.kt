package com.iglesia.jovenes.data

import com.iglesia.jovenes.data.local.AuthDao
import com.iglesia.jovenes.data.local.UserEntity
import com.iglesia.jovenes.data.local.UserPointsEntity
import com.iglesia.jovenes.domain.AuthRepository
import com.iglesia.jovenes.domain.AuthUser
import com.iglesia.jovenes.domain.OnboardingParams
import com.iglesia.jovenes.domain.RegisterParams
import com.iglesia.jovenes.domain.UserRole

class AuthRepositoryImpl(
    private val authDao: AuthDao,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthUser> = runCatching {
        val cleanEmail = email.trim().lowercase()
        var user = authDao.getUserByEmail(cleanEmail)

        // Autogeneración defensiva para Administrador por defecto si no existiera en la BD
        if (user == null && cleanEmail == "admin@iglesia.com" && password == "admin123") {
            val adminUser = UserEntity(
                id = 99,
                name = "Administrador",
                lastName = "General",
                email = "admin@iglesia.com",
                passwordHash = "admin123",
                status = "ACTIVO",
                failedAttempts = 0,
                blockedUntil = null,
                registeredAt = System.currentTimeMillis(),
                roleId = 1,
                church = "Iglesia Central",
                age = 30,
                phone = "0981000000",
                isProfileComplete = true,
            )
            authDao.insertUser(adminUser)
            user = adminUser
        }

        // Autogeneración defensiva para Usuario Joven por defecto si no existiera en la BD
        if (user == null && cleanEmail == "luis@email.com" && password == "123456") {
            val defaultUser = UserEntity(
                id = 1,
                name = "Luis",
                lastName = "Morinigo",
                email = "luis@email.com",
                passwordHash = "123456",
                status = "ACTIVO",
                failedAttempts = 0,
                blockedUntil = null,
                registeredAt = System.currentTimeMillis(),
                roleId = 2,
                church = "Iglesia Central",
                age = 22,
                phone = "0981123456",
                isProfileComplete = true,
            )
            authDao.insertUser(defaultUser)
            val userPoints = UserPointsEntity(
                id = 1,
                userId = 1,
                accumulatedPoints = 1200,
                levelId = 1,
                updatedAt = System.currentTimeMillis(),
            )
            authDao.insertUserPoints(userPoints)
            user = defaultUser
        }

        if (user == null) {
            error("No existe una cuenta registrada con el correo ingresado.")
        }

        if (user.passwordHash != password) {
            error("La contraseña ingresada es incorrecta.")
        }

        if (user.status != "ACTIVO") {
            error("La cuenta no se encuentra activa.")
        }

        user.toAuthUser()
    }

    override suspend fun register(params: RegisterParams): Result<AuthUser> = runCatching {
        val cleanEmail = params.email.trim().lowercase()

        if (cleanEmail.isEmpty() || params.name.isBlank() || params.lastName.isBlank()) {
            error("Por favor completa todos los campos obligatorios.")
        }

        if (params.password.length < 6) {
            error("La contraseña debe tener al menos 6 caracteres.")
        }

        val existingUser = authDao.getUserByEmail(cleanEmail)
        if (existingUser != null) {
            error("Ya existe una cuenta registrada con este correo electrónico.")
        }

        val newUserEntity = UserEntity(
            id = 0,
            name = params.name.trim(),
            lastName = params.lastName.trim(),
            email = cleanEmail,
            passwordHash = params.password,
            status = "ACTIVO",
            failedAttempts = 0,
            blockedUntil = null,
            registeredAt = System.currentTimeMillis(),
            roleId = 2, // Siempre 2 (JUVENIL). Los administradores no se pueden registrar públicamente.
            church = null,
            age = null,
            phone = null,
            isProfileComplete = false,
        )

        val insertedId = authDao.insertUser(newUserEntity).toInt()

        // Asignar puntos y nivel inicial
        val pointsEntity = UserPointsEntity(
            id = insertedId,
            userId = insertedId,
            accumulatedPoints = 100,
            levelId = 1,
            updatedAt = System.currentTimeMillis(),
        )
        authDao.insertUserPoints(pointsEntity)

        AuthUser(
            id = insertedId,
            name = newUserEntity.name,
            lastName = newUserEntity.lastName,
            email = newUserEntity.email,
            role = UserRole.JUVENIL,
            isProfileComplete = false,
        )
    }

    override suspend fun completeProfile(params: OnboardingParams): Result<AuthUser> = runCatching {
        if (params.church.isBlank()) {
            error("Debes especificar la iglesia a la que perteneces.")
        }
        if (params.age <= 0) {
            error("Ingresa una edad válida.")
        }

        authDao.updateUserProfile(
            userId = params.userId,
            church = params.church.trim(),
            age = params.age,
            phone = params.phone.trim().takeIf { it.isNotEmpty() },
        )

        val updatedUser = authDao.getUserById(params.userId)
            ?: error("No se encontró el usuario al actualizar.")

        updatedUser.toAuthUser()
    }

    private fun UserEntity.toAuthUser(): AuthUser {
        val role = if (roleId == 1) UserRole.ADMIN else UserRole.JUVENIL
        return AuthUser(
            id = id,
            name = name,
            lastName = lastName,
            email = email,
            role = role,
            isProfileComplete = isProfileComplete,
            church = church,
            age = age,
            phone = phone,
        )
    }
}
