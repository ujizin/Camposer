package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.copy
import com.ujizin.camposer.info.AndroidCameraInfo
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.fallback
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.mode
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.value
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.test.assertEquals

internal actual class FakeCameraTest(
  internal val cameraXController: FakeCameraXController,
  private val testDispatcher: CoroutineDispatcher,
) {
  actual constructor(testDispatcher: CoroutineDispatcher) : this(
    cameraXController = FakeCameraXController(),
    testDispatcher = testDispatcher,
  )

  actual val cameraController: CameraController = CameraController(testDispatcher)

  actual val cameraInfo: CameraInfo by lazy {
    CameraInfo(
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

  actual var isVideoStabilizationSupported: Boolean
    get() = cameraInfo.state.value.isVideoStabilizationSupported
    set(value) {
      cameraInfo.updateStateForTesting { it.copy(isVideoStabilizationSupported = value) }
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

  actual fun assertIsRecording(expected: Boolean) {
    assertEquals(expected, cameraXController.isRecording)
  }

  actual fun assertVideoStabilization(expected: VideoStabilizationMode) {
    val isVideoStabilizationEnabled = expected != VideoStabilizationMode.Off
    val isPreviewStabilizationEnabled = when (expected) {
      VideoStabilizationMode.Cinematic,
      VideoStabilizationMode.CinematicExtended,
      VideoStabilizationMode.CinematicExtendedEnhanced,
      -> true

      else -> false
    }
    assertEquals(isVideoStabilizationEnabled, cameraXController.isVideoStabilizationEnabled)
    assertEquals(isPreviewStabilizationEnabled, cameraXController.isPreviewStabilizationEnabled)
  }
}
