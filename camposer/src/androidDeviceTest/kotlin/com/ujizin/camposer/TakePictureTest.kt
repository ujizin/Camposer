package com.ujizin.camposer

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ujizin.camposer.state.properties.CaptureMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class TakePictureTest : CameraTest() {
  private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun test_take_picture_with_content_values() {
    var savedUri: Uri? = null
    try {
      with(composeTestRule) {
        initImageCamera()

        var result: CaptureResult<Uri?>? = null
        val contentValues =
          ContentValues().apply {
            put(
              MediaStore.Images.Media.DISPLAY_NAME,
              "camposer_test_${System.currentTimeMillis()}.jpg",
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
          }

        runOnIdle {
          cameraController.takePicture(contentValues = contentValues) { result = it }
        }

        waitUntil(TAKE_PICTURE_TIMEOUT) { result != null }
        runOnIdle {
          val r = checkNotNull(result)
          if (r is CaptureResult.Error) throw r.throwable
          savedUri = (r as CaptureResult.Success).data
          assertTrue(r is CaptureResult.Success)
        }
      }
    } finally {
      savedUri?.let { context.contentResolver.delete(it, null, null) }
    }
  }

  @Test
  fun test_take_picture_with_output_file_options() {
    val outputFile = File(context.filesDir, "camposer_test_${System.currentTimeMillis()}.jpg")
    try {
      with(composeTestRule) {
        initImageCamera()

        val outputFileOptions = OutputFileOptions.Builder(outputFile).build()

        var result: CaptureResult<Uri?>? = null
        runOnIdle {
          cameraController.takePicture(outputFileOptions = outputFileOptions) { result = it }
        }

        waitUntil(TAKE_PICTURE_TIMEOUT) { result != null }
        runOnIdle {
          val r = checkNotNull(result)
          if (r is CaptureResult.Error) throw r.throwable
          assertEquals(Uri.fromFile(outputFile), (result as CaptureResult.Success).data)
          assertTrue(outputFile.exists())
        }
      }
    } finally {
      outputFile.delete()
    }
  }

  private fun ComposeContentTestRule.initImageCamera() =
    initCameraSession { state ->
      CameraPreview(
        cameraSession = state,
        captureMode = CaptureMode.Image,
      )
    }

  private companion object {
    private const val TAKE_PICTURE_TIMEOUT = 10_000L
  }
}
