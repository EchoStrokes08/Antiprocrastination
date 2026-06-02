package com.example.antiprocrastination.domain.repository

import com.example.antiprocrastination.domain.model.AppUsageInfo
import com.example.antiprocrastination.domain.model.DailyStats
import com.example.antiprocrastination.domain.model.Task

interface UsageRepository {
    fun getDistractionAppsUsage(learnedPackages: Set<String> = emptySet()): List<AppUsageInfo>
    fun getWeeklyStats(tasks: List<Task>, learnedPackages: Set<String>): List<DailyStats>
    fun getForegroundPackage(): String?
    fun getActiveSessionMinutes(packageName: String): Int
    fun isDistraction(packageName: String): Boolean
    fun getAppName(packageName: String): String
}