package com.example.antiprocrastination.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────
val Primary       = Color(0xFF1E3A5F)   // deep navy
val PrimaryLight  = Color(0xFF2E5F9E)
val Accent        = Color(0xFF00C896)   // teal-green
val AccentDark    = Color(0xFF009E74)
val Background    = Color(0xFFF4F6FA)
val Surface       = Color(0xFFFFFFFF)
val SurfaceVar    = Color(0xFFEAEFF8)
val OnPrimary     = Color(0xFFFFFFFF)
val OnSurface     = Color(0xFF1A1A2E)
val Muted         = Color(0xFF8A94A6)
val Warning       = Color(0xFFFF6B35)
val CardBg        = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = OnPrimary,
    primaryContainer = PrimaryLight,
    secondary        = Accent,
    onSecondary      = OnPrimary,
    background       = Background,
    surface          = Surface,
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVar,
    outline          = Color(0xFFCDD3DF)
)

@Composable
fun AntiProcrastinationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
