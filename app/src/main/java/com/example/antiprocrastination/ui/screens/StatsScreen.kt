package com.example.antiprocrastination.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.antiprocrastination.domain.model.DailyStats
import com.example.antiprocrastination.ui.theme.*
import com.example.antiprocrastination.ui.viewmodel.AppViewModel
import com.example.antiprocrastination.ui.components.cards.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: AppViewModel) {
    val tasks          by viewModel.tasks.collectAsState()
    val appUsages      by viewModel.appUsages.collectAsState()
    val weeklyStats    by viewModel.weeklyStats.collectAsState()
    val pomodoroState  by viewModel.pomodoro.collectAsState()

    val completedCount = tasks.count { it.completed }
    val pendingCount   = tasks.count { !it.completed }

    // Actualizar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.refreshUsageStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Statistics", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
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
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Summary cards ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier  = Modifier.weight(1f),
                    icon      = Icons.Filled.CheckCircle,
                    iconColor = Accent,
                    value     = "$completedCount",
                    label     = "Done"
                )
                StatCard(
                    modifier  = Modifier.weight(1f),
                    icon      = Icons.Filled.RadioButtonUnchecked,
                    iconColor = Warning,
                    value     = "$pendingCount",
                    label     = "Pending"
                )
                StatCard(
                    modifier  = Modifier.weight(1f),
                    icon      = Icons.Filled.Timer,
                    iconColor = PrimaryLight,
                    value     = "${pomodoroState.completedSessions}",
                    label     = "Pomodoros"
                )
            }

            // ── Weekly bar chart ──────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Activity",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = OnSurface)
                    Spacer(Modifier.height(4.dp))
                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot(Accent,   "Productive")
                        LegendDot(Warning,  "Distraction")
                    }
                    Spacer(Modifier.height(12.dp))
                    WeeklyBarChart(
                        stats    = weeklyStats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            // ── App usage breakdown ───────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App Usage Today",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = OnSurface)
                    HorizontalDivider(color = SurfaceVar)
                    appUsages.forEach { app ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.appName,
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface)
                            Text(app.timeFormatted,
                                style      = MaterialTheme.typography.bodySmall,
                                color      = if (app.isOverLimit) Warning else Muted,
                                fontWeight = if (app.isOverLimit) FontWeight.Bold else FontWeight.Normal,
                                modifier   = Modifier.width(60.dp),
                                textAlign  = TextAlign.End)
                            Spacer(Modifier.width(8.dp))
                            LinearProgressIndicator(
                                progress   = { app.usagePercent.coerceIn(0f, 1f) },
                                modifier   = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color      = if (app.isOverLimit) Warning else Accent,
                                trackColor = SurfaceVar
                            )
                        }
                    }
                }
            }

            // ── Productivity tip ──────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Color(0xFF1E3A5F)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null,
                        tint = Color(0xFFFFC947), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Tip of the day",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFC947), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Try setting a 30-min limit on social media and use the Pomodoro timer to build deep focus habits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


// ── Legend dot ────────────────────────────────────────────────────────────────
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

// ── Weekly bar chart (Canvas) ─────────────────────────────────────────────────
@Composable
private fun WeeklyBarChart(stats: List<DailyStats>, modifier: Modifier = Modifier) {
    val maxMinutes = stats.maxOfOrNull { it.productiveMinutes + it.distractionMinutes } ?: 1

    val animProgress by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label         = "bars"
    )

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val barGroupWidth = size.width / stats.size
        val barPad        = barGroupWidth * 0.12f
        val barWidth      = (barGroupWidth - barPad * 2) / 2f
        val maxBarH       = size.height * 0.85f

        stats.forEachIndexed { i, day ->
            val groupLeft = i * barGroupWidth + barPad
            val proH  = (day.productiveMinutes.toFloat() / maxMinutes) * maxBarH * animProgress
            val distH = (day.distractionMinutes.toFloat() / maxMinutes) * maxBarH * animProgress

            // Productive bar
            drawRoundRect(
                color   = Accent,
                topLeft = Offset(groupLeft, size.height - proH - 20f),
                size    = Size(barWidth, proH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Distraction bar
            drawRoundRect(
                color   = Warning,
                topLeft = Offset(groupLeft + barWidth + 2f, size.height - distH - 20f),
                size    = Size(barWidth, distH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }

    // Day labels row
    Row(Modifier.fillMaxWidth()) {
        stats.forEach { day ->
            Text(day.day, Modifier.weight(1f),
                style     = MaterialTheme.typography.bodySmall,
                color     = Muted,
                textAlign = TextAlign.Center)
        }
    }
}
