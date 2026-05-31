package com.example.antiprocrastination.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_MONITORING_INTERVAL = "monitoring_interval"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_REMINDER_MINUTES = "reminder_minutes"
        const val KEY_YOUTUBE_LIMIT = "youtube_limit"
        const val KEY_TIKTOK_LIMIT = "tiktok_limit"
    }

    var monitoringInterval: Int
        get() = prefs.getInt(KEY_MONITORING_INTERVAL, 5)
        set(value) = prefs.edit().putInt(KEY_MONITORING_INTERVAL, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var reminderMinutesBefore: Int
        get() = prefs.getInt(KEY_REMINDER_MINUTES, 30)
        set(value) = prefs.edit().putInt(KEY_REMINDER_MINUTES, value).apply()

    var youtubeLimitMin: Int
        get() = prefs.getInt(KEY_YOUTUBE_LIMIT, 60)
        set(value) = prefs.edit().putInt(KEY_YOUTUBE_LIMIT, value).apply()

    var tiktokLimitMin: Int
        get() = prefs.getInt(KEY_TIKTOK_LIMIT, 30)
        set(value) = prefs.edit().putInt(KEY_TIKTOK_LIMIT, value).apply()

    fun getAppLimit(packageName: String): Int {
        // Fallback a los valores específicos existentes para no romper compatibilidad
        if (packageName.contains("youtube")) return youtubeLimitMin
        if (packageName.contains("musically")) return tiktokLimitMin
        
        return prefs.getInt("limit_$packageName", 60)
    }

    fun setAppLimit(packageName: String, limitMinutes: Int) {
        if (packageName.contains("youtube")) youtubeLimitMin = limitMinutes
        else if (packageName.contains("musically")) tiktokLimitMin = limitMinutes
        else prefs.edit().putInt("limit_$packageName", limitMinutes).apply()
    }
}
