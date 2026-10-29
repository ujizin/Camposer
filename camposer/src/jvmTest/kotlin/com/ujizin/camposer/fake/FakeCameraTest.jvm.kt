package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.JvmCameraEngine
import com.ujizin.camposer.internal.utils.JvmCameraFormats
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.CameraFormatPicker
import com.ujizin.camposer.state.properties.jpegQuality
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Desktop webcams expose no flash, torch, zoom or stabilization control, so those values
 * live in state only. The assertions below read the engine state for them and the fake
 * controller for everything that reaches hardware.
 */
internal actual class FakeCameraTest(
  internal val fakeJvmCameraController: FakeJvmCameraController,
  private val testDispatcher: CoroutineDispatcher,
) {
  actual constructor(testDispatcher: CoroutineDispatcher) : this(
    fakeJvmCameraController = FakeJvmCameraController(),
    testDispatcher = testDispatcher,
  )

  private var cameraEngine: JvmCameraEngine? = null

  actual val cameraController: CameraController = CameraController(testDispatcher)
  actual val cameraInfo: CameraInfo = CameraInfo(fakeJvmCameraController)

  internal fun attach(engine: JvmCameraEngine) {
    cameraEngine = engine
  }

  private val engineState
    get() = assertNotNull(cameraEngine, "FakeCameraEngine must be created first").cameraState

  actual var isFlashSupported: Boolean
    get() = fakeJvmCameraController.fakeIsFlashSupported
    set(value) {
      fakeJvmCameraController.fakeIsFlashSupported = value
    }

  actual var isExposureSupported: Boolean
    get() = fakeJvmCameraController.fakeIsExposureSupported
    set(value) {
      fakeJvmCameraController.fakeIsExposureSupported = value
    }

  actual var isZSLSupported: Boolean
    get() = false
    set(_) = Unit

  actual var isVideoStabilizationSupported: Boolean
    get() = fakeJvmCameraController.fakeIsVideoStabilizationSupported
    set(value) {
      fakeJvmCameraController.fakeIsVideoStabilizationSupported = value
    }

  actual var hasErrorInRecording: Boolean
    get() = fakeJvmCameraController.fakeErrorInRecording
    set(value) {
      fakeJvmCameraController.fakeErrorInRecording = value
    }

  actual fun assertCamSelector(expected: CamSelector) {
    assertEquals(expected.deviceIndex, fakeJvmCameraController.deviceIndex)
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
    assertEquals(expected, fakeJvmCameraController.fakeExposureCompensation)
  }

  actual fun assertImageCaptureStrategy(expected: ImageCaptureStrategy) {
    assertEquals(expected.jpegQuality, fakeJvmCameraController.fakePictureQuality)
  }

  actual fun assertImageAnalyzer(expected: ImageAnalyzer?) {
    assertEquals(expected?.analyzer, fakeJvmCameraController.fakeFrameAnalyzer)
  }

  actual fun assertCamFormat(expected: CamFormat) {
    val candidates = JvmCameraFormats.candidates(currentFrameRate = 30)
    val bestFormat = CameraFormatPicker.getBestFormatByOrder(expected.configs, candidates)
    assertNotNull(bestFormat)
    assertEquals(bestFormat.width, fakeJvmCameraController.currentFormat.width)
    assertEquals(bestFormat.height, fakeJvmCameraController.currentFormat.height)
  }

  actual fun assertIsRecording(expected: Boolean) {
    assertEquals(expected, fakeJvmCameraController.isRecording.value)
  }

  actual fun assertVideoStabilization(expected: VideoStabilizationMode) {
    assertEquals(expected, engineState.videoStabilizationMode.value)
  }

  actual fun assertTorchEnabled(expected: Boolean) {
    assertEquals(expected, engineState.isTorchEnabled.value)
  }

  actual fun assertFrameRate(expected: Int) {
    assertEquals(expected, fakeJvmCameraController.fakeFrameRate)
  }
}
