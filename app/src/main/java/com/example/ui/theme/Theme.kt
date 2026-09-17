package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
  primary = CoralLight,
  onPrimary = TextPrimaryDark,
  primaryContainer = CoralDark,
  onPrimaryContainer = SurfaceWhite,
  secondary = PeachSecondary,
  onSecondary = TextPrimaryDark,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = DarkTextPrimary,
  tertiary = SuperlikeBlue,
  onTertiary = SurfaceWhite,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkSurfaceElevated
)

@Composable
fun KatkatTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For brand consistency, we prefer our custom Warm Playful palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
