package com.example.antiprocrastination.domain.usage

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

import com.example.antiprocrastination.data.SettingsManagerImpl

class UsageAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Ejecutar el worker una vez
        val workRequest = OneTimeWorkRequestBuilder<UsageWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

        // Re-programar usando el valor actual de SettingsManager
        val settingsManagerImpl = SettingsManagerImpl(context)
        val intervalMinutes = settingsManagerImpl.monitoringInterval

        if (intervalMinutes > 0 && intervalMinutes < 15) {
            scheduleNextAlarm(context, intervalMinutes)
        }
    }

    private fun scheduleNextAlarm(context: Context, minutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UsageAlarmReceiver::class.java).apply {
            putExtra("interval_minutes", minutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}
