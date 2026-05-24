package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.utils.createFakeImageAnalyzer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

  @Test
  fun test_preview_image_analyzer_cleared() {
    val imageAnalyzer = createFakeImageAnalyzer(cameraSession) {}
    updateSession(imageAnalyzer = imageAnalyzer)

    updateSession(imageAnalyzer = null, isImageAnalysisEnabled = false)

    assertNull(cameraSession.state.imageAnalyzer.value)
    assertFalse(cameraSession.state.isImageAnalyzerEnabled.value)
  }

  @Test
  fun test_preview_image_analyzer_enabled_true() {
    val imageAnalyzer = createFakeImageAnalyzer(cameraSession) {}
    updateSession(imageAnalyzer = imageAnalyzer, isImageAnalysisEnabled = true)

    assertTrue(cameraSession.state.isImageAnalyzerEnabled.value)
  }

  @Test
  fun test_preview_image_analyzer_enabled_false() {
    val imageAnalyzer = createFakeImageAnalyzer(cameraSession) {}
    updateSession(imageAnalyzer = imageAnalyzer, isImageAnalysisEnabled = true)

    updateSession(imageAnalyzer = imageAnalyzer, isImageAnalysisEnabled = false)

    assertFalse(cameraSession.state.isImageAnalyzerEnabled.value)
  }
}
