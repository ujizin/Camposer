package com.ujizin.camposer.session

import androidx.compose.ui.test.ExperimentalTestApi
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.properties.selector.inverse
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CameraSelectorTest : CameraSessionTest() {
  @Test
  fun test_preview_cam_selector_back() {
    val expected = CamSelector.Back
    updateSession(camSelector = expected)
    assertCamSelector(expected)
  }

  @Test
  fun test_preview_cam_selector_front() {
    val expected = CamSelector.Front
    updateSession(camSelector = expected)
    assertCamSelector(expected)
  }

  @Test
  fun test_preview_cam_selector_back_to_front() {
    val camSelector = CamSelector.Back
    updateSession(camSelector = camSelector)
    assertCamSelector(CamSelector.Back)

    val expected = camSelector.inverse
    updateSession(camSelector = expected)
    assertCamSelector(expected)
  }

  @Test
  fun test_preview_cam_selector_front_to_back() {
    val camSelector = CamSelector.Front
    updateSession(camSelector = camSelector)
    assertCamSelector(CamSelector.Front)

    val expected = camSelector.inverse
    updateSession(camSelector = expected)
    assertCamSelector(expected)
  }

  private fun assertCamSelector(camSelector: CamSelector) {
    cameraTest.assertCamSelector(camSelector)
    assertEquals(camSelector, cameraSession.state.camSelector)
  }
}
