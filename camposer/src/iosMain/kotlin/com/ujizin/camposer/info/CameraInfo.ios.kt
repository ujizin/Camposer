package com.ujizin.camposer.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.session.IOSCameraSession
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.hasFlash
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isFlashAvailable
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.maxAvailableVideoZoomFactor
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minAvailableVideoZoomFactor
import platform.AVFoundation.minExposureTargetBias

public actual class CameraInfo(
    private val cameraSession: IOSCameraSession,
) {
    public actual var isImageAnalyzerSupported: Boolean by mutableStateOf(true)
        private set

    public actual var isFocusOnTapSupported: Boolean by mutableStateOf(true)
        private set

    public actual val isZoomSupported: Boolean by mutableStateOf(true)
        private set

    public actual var minZoom: Float by mutableFloatStateOf(1F)
        private set

    public actual var maxZoom: Float by mutableFloatStateOf(1F)
        private set

    public actual val isExposureSupported: Boolean by mutableStateOf(true)
        private set

    public actual var minExposure: Float by mutableFloatStateOf(0F)
        private set

    public actual var maxExposure: Float by mutableFloatStateOf(0F)
        private set

    public actual var isFlashSupported: Boolean by mutableStateOf(false)
        private set

    public actual var isFlashAvailable: Boolean by mutableStateOf(false)
        private set

    public actual var isTorchSupported: Boolean by mutableStateOf(false)
        private set

    public actual var isTorchAvailable: Boolean by mutableStateOf(false)
        private set

    public actual var isZeroShutterLagSupported: Boolean by mutableStateOf(false)
        private set

    internal fun rebind(
        output: AVCaptureOutput,
    ) {
        isFocusOnTapSupported = cameraSession.isFocusOnTapSupported
        minZoom = cameraSession.device.minAvailableVideoZoomFactor.toFloat()
        maxZoom = cameraSession.device.maxAvailableVideoZoomFactor.toFloat()
        minExposure = cameraSession.device.minExposureTargetBias
        maxExposure = cameraSession.device.maxExposureTargetBias
        isFlashSupported = cameraSession.device.hasFlash
        isFlashAvailable = cameraSession.device.isFlashAvailable()
        isTorchSupported = cameraSession.device.hasTorch
        isTorchAvailable = cameraSession.device.isTorchAvailable()
        isZeroShutterLagSupported = (output as? AVCapturePhotoOutput)?.isZeroShutterLagSupported()
            ?: false
    }
}