package com.ujizin.camposer

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ujizin.camposer.state.ImplementationMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ImplementationModeTest : CameraTest() {

    private lateinit var implementationMode: MutableState<ImplementationMode>

    @Test
    fun test_implementationMode() = with(composeTestRule) {
        initImplementationModeCamera(ImplementationMode.Performance)

        assertEquals(cameraState.implementationMode, ImplementationMode.Performance)
        implementationMode.value = ImplementationMode.Compatible

        runOnIdle {
            assertEquals(cameraState.implementationMode, ImplementationMode.Compatible)
        }
    }

    private fun ComposeContentTestRule.initImplementationModeCamera(
        initialValue: ImplementationMode
    ) = initCameraState { state ->
        implementationMode = remember { mutableStateOf(initialValue) }
        CameraPreview(
            cameraState = state,
            implementationMode = implementationMode.value,
        )
    }
}