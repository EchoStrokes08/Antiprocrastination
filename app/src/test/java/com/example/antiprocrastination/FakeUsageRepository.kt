package com.example.antiprocrastination

import com.example.antiprocrastination.domain.repository.UsageRepository
import com.example.antiprocrastination.domain.model.AppUsageInfo
import com.example.antiprocrastination.domain.model.DailyStats
import com.example.antiprocrastination.domain.model.Task

class FakeUsageRepository : UsageRepository {
    var usageToReturn = listOf<AppUsageInfo>()
    override fun getDistractionAppsUsage(learnedPackages: Set<String>) = usageToReturn
    override fun getWeeklyStats(tasks: List<Task>, learnedPackages: Set<String>) = emptyList<DailyStats>()
    override fun getForegroundPackage(): String? = null
    override fun getActiveSessionMinutes(packageName: String) = 0
    override fun isDistraction(packageName: String) = false
    override fun getAppName(packageName: String) = "Fake App"
}