package com.iglesia.jovenes.data

import com.iglesia.jovenes.data.local.HomeDao
import com.iglesia.jovenes.domain.HomeData
import com.iglesia.jovenes.domain.HomeRepository

/**
 * Implementación del repositorio para la pantalla principal (Home).
 * Coordina la carga de misiones destacadas, próximas actividades e inscripciones.
 */
class HomeRepositoryImpl(
    private val homeDao: HomeDao,
) : HomeRepository {

    /**
     * Obtiene todos los datos requeridos para la vista principal del joven.
     */
    override suspend fun getHome(userId: Int): Result<HomeData> = runCatching {
        homeDao.home(userId, System.currentTimeMillis())
            ?: error("No se encontraron datos para el usuario activo")
    }

    /**
     * Inscribe al usuario a la actividad especificada.
     */
    override suspend fun enrollInActivity(userId: Int, activityId: Int): Result<Unit> = runCatching {
        homeDao.enrollUserInActivity(userId, activityId)
    }

    /**
     * Cancela la inscripción del usuario a la actividad especificada.
     */
    override suspend fun cancelEnrollment(userId: Int, activityId: Int): Result<Unit> = runCatching {
        homeDao.cancelUserEnrollment(userId, activityId)
    }
}
