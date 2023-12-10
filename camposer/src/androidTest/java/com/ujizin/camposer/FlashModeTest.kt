package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.FlashMode
import com.ujizin.camposer.state.rememberFlashMode
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class FlashModeTest : CameraTest() {

    private lateinit var flashMode: MutableState<FlashMode>

    @Test
    fun test_flashModes() = with(composeTestRule) {
        initFlashCamera(camSelector = CamSelector.Back)
        if (!cameraState.hasFlashUnit) return

        FlashMode.values().forEach { mode ->
            flashMode.value = mode
            onNodeWithTag("${flashMode.value}").assertIsDisplayed()
            runOnIdle { assertEquals(mode, cameraState.flashMode) }
        }
    }

    @Test
    fun test_flashModeWithNoUnit() = with(composeTestRule) {
        initFlashCamera(camSelector = CamSelector.Front)
        // Ensure that there's no flash unit on device
        cameraState.hasFlashUnit = false

        flashMode.value = FlashMode.On
        onNodeWithTag("${FlashMode.On}").assertDoesNotExist()
        runOnIdle { assertEquals(FlashMode.Off, cameraState.flashMode) }
    }

    private fun ComposeContentTestRule.initFlashCamera(
        camSelector: CamSelector
    ) = initCameraState { state ->
        flashMode = state.rememberFlashMode(FlashMode.Off)
        CameraPreview(
            Modifier.testTag("${flashMode.value}"),
            cameraState = state,
            flashMode = flashMode.value,
            camSelector = camSelector,
        )
    }
}
