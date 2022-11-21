package com.ujizin.camposer

import androidx.camera.core.TorchState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.rememberTorch
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TorchTest : CameraTest() {

    private lateinit var torchState: MutableState<Boolean>

    private val cameraXEnableTorch: Boolean
        get() = cameraState.controller.torchState.value == TorchState.ON

    /**
     * AVD does not have torch and [CameraState.hasFlashUnit] it's not enough in this case
     * */
    private val isCameraXFlashSupported: Boolean
        get() = with(cameraState.controller) {
            val oldValue = torchState.value == TorchState.ON
            enableTorch(true)
            val isSupported = torchState.value == TorchState.ON
            enableTorch(oldValue)
            isSupported
        }

    @Test
    fun test_toggleTorch() = with(composeTestRule) {
        initTorchCamera()

        runOnIdle {
            assertEquals(false, cameraState.enableTorch)
            assertEquals(false, cameraXEnableTorch)
        }

        torchState.value = true

        runOnIdle {
            if (isCameraXFlashSupported) {
                assertEquals(true, cameraState.enableTorch)
                assertEquals(true, cameraXEnableTorch)
            }
        }
    }

    private fun ComposeContentTestRule.initTorchCamera(
        initialValue: Boolean = false
    ) = initCameraState { state ->
        torchState = state.rememberTorch(initialValue)
        CameraPreview(
            cameraState = state,
            enableTorch = torchState.value
        )
    }
}