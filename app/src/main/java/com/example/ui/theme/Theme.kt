package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = StudioCobaltLight,
  onPrimary = Color.White,
  primaryContainer = StudioCobalt,
  onPrimaryContainer = Color.White,
  secondary = Slate400,
  onSecondary = DarkStudioBg,
  background = DarkStudioBg,
  surface = DarkStudioSurface,
  surfaceVariant = DarkStudioSurfaceVariant,
  onBackground = Color.White,
  onSurface = Color.White,
  onSurfaceVariant = Slate400,
  outline = Slate700
)

private val LightColorScheme = lightColorScheme(
  primary = StudioCobalt,
  onPrimary = Color.White,
  primaryContainer = StudioCobaltContainer,
  onPrimaryContainer = Slate900,
  secondary = StudioSteel,
  onSecondary = Color.White,
  background = Slate50,
  surface = Color.White,
  surfaceVariant = Slate100,
  onBackground = Slate900,
  onSurface = Slate900,
  onSurfaceVariant = Slate600,
  outline = Slate200
)

@Composable
fun AudioLabsTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

