package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ujizin.camposer.state.ScaleType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ScaleTypeTest : CameraTest() {

    private lateinit var scaleType: MutableState<ScaleType>

    @Test
    fun test_scaleType() = with(composeTestRule) {
        initScaleTypeCamera()
        ScaleType.values().forEach { scale ->
            scaleType.value = scale
            runOnIdle { assertEquals(cameraState.scaleType, scaleType.value) }
        }
    }

    private fun ComposeContentTestRule.initScaleTypeCamera(
        initialValue: ScaleType = ScaleType.FitStart
    ) = initCameraState { state ->
        scaleType = remember { mutableStateOf(initialValue) }
        CameraPreview(
            cameraState = state,
            scaleType = scaleType.value,
        )
    }
}