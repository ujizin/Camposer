package com.ujizin.camposer

import androidx.camera.core.CameraInfo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
      val cameraInfoState = cameraSession.info.state.value

      runOnUiThread {
        cameraController.setExposureCompensation(cameraInfoState.minExposure)
      }

      runOnIdle {
        if (!cameraInfoState.isExposureSupported) return@runOnIdle

        assertEquals(cameraInfoState.minExposure, currentExposure)
        assertEquals(exposureCompensation.value, currentExposure)
      }
    }

  @Test
  fun test_maxExposureCompensation() =
    with(composeTestRule) {
      initCameraWithExposure(0F)
      val cameraInfoState = cameraSession.info.state.value

      runOnUiThread {
        cameraController.setExposureCompensation(cameraInfoState.maxExposure)
      }
      runOnIdle {
        if (!cameraInfoState.isExposureSupported) return@runOnIdle

        assertEquals(cameraInfoState.maxExposure, currentExposure)
        assertEquals(exposureCompensation.value, currentExposure)
      }
    }

  private fun ComposeContentTestRule.initCameraWithExposure(exposure: Float) =
    initCameraSession { state ->
      exposureCompensation = cameraSession.state.exposureCompensation.collectAsStateWithLifecycle()

      LaunchedEffect(exposure) {
        state.controller.setExposureCompensation(exposure)
      }

      CameraPreview(cameraSession = state)
    }
}
