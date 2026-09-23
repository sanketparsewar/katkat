package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
  LIGHT,
  DARK,
  SYSTEM
}

private val LightColorScheme = lightColorScheme(
  primary = CoralPrimary,
  onPrimary = SurfaceWhite,
  primaryContainer = PeachLight,
  onPrimaryContainer = TextPrimaryDark,
  secondary = PeachSecondary,
  onSecondary = SurfaceWhite,
  secondaryContainer = PeachBlush,
  onSecondaryContainer = TextPrimaryDark,
  tertiary = SuperlikeBlue,
  onTertiary = SurfaceWhite,
  background = WarmCream,
  onBackground = TextPrimaryDark,
  surface = SurfaceWhite,
  onSurface = TextPrimaryDark,
  surfaceVariant = WarmSand,
  onSurfaceVariant = TextSecondaryDark,
  outline = DividerWarm
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFFFF7A7A),
  onPrimary = Color.White,
  primaryContainer = Color(0xFF5E1B24),
  onPrimaryContainer = Color(0xFFFFD8DC),
  secondary = Color(0xFFFF9E70),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF522819),
  onSecondaryContainer = Color(0xFFFFDAC6),
  tertiary = SuperlikeBlue,
  onTertiary = Color.White,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkDivider,
  outlineVariant = Color(0xFF4C3E4D)
)

@Composable
fun KatkatTheme(
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  // For brand consistency, we prefer our custom Warm Playful palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val isDark = when (themeMode) {
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    isDark -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
