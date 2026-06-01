package com.example.antiprocrastination

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.antiprocrastination.navigation.*
import com.example.antiprocrastination.ui.screens.*
import com.example.antiprocrastination.ui.theme.AntiProcrastinationTheme
import com.example.antiprocrastination.viewmodel.AppViewModel
import com.example.antiprocrastination.usage.UsageWorker
import com.example.antiprocrastination.usage.UsageAlarmReceiver
import java.util.concurrent.TimeUnit

import com.example.antiprocrastination.model.AppDatabase
import com.example.antiprocrastination.viewmodel.AppViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var isPermissionDialogShown by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso de notificaciones concedido
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar permisos de acceso a uso
        checkPermission()
        
        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Inicializar DB, Settings y ViewModel
        val database = AppDatabase.getDatabase(this)
        val settingsManager = com.example.antiprocrastination.data.SettingsManager(this)
        val usageTracker = com.example.antiprocrastination.usage.UsageTracker(this)
        val viewModel: AppViewModel by viewModels { 
            AppViewModelFactory(database.taskDao(), database.distractionDao(), settingsManager, usageTracker)
        }

        // Observar cambios en el intervalo de monitoreo
        lifecycleScope.launch {
            viewModel.monitoringInterval.collect { interval ->
                scheduleSmartMonitoring(interval)
            }
        }

        setContent {
            AntiProcrastinationTheme {
                Box {
                    AppScaffold(viewModel)

                    if (isPermissionDialogShown) {
                        AlertDialog(
                            onDismissRequest = { /* No permitir cerrar sin acción */ },
                            title = { Text("Permiso Requerido") },
                            text = { Text("Permiso de 'Acceso a Uso en segundo plano' no activado, por favor active el permiso para el correcto funcionamiento de la app") },
                            confirmButton = {
                                Button(onClick = {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    startActivity(intent)
                                }) {
                                    Text("Ir a Ajustes")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-verificar cuando el usuario vuelve de ajustes
        checkPermission()
    }

    private fun checkPermission() {
        isPermissionDialogShown = !hasUsageStatsPermission()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun scheduleSmartMonitoring(minutes: Int) {
        if (minutes < 15) {
            // Cancelar WorkManager si existe
            WorkManager.getInstance(this).cancelUniqueWork("UsageTrackingWork")
            
            // Programar Alarma Exacta
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, UsageAlarmReceiver::class.java).apply {
                putExtra("interval_minutes", minutes)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)
            
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Si no tiene permiso de alarma exacta, cae a WorkManager
                scheduleUsageWorker(15)
            }
        } else {
            // Cancelar alarmas previas
            val intent = Intent(this, UsageAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            pendingIntent?.let {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(it)
            }
            
            scheduleUsageWorker(minutes)
        }
    }

    private fun scheduleUsageWorker(minutes: Int = 15) {
        val workRequest = PeriodicWorkRequestBuilder<UsageWorker>(minutes.toLong(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "UsageTrackingWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    // Routes where bottom bar is visible
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.HOME,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME)     { HomeScreen(viewModel, navController) }
            composable(Routes.TASKS)    { TasksScreen(viewModel, navController) }
            composable(Routes.NEW_TASK) { NewTaskScreen(viewModel, navController) }
            composable(Routes.POMODORO) { PomodoroScreen(viewModel) }
            composable(Routes.STATS)    { StatsScreen(viewModel) }
            composable(Routes.SETTINGS) { SettingsScreen(viewModel, navController) }
        }
    }
}
