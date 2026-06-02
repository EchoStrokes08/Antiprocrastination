package com.example.antiprocrastination.domain.usage

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.antiprocrastination.data.AppDatabase
import com.example.antiprocrastination.data.SettingsManagerImpl
import com.example.antiprocrastination.data.UsageTrackerImpl
import com.example.antiprocrastination.domain.model.DistractionApp
import com.example.antiprocrastination.domain.notifications.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class UsageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val usageTrackerImpl = UsageTrackerImpl(context)
    private val notificationHelper = NotificationHelper(context)
    private val database = AppDatabase.getDatabase(context)
    private val settingsManagerImpl = SettingsManagerImpl(context)

    override suspend fun doWork(): Result {
        if (!settingsManagerImpl.notificationsEnabled) return Result.success()

        // 1. Obtener la app que está actualmente en pantalla (foreground)
        val currentPackage = usageTrackerImpl.getForegroundPackage() ?: run {
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
        val isDistraction = isLearned || usageTrackerImpl.isDistraction(currentPackage)
        
        Log.d("Antiprocrastination", "¿Es distracción? $isDistraction (Learned: $isLearned) para $currentPackage")

        if (isDistraction) {
            // Guardar en la base de datos de "aprendidas" si no estaba
            if (!isLearned) {
                distractionDao.insertDistraction(
                    DistractionApp(
                        packageName = currentPackage,
                        appName = usageTrackerImpl.getAppName(currentPackage)
                    )
                )
                Log.d("Antiprocrastination", "Nueva app de distracción guardada: $currentPackage")
            }
            // Usamos el nombre amigable centralizado
            val appName = usageTrackerImpl.getAppName(currentPackage)
            val sessionMinutes = usageTrackerImpl.getActiveSessionMinutes(currentPackage)

            // 1. Siempre mostrar la notificación de tiempo en la app (Notificación Tipo 1)
            notificationHelper.showSimpleNotification(
                "¡Alerta de Enfoque!",
                "Llevas $sessionMinutes min en $appName. ¡Cuidado con la distracción!"
            )

            // 2. Obtener tareas pendientes para la segunda notificación (Notificación Tipo 2)
            val tasksFlow = database.taskDao().getAllTasks()
            val allTasks = tasksFlow.first()
            val pendingTasks = allTasks
                .filter { !it.completed }
                .sortedBy { it.dueDate }
            
            if (pendingTasks.isNotEmpty()) {
                val today = LocalDate.now()
                val overdueTasks = pendingTasks.filter { it.dueDate.isBefore(today) }
                val overdueCount = overdueTasks.size

                if (overdueCount > 0) {
                    notificationHelper.showInteractiveNotification(
                        "Tareas Vencidas",
                        "Tienes $overdueCount tareas atrasadas. ¿Volvemos al trabajo?",
                        pendingTasks.size
                    )
                } else {
                    val nextTask = pendingTasks.first()
                    notificationHelper.showInteractiveNotification(
                        "Próximo Objetivo",
                        "Recuerdas la tarea: '${nextTask.name}'. ¡Ánimo, tú puedes, vamos por un git commit -mbono de productividad! ",
                        pendingTasks.size
                    )
                }
            }
        }

        return Result.success()
    }
}
