package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal actual class FakeCameraTest(
  val fakeIosCameraController: FakeIosCameraController,
) {
  actual constructor() : this(fakeIosCameraController = FakeIosCameraController())

  actual val cameraController: CameraController = CameraController()
  actual val cameraInfo: CameraInfo = CameraInfo(fakeIosCameraController)

  actual var hasErrorInRecording: Boolean
    get() = fakeIosCameraController.fakeErrorInRecording
    set(value) {
      fakeIosCameraController.fakeErrorInRecording = value
    }

  actual var isFlashSupported: Boolean
    get() = fakeIosCameraController.fakeIsFlashSupported
    set(value) {
      fakeIosCameraController.fakeIsFlashSupported = value
    }
  actual var isExposureSupported: Boolean
    get() = fakeIosCameraController.fakeIsExposureSupported
    set(value) {
      fakeIosCameraController.fakeIsExposureSupported = value
    }

  actual var isZSLSupported: Boolean
    get() = fakeIosCameraController.fakeIsZSLSupported
    set(value) {
      fakeIosCameraController.fakeIsZSLSupported = value
    }

  actual fun assertCamSelector(expected: CamSelector) {
    assertEquals(
      expected = expected.camPosition.value,
      actual = fakeIosCameraController.getCurrentPosition(),
    )
  }

  actual fun assertCaptureMode(expected: CaptureMode) {
    val outputs = fakeIosCameraController.fakeOutputs
    assertTrue(
      message = "Expected output to be $expected (${expected.output}), outputs: $outputs",
      actual = outputs.contains(expected.output),
    )
  }

  actual fun assertZoomRatio(expected: Float) {
    assertEquals(
      message = "Expected zoom ratio $expected actual ${fakeIosCameraController.fakeZoomRatio}",
      expected = expected,
      actual = fakeIosCameraController.fakeZoomRatio,
    )
  }

  actual fun assertFlashMode(expected: FlashMode) {
    assertEquals(
      message = "Expected flash mode $expected actual ${fakeIosCameraController.fakeFlashMode}",
      expected = expected.mode,
      actual = fakeIosCameraController.fakeFlashMode,
    )
  }

  actual fun assertExposureCompensation(expected: Float) {
    val fakeExposureCompensation = fakeIosCameraController.fakeExposureCompensation
    assertEquals(
      message = "Expected exposure compensation $expected actual $fakeExposureCompensation",
      expected = expected,
      actual = fakeExposureCompensation,
    )
  }

  actual fun assertImageCaptureStrategy(expected: ImageCaptureStrategy) {
    val fakeQuality = fakeIosCameraController.fakeQuality
    val fakeHighResolutionEnabled = fakeIosCameraController.isFakeHighResolutionEnabled
    assertEquals(
      message = "Expected strategy $expected (${expected.quality}) actual $fakeQuality",
      expected = expected.quality,
      actual = fakeQuality,
    )
    assertEquals(
      message = "Expected high resolution enabled $expected actual $fakeHighResolutionEnabled",
      expected = expected.highResolutionEnabled,
      actual = fakeHighResolutionEnabled,
    )
  }

  actual fun assertImageAnalyzer(expected: ImageAnalyzer?) {
    val fakeOutputs = fakeIosCameraController.fakeOutputs
    assertTrue(
      message = "Expected analyzer $expected, actual outputs: $fakeOutputs",
      actual = fakeOutputs.contains(expected?.analyzer?.output),
    )
  }

  actual fun assertCamFormat(expected: CamFormat) {
    // no-op
  }
}
