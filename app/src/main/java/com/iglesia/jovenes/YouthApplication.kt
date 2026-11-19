package com.iglesia.jovenes

import android.app.Application
import com.iglesia.jovenes.data.AdminRepositoryImpl
import com.iglesia.jovenes.data.AuthRepositoryImpl
import com.iglesia.jovenes.data.HomeRepositoryImpl
import com.iglesia.jovenes.data.LeaderboardRepositoryImpl
import com.iglesia.jovenes.data.MissionsRepositoryImpl
import com.iglesia.jovenes.data.ProfileRepositoryImpl
import com.iglesia.jovenes.data.local.ChurchDatabase
import com.iglesia.jovenes.domain.AdminRepository
import com.iglesia.jovenes.domain.AuthRepository
import com.iglesia.jovenes.domain.HomeRepository
import com.iglesia.jovenes.domain.LeaderboardRepository
import com.iglesia.jovenes.domain.MissionsRepository
import com.iglesia.jovenes.domain.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Clase principal de Application que gestiona el ciclo de vida global de la aplicación Ascend.
 * Proporciona inyección de dependencias diferida (lazy) para la Base de Datos y los Repositorios del sistema.
 */
class YouthApplication : Application() {

    /** Scope de corrutinas para tareas globales en hilos de I/O */
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    /** Instancia Singleton de la Base de Datos Room */
    val database: ChurchDatabase by lazy {
        ChurchDatabase.getDatabase(this, applicationScope)
    }

    /** Repositorio para la autenticación y onboarding de usuarios */
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.authDao())
    }

    /** Repositorio para la pantalla de inicio y actividades comunitarias */
    val homeRepository: HomeRepository by lazy {
        HomeRepositoryImpl(database.homeDao())
    }

    /** Repositorio para el panel de gestión del Administrador */
    val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(database.adminDao())
    }

    /** Repositorio para el perfil del usuario, edición e insignias */
    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(database.profileDao())
    }

    /** Repositorio para el catálogo de misiones y tareas */
    val missionsRepository: MissionsRepository by lazy {
        MissionsRepositoryImpl(database.missionsDao())
    }

    /** Repositorio para la tabla de clasificación y ranking de líderes */
    val leaderboardRepository: LeaderboardRepository by lazy {
        LeaderboardRepositoryImpl(database.leaderboardDao())
    }
}
