package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ujizin.camposer.state.properties.CamSelector
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.session.rememberFlashMode
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
        if (!cameraSession.info.isFlashSupported) return

        FlashMode.entries.forEach { mode ->
            flashMode.value = mode
            onNodeWithTag("${flashMode.value}").assertIsDisplayed()
            runOnIdle { assertEquals(mode, cameraSession.state.flashMode) }
        }
    }

    @Test
    fun test_flashModeWithNoUnit() = with(composeTestRule) {
        initFlashCamera(camSelector = CamSelector.Front)
        // Ensure that there's no flash unit on device
        cameraSession.info.isFlashSupported = false

        flashMode.value = FlashMode.On
        onNodeWithTag("${FlashMode.On}").assertDoesNotExist()
        runOnIdle { assertEquals(FlashMode.Off, cameraSession.state.flashMode) }
    }

    private fun ComposeContentTestRule.initFlashCamera(
        camSelector: CamSelector
    ) = initCameraSession { state ->
        flashMode = state.rememberFlashMode(FlashMode.Off)
        CameraPreview(
            Modifier.testTag("${flashMode.value}"),
            cameraSession = state,
            flashMode = flashMode.value,
            camSelector = camSelector,
        )
    }
}
