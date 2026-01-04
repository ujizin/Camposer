package com.ujizin.camposer

import androidx.camera.core.CameraInfo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ExposureCompensationTest : CameraTest() {
  private lateinit var exposureCompensation: State<Float>

  private val cameraInfo: CameraInfo?
    get() = cameraSession.cameraXControllerWrapper.cameraInfo

  private val currentExposure: Float?
    get() = cameraInfo?.exposureState?.exposureCompensationIndex?.toFloat()

  @Test
  fun test_minExposureCompensation() =
    with(composeTestRule) {
      initCameraWithExposure(0F)

      runOnUiThread {
        cameraController.setExposureCompensation(cameraSession.info.minExposure)
      }

      runOnIdle {
        if (!cameraSession.info.isExposureSupported) return@runOnIdle

        assertEquals(cameraSession.info.minExposure, currentExposure)
        assertEquals(exposureCompensation.value, currentExposure)
      }
    }

  @Test
  fun test_maxExposureCompensation() =
    with(composeTestRule) {
      initCameraWithExposure(0F)

      runOnUiThread {
        cameraController.setExposureCompensation(cameraSession.info.maxExposure)
      }
      runOnIdle {
        if (!cameraSession.info.isExposureSupported) return@runOnIdle

        assertEquals(cameraSession.info.maxExposure, currentExposure)
        assertEquals(exposureCompensation.value, currentExposure)
      }
    }

  private fun ComposeContentTestRule.initCameraWithExposure(exposure: Float) =
    initCameraSession { state ->
      exposureCompensation = rememberUpdatedState(cameraSession.state.exposureCompensation)

      LaunchedEffect(exposure) {
        state.controller.setExposureCompensation(exposure)
      }

      CameraPreview(cameraSession = state)
    }
}
