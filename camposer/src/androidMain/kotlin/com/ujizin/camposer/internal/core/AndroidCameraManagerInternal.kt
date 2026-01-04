package com.ujizin.camposer.internal.core

import android.content.ContentResolver
import com.ujizin.camposer.internal.core.camerax.CameraXController
import java.util.concurrent.Executor

internal interface AndroidCameraManagerInternal : CameraManagerInternal {
  val controller: CameraXController
  val mainExecutor: Executor
  val contentResolver: ContentResolver

  fun onCameraInitialized()
}
