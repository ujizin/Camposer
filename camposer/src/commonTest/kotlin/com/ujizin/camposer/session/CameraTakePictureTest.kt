package com.ujizin.camposer.session

import androidx.compose.ui.test.ExperimentalTestApi
import com.ujizin.camposer.CaptureResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
internal class CameraTakePictureTest : CameraSessionTest() {
  @Test
  fun test_preview_take_picture() {
    var isPictureTaken = false
    controller.takePicture {
      isPictureTaken = it is CaptureResult.Success
    }

    assertTrue(isPictureTaken)
  }

  @OptIn(ExperimentalUuidApi::class)
  @Test
  fun test_preview_take_picture_file() {
    val expectedFile = "${Uuid.random().toHexString()}.jpg"
    var actualFile = ""

    controller.takePicture(filename = expectedFile) {
      it as CaptureResult.Success
      actualFile = it.data
    }

    assertEquals(expectedFile, actualFile)
  }
}
