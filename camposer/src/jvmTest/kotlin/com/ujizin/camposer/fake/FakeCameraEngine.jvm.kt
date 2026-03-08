package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.core.JvmCameraEngine
import com.ujizin.camposer.state.properties.ImageAnalyzer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.opencv_core.Mat

private fun buildEngine(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
): CameraEngineImpl {
  val capture = FakeJvmCameraCapture()
  // Pre-populate currentMat with a 1×1 BGR frame so takePicture works in tests
  // without waiting for the asynchronous frame loop to produce the first frame.
  capture.currentMat = Mat(1, 1, CV_8UC3)
  return CameraEngineImpl(
    cameraController = cameraTest.cameraController,
    cameraInfo = cameraTest.cameraInfo,
    capture = capture,
    dispatcher = testDispatcher,
  ).also { impl -> cameraTest.fakeEngine = impl }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal actual class FakeCameraEngine actual constructor(
  private val cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : JvmCameraEngine by buildEngine(cameraTest, testDispatcher) {
  /**
   * Override the delegated method so that setting an analyzer immediately triggers one
   * analysis cycle with the pre-populated frame.  This avoids a race between the test
   * thread and the Dispatchers.IO frame loop when asserting [ImageAnalyzer] callbacks.
   */
  actual override fun updateImageAnalyzer(imageAnalyzer: ImageAnalyzer?) {
    cameraTest.fakeEngine?.updateImageAnalyzer(imageAnalyzer)
    imageAnalyzer?.analyze(capture.currentMat ?: return)
  }
}
