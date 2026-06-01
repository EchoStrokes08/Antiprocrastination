package com.example.antiprocrastination.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.antiprocrastination.MainActivity
import com.example.antiprocrastination.R

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "antiprocrastination_reminders_v2"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Productividad",
                NotificationManager.IMPORTANCE_HIGH // Necesario para que sea flotante
            ).apply {
                description = "Breves avisos para mantener el enfoque"
                enableLights(true)
                enableVibration(false) // Sin vibración para ser menos intrusiva
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Notificación Tipo 1: Notificación estándar
    fun showSimpleNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    // Notificación Tipo 2: Flotante/Interactiva (Heads-up)
    fun showInteractiveNotification(title: String, message: String, pendingTasksCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOnlyAlertOnce(true) // No repetir sonido si ya está visible
            .addAction(android.R.drawable.ic_menu_view, "Abrir App", pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }
}
