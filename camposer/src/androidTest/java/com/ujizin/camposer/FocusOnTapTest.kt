package com.ujizin.camposer

import androidx.camera.view.CameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class FocusOnTapTest : CameraTest() {

    private lateinit var isFocusTappedState: MutableState<Boolean>

    private val isCameraXFocused: Boolean
        get() = when (cameraState.controller.tapToFocusState.value) {
            CameraController.TAP_TO_FOCUS_STARTED, CameraController.TAP_TO_FOCUS_FOCUSED -> true
            else -> false
        }

    @Test
    fun test_focusOnTap(): Unit = with(composeTestRule) {
        initFocusCamera(initialValue = true)

        if (!cameraState.isFocusOnTapSupported) return

        onNodeWithTag(FOCUS_TEST_TAG).performClick()

        runOnIdle {
            assertEquals(true, isCameraXFocused)
            assertEquals(true, isFocusTappedState.value)
        }

        onNodeWithTag(FOCUS_TAP_CONTENT_TAG).assertIsDisplayed()
    }

    @Test
    fun test_focusOnTapDisable() = with(composeTestRule) {
        initFocusCamera(initialValue = false)

        if (!cameraState.isFocusOnTapSupported) return

        onNodeWithTag(FOCUS_TEST_TAG).performClick()

        runOnIdle {
            assertEquals(false, isFocusTappedState.value)
            assertEquals(false, isCameraXFocused)
        }
    }

    @Test
    fun test_onFocusCallbackOnComplete() = with(composeTestRule) {
        var completed = false
        initFocusCamera(initialValue = true) { onComplete ->
            delay(FOCUS_DELAY)
            onComplete()
            completed = true
        }

        if (!cameraState.isFocusOnTapSupported) return

        onNodeWithTag(FOCUS_TEST_TAG).performClick()
        onNodeWithTag(FOCUS_TAP_CONTENT_TAG).assertIsDisplayed()

        runOnIdle {
            assertEquals(true, isCameraXFocused)
            assertEquals(true, isFocusTappedState.value)
        }

        waitUntil(FOCUS_ON_COMPLETE_DELAY) { completed }

        onNodeWithTag(FOCUS_TAP_CONTENT_TAG).assertDoesNotExist()
    }

    private fun ComposeContentTestRule.initFocusCamera(
        initialValue: Boolean = true,
        onFocus: suspend (onComplete: () -> Unit) -> Unit = { onComplete ->
            delay(DEFAULT_FOCUS_DELAY)
            onComplete()
        }
    ) = initCameraState { state ->
        val isFocusOnTapEnabled by remember { mutableStateOf(initialValue) }
        isFocusTappedState = remember { mutableStateOf(false) }
        CameraPreview(
            modifier = Modifier.testTag(FOCUS_TEST_TAG),
            cameraState = state,
            isFocusOnTapEnabled = isFocusOnTapEnabled,
            focusTapContent = {
                Box(
                    modifier = Modifier
                        .testTag(FOCUS_TAP_CONTENT_TAG)
                        .background(Color.Red)
                        .size(64.dp)
                )
                LaunchedEffect(Unit) { isFocusTappedState.value = true }
            },
            onFocus = onFocus
        )
    }

    private companion object {
        private const val FOCUS_TEST_TAG = "focus_test_tag"
        private const val FOCUS_TAP_CONTENT_TAG = "focus_tap_content_tag"
        private const val DEFAULT_FOCUS_DELAY = 300L
        private const val FOCUS_DELAY = 500L
        private const val FOCUS_ON_COMPLETE_DELAY = 2_500L
    }
}
