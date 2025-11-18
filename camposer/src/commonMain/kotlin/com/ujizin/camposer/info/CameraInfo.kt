package com.ujizin.camposer.info

import com.ujizin.camposer.state.properties.CameraData

public expect class CameraInfo {
    public val isZoomSupported: Boolean

    public val isExposureSupported: Boolean

    public var minZoom: Float
        private set
    public var maxZoom: Float
        private set
    public var minExposure: Float
        private set
    public var maxExposure: Float
        private set
    public var isFlashSupported: Boolean
        private set
    public var isFlashAvailable: Boolean
        private set
    public var isTorchSupported: Boolean
        private set
    public var isTorchAvailable: Boolean
        private set
    public var isZeroShutterLagSupported: Boolean
        private set

    public var isFocusSupported: Boolean
        private set

    public var photoFormats: List<CameraData>
        private set
    public var videoFormats: List<CameraData>
        private set
}
