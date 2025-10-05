package com.ujizin.camposer.config

import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CaptureMode
import com.ujizin.camposer.state.ImageCaptureStrategy
import com.ujizin.camposer.state.FlashMode
import com.ujizin.camposer.state.ImageAnalyzer
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ResolutionPreset
import com.ujizin.camposer.state.ScaleType

public expect class CameraConfig {
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
    public var resolutionPreset: ResolutionPreset
        internal set
    public var implementationMode: ImplementationMode
        internal set
    public var imageAnalyzer: ImageAnalyzer?
        internal set
    public var zoomRatio: Float
        internal set
    public var exposureCompensation: Float?
        internal set
    public var isPinchToZoomEnabled: Boolean
        internal set
    public var isFocusOnTapEnabled: Boolean
        internal set
    public var isTorchEnabled: Boolean
        internal set
}

internal fun CameraConfig.update(
    camSelector: CamSelector,
    captureMode: CaptureMode,
    scaleType: ScaleType,
    isImageAnalysisEnabled: Boolean,
    imageAnalyzer: ImageAnalyzer?,
    implementationMode: ImplementationMode,
    isFocusOnTapEnabled: Boolean,
    flashMode: FlashMode,
    zoomRatio: Float,
    imageCaptureStrategy: ImageCaptureStrategy,
    isTorchEnabled: Boolean,
    exposureCompensation: Float?,
    resolutionPreset: ResolutionPreset,
    isPinchToZoomEnabled: Boolean,
) {
    this.camSelector = camSelector
    this.captureMode = captureMode
    this.scaleType = scaleType
    this.isImageAnalyzerEnabled = isImageAnalysisEnabled
    this.imageAnalyzer = imageAnalyzer
    this.implementationMode = implementationMode
    this.isFocusOnTapEnabled = isFocusOnTapEnabled
    this.flashMode = flashMode
    this.zoomRatio = zoomRatio
    this.imageCaptureStrategy = imageCaptureStrategy
    this.isTorchEnabled = isTorchEnabled
    this.exposureCompensation = exposureCompensation
    this.resolutionPreset = resolutionPreset
    this.isPinchToZoomEnabled = isPinchToZoomEnabled
}