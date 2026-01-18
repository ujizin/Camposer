package com.ujizin.camposer.shared.navigation

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ujizin.camposer.shared.features.camera.CameraScreen
import com.ujizin.camposer.shared.features.permission.PermissionsScreen

@Composable
fun SampleNavigation() {
  val backStack = rememberNavBackStack(Routes.config, Routes.PermissionRoute)

  NavDisplay(
    modifier = Modifier.safeDrawingPadding(),
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