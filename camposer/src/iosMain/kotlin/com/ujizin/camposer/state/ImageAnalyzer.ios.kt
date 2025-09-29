package com.ujizin.camposer.state

import com.ujizin.camposer.controller.IOSCameraManager
import platform.AVFoundation.AVCaptureOutput

public actual class ImageAnalyzer(
    internal val cameraManager: IOSCameraManager,
    internal val analyzer: Analyzer<*>,
) {

    public class Analyzer<out T : AVCaptureOutput>(
        public val output: T,
        public val onOutputAttached: (@UnsafeVariance T) -> Unit,
    )

    private var isOutputAdded = false

    internal var isEnabled: Boolean = true
        set(value) {
            if (isEnabled == isOutputAdded) return
            field = value
            if (value) add() else remove()
        }

    internal fun add() {
        cameraManager.addOutput(analyzer.output)
        analyzer.onOutputAttached(analyzer.output)
        isOutputAdded = true
    }

    private fun remove() {
        cameraManager.removeOutput(analyzer.output)
        isOutputAdded = false
    }

    public fun onDispose() {
        isOutputAdded = false
        cameraManager.removeOutput(analyzer.output)
    }
}