package com.example.antiprocrastination.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.perf.util.Timer

// ── Route constants ────────────────────────────────────────────────────────────
object Routes {
    const val HOME       = "home"
    const val TASKS      = "tasks"
    const val NEW_TASK   = "new_task"
    const val POMODORO   = "pomodoro"
    const val STATS      = "stats"
    const val SETTINGS   = "settings"
}

// ── Bottom nav items ───────────────────────────────────────────────────────────
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home     : BottomNavItem(Routes.HOME,     "Start",    Icons.Filled.Home)
    object Tasks    : BottomNavItem(Routes.TASKS,    "Tasks",    Icons.Filled.CheckCircle)
    object Pomodoro : BottomNavItem(Routes.POMODORO, "Pomodoro", Icons.Filled.Timer)
    object Stats    : BottomNavItem(Routes.STATS,    "Stats",    Icons.Filled.BarChart)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Tasks,
    BottomNavItem.Pomodoro,
    BottomNavItem.Stats,
)
