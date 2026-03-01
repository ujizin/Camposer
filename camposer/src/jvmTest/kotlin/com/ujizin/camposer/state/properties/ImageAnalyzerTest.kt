package com.ujizin.camposer.state.properties

import org.bytedeco.opencv.opencv_core.Mat
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ImageAnalyzerTest {

  @Test
  fun `analyzer callback is called with frame`() {
    var called = false
    val analyzer = ImageAnalyzer { _ -> called = true }
    analyzer.analyze(Mat())
    assertTrue(called)
  }
}
