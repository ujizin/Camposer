package com.ujizin.camposer.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.extensions.withConfigurationLock
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.config.properties.CamSelector
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import com.ujizin.camposer.config.properties.FlashMode
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.config.properties.ImplementationMode
import com.ujizin.camposer.config.properties.ResolutionPreset
import com.ujizin.camposer.config.properties.ScaleType
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.videoZoomFactor

@OptIn(ExperimentalForeignApi::class)
public actual class CameraConfig(
    private val iosCameraSession: IOSCameraSession,
    private val cameraInfo: CameraInfo,
    internal var rebindCamera: () -> Unit = {},
) {
    public actual var captureMode: CaptureMode by config(
        value = CaptureMode.Image,
        onDispose = { iosCameraSession.removeOutput(it.output) },
        block = {
            iosCameraSession.addOutput(it.output)
            rebindCamera()
        },
    )
        internal set

    public actual var camSelector: CamSelector by config(CamSelector.Back) {
        iosCameraSession.setCameraPosition(it.position)
        rebindCamera()
    }
        internal set

    public actual var scaleType: ScaleType = ScaleType.FillCenter
        internal set

    public actual var flashMode: FlashMode by config(FlashMode.Off) {
        iosCameraSession.setFlashMode(it.mode)
    }
        internal set

    public actual var resolutionPreset: ResolutionPreset by config(ResolutionPreset.Default) {
        iosCameraSession.setCameraPreset(it.presets.toList())
    }
        internal set

    // No-op in iOS
    public actual var implementationMode: ImplementationMode = ImplementationMode.Performance
        internal set

    public actual var imageAnalyzer: ImageAnalyzer? by config(
        value = null,
        onDispose = { it?.onDispose() }
    ) {
        imageAnalyzer?.isEnabled = isImageAnalyzerEnabled
    }
        internal set

    public actual var isImageAnalyzerEnabled: Boolean by config(

        value = imageAnalyzer != null
    ) { isEnabled ->
        imageAnalyzer?.isEnabled = isEnabled
    }
        internal set

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(true)
        internal set

    public actual var exposureCompensation: Float? by config(
        value = null,
        predicate = { old, new -> new != null && old != new },
    ) { value ->
        iosCameraSession.device.withConfigurationLock {
            setExposureTargetBias(
                bias = value!!.coerceIn(cameraInfo.minExposure, cameraInfo.maxExposure),
                completionHandler = {},
            )
        }
    }
        internal set

    public actual var imageCaptureStrategy: ImageCaptureStrategy by config(ImageCaptureStrategy.Balanced) { value ->
        iosCameraSession.setCameraOutputQuality(
            quality = value.strategy,
            highResolutionEnabled = value.highResolutionEnabled
        )
    }
        internal set

    public actual var zoomRatio: Float by config(cameraInfo.minZoom) {
        iosCameraSession.device.withConfigurationLock {
            videoZoomFactor = it.coerceIn(cameraInfo.minZoom, cameraInfo.maxZoom).toDouble()
        }
    }
        internal set

    public actual var isFocusOnTapEnabled: Boolean by mutableStateOf(true)
        internal set

    public actual var isTorchEnabled: Boolean by config(false) {
        iosCameraSession.setTorchEnabled(it)
    }
        internal set
}