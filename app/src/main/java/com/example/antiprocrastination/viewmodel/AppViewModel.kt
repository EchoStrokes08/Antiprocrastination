package com.example.antiprocrastination.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiprocrastination.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class AppViewModel : ViewModel() {

    // ── Tasks ──────────────────────────────────────────────────────────────────
    private val _tasks = MutableStateFlow(
        listOf(
            Task(1, "Lectura 4 ppc",       LocalDate.of(2026, 3,  9)),
            Task(2, "Taller 1 ppc",        LocalDate.of(2026, 3, 11)),
            Task(3, "Guia globalizacion",  LocalDate.of(2026, 4,  7), completed = true),
            Task(4, "Proyecto final PPC",  LocalDate.of(2026, 5, 20)),
        )
    )
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun addTask(name: String, dueDate: LocalDate, description: String) {
        val newId = (_tasks.value.maxOfOrNull { it.id } ?: 0) + 1
        _tasks.update { it + Task(newId, name, dueDate, description) }
    }

    fun toggleTask(id: Int) {
        _tasks.update { list ->
            list.map { if (it.id == id) it.copy(completed = !it.completed) else it }
        }
    }

    fun deleteTask(id: Int) {
        _tasks.update { it.filter { t -> t.id != id } }
    }

    // ── App Usage (stub – will use UsageStatsManager in sprint 2) ─────────────
    private val _appUsages = MutableStateFlow(
        listOf(
            AppUsageInfo("YouTube",   "com.google.android.youtube",    87, 60),
            AppUsageInfo("TikTok",    "com.zhiliaoapp.musically",       54, 30),
            AppUsageInfo("Facebook",  "com.facebook.katana",            32, 30),
            AppUsageInfo("Instagram", "com.instagram.android",          28, 30),
        )
    )
    val appUsages: StateFlow<List<AppUsageInfo>> = _appUsages.asStateFlow()

    // ── Pomodoro ───────────────────────────────────────────────────────────────
    private val _pomodoro = MutableStateFlow(PomodoroState())
    val pomodoro: StateFlow<PomodoroState> = _pomodoro.asStateFlow()

    private var timerJob: Job? = null

    fun startPomodoro() {
        if (_pomodoro.value.isRunning) return
        _pomodoro.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_pomodoro.value.remainingSeconds > 0 && _pomodoro.value.isRunning) {
                delay(1_000L)
                _pomodoro.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            if (_pomodoro.value.remainingSeconds == 0) {
                switchPhase()
            }
        }
    }

    fun stopPomodoro() {
        timerJob?.cancel()
        _pomodoro.update { it.copy(isRunning = false) }
    }

    fun resetPomodoro() {
        stopPomodoro()
        _pomodoro.update {
            val total = if (it.phase == PomodoroPhase.WORK) 25 * 60 else 5 * 60
            it.copy(remainingSeconds = total, isRunning = false)
        }
    }

    fun setWorkMinutes(minutes: Int) {
        stopPomodoro()
        val secs = minutes * 60
        _pomodoro.update { it.copy(totalSeconds = secs, remainingSeconds = secs) }
    }

    private fun switchPhase() {
        _pomodoro.update { state ->
            val newPhase = if (state.phase == PomodoroPhase.WORK) PomodoroPhase.REST else PomodoroPhase.WORK
            val newTotal = if (newPhase == PomodoroPhase.WORK) 25 * 60 else 5 * 60
            state.copy(
                phase = newPhase,
                totalSeconds = newTotal,
                remainingSeconds = newTotal,
                isRunning = false,
                completedSessions = if (newPhase == PomodoroPhase.REST) state.completedSessions + 1 else state.completedSessions
            )
        }
    }

    // ── Statistics (stub) ─────────────────────────────────────────────────────
    val weeklyStats: List<DailyStats> = listOf(
        DailyStats("Mon", 120, 45),
        DailyStats("Tue",  90, 80),
        DailyStats("Wed", 150, 30),
        DailyStats("Thu",  60, 90),
        DailyStats("Fri", 180, 20),
        DailyStats("Sat",  45, 120),
        DailyStats("Sun",  30, 60),
    )
}
