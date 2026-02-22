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
import com.ujizin.camposer.session.rememberImageAnalyzer
import com.ujizin.camposer.state.properties.CaptureMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class CaptureModeTest : CameraTest() {
  private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun test_captureMode() =
    with(composeTestRule) {
      initCaptureModeCamera(CaptureMode.Image)

      var isFinalized = false
      runOnIdle {
        val imageFile = File(context.filesDir, IMAGE_TEST_FILENAME).apply { createNewFile() }
        cameraController.takePicture(imageFile) { result ->
          when (result) {
            is CaptureResult.Error -> {
              throw result.throwable
            }

            is CaptureResult.Success -> {
              assertEquals(Uri.fromFile(imageFile), result.data)
              assertEquals(CaptureMode.Image, cameraSession.state.captureMode.value)
              isFinalized = true
            }
          }
        }
      }

      waitUntil(CAPTURE_MODE_TIMEOUT) { isFinalized }
    }

  @Test
  fun test_videoCaptureMode() =
    with(composeTestRule) {
      initCaptureModeCamera(CaptureMode.Video)

      runOnIdle {
        assumeTrue(
          "Video capture is not supported on this device",
          cameraSession.info.state.value.videoFormats
            .isNotEmpty(),
        )
      }
      waitUntil(CAPTURE_MODE_TIMEOUT) {
        cameraSession.state.captureMode.value == CaptureMode.Video
      }

      // Create file
      val videoFile = File(context.filesDir, VIDEO_TEST_FILENAME).apply {
        deleteRecursively()
      }

      var isFinished = false
      var error: Throwable? = null
      runOnIdle {
        cameraController.startRecording(
          FileOutputOptions.Builder(videoFile).build(),
          AudioConfig.AUDIO_DISABLED,
        ) { result ->
          when (result) {
            is CaptureResult.Error -> {
              error = result.throwable
              isFinished = true
            }

            is CaptureResult.Success -> {
              val resultUri = result.data
              if (resultUri != null && resultUri != Uri.EMPTY) {
                assertEquals(Uri.fromFile(videoFile), resultUri)
              }
              assertEquals(CaptureMode.Video, cameraSession.state.captureMode.value)
              isFinished = true
            }
          }
        }
      }
      waitUntil(CAPTURE_MODE_TIMEOUT) { cameraController.isRecording }

      runBlocking {
        delay(RECORD_VIDEO_DELAY)
        cameraController.stopRecording()
      }

      waitUntil(CAPTURE_MODE_TIMEOUT) { isFinished }
      runOnIdle {
        if (error != null) throw checkNotNull(error)
        assertTrue(videoFile.exists())
      }
    }

  private fun ComposeContentTestRule.initCaptureModeCamera(
    captureMode: CaptureMode,
    analyzer: ((ImageProxy) -> Unit)? = null,
  ) = initCameraSession { state ->
    CameraPreview(
      cameraSession = state,
      captureMode = captureMode,
      imageAnalyzer = analyzer?.let { state.rememberImageAnalyzer(analyze = it) },
      isImageAnalysisEnabled = analyzer != null,
    )
  }

  private companion object {
    private const val RECORD_VIDEO_DELAY = 2000L
    private const val CAPTURE_MODE_TIMEOUT = 10000L
    private const val IMAGE_TEST_FILENAME = "capture_mode_test.jpg"
    private const val VIDEO_TEST_FILENAME = "capture_mode_test.mp4"
  }
}
