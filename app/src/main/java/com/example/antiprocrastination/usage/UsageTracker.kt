package com.example.antiprocrastination.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.example.antiprocrastination.model.AppUsageInfo
import com.example.antiprocrastination.model.DailyStats
import com.example.antiprocrastination.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Clase de utilidad para rastrear y categorizar el uso de aplicaciones.
 * Se encarga de identificar aplicaciones de distracción y agregar estadísticas de uso.
 */
class UsageTracker(private val context: Context) {

    private val tag = "UsageTracker"

    // Paquetes identificados manualmente como distracciones (Entretenimiento/Redes Sociales)
    private val manualDistractionPackages = setOf(
        "com.google.android.youtube",
        "com.zhiliaoapp.musically", // TikTok
        "com.facebook.katana",
        "com.instagram.android",
        "com.twitter.android",
        "com.netflix.mediaclient",
        "com.disney.disneyplus",
        "com.whatsapp"
    )

    // Paquetes que NUNCA deben ser considerados distracciones (Productividad/Estudio)
    private val productivePackages = setOf(
        "com.google.android.apps.docs", // Drive
        "com.google.android.apps.pdfviewer",
        "com.microsoft.office.word",
        "com.microsoft.office.excel",
        "com.google.android.calendar",
        "com.duolingo",
        "com.khanacademy.android",
        "org.coursera.android",
        "com.google.android.gm", // Gmail
        "com.notion.id",
        "com.evernote",
        "com.android.chrome",
        "com.spotify.music"
    )

    // Caché en memoria para evitar llamadas costosas y repetidas al PackageManager
    private val distractionCache = mutableMapOf<String, Boolean>()

    /**
     * Determina si un paquete dado se considera una distracción.
     * Utiliza una verificación de varios pasos: app propia, lista blanca, lista manual y categoría del sistema.
     *
     * @param packageName El nombre del paquete a analizar.
     * @return True si es una distracción, false de lo contrario.
     */
    fun isDistraction(packageName: String): Boolean {
        // 0. Primero revisar la caché
        distractionCache[packageName]?.let { return it }

        // 1. La propia aplicación no es una distracción
        if (packageName == context.packageName) {
            distractionCache[packageName] = false
            return false
        }

        // 2. Aplicaciones explícitamente productivas
        if (packageName in productivePackages) {
            distractionCache[packageName] = false
            return false
        }
        
        // 3. Distracciones manuales conocidas
        if (packageName in manualDistractionPackages) {
            distractionCache[packageName] = true
            return true
        }
        
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            
            // Categorías automáticas del sistema (Android O+)
            val category = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) appInfo.category else -1
            val isGameFlag = (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            
            // 4. Categorización del sistema (Juegos, Social, Video)
            if (category == ApplicationInfo.CATEGORY_GAME || 
                category == ApplicationInfo.CATEGORY_SOCIAL || 
                category == ApplicationInfo.CATEGORY_VIDEO ||
                isGameFlag) {
                distractionCache[packageName] = true
                return true
            }

            // 5. Verificación de lanzador: Si es una app que el usuario abre y no es una categoría productiva conocida
            val hasLauncher = pm.getLaunchIntentForPackage(packageName) != null
            val isProductiveCategory = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                category == ApplicationInfo.CATEGORY_MAPS || category == ApplicationInfo.CATEGORY_PRODUCTIVITY
            } else false

