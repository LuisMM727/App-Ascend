package com.iglesia.jovenes.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.iglesia.jovenes.R

/**
 * Objeto utilitario encargado de crear canales de notificación de Android y enviar avisos locales
 * para la confirmación de inscripciones a eventos y acreditación de puntos de experiencia (XP).
 */
object NotificationHelper {

    /** Identificador único del canal de notificaciones */
    const val CHANNEL_ID = "ascend_events_channel"
    private const val CHANNEL_NAME = "Eventos y Misiones Ascend"
    private const val CHANNEL_DESC = "Avisos de inscripciones a eventos y acreditación de puntos XP"

    /**
     * Crea el canal de notificaciones en Android 8.0 (API 26) o superior.
     */
    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESC
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        } catch (t: Throwable) {
            // Protección contra excepciones en la creación del canal
        }
    }

    /**
     * Dispara una notificación nativa cuando el joven se inscribe exitosamente a una actividad.
     */
    @SuppressLint("MissingPermission")
    fun sendEnrollmentNotification(context: Context, activityTitle: String, location: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("¡Inscripción Confirmada! 📌")
                .setContentText("Te has inscrito a '$activityTitle' en $location.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Te has inscrito exitosamente a '$activityTitle'. Te esperamos en $location. ¡Sigue sumando experiencia y creciendo!"),
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (t: Throwable) {
            // Protección defensiva contra errores de permisos en tiempo de ejecución
        }
    }

    /**
     * Dispara una notificación nativa cuando el Administrador valida la asistencia del joven y le otorga puntos XP.
     */
    @SuppressLint("MissingPermission")
    fun sendXpGainedNotification(context: Context, points: Int, activityTitle: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("¡Has ganado +$points XP! 🌟")
                .setContentText("Se han acreditado +$points XP a tu cuenta por '$activityTitle'.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("¡Felicitaciones! Se han acreditado +$points XP a tu cuenta por tu participación en '$activityTitle'. ¡Sigue subiendo de nivel en Ascend!"),
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (t: Throwable) {
            // Protección defensiva contra errores de permisos en tiempo de ejecución
        }
    }
}
