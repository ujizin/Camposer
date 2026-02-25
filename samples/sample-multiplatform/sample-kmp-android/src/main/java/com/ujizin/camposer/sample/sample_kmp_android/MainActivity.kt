package com.ujizin.camposer.sample.sample_kmp_android

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ujizin.camposer.shared.navigation.SampleNavigation
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    FileKit.init(this)
    setContent {
      ApplySystemBarColors()
      SampleNavigation()
    }
  }
}

@Composable
private fun ApplySystemBarColors() {
  val view = LocalView.current
  val systemBarColor = MaterialTheme.colorScheme.primary
  val useDarkIcons = systemBarColor.luminance() > 0.5f

  SideEffect {
    val window = (view.context as? Activity)?.window ?: return@SideEffect
    val colorInt = systemBarColor.toArgb()
    window.statusBarColor = colorInt
    window.navigationBarColor = colorInt
    WindowCompat.getInsetsController(window, view).apply {
      isAppearanceLightStatusBars = useDarkIcons
      isAppearanceLightNavigationBars = useDarkIcons
    }
  }
}
