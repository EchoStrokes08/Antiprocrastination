package com.example.antiprocrastination.domain.repository

interface SettingsRepository {
    var monitoringInterval: Int
    var notificationsEnabled: Boolean
    var reminderMinutesBefore: Int
    var youtubeLimitMin: Int
    var tiktokLimitMin: Int

    fun getAppLimit(packageName: String): Int
    fun setAppLimit(packageName: String, limitMinutes: Int)
}