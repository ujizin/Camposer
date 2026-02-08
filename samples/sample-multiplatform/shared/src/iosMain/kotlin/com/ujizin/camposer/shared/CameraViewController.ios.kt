package com.ujizin.camposer.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.ujizin.camposer.shared.features.camera.CameraScreen
import com.ujizin.camposer.shared.theme.CamposerTheme
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun CameraViewController(): UIViewController =
  ComposeUIViewController {
    CamposerTheme {
      CameraScreen()
    }
  }
