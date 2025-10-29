package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastCoerceIn
import com.ujizin.camposer.extensions.toVideoOrientation
import com.ujizin.camposer.extensions.withConfigurationLock
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.session.IOSCameraSession
import com.ujizin.camposer.state.properties.CamSelector
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.ResolutionPreset
import com.ujizin.camposer.state.properties.ScaleType
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.videoZoomFactor
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState(
    private val iosCameraSession: IOSCameraSession,
    private val cameraInfo: CameraInfo,
) {
    public actual var captureMode: CaptureMode by asyncConfig(
        value = CaptureMode.Image,
        onDispose = { iosCameraSession.removeOutput(it.output) },
        block = { iosCameraSession.addOutput(it.output) },
    )
        internal set

    public actual var camSelector: CamSelector by asyncConfig(CamSelector.Back) {
        iosCameraSession.setCameraPosition(it.position)
    }
        internal set

    public actual var scaleType: ScaleType by config(ScaleType.FillCenter) {
        iosCameraSession.setPreviewGravity(it.gravity)
    }
        internal set

    public actual var flashMode: FlashMode by config(
        value = FlashMode.Off,
        predicate = { old, new -> old != new && new.isFlashAvailable() }
    ) {
        iosCameraSession.setFlashMode(it.mode)
    }
        internal set

    public actual var resolutionPreset: ResolutionPreset by config(ResolutionPreset.Default) {
        iosCameraSession.setCameraPreset(it.presets.toList())
    }
        internal set

    // No-op in iOS
    public actual var implementationMode: ImplementationMode by mutableStateOf(ImplementationMode.Performance)
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

    public actual var exposureCompensation: Float by config(
        0F,
        onSet = { it.fastCoerceIn(cameraInfo.minExposure, cameraInfo.maxExposure) },
        block = {
            iosCameraSession.device.withConfigurationLock {
                setExposureTargetBias(bias = it, completionHandler = {})
            }
        },
    )
        internal set

    public actual var imageCaptureStrategy: ImageCaptureStrategy by config(ImageCaptureStrategy.Balanced) { value ->
        iosCameraSession.setCameraOutputQuality(
            quality = value.strategy,
            highResolutionEnabled = value.highResolutionEnabled
        )
    }
        internal set

    public actual var zoomRatio: Float by config(
        value = cameraInfo.minZoom,
        onSet = { it.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom) }
    ) {
        iosCameraSession.device.withConfigurationLock { videoZoomFactor = it.toDouble() }
    }
        internal set

    public actual var isFocusOnTapEnabled: Boolean by mutableStateOf(true)
        internal set

    public actual var isTorchEnabled: Boolean by config(
        false,
        predicate = { old, new -> old != new && (!new || cameraInfo.isTorchAvailable) }
    ) {
        iosCameraSession.setTorchEnabled(it)
    }
        internal set

    public actual var orientationStrategy: OrientationStrategy by config(OrientationStrategy.Device)
        internal set

    init {
        iosCameraSession.setPreviewGravity(scaleType.gravity)
    }

    internal actual fun resetConfig() {
        zoomRatio = cameraInfo.minZoom
        exposureCompensation = 0F
        flashMode = FlashMode.Off
        isTorchEnabled = false
        iosCameraSession.setCameraOutputQuality(
            quality = imageCaptureStrategy.strategy,
            highResolutionEnabled = imageCaptureStrategy.highResolutionEnabled,
        )
    }

    internal fun getCurrentVideoOrientation() = when (orientationStrategy) {
        OrientationStrategy.Device -> iosCameraSession.orientationListener.currentOrientation.toVideoOrientation()
        OrientationStrategy.Preview -> UIApplication.sharedApplication.statusBarOrientation.toVideoOrientation()
    }

    private fun FlashMode.isFlashAvailable() = this == FlashMode.Off || cameraInfo.isFlashAvailable
}

internal actual fun CameraSession.isToUpdateCameraInfo(
    isCamSelectorChanged: Boolean,
    isCaptureModeChanged: Boolean,
) = isCamSelectorChanged || isCaptureModeChanged
