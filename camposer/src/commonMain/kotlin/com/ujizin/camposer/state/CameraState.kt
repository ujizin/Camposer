package com.ujizin.camposer.state

import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.CamSelector
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat

public expect class CameraState {
    public var isImageAnalyzerEnabled: Boolean
        internal set
    public var captureMode: CaptureMode
        internal set
    public var imageCaptureStrategy: ImageCaptureStrategy
        internal set
    public var camSelector: CamSelector
        internal set
    public var scaleType: ScaleType
        internal set
    public var flashMode: FlashMode
        internal set
    public var camFormat: CamFormat
        internal set
    public var implementationMode: ImplementationMode
        internal set
    public var imageAnalyzer: ImageAnalyzer?
        internal set
    public var zoomRatio: Float
        internal set
    public var exposureCompensation: Float
        internal set
    public var isPinchToZoomEnabled: Boolean
        internal set
    public var isFocusOnTapEnabled: Boolean
        internal set
    public var isTorchEnabled: Boolean
        internal set
    public var orientationStrategy: OrientationStrategy
        internal set
    public var frameRate: Int
        internal set
    public var videoStabilizationMode: VideoStabilizationMode
        internal set

    internal fun resetConfig()
}

internal expect fun CameraSession.isToResetConfig(
    isCamSelectorChanged: Boolean,
    isCaptureModeChanged: Boolean,
): Boolean

internal fun CameraSession.update(
    camSelector: CamSelector,
    captureMode: CaptureMode,
    scaleType: ScaleType,
    isImageAnalysisEnabled: Boolean,
    imageAnalyzer: ImageAnalyzer?,
    implementationMode: ImplementationMode,
    isFocusOnTapEnabled: Boolean,
    imageCaptureStrategy: ImageCaptureStrategy,
    camFormat: CamFormat,
    isPinchToZoomEnabled: Boolean,
) {
    val isCamSelectorChanged = state.camSelector != camSelector
    val isCaptureModeChanged = state.captureMode != captureMode

    val isToUpdateCamera = isToResetConfig(isCamSelectorChanged, isCaptureModeChanged)
    with(state) {
        if (isToUpdateCamera) {
            resetConfig()
        }

        this.camSelector = camSelector
        this.camFormat = camFormat
        this.captureMode = captureMode
        this.scaleType = scaleType
        this.isImageAnalyzerEnabled = isImageAnalysisEnabled
        this.imageAnalyzer = imageAnalyzer
        this.implementationMode = implementationMode
        this.isFocusOnTapEnabled = isFocusOnTapEnabled
        this.imageCaptureStrategy = imageCaptureStrategy
        this.isPinchToZoomEnabled = isPinchToZoomEnabled
    }
}
