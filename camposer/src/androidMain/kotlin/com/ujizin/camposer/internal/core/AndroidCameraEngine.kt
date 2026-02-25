package com.ujizin.camposer.internal.core

import android.content.ContentResolver
import com.ujizin.camposer.internal.core.camerax.CameraXController
import java.util.concurrent.Executor

internal interface AndroidCameraEngine : CameraEngine {
  val cameraXController: CameraXController
  val mainExecutor: Executor
  val contentResolver: ContentResolver

  fun onCameraInitialized()
}
