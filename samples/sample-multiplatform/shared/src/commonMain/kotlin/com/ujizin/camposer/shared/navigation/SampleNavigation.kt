package com.ujizin.camposer.shared.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ujizin.camposer.shared.features.camera.CameraScreen
import com.ujizin.camposer.shared.features.permission.PermissionsScreen
import com.ujizin.camposer.shared.theme.CamposerTheme

@Composable
fun SampleNavigation() {
  CamposerTheme {
    val backStack = rememberNavBackStack(Routes.config, Routes.PermissionRoute)

    NavDisplay(
      modifier = Modifier
        .windowInsetsPadding(WindowInsets.safeDrawing),
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      entryProvider = entryProvider {
        entry<Routes.PermissionRoute> {
          PermissionsScreen(
            onAllPermissionGranted = {
              backStack.removeLastOrNull()
              backStack.add(Routes.CameraRoute)
            }
          )
        }
        entry<Routes.CameraRoute> {
          CameraScreen()
        }
      }
    )
  }
}
