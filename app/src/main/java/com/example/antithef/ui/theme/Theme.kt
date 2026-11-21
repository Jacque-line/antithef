package com.example.antithef.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkSecurityColorScheme = darkColorScheme(
    primary = SecurityGreen,
    onPrimary = SecurityNavy,
    primaryContainer = SecurityDarkBlue,
    onPrimaryContainer = SecurityGreen,
    secondary = SecurityCyan,
    onSecondary = SecurityNavy,
    secondaryContainer = SecurityDarkSurface,
    onSecondaryContainer = SecurityCyan,
    tertiary = SecurityAmber,
    onTertiary = SecurityNavy,
    error = SecurityRed,
    onError = SecurityWhite,
    errorContainer = SecurityDarkSurface,
    onErrorContainer = SecurityRed,
    background = SecurityNavy,
    onBackground = SecurityWhite,
    surface = SecurityDarkBlue,
    onSurface = SecurityWhite,
    surfaceVariant = SecurityDarkSurface,
    onSurfaceVariant = SecurityGray,
    outline = SecurityBorder
)

@Composable
fun AntithefTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkSecurityColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SecurityNavy.toArgb()
                window.navigationBarColor = SecurityNavy.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
