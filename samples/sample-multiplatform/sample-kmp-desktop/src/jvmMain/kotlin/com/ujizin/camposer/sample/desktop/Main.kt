package com.ujizin.camposer.sample.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ujizin.camposer.shared.navigation.SampleNavigation

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "Camposer Sample",
    state = rememberWindowState(),
  ) {
    SampleNavigation()
  }
}
