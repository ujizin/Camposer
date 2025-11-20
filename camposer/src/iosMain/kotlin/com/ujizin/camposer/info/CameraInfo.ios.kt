package com.ujizin.camposer.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.utils.CameraFormatUtils
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVFrameRateRange
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

    public actual val isZoomSupported: Boolean by mutableStateOf(true)

    public actual var minZoom: Float by mutableFloatStateOf(1F)
        private set

    public actual var maxZoom: Float by mutableFloatStateOf(1F)
        private set

    public actual val isExposureSupported: Boolean by mutableStateOf(true)

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

    public actual var isFocusSupported: Boolean by mutableStateOf(false)
        private set

    private val formats: List<AVCaptureDeviceFormat>
        get() = cameraSession.device.formats.filterIsInstance<AVCaptureDeviceFormat>()

    public actual var photoFormats: List<CameraData> = listOf()
        get() = CameraFormatUtils.getPhotoFormats(formats)
        private set

    public actual var videoFormats: List<CameraData> = listOf()
        get() = CameraFormatUtils.getVideoFormats(formats)
        private set

    public actual var minFPS: Int by mutableIntStateOf(-1)
        internal set

    public actual var maxFPS: Int by mutableIntStateOf(-1)
        internal set

    internal val allFormats: List<CameraData>
        get() = (photoFormats + videoFormats).distinct()

    @OptIn(ExperimentalForeignApi::class)
    internal fun rebind(
        output: AVCaptureOutput,
    ): Unit = with(cameraSession) {
        val fpsRange = device.activeFormat.videoSupportedFrameRateRanges
            .filterIsInstance<AVFrameRateRange>()

        minZoom = device.minAvailableVideoZoomFactor.toFloat()
        maxZoom = device.maxAvailableVideoZoomFactor.toFloat()
        minExposure = device.minExposureTargetBias
        maxExposure = device.maxExposureTargetBias
        minFPS = fpsRange.minOf { it.minFrameRate }.toInt()
        maxFPS = fpsRange.maxOf { it.maxFrameRate }.toInt()
        this@CameraInfo.isFocusSupported = isFocusSupported
        isFlashSupported = device.hasFlash
        isFlashAvailable = device.isFlashAvailable()
        isTorchSupported = device.hasTorch
        isTorchAvailable = device.isTorchAvailable()
        isZeroShutterLagSupported = (output as? AVCapturePhotoOutput)?.isZeroShutterLagSupported()
            ?: false
    }
}