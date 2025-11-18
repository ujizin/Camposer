package com.ujizin.camposer.info

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.state.properties.CameraData

public actual class CameraInfo internal constructor(internal val cameraInfo: AndroidCameraInfo) {

    public actual val isZoomSupported: Boolean by derivedStateOf { maxZoom != cameraInfo.initialZoom }

    public actual var minZoom: Float by mutableFloatStateOf(cameraInfo.minZoom)
        private set
    public actual var maxZoom: Float by mutableFloatStateOf(cameraInfo.maxZoom)
        private set

    public actual val isExposureSupported: Boolean by derivedStateOf { maxExposure != cameraInfo.initialExposure }
    public actual var minExposure: Float by mutableFloatStateOf(cameraInfo.minExposure)
        private set
    public actual var maxExposure: Float by mutableFloatStateOf(cameraInfo.maxExposure)
        private set
    public actual var isFlashSupported: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
        internal set
    public actual var isFlashAvailable: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
        private set
    public actual var isTorchSupported: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
        private set
    public actual var isTorchAvailable: Boolean by mutableStateOf(cameraInfo.isFlashSupported)
        private set
    public actual var isZeroShutterLagSupported: Boolean by mutableStateOf(cameraInfo.isZeroShutterLagSupported)
        private set

    public actual var isFocusSupported: Boolean by mutableStateOf(cameraInfo.isFocusSupported)
        private set

    public actual var photoFormats: List<CameraData> = emptyList()
        private set

    public actual var videoFormats: List<CameraData> = emptyList()
        private set

    internal fun rebind() {
        minZoom = cameraInfo.minZoom
        maxZoom = cameraInfo.maxZoom
        minExposure = cameraInfo.minExposure
        maxExposure = cameraInfo.maxExposure
        isFlashSupported = cameraInfo.isFlashSupported
        isFlashAvailable = cameraInfo.isFlashSupported
        isTorchSupported = cameraInfo.isFlashSupported
        isTorchAvailable = cameraInfo.isFlashSupported
        isFocusSupported = cameraInfo.isFocusSupported
        isZeroShutterLagSupported = cameraInfo.isZeroShutterLagSupported
        photoFormats = cameraInfo.photoFormats
        videoFormats = cameraInfo.videoFormats
    }
}
