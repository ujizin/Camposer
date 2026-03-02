package com.ujizin.camposer.shared.features.permission

import androidx.compose.runtime.Composable

@Composable
expect fun PermissionsScreen(onAllPermissionGranted: () -> Unit)
