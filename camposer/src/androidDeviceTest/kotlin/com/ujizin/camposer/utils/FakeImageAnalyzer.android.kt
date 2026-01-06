package com.ujizin.camposer.utils

import android.content.Context
import androidx.camera.view.LifecycleCameraController
import androidx.test.core.app.ApplicationProvider
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalysisBackpressureStrategy
import com.ujizin.camposer.state.properties.ImageAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual fun createFakeImageAnalyzer(
  cameraSession: CameraSession,
  block: () -> Unit,
): ImageAnalyzer {
  val context = ApplicationProvider.getApplicationContext<Context>()
  return runBlocking(Dispatchers.Main) {
    ImageAnalyzer(
      controller = LifecycleCameraController(context),
      imageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
      analyzer = {
        block()
        it.close()
      },
    )
  }
}
