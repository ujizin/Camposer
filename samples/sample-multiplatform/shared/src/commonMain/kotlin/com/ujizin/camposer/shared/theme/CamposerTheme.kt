package com.ujizin.camposer.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KotlinPurple = Color(0xFF7F52FF)
val KotlinPurpleLight = Color(0xFFB39DFF)
val KotlinPurpleDark = Color(0xFF5A2ECC)

private val CamposerColorScheme = darkColorScheme(
  primary = KotlinPurple,
  onPrimary = Color.White,
  primaryContainer = KotlinPurpleDark,
  onPrimaryContainer = Color.White,
  secondary = KotlinPurpleLight,
  onSecondary = Color.Black,
  background = Color.Black,
  onBackground = Color.White,
  surface = Color.Black,
  onSurface = Color.White,
)

@Composable
fun CamposerTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = CamposerColorScheme,
    content = content,
  )
}
