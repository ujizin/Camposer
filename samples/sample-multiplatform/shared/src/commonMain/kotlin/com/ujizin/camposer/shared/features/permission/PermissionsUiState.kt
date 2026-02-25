package com.ujizin.camposer.shared.features.permission

data class PermissionsUiState(
  val isCameraGranted: Boolean = false,
  val isRecordAudioGranted: Boolean = false,
  val isLoading: Boolean = true,
) {
  val isAllPermissionGranted: Boolean
    get() = isCameraGranted && isRecordAudioGranted
}
