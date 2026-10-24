package com.ujizin.camposer.session

import com.ujizin.camposer.fake.createCameraSession
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.update
import kotlin.test.Test

internal class CameraStartConfigTest : CameraSessionTest() {
  @Test
  fun test_image_capture_strategy_set_before_start_is_applied_on_start() {
    val session = createCameraSession(
      fakeCameraTest = cameraTest,
      testDispatcher = testDispatcher,
      autoStart = false,
    )

    with(session.state) {
      session.update(
        camSelector = camSelector.value,
        captureMode = captureMode.value,
        scaleType = scaleType.value,
        isImageAnalysisEnabled = isImageAnalyzerEnabled.value,
        imageAnalyzer = imageAnalyzer.value,
        implementationMode = implementationMode.value,
        isFocusOnTapEnabled = isFocusOnTapEnabled.value,
        imageCaptureStrategy = ImageCaptureStrategy.MaxQuality,
        camFormat = camFormat.value,
        isPinchToZoomEnabled = isPinchToZoomEnabled.value,
      )
    }

    session.startCamera()

    cameraTest.assertImageCaptureStrategy(ImageCaptureStrategy.MaxQuality)
  }
}
