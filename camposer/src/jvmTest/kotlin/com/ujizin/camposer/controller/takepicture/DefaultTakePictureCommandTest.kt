package com.ujizin.camposer.controller.takepicture

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.JvmCameraInfo
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.internal.core.CameraEngineImpl
import kotlin.test.Test
import kotlin.test.assertTrue

internal class DefaultTakePictureCommandTest {
  private fun buildCommand(): DefaultTakePictureCommand {
    val capture = FakeJvmCameraCapture()
    val engine = CameraEngineImpl(
      cameraController = CameraController(),
      cameraInfo = CameraInfo(JvmCameraInfo()),
      capture = capture,
    )
    return DefaultTakePictureCommand(engine)
  }

  @Test
  fun `takePicture fails when no frame available`() {
    val command = buildCommand()
    var result: CaptureResult<ByteArray>? = null
    command.takePicture { result = it }
    // currentMat is null by default — expect Error
    assertTrue(result is CaptureResult.Error)
  }

  @Test
  fun `takePicture to file fails when no frame available`() {
    val command = buildCommand()
    var result: CaptureResult<String>? = null
    command.takePicture(filename = "/tmp/test_capture.jpg") { result = it }
    // currentMat is null by default — expect Error
    assertTrue(result is CaptureResult.Error)
  }
}
