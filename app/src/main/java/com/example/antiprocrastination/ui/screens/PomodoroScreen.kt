package com.example.antiprocrastination.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.antiprocrastination.domain.model.PomodoroPhase
import com.example.antiprocrastination.ui.theme.*
import com.example.antiprocrastination.ui.viewmodel.AppViewModel
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(viewModel: AppViewModel) {
    val state by viewModel.pomodoro.collectAsState()

    val arcColor    = if (state.phase == PomodoroPhase.WORK) Accent else Warning
    val phaseLabel  = if (state.phase == PomodoroPhase.WORK) "Focus Time" else "Rest Time"
    val phaseIcon   = if (state.phase == PomodoroPhase.WORK) Icons.Filled.MenuBook else Icons.Filled.Coffee

    // Animate arc sweep
    val animSweep by animateFloatAsState(
        targetValue   = state.progressFraction * 360f,
        animationSpec = tween(800, easing = LinearEasing),
        label         = "sweep"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pomodoro", style = MaterialTheme.typography.titleLarge,
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Phase badge
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(phaseIcon, contentDescription = null, tint = arcColor,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    phaseLabel,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = arcColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Clock face
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    drawPomodoroClockFace(
                        sweepDegrees = animSweep,
                        arcColor     = arcColor
                    )
                }
                // Time display in center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.minutesDisplay,
                        style      = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp),
                        fontWeight = FontWeight.Bold,
                        color      = OnSurface
                    )
                    Text(
                        if (state.phase == PomodoroPhase.WORK) "minutes" else "rest",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }

            // Session indicators
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i ->
                    Box(
                        Modifier.size(12.dp).clip(CircleShape)
                            .background(if (i < state.completedSessions % 4) Accent else SurfaceVar)
                    )
                }
            }
            Text(
                "${state.completedSessions} sessions completed today",
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Reset
                FilledTonalIconButton(
                    onClick = viewModel::resetPomodoro,
                    modifier = Modifier.size(52.dp),
                    colors   = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = SurfaceVar
                    )
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = Muted)
                }

                // Start / Stop
                Button(
                    onClick  = { if (state.isRunning) viewModel.stopPomodoro() else viewModel.startPomodoro() },
                    modifier = Modifier
                        .height(56.dp)
                        .width(160.dp),
                    shape    = RoundedCornerShape(28.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRunning) Warning else Accent
                    )
                ) {
                    Icon(
                        if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.isRunning) "Stop" else "Start",
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Settings card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = OnSurface)
                    HorizontalDivider(color = SurfaceVar)
                    PomodoroSettingRow(
                        label    = "Work duration",
                        value    = "${state.totalSeconds / 60} min",
                        onMinus  = { if (state.totalSeconds > 60 * 5) viewModel.setWorkMinutes(state.totalSeconds / 60 - 5) },
                        onPlus   = { viewModel.setWorkMinutes(state.totalSeconds / 60 + 5) },
                        enabled  = !state.isRunning
                    )
                    Text(
                        "25 min work · 5 min rest (standard Pomodoro)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }
        }
    }
}

@Composable
private fun PomodoroSettingRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus, enabled = enabled, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = if (enabled) Primary else Muted,
                    modifier = Modifier.size(16.dp))
            }
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = OnSurface,
                modifier = Modifier.width(56.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = onPlus, enabled = enabled, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Increase", tint = if (enabled) Primary else Muted,
                    modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Canvas clock face ─────────────────────────────────────────────────────────
private fun DrawScope.drawPomodoroClockFace(sweepDegrees: Float, arcColor: Color) {
    val cx          = size.width / 2f
    val cy          = size.height / 2f
    val outerRadius = size.minDimension / 2f
    val trackWidth  = outerRadius * 0.12f
    val trackRadius = outerRadius - trackWidth / 2f

    // Background track
    drawCircle(
        color  = Color(0xFFEAEFF8),
        radius = trackRadius,
        center = Offset(cx, cy),
        style  = Stroke(width = trackWidth)
    )

    // Progress arc
    drawArc(
        color      = arcColor,
        startAngle = -90f,
        sweepAngle = sweepDegrees,
        useCenter  = false,
        topLeft    = Offset(cx - trackRadius, cy - trackRadius),
        size       = androidx.compose.ui.geometry.Size(trackRadius * 2, trackRadius * 2),
        style      = Stroke(width = trackWidth, cap = StrokeCap.Round)
    )

    // Hour tick marks
    val tickCount = 12
    for (i in 0 until tickCount) {
        val angle     = (i * 360f / tickCount - 90f) * (PI / 180f).toFloat()
        val innerR    = outerRadius * 0.72f
        val outerR    = outerRadius * 0.82f
        val startPt   = Offset(cx + innerR * cos(angle), cy + innerR * sin(angle))
        val endPt     = Offset(cx + outerR * cos(angle), cy + outerR * sin(angle))
        drawLine(
            color       = Color(0xFFCDD3DF),
            start       = startPt,
            end         = endPt,
            strokeWidth = if (i % 3 == 0) 3f else 1.5f
        )
    }
}
