package com.ujizin.camposer.internal.core

import com.ujizin.camposer.internal.capture.JvmCameraCapture

internal interface JvmCameraEngine : CameraEngine {
  val capture: JvmCameraCapture

  /**
   * Controls whether [DefaultRecordController] produces an error on [stopRecording].
   * Defaults to false; overridden in test fakes to simulate recording failures.
   */
  val hasRecordingError: Boolean get() = false
}
