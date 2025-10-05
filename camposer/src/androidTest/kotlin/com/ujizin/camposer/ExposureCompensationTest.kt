package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ExposureCompensationTest: CameraTest() {

    private lateinit var exposureCompensation: MutableState<Float>

    private val currentExposure: Float?
        get() = cameraState.controller.cameraInfo?.exposureState?.exposureCompensationIndex?.toFloat()

    @Test
    fun test_minExposureCompensation() = with(composeTestRule) {
        initCameraWithExposure(0F)

        exposureCompensation.value = cameraState.info.minExposure

        runOnIdle {
            if (!cameraState.info.isExposureSupported) return@runOnIdle

            assertEquals(cameraState.info.minExposure, currentExposure)
            assertEquals(exposureCompensation.value, currentExposure)
        }
    }

    @Test
    fun test_maxExposureCompensation() = with(composeTestRule) {
        initCameraWithExposure(0F)

        exposureCompensation.value = cameraState.info.maxExposure

        runOnIdle {
            if (!cameraState.info.isExposureSupported) return@runOnIdle

            assertEquals(cameraState.info.maxExposure, currentExposure)
            assertEquals(exposureCompensation.value, currentExposure)
        }
    }

    private fun ComposeContentTestRule.initCameraWithExposure(
        exposure: Float,
    ) = initCameraState { state ->
        exposureCompensation = remember { mutableStateOf(exposure) }
        CameraPreview(
            cameraState = state,
            exposureCompensation = exposureCompensation.value
        )
    }
}
