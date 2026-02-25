package com.ujizin.camposer.fake

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.coroutines.CoroutineDispatcher

internal expect class FakeCameraTest(
  testDispatcher: CoroutineDispatcher,
) {
  val cameraController: CameraController

  val cameraInfo: CameraInfo

  var isFlashSupported: Boolean

  var isExposureSupported: Boolean

  var isZSLSupported: Boolean

  var hasErrorInRecording: Boolean

  fun assertCamSelector(expected: CamSelector)

  fun assertCaptureMode(expected: CaptureMode)

  fun assertZoomRatio(expected: Float)

  fun assertFlashMode(expected: FlashMode)

  fun assertExposureCompensation(expected: Float)

  fun assertImageCaptureStrategy(expected: ImageCaptureStrategy)

  fun assertImageAnalyzer(expected: ImageAnalyzer?)

  fun assertCamFormat(expected: CamFormat)

  fun assertIsRecording(expected: Boolean)
}
