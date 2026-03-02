package com.ujizin.camposer.shared.features.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun PermissionsScreen(onAllPermissionGranted: () -> Unit) {
  // Desktop apps don't require runtime permission requests.
  LaunchedEffect(Unit) { onAllPermissionGranted() }
}
