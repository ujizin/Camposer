package com.ujizin.camposer

import androidx.camera.core.TorchState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TorchTest : CameraTest() {
  private lateinit var torchState: State<Boolean>

  private val cameraXEnableTorch: Boolean
    get() = cameraSession.cameraXController.torchState.value == TorchState.ON

  /**
   * AVD does not have torch and [com.ujizin.camposer.info.CameraInfo.isTorchSupported] it's not enough in this case
   * */
  private val isCameraXFlashSupported: Boolean
    get() = with(cameraSession.cameraXController) {
      val oldValue = torchState.value == TorchState.ON
      enableTorch(true)
      val isSupported = torchState.value == TorchState.ON
      enableTorch(oldValue)
      isSupported
    }

  @Test
  fun test_toggleTorch() =
    with(composeTestRule) {
      initTorchCamera()

      runOnIdle {
        assertEquals(false, cameraSession.state.isTorchEnabled)
        assertEquals(false, cameraXEnableTorch)
      }

      cameraController.setTorchEnabled(true)

      runOnIdle {
        if (isCameraXFlashSupported) {
          assertEquals(true, cameraSession.state.isTorchEnabled)
          assertEquals(true, cameraXEnableTorch)
        }
      }
    }

  private fun ComposeContentTestRule.initTorchCamera(initialValue: Boolean = false) =
    initCameraSession { state ->
      torchState = rememberUpdatedState(cameraSession.state.isTorchEnabled)

      LaunchedEffect(Unit) {
        cameraController.setTorchEnabled(initialValue)
      }

      CameraPreview(cameraSession = state)
    }
}
