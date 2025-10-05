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
            if (cameraState.info.isImageAnalyzerSupported) {
                assertEquals(true, cameraState.config.isImageAnalyzerEnabled)
                assertEquals(true, isAnalyzeCalled)
            }
        }
    }

    @Test
    fun test_imageAnalyzerDisabled() = with(composeTestRule) {
        var isAnalyzeCalled = false
        initImageAnalyzerCamera(isImageAnalyzeEnabled = false) { isAnalyzeCalled = true }

        runOnIdle {
            if (cameraState.info.isImageAnalyzerSupported) {
                assertEquals(false, cameraState.config.isImageAnalyzerEnabled)
                assertEquals(false, isAnalyzeCalled)
            }
        }
    }

    @Test
    fun test_imageAnalysisSupported() = with(composeTestRule) {
        val expectImageAnalysisSupported: Boolean?
        initImageAnalyzerCamera(isImageAnalyzeEnabled = true)

        expectImageAnalysisSupported = cameraState.info.isImageAnalyzerSupported

        runOnIdle {
            assertEquals(expectImageAnalysisSupported, cameraState.info.isImageAnalyzerSupported)
            assertEquals(expectImageAnalysisSupported, cameraState.config.isImageAnalyzerEnabled)
        }
    }

    private fun ComposeContentTestRule.initImageAnalyzerCamera(
        isImageAnalyzeEnabled: Boolean = true,
        analyze: (ImageProxy) -> Unit = {},
    ) = initCameraState { state ->
        imageAnalyzer = state.rememberImageAnalyzer(analyze = analyze)
        CameraPreview(
            cameraState = state,
            imageAnalyzer = imageAnalyzer,
            isImageAnalysisEnabled = isImageAnalyzeEnabled
        )
    }

    private companion object {
        private const val ANALYZER_TIME_OUT = 5_000L
    }
}
