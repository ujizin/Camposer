package com.ujizin.camposer.info

import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import kotlin.test.Test
import kotlin.test.assertFalse

internal class CameraInfoTest {

  private fun makeCameraInfo(openResult: Boolean = true): CameraInfo {
    val fakeCapture = FakeJvmCameraCapture(openResult)
    val jvmCameraInfo = JvmCameraInfo(fakeCapture)
    return CameraInfo(jvmCameraInfo)
  }

  @Test
  fun `JVM camera info reports no flash or torch support`() {
    val cameraInfo = makeCameraInfo()
    val state = cameraInfo.state.value
    assertFalse(state.isFlashSupported)
    assertFalse(state.isFlashAvailable)
    assertFalse(state.isTorchSupported)
    assertFalse(state.isTorchAvailable)
  }

  @Test
  fun `rebind updates state`() {
    val cameraInfo = makeCameraInfo()
    cameraInfo.rebind()
    val state = cameraInfo.state.value
    assertFalse(state.isFlashSupported)
    assertFalse(state.isTorchSupported)
  }
}
