package com.example.antiprocrastination.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.antiprocrastination.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var notificationsEnabled  by remember { mutableStateOf(true) }
    var reminderMinutesBefore by remember { mutableStateOf(30) }
    var youtubeLimitMin       by remember { mutableStateOf(60) }
    var tiktokLimitMin        by remember { mutableStateOf(30) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Notifications ─────────────────────────────────────────────────
            SettingsSection(title = "Notifications", icon = Icons.Filled.Notifications) {
                SwitchSettingRow(
                    label    = "Enable notifications",
                    subLabel = "Get alerted when you exceed app limits",
                    checked  = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )
                if (notificationsEnabled) {
                    StepperSettingRow(
                        label    = "Reminder before due date",
                        subLabel = "${reminderMinutesBefore} min before",
                        onMinus  = { if (reminderMinutesBefore > 10) reminderMinutesBefore -= 10 },
                        onPlus   = { reminderMinutesBefore += 10 },
                        value    = "${reminderMinutesBefore}m"
                    )
                }
            }

            // ── App limits ────────────────────────────────────────────────────
            SettingsSection(title = "App Time Limits", icon = Icons.Filled.Timelapse) {
                StepperSettingRow(
                    label    = "YouTube",
                    subLabel = "Daily limit",
                    onMinus  = { if (youtubeLimitMin > 15) youtubeLimitMin -= 15 },
                    onPlus   = { youtubeLimitMin += 15 },
                    value    = "${youtubeLimitMin}m"
                )
                HorizontalDivider(color = SurfaceVar)
                StepperSettingRow(
                    label    = "TikTok",
                    subLabel = "Daily limit",
                    onMinus  = { if (tiktokLimitMin > 15) tiktokLimitMin -= 15 },
                    onPlus   = { tiktokLimitMin += 15 },
                    value    = "${tiktokLimitMin}m"
                )
            }

            // ── About ─────────────────────────────────────────────────────────
            SettingsSection(title = "About", icon = Icons.Filled.Info) {
                InfoRow("Version",  "1.0.0 – Sprint 1")
                HorizontalDivider(color = SurfaceVar)
                InfoRow("Authors",  "O. González · J. Bernal")
                HorizontalDivider(color = SurfaceVar)
                InfoRow("Course",   "Technology in Data Systemization")
                HorizontalDivider(color = SurfaceVar)
                InfoRow("Professor","J. E. Hernández Rodríguez")
            }
        }
    }
}

// ── Section wrapper ────────────────────────────────────────────────────────────
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Primary,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = OnSurface)
        }
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp), content = content)
        }
    }
}

// ── Switch row ────────────────────────────────────────────────────────────────
@Composable
private fun SwitchSettingRow(
    label: String,
    subLabel: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium, color = OnSurface)
            Text(subLabel, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White,
                checkedTrackColor = Accent)
        )
    }
}

// ── Stepper row ───────────────────────────────────────────────────────────────
@Composable
private fun StepperSettingRow(
    label: String,
    subLabel: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium, color = OnSurface)
            Text(subLabel, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease",
                    tint = Primary, modifier = Modifier.size(16.dp))
            }
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = OnSurface,
                modifier = Modifier.width(48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = onPlus, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Increase",
                    tint = Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Info row ──────────────────────────────────────────────────────────────────
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, color = OnSurface)
    }
}
