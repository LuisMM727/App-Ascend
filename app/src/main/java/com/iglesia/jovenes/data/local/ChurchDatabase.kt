package com.iglesia.jovenes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        RoleEntity::class,
        UserEntity::class,
        UserPointsEntity::class,
        LevelEntity::class,
        MissionEntity::class,
        ActivityEntity::class,
        EnrollmentEntity::class,
        BadgeEntity::class,
        MissionActivityEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class ChurchDatabase : RoomDatabase() {

    abstract fun homeDao(): HomeDao
    abstract fun authDao(): AuthDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: ChurchDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ChurchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChurchDatabase::class.java,
                    "church_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(ChurchDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ChurchDatabaseCallback : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            populateDatabase(db)
        }

        private fun populateDatabase(db: SupportSQLiteDatabase) {
            try {
                // 1. Insertar Roles
                db.execSQL("INSERT OR IGNORE INTO ROLES (ID_Rol, Nombre) VALUES (1, 'ADMIN')")
                db.execSQL("INSERT OR IGNORE INTO ROLES (ID_Rol, Nombre) VALUES (2, 'JUVENIL')")

                // 2. Insertar Niveles iniciales
                db.execSQL("INSERT OR IGNORE INTO NIVELES (ID_Niveles, Nombre, Descripcion, Puntos_requeridos, Orden) VALUES (1, 'Líder Juvenil', 'Nivel inicial de crecimiento', 100, 1)")
                db.execSQL("INSERT OR IGNORE INTO NIVELES (ID_Niveles, Nombre, Descripcion, Puntos_requeridos, Orden) VALUES (2, 'Servidor Comprometido', 'Nivel intermedio', 500, 2)")
                db.execSQL("INSERT OR IGNORE INTO NIVELES (ID_Niveles, Nombre, Descripcion, Puntos_requeridos, Orden) VALUES (3, 'Embajador de Fe', 'Nivel avanzado', 1500, 3)")

                val now = System.currentTimeMillis()

                // 3. Insertar Usuario Administrador por defecto (FK_Rol = 1)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO USUARIOS (ID_Usuarios, Nombre, Apellido, Correo, Password, Estado, Intentos_fallidos, Bloqueado_hasta, Fecha_registro, FK_Rol, PerfilCompletado)
                    VALUES (99, 'Administrador', 'General', 'admin@iglesia.com', 'admin123', 'ACTIVO', 0, NULL, $now, 1, 1)
                    """
                )

                // 4. Insertar Usuario Juvenil por defecto (FK_Rol = 2)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO USUARIOS (ID_Usuarios, Nombre, Apellido, Correo, Password, Estado, Intentos_fallidos, Bloqueado_hasta, Fecha_registro, FK_Rol, Iglesia, Edad, Telefono, PerfilCompletado)
                    VALUES (1, 'Luis', 'Morinigo', 'luis@email.com', '123456', 'ACTIVO', 0, NULL, $now, 2, 'Iglesia Central', 22, '0981123456', 1)
                    """
                )

                // 5. Insertar Registro de Puntos asociado a Luis (FK_Usuarios = 1, FK_Nivel = 1)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO USUARIO_PUNTOS (ID_Usuarios_puntos, FK_Usuarios, Puntos_acumulados, FK_Nivel, Ultima_actualizacion)
                    VALUES (1, 1, 1200, 1, $now)
                    """
                )

                // 6. Misiones Semilla
                val nextMonth = now + (30L * 24 * 60 * 60 * 1000)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO MISIONES (ID_Misiones, Nombre, Descripcion, Puntos_bonus, Fecha_inicio, Fecha_fin, Estado)
                    VALUES (1, 'Misión Servicio Social', 'Ayuda a la comunidad en el comedor comunitario local', 250, $now, $nextMonth, 'ACTIVA')
                    """
                )

                // 7. Actividades Semilla
                val nextWeek = now + (7L * 24 * 60 * 60 * 1000)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO ACTIVIDADES (ID_Actividades, Titulo, Descripcion, Fecha, Lugar, Cupo_maximo, Cupo_actual, Puntos_base, Puntos_bonificacion, Estado, Fecha_publicacion, FK_Usuarios)
                    VALUES (1, 'Reunión de Jóvenes Ascend', 'Noche de adoración, palabra y convivencia', $nextWeek, 'Auditorio Principal', 50, 12, 100, 50, 'ACTIVA', $now, 99)
                    """
                )
            } catch (e: Exception) {
                // Ignore gracefully
            }
        }
    }
}
