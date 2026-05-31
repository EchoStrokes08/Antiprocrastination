package com.example.antiprocrastination.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.antiprocrastination.model.AppUsageInfo
import com.example.antiprocrastination.navigation.Routes
import com.example.antiprocrastination.ui.theme.*
import com.example.antiprocrastination.viewmodel.AppViewModel

// Colours assigned to each app slice
private val sliceColors = listOf(
    Color(0xFF1E3A5F),
    Color(0xFF00C896),
    Color(0xFFFF6B35),
    Color(0xFF7B61FF),
    Color(0xFFFFC947),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel, navController: NavController) {
    val appUsages by viewModel.appUsages.collectAsState()
    val totalMinutes = appUsages.sumOf { it.usageMinutes }.coerceAtLeast(1)

    // Actualizar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.refreshUsageStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Focus Guard", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Pie chart card ────────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Most Used Applications",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = OnSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        UsagePieChart(
                            usages      = appUsages,
                            total       = totalMinutes,
                            sliceColors = sliceColors,
                            modifier    = Modifier.size(180.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Today: ${totalMinutes}m total screen time",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                    }
                }
            }

            // ── App list ──────────────────────────────────────────────────────
            item {
                Text(
                    "Top Apps",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = OnSurface,
                    modifier   = Modifier.padding(horizontal = 4.dp)
                )
            }

            itemsIndexed(appUsages) { index, app ->
                AppUsageRow(
                    rank       = index + 1,
                    app        = app,
                    sliceColor = sliceColors.getOrElse(index) { Muted },
                    total      = totalMinutes
                )
            }
        }
    }
}

// ── Pie Chart ─────────────────────────────────────────────────────────────────
@Composable
fun UsagePieChart(
    usages: List<AppUsageInfo>,
    total: Int,
    sliceColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue    = 1f,
        animationSpec  = tween(1200, easing = FastOutSlowInEasing),
        label          = "pie"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val radius      = (size.minDimension - strokeWidth) / 2f
        val center      = Offset(size.width / 2f, size.height / 2f)
        val topLeft     = Offset(center.x - radius, center.y - radius)
        val arcSize     = Size(radius * 2f, radius * 2f)

        var startAngle = -90f
        usages.forEachIndexed { i, app ->
            val sweep = (app.usageMinutes.toFloat() / total) * 360f * animProgress
            drawArc(
                color      = sliceColors.getOrElse(i) { Color.Gray },
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += (app.usageMinutes.toFloat() / total) * 360f
        }
    }
}

// ── App row card ──────────────────────────────────────────────────────────────
@Composable
fun AppUsageRow(
    rank: Int,
    app: AppUsageInfo,
    sliceColor: Color,
    total: Int
) {
    val progress by animateFloatAsState(
        targetValue   = app.usageMinutes.toFloat() / total,
        animationSpec = tween(800),
        label         = "bar"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier        = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier            = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(sliceColor),
                contentAlignment    = Alignment.Center
            ) {
                Text(
                    "$rank",
                    color      = Color.White,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        app.appName,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color      = OnSurface
                    )
                    Text(
                        "${app.usageMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (app.isOverLimit) Warning else Muted,
                        fontWeight = if (app.isOverLimit) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress         = { progress },
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color            = if (app.isOverLimit) Warning else sliceColor,
                    trackColor       = SurfaceVar
                )
            }

            if (app.isOverLimit) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Over limit",
                    tint   = Warning,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
