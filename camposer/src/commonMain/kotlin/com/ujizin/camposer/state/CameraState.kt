package com.ujizin.camposer.state

import com.ujizin.camposer.info.CameraInfo

public expect class CameraState {
    internal var camSelector: CamSelector
    internal var captureMode: CaptureMode
    internal var imageCaptureMode: ImageCaptureMode
    internal var resolutionPreset: ResolutionPreset

    internal var flashMode: FlashMode
    internal var scaleType: ScaleType
    internal var implementationMode: ImplementationMode
    internal var imageAnalyzer: ImageAnalyzer?
    internal var isImageAnalyzerEnabled: Boolean
    internal var isFocusOnTapEnabled: Boolean
    internal var enableTorch: Boolean

    public var isPinchToZoomEnabled: Boolean

    @Deprecated("Use ResolutionPreset instead")
    internal var imageCaptureTargetSize: ImageTargetSize?

    public val info: CameraInfo

    public var exposureCompensation: Float?
        private set

    /**
     * Check if camera is streaming or not.
     * */
    public var isStreaming: Boolean
        internal set

    /**
     * Check if camera state is initialized or not.
     * */
    public var isInitialized: Boolean
        private set

    /**
     * Return true if it's muted.
     * */
    public var isMuted: Boolean
        private set
}
