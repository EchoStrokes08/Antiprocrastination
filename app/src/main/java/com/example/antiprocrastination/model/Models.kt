package com.example.antiprocrastination.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate

// ── Task ─────────────────────────────────────────────────────────────────────
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dueDate: LocalDate,
    val description: String = "",
    val completed: Boolean = false,
    val completedDate: LocalDate? = null
) {
    /** Days remaining until due date (negative = overdue) */
    val daysRemaining: Long get() = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
}

// ── App Usage ─────────────────────────────────────────────────────────────────
data class AppUsageInfo(
    val appName: String,
    val packageName: String,
    val usageMinutes: Int,
    val limitMinutes: Int = 60,
    val lastTimeUsed: Long = 0
) {
    val usagePercent: Float get() = usageMinutes.toFloat() / limitMinutes.coerceAtLeast(1)
    val isOverLimit: Boolean get() = usageMinutes > limitMinutes

    /** Formato amigable de tiempo (ej: 1h 20m o 45m) */
    val timeFormatted: String get() {
        val h = usageMinutes / 60
        val m = usageMinutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
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

// ── Learned Distractions ──────────────────────────────────────────────────────
@Entity(tableName = "distraction_apps")
data class DistractionApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val detectedDate: Long = System.currentTimeMillis()
)

// Room Type Converters for LocalDate
class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }
}
