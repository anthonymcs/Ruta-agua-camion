package com.rutaagua.camion.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rutaagua.camion.R

/**
 * Gestor de notificaciones nativas en tiempo real para alertar entregas
 * realizadas por el otro vehículo en ruta (Camión vs Camioneta).
 */
object RutaNotificationHelper {

    private const val CHANNEL_ID = "ruta_actualizaciones_channel"
    private const val CHANNEL_NAME = "Actualizaciones de Ruta"
    private const val CHANNEL_DESC = "Alertas en tiempo real cuando el otro vehículo atiende a un cliente"

    fun crearCanalSiEsNecesario(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarNotificacionRuta(context: Context, titulo: String, mensaje: String) {
        crearCanalSiEsNecesario(context)

        // En Android 13 (Tiramisu)+ se requiere permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            if (permiso != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            notificationManager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permiso denegado por el usuario
        } catch (_: Exception) {
            // Ignorar errores inesperados de notificación para no afectar el flujo offline
        }
    }
}
