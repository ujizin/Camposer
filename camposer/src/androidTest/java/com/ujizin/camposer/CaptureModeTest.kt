package com.ujizin.camposer

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ujizin.camposer.state.CaptureMode
import com.ujizin.camposer.state.ImageCaptureResult
import com.ujizin.camposer.state.VideoCaptureResult
import com.ujizin.camposer.state.rememberImageAnalyzer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class CaptureModeTest : CameraTest() {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    @Test
    @Ignore("Flaky test, sometimes throw exception \"Camera closed\"")
    fun test_captureMode() = with(composeTestRule) {
        initCaptureModeCamera(CaptureMode.Image)

        runOnIdle {
            val imageFile = File(context.filesDir, IMAGE_TEST_FILENAME).apply { createNewFile() }

            cameraState.takePicture(imageFile) { result ->
                when (result) {
                    is ImageCaptureResult.Error -> throw result.throwable
                    is ImageCaptureResult.Success -> {
                        assertEquals(Uri.fromFile(imageFile), result.savedUri)
                        assertEquals(CaptureMode.Image, cameraState.captureMode)
                    }
                }
            }
        }
    }

    @Test
    fun test_videoCaptureMode() = with(composeTestRule) {
        initCaptureModeCamera(CaptureMode.Video)

        runOnIdle {
            val videoFile = File(context.filesDir, VIDEO_TEST_FILENAME).apply { createNewFile() }
            cameraState.startRecording(videoFile) { result ->
                when (result) {
                    is VideoCaptureResult.Error -> {
                        throw result.throwable ?: Exception(result.message)
                    }

                    is VideoCaptureResult.Success -> {
                        assertEquals(Uri.fromFile(videoFile), result.savedUri)
                        assertEquals(CaptureMode.Video, cameraState.captureMode)
                    }
                }
            }
            runBlocking {
                delay(RECORD_VIDEO_DELAY)
                cameraState.stopRecording()
            }
        }
    }

    @Test
    fun text_videoCaptureModeWithAnalysis() = with(composeTestRule) {
        var isAnalyzeCalled = false
        initCaptureModeCamera(CaptureMode.Video) {
            isAnalyzeCalled = true
        }

        runOnIdle {
            val videoFile = File(context.filesDir, VIDEO_TEST_FILENAME).apply { createNewFile() }

            cameraState.startRecording(videoFile) { result ->
                when (result) {
                    is VideoCaptureResult.Error -> {
                        throw result.throwable ?: Exception(result.message)
                    }

                    is VideoCaptureResult.Success -> {
                        assertEquals(Uri.fromFile(videoFile), result.savedUri)
                        assertEquals(CaptureMode.Video, cameraState.captureMode)
                    }
                }
            }
            runBlocking {
                delay(RECORD_VIDEO_DELAY)
                cameraState.stopRecording()
                assertEquals(false, cameraState.isImageAnalysisEnabled)
                assertEquals(false, isAnalyzeCalled)
            }
        }
    }

    private fun ComposeContentTestRule.initCaptureModeCamera(
        captureMode: CaptureMode,
        analyzer: ((ImageProxy) -> Unit)? = null,
    ) = initCameraState { state ->
        CameraPreview(
            cameraState = state,
            captureMode = captureMode,
            imageAnalyzer = analyzer?.let { state.rememberImageAnalyzer(analyze = it) },
        )
    }

    private companion object {
        private const val RECORD_VIDEO_DELAY = 500L
        private const val IMAGE_TEST_FILENAME = "capture_mode_test.jpg"
        private const val VIDEO_TEST_FILENAME = "capture_mode_test.mp4"
    }
}
