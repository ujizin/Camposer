package com.ujizin.camposer.state

public expect class CameraState {
    internal var camSelector: CamSelector
    internal var captureMode: CaptureMode
    internal var imageCaptureMode: ImageCaptureMode
    internal var resolutionPreset: ResolutionPreset

    internal var flashMode: FlashMode
    internal var scaleType: ScaleType
    internal var implementationMode: ImplementationMode
    internal var imageAnalyzer: ImageAnalyzer?
    internal var isImageAnalysisEnabled: Boolean
    internal var isFocusOnTapEnabled: Boolean
    internal var enableTorch: Boolean
    public val initialExposure: Float
    public val isZoomSupported: Boolean

    public var isPinchToZoomEnabled: Boolean

    @Deprecated("Use ResolutionPreset instead")
    internal var imageCaptureTargetSize: ImageTargetSize?

    /**
     * Get max zoom from camera.
     * */
    public var maxZoom: Float
        private set

    /**
     * Get min zoom from camera.
     * */
    public var minZoom: Float
        private set

    public var exposureCompensation: Float?
        private set

    /**
     * Get min exposure from camera.
     * */
    public var minExposure: Float
        private set

    /**
     * Get max exposure from camera.
     * */
    public var maxExposure: Float
        private set

    /**
     * Check if compensation exposure is supported.
     * */
    public val isExposureSupported: Boolean

    /**
     * Check if camera is streaming or not.
     * */
    public var isStreaming: Boolean
        internal set

    /**
     * Check if focus on tap supported
     * */
    public var isFocusOnTapSupported: Boolean
        private set

    /**
     * Check if camera state is initialized or not.
     * */
    public var isInitialized: Boolean
        private set

    /**
     * Verify if camera has torch available.
     * */
    public var hasTorchAvailable: Boolean
        private set

    /**
     * Verify if camera has flash or not.
     * */
    public var hasFlashUnit: Boolean
        private set

    /**
     * Return true if it's muted.
     * */
    public var isMuted: Boolean
        private set
}
