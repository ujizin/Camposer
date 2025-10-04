package com.ujizin.camposer.state

import com.ujizin.camposer.session.IOSCameraSession
import platform.AVFoundation.AVCaptureOutput

public actual class ImageAnalyzer(
    internal val iosCameraSession: IOSCameraSession,
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
        iosCameraSession.addOutput(analyzer.output)
        analyzer.onOutputAttached(analyzer.output)
        isOutputAdded = true
    }

    private fun remove() {
        iosCameraSession.removeOutput(analyzer.output)
        isOutputAdded = false
    }

    public fun onDispose() {
        isOutputAdded = false
        iosCameraSession.removeOutput(analyzer.output)
    }
}