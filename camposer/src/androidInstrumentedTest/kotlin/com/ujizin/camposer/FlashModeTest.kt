package com.ujizin.camposer

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.selector.CamSelector
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class FlashModeTest : CameraTest() {
  private lateinit var flashMode: State<FlashMode>

  @Test
  fun test_flashModes() =
    with(composeTestRule) {
      initFlashCamera(camSelector = CamSelector.Back)
      if (!cameraSession.info.isFlashSupported) return

      FlashMode.entries.forEach { mode ->
        val oldMode = flashMode.value

        cameraController.setFlashMode(mode)

        waitUntil { flashMode.value != oldMode }

        onNodeWithTag("${flashMode.value}").assertIsDisplayed()
        runOnIdle { assertEquals(mode, cameraSession.state.flashMode) }
      }
    }

  @Test
  fun test_startFlashModeAsOn() =
    with(composeTestRule) {
      initFlashCamera(camSelector = CamSelector.Back, mode = FlashMode.On)

      waitUntil(10000) { flashMode.value == FlashMode.On || !cameraSession.info.isFlashSupported }

      if (!cameraSession.info.isFlashSupported) {
        return@with // Not supported, skipping test
      }

      onNodeWithTag("${FlashMode.On}").assertExists()
      runOnIdle { assertEquals(FlashMode.On, cameraSession.state.flashMode) }
    }

  @Test
  fun test_flashModeWithNoUnit() =
    with(composeTestRule) {
      initFlashCamera(camSelector = CamSelector.Front)
      // Ensure that there's no flash unit on device
      cameraSession.info.isFlashSupported = false

      cameraController.setFlashMode(FlashMode.On)
      onNodeWithTag("${FlashMode.On}").assertDoesNotExist()
      runOnIdle { assertEquals(FlashMode.Off, cameraSession.state.flashMode) }
    }

  private fun ComposeContentTestRule.initFlashCamera(
    camSelector: CamSelector,
    mode: FlashMode = FlashMode.Off,
  ) = initCameraSession { state ->
    flashMode = rememberUpdatedState(cameraSession.state.flashMode)

    LaunchedEffect(mode) {
      cameraController.setFlashMode(mode)
    }

    CameraPreview(
      Modifier.testTag("${flashMode.value}"),
      cameraSession = state,
      camSelector = camSelector,
    )
  }
}
