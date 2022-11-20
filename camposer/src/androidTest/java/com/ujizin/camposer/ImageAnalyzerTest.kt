package com.ujizin.camposer

import androidx.camera.core.ImageProxy
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ujizin.camposer.state.ImageAnalyzer
import com.ujizin.camposer.state.rememberImageAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ImageAnalyzerTest : CameraTest() {

    private lateinit var imageAnalyzer: ImageAnalyzer

    private lateinit var isImageAnalyzeEnabledState: MutableState<Boolean>

    @Test
    fun test_imageAnalyzer() = with(composeTestRule) {
        var isAnalyzeCalled = false
        initImageAnalyzerCamera {
            isAnalyzeCalled = true
            it.close()
        }

        waitUntil { cameraState.isStreaming }
        waitUntil { isAnalyzeCalled }

        runOnIdle {
            assertEquals(true, cameraState.isImageAnalysisEnabled)
            assertEquals(true, isAnalyzeCalled)
        }
    }

    @Test
    fun test_imageAnalyzerDisabled() = with(composeTestRule) {
        var isAnalyzeCalled = false
        initImageAnalyzerCamera { isAnalyzeCalled = true }
        isImageAnalyzeEnabledState.value = false

        waitUntil { cameraState.isStreaming }

        runOnIdle {
            assertEquals(false, cameraState.isImageAnalysisEnabled)
            assertEquals(false, isAnalyzeCalled)
        }
    }

    private fun ComposeContentTestRule.initImageAnalyzerCamera(analyze: (ImageProxy) -> Unit = {}) =
        initCameraState { state ->
            imageAnalyzer = state.rememberImageAnalyzer(analyze = analyze)
            isImageAnalyzeEnabledState = remember { mutableStateOf(true) }
            CameraPreview(
                cameraState = state,
                imageAnalyzer = imageAnalyzer,
                isImageAnalysisEnabled = isImageAnalyzeEnabledState.value
            )
        }
}
