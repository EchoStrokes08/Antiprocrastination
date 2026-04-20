package com.example.antiprocrastination.model

import java.time.LocalDate

// ── Task ─────────────────────────────────────────────────────────────────────
data class Task(
    val id: Int,
    val name: String,
    val dueDate: LocalDate,
    val description: String = "",
    val completed: Boolean = false
) {
    /** Days remaining until due date (negative = overdue) */
    val daysRemaining: Long get() = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
}

// ── App Usage ─────────────────────────────────────────────────────────────────
data class AppUsageInfo(
    val appName: String,
    val packageName: String,
    val usageMinutes: Int,
    val limitMinutes: Int = 60
) {
    val usagePercent: Float get() = usageMinutes.toFloat() / limitMinutes.coerceAtLeast(1)
    val isOverLimit: Boolean get() = usageMinutes > limitMinutes
}

// ── Pomodoro State ─────────────────────────────────────────────────────────────
enum class PomodoroPhase { WORK, REST }

data class PomodoroState(
    val phase: PomodoroPhase = PomodoroPhase.WORK,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0
) {
    val progressFraction: Float
        get() = 1f - remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1)
    val minutesDisplay: String
        get() = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)
}

// ── Statistics ─────────────────────────────────────────────────────────────────
data class DailyStats(
    val day: String,          // "Mon", "Tue", …
    val productiveMinutes: Int,
    val distractionMinutes: Int
)
