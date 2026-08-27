package com.cabelas.smokercompanion.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = FlameOrange,
    onPrimary = TextWhite,
    primaryContainer = SmokerDark,
    onPrimaryContainer = FlameAmber,
    secondary = FlameAmber,
    onSecondary = Charcoal900,
    background = Charcoal900,
    onBackground = TextWhite,
    surface = Charcoal800,
    onSurface = TextWhite,
    surfaceVariant = Charcoal700,
    onSurfaceVariant = TextGray,
    error = AlertRed,
    onError = TextWhite
)

@Composable
fun CabelasSmokerCompanionTheme(
    darkTheme: Boolean = true, // Default to rich Charcoal Dark BBQ UI
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
