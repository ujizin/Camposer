package com.ujizin.camposer

import androidx.camera.core.ImageProxy
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ujizin.camposer.state.ImageAnalyzer
import com.ujizin.camposer.state.rememberImageAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ImageAnalyzerTest : CameraTest() {

    private lateinit var imageAnalyzer: ImageAnalyzer

    @Test
    fun test_imageAnalyzer() = with(composeTestRule) {
        var isAnalyzeCalled = false
        initImageAnalyzerCamera {
            isAnalyzeCalled = true
            it.close()
        }

        waitUntil(ANALYZER_TIME_OUT) { isAnalyzeCalled }

        runOnIdle {
            assertEquals(true, cameraState.isImageAnalysisEnabled)
            assertEquals(true, isAnalyzeCalled)
        }
    }

    @Test
    fun test_imageAnalyzerDisabled() = with(composeTestRule) {
        var isAnalyzeCalled = false
        initImageAnalyzerCamera(isImageAnalyzeEnabled = false) { isAnalyzeCalled = true }

        runOnIdle {
            assertEquals(false, cameraState.isImageAnalysisEnabled)
            assertEquals(false, isAnalyzeCalled)
        }
    }

    private fun ComposeContentTestRule.initImageAnalyzerCamera(
        isImageAnalyzeEnabled: Boolean = true,
        analyze: (ImageProxy) -> Unit = {},
    ) =
        initCameraState { state ->
            imageAnalyzer = state.rememberImageAnalyzer(analyze = analyze)
            CameraPreview(
                cameraState = state,
                imageAnalyzer = imageAnalyzer,
                isImageAnalysisEnabled = isImageAnalyzeEnabled
            )
        }

    private companion object {
        private const val ANALYZER_TIME_OUT = 2_000L
    }
}
