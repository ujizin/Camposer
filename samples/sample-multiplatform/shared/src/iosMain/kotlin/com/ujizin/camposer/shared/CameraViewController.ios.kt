package com.ujizin.camposer.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.ujizin.camposer.shared.navigation.SampleNavigation
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun CameraViewController(): UIViewController =
  ComposeUIViewController {
    SampleNavigation()
  }