            val result = hasLauncher && !isProductiveCategory
            distractionCache[packageName] = result
            result
        } catch (e: PackageManager.NameNotFoundException) {
            // El paquete no es accesible o fue desinstalado
            distractionCache[packageName] = false
            false
        } catch (e: Exception) {
            Log.e(tag, "Error al analizar el paquete $packageName", e)
            false
        }
    }

    /**
     * Obtiene un nombre legible para un paquete.
     * Devuelve un mapeo manual para apps comunes o consulta al PackageManager.
     */
    fun getAppName(packageName: String): String {
        val manualNames = mapOf(
            "com.zhiliaoapp.musically" to "TikTok",
            "com.google.android.youtube" to "YouTube",
            "com.facebook.katana" to "Facebook",
            "com.instagram.android" to "Instagram",
            "com.twitter.android" to "Twitter",
            "com.spotify.music" to "Spotify",
            "com.netflix.mediaclient" to "Netflix",
            "com.disney.disneyplus" to "Disney+",
            "com.whatsapp" to "WhatsApp",
            "com.android.chrome" to "Chrome"
        )

        manualNames[packageName]?.let { return it }

        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            // Reintento capitalizando la última parte del nombre del paquete
            packageName.split(".").last().lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    /**
     * Devuelve información de uso para todas las aplicaciones identificadas como distracciones en el día actual.
     * Utiliza queryUsageStats como base y lo complementa con eventos recientes para máxima precisión.
     */
    fun getDistractionAppsUsage(learnedPackages: Set<String> = emptySet()): List<AppUsageInfo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        // 1. Obtener estadísticas base (el sistema las agrega periódicamente)
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        )

        val consolidatedStats = mutableMapOf<String, Long>()
        stats.forEach {
            if (it.totalTimeInForeground > 0) {
                // Nos quedamos con el valor máximo reportado para el paquete hoy
                val current = consolidatedStats[it.packageName] ?: 0L
                if (it.totalTimeInForeground > current) {
                    consolidatedStats[it.packageName] = it.totalTimeInForeground
                }
            }
        }

        // 2. CORRECCIÓN DE PRECISIÓN: Si una app está abierta AHORA, queryUsageStats podría estar desactualizado.
        // Verificamos los eventos de los últimos 30 minutos para ver si hay una sesión activa.
        val usageEvents = usageStatsManager.queryEvents(now - 1000 * 60 * 30, now)
        val event = android.app.usage.UsageEvents.Event()
        val lastEvents = mutableMapOf<String, Int>()
        val lastEventTime = mutableMapOf<String, Long>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            lastEvents[event.packageName] = event.eventType
            lastEventTime[event.packageName] = event.timeStamp
        }

        // Si el último evento de una app fue RESUMED, sumamos el tiempo transcurrido desde entonces
        lastEvents.forEach { (pkg, type) ->
            if (type == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                val startTime = lastEventTime[pkg] ?: now
                val extraTime = now - startTime
                if (extraTime > 0) {
                    consolidatedStats[pkg] = (consolidatedStats[pkg] ?: 0L) + extraTime
                }
            }
        }

        return consolidatedStats.filter { (packageName, _) ->
            packageName != context.packageName && 
            packageName !in productivePackages &&
            (isDistraction(packageName) || packageName in learnedPackages)
        }
            .map { (packageName, totalTime) ->
                AppUsageInfo(
                    appName = getAppName(packageName),
                    packageName = packageName,
                    usageMinutes = (totalTime / 60000).toInt()
                )
            }.filter { it.usageMinutes > 0 }
            .sortedByDescending { it.usageMinutes }
    }

    /**
     * Identifica el nombre del paquete que está actualmente en primer plano.
     */
    fun getForegroundPackage(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 5 // Últimos 5 minutos
        
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = android.app.usage.UsageEvents.Event()
        var lastPackage: String? = null
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                lastPackage = event.packageName
            }
        }
        
        // Respaldo a queryUsageStats si los eventos no proporcionan un resultado claro
        if (lastPackage == null) {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            )
            lastPackage = stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        }
        
        return lastPackage
    }

    /**
     * Obtiene los minutos exactos que una aplicación ha estado en primer plano desde su última activación
     * (o el total del día si es más relevante para el contexto de la notificación).
     * @param packageName Nombre del paquete.
     * @return Minutos de uso en la sesión actual o reciente.
     */
    fun getActiveSessionMinutes(packageName: String): Int {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 // Mirar la última hora
        
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = android.app.usage.UsageEvents.Event()
        
        var lastResumedTime = 0L
        var totalSessionTime = 0L
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.packageName == packageName) {
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED -> {
                        lastResumedTime = event.timeStamp
                    }
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                    android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED -> {
                        if (lastResumedTime != 0L) {
                            totalSessionTime += (event.timeStamp - lastResumedTime)
                            lastResumedTime = 0L
                        }
                    }
                }
            }
        }
        
        // Si sigue en primer plano (no hubo evento de pausa/parada después del último RESUMED)
        if (lastResumedTime != 0L) {
            totalSessionTime += (endTime - lastResumedTime)
        }
        
        return (totalSessionTime / 60000).toInt().coerceAtLeast(1)
    }

    /**
     * Agrega el uso de distracciones frente al productivo para la semana actual (Dom-Sab).
     * Muestra valores reales para días pasados y hoy, y 0 para días futuros.
     * Incluye bonificaciones por tareas completadas y penalizaciones por tareas vencidas.
     */
    fun getWeeklyStats(tasks: List<Task>, learnedPackages: Set<String>): List<DailyStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val result = mutableListOf<DailyStats>()
        
        val now = System.currentTimeMillis()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 0..6) {
            val startOfDay = calendar.timeInMillis
            val dateOfBar = newjavaTimeLocalDate(startOfDay)
            val isToday = startOfDay == todayStart
            val isFuture = startOfDay > todayStart
            
            var distractionMin = 0
            var productiveMin = 0

            if (!isFuture) {
                // 1. Estadísticas de uso de aplicaciones
                val endOfDay = startOfDay + 24 * 60 * 60 * 1000
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startOfDay,
                    if (isToday) now else endOfDay
                )

                val consolidatedStats = mutableMapOf<String, Long>()
                stats.forEach {
                    if (it.totalTimeInForeground > 0) {
                        val current = consolidatedStats[it.packageName] ?: 0L
                        if (it.totalTimeInForeground > current) {
                            consolidatedStats[it.packageName] = it.totalTimeInForeground
                        }
                    }
                }

                if (isToday) {
                    val usageEvents = usageStatsManager.queryEvents(now - 1000 * 60 * 30, now)
                    val event = android.app.usage.UsageEvents.Event()
                    val lastEvents = mutableMapOf<String, Int>()
                    val lastEventTime = mutableMapOf<String, Long>()

                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event)
                        lastEvents[event.packageName] = event.eventType
                        lastEventTime[event.packageName] = event.timeStamp
                    }

                    lastEvents.forEach { (pkg, type) ->
                        if (type == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                            val startTime = lastEventTime[pkg] ?: now
                            val extraTime = now - startTime
                            if (extraTime > 0) {
                                consolidatedStats[pkg] = (consolidatedStats[pkg] ?: 0L) + extraTime
                            }
                        }
                    }
                }

                consolidatedStats.forEach { (pkg, time) ->
                    val mins = (time / 60000).toInt()
                    if (mins > 0) {
                        if (isDistraction(pkg) || pkg in learnedPackages) {
                            distractionMin += mins
                        } else if (pkg != context.packageName) {
                            productiveMin += mins
                        }
                    }
                }

                // 2. Bonificación por tareas completadas
                // "entre mas cerca la fecha menos porcentaje... entre mas lejos mejor"
                tasks.filter { it.completed && it.completedDate == dateOfBar }.forEach { task ->
                    val daysAdvance = ChronoUnit.DAYS.between(dateOfBar, task.dueDate).coerceAtLeast(0)
                    // Escala 0 a 1: Si es para hoy es 0.1, si es para dentro de 7+ días es 1.0
                    val bonusFactor = (0.1f + (daysAdvance.toFloat() / 7f)).coerceIn(0.1f, 1.0f)
                    productiveMin += (bonusFactor * 60).toInt() // 1.0 = 60 min extra de productividad
                }

                // 3. Penalización por tareas vencidas (vencían ayer o antes y no se hicieron)
                // "si hay una tarea que no se era para hoy y no se marco como hecha, que mañana se sume como un porcentaje fijo de Distraccion"
                val overdueTasks = tasks.filter { 
                    it.dueDate.isBefore(dateOfBar) && (!it.completed || (it.completedDate != null && it.completedDate?.isAfter(dateOfBar) == true))
                }
                if (overdueTasks.isNotEmpty()) {
                    distractionMin += (overdueTasks.size * 20) // 20 min de penalización por cada tarea vencida
                }
            }

            result.add(DailyStats(
                day = days[i],
                productiveMinutes = productiveMin,
                distractionMinutes = distractionMin
            ))
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return result
    }

    private fun newjavaTimeLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
