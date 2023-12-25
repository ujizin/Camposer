package com.ujizin.camposer

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.view.video.AudioConfig
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
    fun test_captureMode() = with(composeTestRule) {
        initCaptureModeCamera(CaptureMode.Image)

        var isFinalized = false
        runOnIdle {
            val imageFile = File(context.filesDir, IMAGE_TEST_FILENAME).apply { createNewFile() }
            cameraState.takePicture(imageFile) { result ->
                when (result) {
                    is ImageCaptureResult.Error -> throw result.throwable
                    is ImageCaptureResult.Success -> {
                        assertEquals(Uri.fromFile(imageFile), result.savedUri)
                        assertEquals(CaptureMode.Image, cameraState.captureMode)
                        isFinalized = true
                    }
                }
            }
        }

        waitUntil(CAPTURE_MODE_TIMEOUT) { isFinalized }
    }

    @Test
    fun test_videoCaptureMode() = with(composeTestRule) {
        initCaptureModeCamera(CaptureMode.Video)

        if (!cameraState.isVideoSupported) return

        // Create file
        val videoFile = File(context.filesDir, VIDEO_TEST_FILENAME).apply {
            deleteRecursively()
            createNewFile()
        }

        waitUntil(CAPTURE_MODE_TIMEOUT) { videoFile.exists() }

        var isFinished = false
        runOnIdle {
            cameraState.startRecording(
                FileOutputOptions.Builder(videoFile).build(),
                AudioConfig.AUDIO_DISABLED
            ) { result ->
                when (result) {
                    is VideoCaptureResult.Error -> throw result.throwable ?: error(result.message)
                    is VideoCaptureResult.Success -> {
                        assertEquals(Uri.fromFile(videoFile), result.savedUri)
                        assertEquals(CaptureMode.Video, cameraState.captureMode)
                        isFinished = true
                    }
                }
            }
        }

        runBlocking {
            delay(RECORD_VIDEO_DELAY)
            cameraState.stopRecording()
        }

        waitUntil(CAPTURE_MODE_TIMEOUT) { isFinished }
    }

    private fun ComposeContentTestRule.initCaptureModeCamera(
        captureMode: CaptureMode,
        analyzer: ((ImageProxy) -> Unit)? = null,
    ) = initCameraState { state ->
        CameraPreview(
            cameraState = state,
            captureMode = captureMode,
            imageAnalyzer = analyzer?.takeIf {
                cameraState.isImageAnalysisSupported
            }?.let { state.rememberImageAnalyzer(analyze = it) },
            isImageAnalysisEnabled = cameraState.isImageAnalysisSupported && analyzer != null
        )
    }

    private companion object {
        private const val RECORD_VIDEO_DELAY = 2000L
        private const val CAPTURE_MODE_TIMEOUT = 10000L
        private const val IMAGE_TEST_FILENAME = "capture_mode_test.jpg"
        private const val VIDEO_TEST_FILENAME = "capture_mode_test.mp4"
    }
}
