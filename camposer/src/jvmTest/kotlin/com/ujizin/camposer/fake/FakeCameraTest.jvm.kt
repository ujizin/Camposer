package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.FakeJvmCameraInfo
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.test.assertEquals

internal actual class FakeCameraTest actual constructor(
  private val testDispatcher: CoroutineDispatcher,
) {
  internal val fakeJvmCameraInfo = FakeJvmCameraInfo()
  actual val cameraController: CameraController = CameraController(testDispatcher)
  actual val cameraInfo: CameraInfo = CameraInfo(fakeJvmCameraInfo)

  // Set by FakeCameraEngine.jvm.kt after the CameraEngineImpl is created
  internal var fakeEngine: CameraEngineImpl? = null

  private val engineState
    get() = fakeEngine?.cameraState
      ?: error("FakeCameraEngine not initialized — call createCameraSession first")

  /**
   * Flash support defaults to true so tests that don't explicitly disable flash work the same
   * way as the iOS fake (fakeIsFlashSupported defaults to true there too).
   */
  actual var isFlashSupported: Boolean
    get() = fakeJvmCameraInfo.isFlashSupported
    set(value) {
      fakeJvmCameraInfo.isFlashSupported = value
      cameraInfo.rebind()
    }

  actual var isExposureSupported: Boolean
    get() = fakeJvmCameraInfo.isExposureSupported
    set(value) {
      fakeJvmCameraInfo.isExposureSupported = value
      cameraInfo.rebind()
    }

  actual var isZSLSupported: Boolean = false

  actual var hasErrorInRecording: Boolean = false

  actual fun assertCamSelector(expected: CamSelector) {
    assertEquals(expected, engineState.camSelector.value)
  }

  actual fun assertCaptureMode(expected: CaptureMode) {
    assertEquals(expected, engineState.captureMode.value)
  }

  actual fun assertZoomRatio(expected: Float) {
    assertEquals(expected, engineState.zoomRatio.value)
  }

  actual fun assertFlashMode(expected: FlashMode) {
    assertEquals(expected, engineState.flashMode.value)
  }

  actual fun assertExposureCompensation(expected: Float) {
    assertEquals(expected, engineState.exposureCompensation.value)
  }

  actual fun assertImageCaptureStrategy(expected: ImageCaptureStrategy) {
    assertEquals(expected, engineState.imageCaptureStrategy.value)
  }

  actual fun assertImageAnalyzer(expected: ImageAnalyzer?) {
    assertEquals(expected, engineState.imageAnalyzer.value)
  }

  actual fun assertCamFormat(expected: CamFormat) {
    // no-op — CamFormat hardware assertions not meaningful on JVM desktop (same as iOS)
  }

  actual fun assertIsRecording(expected: Boolean) {
    assertEquals(expected, cameraController.isRecording.value)
  }
}
