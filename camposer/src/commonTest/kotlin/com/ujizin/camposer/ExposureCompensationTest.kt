package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ExposureCompensationTest: CameraTest() {

    private lateinit var exposureCompensation: MutableState<Int>

    private val currentExposure: Int?
        get() = cameraState.controller.cameraInfo?.exposureState?.exposureCompensationIndex

    @Test
    fun test_minExposureCompensation() = with(composeTestRule) {
        initCameraWithExposure(0)

        exposureCompensation.value = cameraState.minExposure

        runOnIdle {
            assertEquals(cameraState.minExposure, currentExposure)
            assertEquals(exposureCompensation.value, currentExposure)
        }
    }

    @Test
    fun test_maxExposureCompensation() = with(composeTestRule) {
        initCameraWithExposure(0)

        exposureCompensation.value = cameraState.maxExposure

        runOnIdle {
            assertEquals(cameraState.maxExposure, currentExposure)
            assertEquals(exposureCompensation.value, currentExposure)
        }
    }


    @Test
    fun test_invalidExposureCompensation() = with(composeTestRule) {
        initCameraWithExposure(0)

        exposureCompensation.value = Int.MAX_VALUE

        runOnIdle {
            assertNotEquals(cameraState.maxExposure, currentExposure)
            assertNotEquals(exposureCompensation.value, currentExposure)
            assertEquals(cameraState.initialExposure, currentExposure)
        }
    }

    private fun ComposeContentTestRule.initCameraWithExposure(
        exposure: Int,
    ) = initCameraState { state ->
        exposureCompensation = remember { mutableStateOf(exposure) }
        CameraPreview(
            cameraState = state,
            exposureCompensation = exposureCompensation.value
        )
    }
}