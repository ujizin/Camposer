package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.utils.createFakeImageAnalyzer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class CameraImageAnalyzerTest : CameraSessionTest() {
  @Test
  fun test_preview_image_analyzer_set() {
    var isImageAnalyzedCalled = false
    val imageAnalyzer: ImageAnalyzer? = createFakeImageAnalyzer(cameraSession) {
      isImageAnalyzedCalled = true
    }

    updateSession(imageAnalyzer = imageAnalyzer)

    cameraTest.assertImageAnalyzer(imageAnalyzer)
    assertTrue(isImageAnalyzedCalled)
    assertNotNull(cameraSession.state.imageAnalyzer.value)
    assertEquals(imageAnalyzer, cameraSession.state.imageAnalyzer.value)
  }
}
