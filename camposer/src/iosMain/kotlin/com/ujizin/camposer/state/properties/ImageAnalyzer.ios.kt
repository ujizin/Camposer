package com.ujizin.camposer.state.properties

import com.ujizin.camposer.internal.core.ios.IOSCameraController
import platform.AVFoundation.AVCaptureOutput

public actual class ImageAnalyzer(
  internal val controller: IOSCameraController,
  internal val analyzer: Analyzer<*>,
) {
  public class Analyzer<out T : AVCaptureOutput>(
    public val output: T,
    public val onOutputAttached: (@UnsafeVariance T) -> Unit,
  )

  private var isOutputAdded = false

  internal var isEnabled: Boolean = true
    set(value) {
      if (value == isOutputAdded) return
      field = value
      if (value) add() else remove()
    }

  internal fun add() {
    controller.addOutput(analyzer.output)
    analyzer.onOutputAttached(analyzer.output)
    isOutputAdded = true
  }

  private fun remove() {
    controller.removeOutput(analyzer.output)
    isOutputAdded = false
  }

  public fun dispose() {
    isOutputAdded = false
    controller.removeOutput(analyzer.output)
  }
}
