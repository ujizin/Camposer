package com.ujizin.camposer.internal.core.applier

internal interface CameraStateApplier {
  fun onCameraInitialized() {}

  fun onCameraResumed() {}

  fun onCameraPaused() {}
}
