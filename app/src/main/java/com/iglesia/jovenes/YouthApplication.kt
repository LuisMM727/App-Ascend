package com.iglesia.jovenes

import android.app.Application
import com.iglesia.jovenes.data.AdminRepositoryImpl
import com.iglesia.jovenes.data.AuthRepositoryImpl
import com.iglesia.jovenes.data.HomeRepositoryImpl
import com.iglesia.jovenes.data.local.ChurchDatabase
import com.iglesia.jovenes.domain.AdminRepository
import com.iglesia.jovenes.domain.AuthRepository
import com.iglesia.jovenes.domain.HomeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class YouthApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    val database: ChurchDatabase by lazy {
        ChurchDatabase.getDatabase(this, applicationScope)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.authDao())
    }

    val homeRepository: HomeRepository by lazy {
        HomeRepositoryImpl(database.homeDao())
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(database.adminDao())
    }
}
