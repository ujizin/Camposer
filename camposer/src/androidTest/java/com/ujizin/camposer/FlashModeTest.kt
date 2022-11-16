package com.ujizin.camposer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.FlashMode
import com.ujizin.camposer.state.rememberCameraState
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class FlashModeTest: CameraTest() {

    private lateinit var cameraState: CameraState

    private var flashMode by mutableStateOf(FlashMode.Off)

    @Before
    fun setup() {
        composeTestRule.setContent {
            cameraState = rememberCameraState()
            CameraPreview(
                cameraState = cameraState,
                flashMode = flashMode
            )
        }
    }

    @Test
    fun test_flashModeIsOn() {
        flashMode = FlashMode.On

        assertEquals(flashMode, cameraState.flashMode)
    }

    @Test
    fun test_flashModeIsAuto() {
        flashMode = FlashMode.Auto

        assertEquals(flashMode, cameraState.flashMode)
    }

    @Test
    fun test_flashModeIsOff() {
        flashMode = FlashMode.Off

        assertEquals(flashMode, cameraState.flashMode)
    }
}