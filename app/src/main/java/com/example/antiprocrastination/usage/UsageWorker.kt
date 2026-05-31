package com.example.antiprocrastination.usage

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.antiprocrastination.model.AppDatabase
import com.example.antiprocrastination.data.SettingsManager
import com.example.antiprocrastination.notifications.NotificationHelper
import kotlinx.coroutines.flow.first

class UsageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val usageTracker = UsageTracker(context)
    private val notificationHelper = NotificationHelper(context)
    private val database = AppDatabase.getDatabase(context)
    private val settingsManager = SettingsManager(context)

    override suspend fun doWork(): Result {
        if (!settingsManager.notificationsEnabled) return Result.success()

        // 1. Obtener la app que está actualmente en pantalla (foreground)
        val currentPackage = usageTracker.getForegroundPackage() ?: run {
            Log.d("Antiprocrastination", "No se detectó ninguna app en primer plano")
            return Result.success()
        }

        Log.d("Antiprocrastination", "App detectada en pantalla: $currentPackage")

        // 2. Si la app en primer plano es la nuestra, NO mandamos notificación
        if (currentPackage == applicationContext.packageName) {
            Log.d("Antiprocrastination", "El usuario está dentro de la app Antiprocrastination")
            return Result.success()
        }

        // 3. Verificar si la app en primer plano es una distracción
        val distractionDao = database.distractionDao()
        val isLearned = distractionDao.isLearnedDistraction(currentPackage)
        val isDistraction = isLearned || usageTracker.isDistraction(currentPackage)
        
        Log.d("Antiprocrastination", "¿Es distracción? $isDistraction (Learned: $isLearned) para $currentPackage")

        if (isDistraction) {
            // Guardar en la base de datos de "aprendidas" si no estaba
            if (!isLearned) {
                distractionDao.insertDistraction(
                    com.example.antiprocrastination.model.DistractionApp(
                        packageName = currentPackage,
                        appName = usageTracker.getAppName(currentPackage)
                    )
                )
                Log.d("Antiprocrastination", "Nueva app de distracción guardada: $currentPackage")
            }
            val distractionApps = usageTracker.getDistractionAppsUsage()
            val currentAppUsage = distractionApps.find { it.packageName == currentPackage }
            
            // Usamos el nombre amigable centralizado
            val appName = usageTracker.getAppName(currentPackage)
            val minutes = currentAppUsage?.usageMinutes ?: 0

            // 4. Obtener tareas pendientes
            val tasksFlow = database.taskDao().getAllTasks()
            val allTasks = tasksFlow.first()
            val pendingTasks = allTasks
                .filter { !it.completed }
                .sortedBy { it.dueDate }
            
            Log.d("Antiprocrastination", "Tareas pendientes encontradas: ${pendingTasks.size}")
            
            if (pendingTasks.isNotEmpty()) {
                val nextTask = pendingTasks.first()
                
                notificationHelper.showSimpleNotification(
                    "¡Momento de enfoque!", 
                    "Estás usando $appName ($minutes min). Recuerda que tienes pendiente: ${nextTask.name}"
                )
                
                notificationHelper.showInteractiveNotification(
                    "Pausa de productividad",
                    "Tienes ${pendingTasks.size} tareas esperando. ¿Dejamos $appName por ahora?",
                    pendingTasks.size
                )
            } else {
                Log.d("Antiprocrastination", "No hay tareas pendientes, no se envía notificación")
            }
        }

        return Result.success()
    }
}
