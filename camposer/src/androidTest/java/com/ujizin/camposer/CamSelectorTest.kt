package com.ujizin.camposer

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ujizin.camposer.state.CamSelector
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CamSelectorTest : CameraTest() {

    private lateinit var camSelectorState: MutableState<CamSelector>

    private lateinit var isCamSwitchedToFront: MutableState<Boolean>
    private lateinit var isCamSwitchedToBack: MutableState<Boolean>
    private lateinit var isPreviewStreamChanged: MutableState<Boolean>

    @Before
    fun setup() {
        isCamSwitchedToFront = mutableStateOf(false)
        isCamSwitchedToBack = mutableStateOf(false)
        isPreviewStreamChanged = mutableStateOf(false)
    }

    @Test
    fun test_camSelectorToFront() = with(composeTestRule) {
        initCamSelectorCamera(CamSelector.Back)

        camSelectorState.value = CamSelector.Front

        runOnIdle {
            assertEquals(true, isCamSwitchedToFront.value)
            assertEquals(true, isPreviewStreamChanged.value)
            assertEquals(false, isCamSwitchedToBack.value)
        }
    }

    @Test
    fun test_camSelectorToBack() = with(composeTestRule) {
        initCamSelectorCamera(CamSelector.Front)

        camSelectorState.value = CamSelector.Back

        runOnIdle {
            assertEquals(true, isCamSwitchedToBack.value)
            assertEquals(true, isPreviewStreamChanged.value)
            assertEquals(false, isCamSwitchedToFront.value)
        }
    }

    private fun ComposeContentTestRule.initCamSelectorCamera(
        initialValue: CamSelector
    ) = initCameraState { state ->
        camSelectorState = remember { mutableStateOf(initialValue) }
        isCamSwitchedToBack = remember { mutableStateOf(false) }
        isCamSwitchedToFront = remember { mutableStateOf(false) }
        isPreviewStreamChanged = remember { mutableStateOf(false) }

        CameraPreview(
            cameraState = state,
            camSelector = camSelectorState.value,
            onSwitchToBack = {
                LaunchedEffect(Unit) { isCamSwitchedToBack.value = true }
            },
            onSwitchToFront = {
                LaunchedEffect(Unit) { isCamSwitchedToFront.value = true }
            },
            onPreviewStreamChanged = { isPreviewStreamChanged.value = true }
        )
    }
}
