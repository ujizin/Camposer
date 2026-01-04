package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.AndroidCameraInfo
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.test.assertEquals

internal actual class FakeCameraTest(
  internal val cameraXController: FakeCameraXController,
) {
  actual constructor() : this(FakeCameraXController())

  actual val cameraController: CameraController = CameraController()

  actual val cameraInfo: CameraInfo by lazy {
    CameraInfo(
      cameraXController.mainExecutor,
      AndroidCameraInfo(cameraXController),
    )
  }

  actual var isFlashSupported: Boolean
    get() = cameraXController.hasFlashUnit
    set(value) {
      cameraXController.hasFlashUnit = value
    }

  actual var isExposureSupported: Boolean
    get() = cameraXController.cameraInfo.exposureState.isExposureCompensationSupported
    set(value) {
      cameraXController.isExposureSupported = value
    }

  actual var isZSLSupported: Boolean
    get() = cameraXController.cameraInfo.isZslSupported
    set(value) {
      cameraXController.isZSLSupported = value
    }

  actual var hasErrorInRecording: Boolean
    get() = cameraXController.hasErrorInRecording
    set(value) {
      cameraXController.hasErrorInRecording = value
    }

  actual fun assertCamSelector(expected: CamSelector) {
    assertEquals(expected.selector, cameraXController.cameraSelector)
  }

  actual fun assertCaptureMode(expected: CaptureMode) {
    assertEquals(expected.value, cameraXController.useCases)
  }

  actual fun assertZoomRatio(expected: Float) =
    runBlocking(Dispatchers.Main) {
      assertEquals(expected, cameraXController.zoomState.value?.zoomRatio)
    }

  actual fun assertFlashMode(expected: FlashMode) {
    assertEquals(expected.mode, cameraXController.imageCaptureFlashMode)
  }

  actual fun assertExposureCompensation(expected: Float) {
    assertEquals(
      expected.roundToInt(),
      cameraXController.cameraInfo.exposureState.exposureCompensationIndex,
    )
  }

  actual fun assertImageCaptureStrategy(expected: ImageCaptureStrategy) {
    val mode = if (isZSLSupported) expected.mode else expected.fallback
    assertEquals(mode, cameraXController.imageCaptureMode)
  }

  actual fun assertImageAnalyzer(expected: ImageAnalyzer?) {
    assertEquals(expected?.analyzer, cameraXController.analyzer)
  }

  actual fun assertCamFormat(expected: CamFormat) {
    val resolutionSelector = expected.resolutionSelector
    assertEquals(resolutionSelector, cameraXController.previewResolutionSelector)
    assertEquals(resolutionSelector, cameraXController.imageCaptureResolutionSelector)
    assertEquals(resolutionSelector, cameraXController.imageAnalysisResolutionSelector)
  }
}
