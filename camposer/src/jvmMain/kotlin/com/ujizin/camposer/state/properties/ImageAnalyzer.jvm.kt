package com.ujizin.camposer.state.properties

import androidx.compose.runtime.Stable
import org.bytedeco.opencv.opencv_core.Mat

@Stable
public actual class ImageAnalyzer(
  internal val onAnalyze: (Mat) -> Unit,
) {
  internal fun analyze(mat: Mat) = onAnalyze(mat)
}
