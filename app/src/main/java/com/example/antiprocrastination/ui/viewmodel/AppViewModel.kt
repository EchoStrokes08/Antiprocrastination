package com.example.antiprocrastination.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiprocrastination.domain.repository.SettingsRepository
import com.example.antiprocrastination.domain.model.AppUsageInfo
import com.example.antiprocrastination.domain.model.DailyStats
import com.example.antiprocrastination.domain.model.DistractionApp
import com.example.antiprocrastination.data.DistractionDao
import com.example.antiprocrastination.domain.model.PomodoroPhase
import com.example.antiprocrastination.domain.model.PomodoroState
import com.example.antiprocrastination.domain.model.Task
import com.example.antiprocrastination.data.TaskDao
import com.example.antiprocrastination.domain.repository.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * ViewModel principal de la aplicación.
 * Gestiona el estado de las tareas, la configuración, las estadísticas de uso y el temporizador Pomodoro.
 */
class AppViewModel(
    private val taskDao: TaskDao,
    private val distractionDao: DistractionDao,
    private val settingsRepository: SettingsRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    // ── Configuración (Settings) ──────────────────────────────────────────────

    private val _monitoringInterval = MutableStateFlow(settingsRepository.monitoringInterval)
    /** Intervalo de monitoreo en minutos */
    val monitoringInterval: StateFlow<Int> = _monitoringInterval.asStateFlow()

    fun setMonitoringInterval(minutes: Int) {
        _monitoringInterval.value = minutes
        settingsRepository.monitoringInterval = minutes
    }

    private val _notificationsEnabled = MutableStateFlow(settingsRepository.notificationsEnabled)
    /** Indica si las notificaciones están habilitadas */
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        settingsRepository.notificationsEnabled = enabled
    }

    private val _reminderMinutesBefore = MutableStateFlow(settingsRepository.reminderMinutesBefore)
    /** Minutos de recordatorio antes de una tarea */
    val reminderMinutesBefore: StateFlow<Int> = _reminderMinutesBefore.asStateFlow()

    fun setReminderMinutesBefore(minutes: Int) {
        _reminderMinutesBefore.value = minutes
        settingsRepository.reminderMinutesBefore = minutes
    }

    private val _youtubeLimitMin = MutableStateFlow(settingsRepository.youtubeLimitMin)
    /** Límite diario para YouTube en minutos */
    val youtubeLimitMin: StateFlow<Int> = _youtubeLimitMin.asStateFlow()

    fun setYoutubeLimitMin(minutes: Int) {
        _youtubeLimitMin.value = minutes
        settingsRepository.youtubeLimitMin = minutes
    }

    private val _tiktokLimitMin = MutableStateFlow(settingsRepository.tiktokLimitMin)
    /** Límite diario para TikTok en minutos */
    val tiktokLimitMin: StateFlow<Int> = _tiktokLimitMin.asStateFlow()

    fun setTiktokLimitMin(minutes: Int) {
        _tiktokLimitMin.value = minutes
        settingsRepository.tiktokLimitMin = minutes
    }

    /** Establece un límite de tiempo para una aplicación específica */
    fun setAppLimit(packageName: String, minutes: Int) {
        settingsRepository.setAppLimit(packageName, minutes)
        refreshUsageStats()
    }

    // ── Gestión de Tareas (Tasks) ──────────────────────────────────────────────

    /** Flujo de todas las tareas almacenadas en la base de datos */
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Añade una nueva tarea */
    fun addTask(name: String, dueDate: LocalDate, description: String) {
        viewModelScope.launch {
            taskDao.insertTask(Task(name = name, dueDate = dueDate, description = description))
            refreshUsageStats()
        }
    }

    /** Alterna el estado de completado de una tarea */
    fun toggleTask(id: Int) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == id }
            task?.let {
                val isCompleting = !it.completed
                taskDao.updateTask(it.copy(
                    completed = isCompleting,
                    completedDate = if (isCompleting) LocalDate.now() else null
                ))
                refreshUsageStats()
            }
        }
    }

    /** Elimina una tarea por su ID */
    fun deleteTask(id: Int) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == id }
            task?.let {
                taskDao.deleteTask(it)
                refreshUsageStats()
            }
        }
    }

    // ── Distracciones Aprendidas ─────────────────────────────────────────────

    /** Lista de aplicaciones que el sistema ha identificado como distracciones */
    val learnedDistractions: StateFlow<List<DistractionApp>> = distractionDao.getAllDistractions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Elimina una aplicación de la lista de distracciones aprendidas */
    fun removeDistraction(app: DistractionApp) {
        viewModelScope.launch {
            distractionDao.deleteDistraction(app)
            refreshUsageStats()
        }
    }

    // ── Estadísticas de Uso ──────────────────────────────────────────────────

    private val _appUsages = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    /** Información de uso de aplicaciones de distracción hoy */
    val appUsages: StateFlow<List<AppUsageInfo>> = _appUsages.asStateFlow()

    private val _weeklyStats = MutableStateFlow<List<DailyStats>>(emptyList())
    /** Estadísticas comparativas de la última semana */
    val weeklyStats: StateFlow<List<DailyStats>> = _weeklyStats.asStateFlow()

    /**
     * Refresca las estadísticas de uso obteniendo datos del UsageTracker.
     * Se ejecuta en un hilo de fondo (IO) para no bloquear la interfaz.
     */
    fun refreshUsageStats() {
        viewModelScope.launch {
            val currentTasks = tasks.value
            val learnedPkgs = learnedDistractions.value.map { it.packageName }.toSet()
            
            val (usage, weekly) = withContext(Dispatchers.IO) {
                val usageData = usageRepository.getDistractionAppsUsage(learnedPkgs).map {
                    val limit = settingsRepository.getAppLimit(it.packageName)
                    it.copy(limitMinutes = limit)
                }
                val weeklyData = usageRepository.getWeeklyStats(currentTasks, learnedPkgs)
                usageData to weeklyData
            }
            _appUsages.value = usage
            _weeklyStats.value = weekly
        }
    }

    init {
        refreshUsageStats()
    }

    // ── Temporizador Pomodoro ────────────────────────────────────────────────

    private val _pomodoro = MutableStateFlow(PomodoroState())
    /** Estado actual del temporizador Pomodoro */
    val pomodoro: StateFlow<PomodoroState> = _pomodoro.asStateFlow()

    private var timerJob: Job? = null

    /** Inicia el temporizador Pomodoro */
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

    /** Detiene el temporizador Pomodoro */
    fun stopPomodoro() {
        timerJob?.cancel()
        _pomodoro.update { it.copy(isRunning = false) }
    }

    /** Reinicia el temporizador al tiempo inicial de la fase actual */
    fun resetPomodoro() {
        stopPomodoro()
        _pomodoro.update {
            val total = if (it.phase == PomodoroPhase.WORK) 25 * 60 else 5 * 60
            it.copy(remainingSeconds = total, isRunning = false)
        }
    }

    /** Configura manualmente los minutos de la fase de trabajo */
    fun setWorkMinutes(minutes: Int) {
        stopPomodoro()
        val secs = minutes * 60
        _pomodoro.update { it.copy(totalSeconds = secs, remainingSeconds = secs) }
    }

    /** Cambia automáticamente entre la fase de trabajo y la de descanso */
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
}
