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
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.Default
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.sync.Mutex
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.videoZoomFactor
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState(
    private val iosCameraSession: IOSCameraSession,
    private val cameraInfo: CameraInfo,
) {

    private val cameraMutex = Mutex()

    public actual var captureMode: CaptureMode by asyncCameraConfig(
        mutex = cameraMutex,
        value = CaptureMode.Image,
        onDispose = { iosCameraSession.removeOutput(it.output) },
        block = {
            iosCameraSession.addOutput(it.output)
            updateConfig(captureModeChanged = true)
        },
    )
        internal set

    public actual var camSelector: CamSelector by asyncCameraConfig(
        mutex = cameraMutex,
        value = CamSelector.Back
    ) {
        iosCameraSession.setCameraSelector(it.position)
        updateConfig(camSelectorChanged = true)
    }
        internal set

    public actual var scaleType: ScaleType by distinctConfig(ScaleType.FillCenter) {
        iosCameraSession.setPreviewGravity(it.gravity)
    }
        internal set

    public actual var flashMode: FlashMode by distinctConfig(
        value = FlashMode.Off,
        check = { check(it.isFlashAvailable()) { "Flash must be supported to be enabled" } },
        predicate = { old, new -> old != new && new.isFlashAvailable() }
    ) {
        iosCameraSession.setFlashMode(it.mode)
    }
        internal set

    public actual var camFormat: CamFormat by distinctConfig(
        value = CamFormat.Default
    ) { setDeviceFormat(camFormat) }
        internal set

    // No-op in iOS
    public actual var implementationMode: ImplementationMode by mutableStateOf(ImplementationMode.Performance)
        internal set

    public actual var imageAnalyzer: ImageAnalyzer? by distinctConfig(
        value = null,
        onDispose = { it?.onDispose() }
    ) {
        imageAnalyzer?.isEnabled = isImageAnalyzerEnabled
    }
        internal set

    public actual var isImageAnalyzerEnabled: Boolean by distinctConfig(
        value = imageAnalyzer != null
    ) { isEnabled ->
        imageAnalyzer?.isEnabled = isEnabled
    }
        internal set

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(true)
        internal set

    public actual var exposureCompensation: Float by distinctConfig(
        0F,
        onSet = { it.fastCoerceIn(cameraInfo.minExposure, cameraInfo.maxExposure) },
        block = {
            iosCameraSession.device.withConfigurationLock {
                setExposureTargetBias(bias = it, completionHandler = {})
            }
        },
    )
        internal set

    public actual var imageCaptureStrategy: ImageCaptureStrategy by distinctConfig(ImageCaptureStrategy.Balanced) { value ->
        iosCameraSession.setCameraOutputQuality(
            quality = value.strategy,
            highResolutionEnabled = value.highResolutionEnabled
        )
    }
        internal set

    public actual var zoomRatio: Float by distinctConfig(
        value = cameraInfo.minZoom,
        onSet = { it.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom) }
    ) {
        iosCameraSession.device.withConfigurationLock { videoZoomFactor = it.toDouble() }
    }
        internal set

    public actual var isFocusOnTapEnabled: Boolean by distinctConfig(value = true)
        internal set

    public actual var isTorchEnabled: Boolean by distinctConfig(
        value = false,
        check = { check((!it || cameraInfo.isTorchAvailable)) { "Torch must be supported to enable" } },
        predicate = { old, new -> old != new && (!new || cameraInfo.isTorchAvailable) }
    ) {
        iosCameraSession.setTorchEnabled(it)
    }
        internal set

    public actual var orientationStrategy: OrientationStrategy by distinctConfig(OrientationStrategy.Device)
        internal set

    public actual var frameRate: Int by distinctConfig(
        value = -1,
        check = {
            check(it in cameraInfo.minFPS..cameraInfo.maxFPS) {
                "FPS $it must be in range ${cameraInfo.minFPS..cameraInfo.maxFPS}"
            }
        },
        block = ::setFrameRate,
    )
        internal set

    public actual var videoStabilizationMode: VideoStabilizationMode by distinctConfig(
        value = VideoStabilizationMode.Off,
        check = {
            check(iosCameraSession.isVideoStabilizationSupported(it.value)) {
                "Video stabilization mode must be supported to enable"
            }
        },
        block = ::setStabilizationMode,
    )
        internal set

    init {
        iosCameraSession.setPreviewGravity(scaleType.gravity)
    }

    private fun updateConfig(
        captureModeChanged: Boolean = false,
        camSelectorChanged: Boolean = false,
    ) {
        resetConfig()

        if (captureModeChanged || camSelectorChanged) {
            setDeviceFormat(camFormat)
        }
    }

    internal actual fun resetConfig() {
        cameraInfo.rebind(output = captureMode.output)

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

    private fun setDeviceFormat(camFormat: CamFormat) {
        camFormat.applyConfigs(
            cameraInfo = cameraInfo,
            iosCameraSession = iosCameraSession,
            onDeviceFormatUpdated = { cameraInfo.rebind(output = captureMode.output) },
            onStabilizationModeChanged = ::setStabilizationMode,
            onFrameRateChanged = ::setFrameRate,
        )
    }

    private fun setFrameRate(fps: Int) = when {
        frameRate != fps -> frameRate = fps
        else -> iosCameraSession.setFrameRate(frameRate)
    }

    private fun setStabilizationMode(mode: VideoStabilizationMode) = when {
        mode != videoStabilizationMode -> videoStabilizationMode = mode
        else -> iosCameraSession.setVideoStabilization(mode.value)
    }

    private fun FlashMode.isFlashAvailable() = this == FlashMode.Off || cameraInfo.isFlashAvailable
}

internal actual fun CameraSession.isToResetConfig(
    isCamSelectorChanged: Boolean,
    isCaptureModeChanged: Boolean,
) = isCamSelectorChanged || isCaptureModeChanged
