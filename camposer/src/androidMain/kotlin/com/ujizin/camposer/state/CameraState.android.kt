package com.ujizin.camposer.state

import android.annotation.SuppressLint
import android.content.Context
import android.util.Range
import androidx.camera.core.CameraEffect
import androidx.camera.core.ExperimentalSessionConfig
import androidx.camera.core.TorchState
import androidx.camera.view.CameraController
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastCoerceIn
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.selector.CamSelector
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
import java.util.concurrent.Executor
import kotlin.math.roundToInt

@SuppressLint("UnsafeOptInUsageError")
public actual class CameraState internal constructor(
    context: Context,
    private val mainExecutor: Executor,
    private val controller: CameraController,
    private val cameraInfo: CameraInfo,
) {

    public actual var captureMode: CaptureMode by distinctConfig(CaptureMode.Image) { mode ->
        controller.setEnabledUseCases(getUseCases(mode))
        setCamFormat(camFormat)
    }
        internal set

    public actual var imageCaptureStrategy: ImageCaptureStrategy by distinctConfig(
        ImageCaptureStrategy.Balanced
    ) { value ->
        val mode = when {
            value == ImageCaptureStrategy.MinLatency && !cameraInfo.isZeroShutterLagSupported -> value.fallback
            else -> value.mode
        }
        controller.imageCaptureMode = mode
    }
        internal set

    public actual var camSelector: CamSelector by distinctConfig(
        check = { new ->
            check(controller.hasCamera(new.selector)) {
                "Camera with position ${new.camPosition} not found. Please ensure the device has this camera available."
            }
        },
        value = CamSelector.Back,
        predicate = { old, new -> old != new && !controller.isRecording },
        block = ::setCamSelector
    )
        internal set

    public actual var scaleType: ScaleType = ScaleType.FillCenter
        internal set

    public actual var flashMode: FlashMode by distinctConfig(
        value = FlashMode.find(controller.imageCaptureFlashMode),
        check = { check(it.isFlashSupported()) { "Flash must be supported to be enabled" } },
        predicate = { old, new -> old != new && new.isFlashSupported() }
    ) {
        mainExecutor.execute { controller.imageCaptureFlashMode = it.mode }
    }
        internal set

    public actual var camFormat: CamFormat by distinctConfig(CamFormat.Default) { format ->
        setCamFormat(format)
    }
        internal set

    public actual var implementationMode: ImplementationMode = ImplementationMode.Performance
        internal set

    public actual var imageAnalyzer: ImageAnalyzer? by distinctConfig(null) { analyzer ->
        controller.setImageAnalysisAnalyzer(
            mainExecutor,
            analyzer?.analyzer ?: return@distinctConfig
        )
    }
        internal set

    public actual var isImageAnalyzerEnabled: Boolean by distinctConfig(value = imageAnalyzer != null) {
        controller.setEnabledUseCases(getUseCases())
    }
        internal set

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(true)
        internal set

    public actual var isFocusOnTapEnabled: Boolean by distinctConfig(
        value = cameraInfo.isFocusSupported,
    ) {
        controller.isTapToFocusEnabled = it
    }
        internal set

    public actual var exposureCompensation: Float by distinctConfig(
        value = 0F,
        onSet = { it.fastCoerceIn(cameraInfo.minExposure, cameraInfo.maxExposure) },
        block = ::setExposureCompensation,
    )
        internal set

    public actual var isTorchEnabled: Boolean by distinctConfig(
        value = controller.torchState.value == TorchState.ON,
        check = { check(!it || cameraInfo.isTorchAvailable) { "Torch must be supported to enable" } },
        predicate = { old, new -> old != new && (!new || cameraInfo.isTorchAvailable) },
        block = { mainExecutor.execute { controller.enableTorch(it) } }
    )
        internal set

    public actual var zoomRatio: Float by distinctConfig(
        value = cameraInfo.minZoom,
        onSet = { it.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom) },
        block = ::setZoomRatio,
    )
        internal set

    public actual var orientationStrategy: OrientationStrategy by distinctConfig(
        value = OrientationStrategy.Device,
        block = { /** TODO Not supported yet - https://issuetracker.google.com/issues/449573627*/ }
    )
        internal set

    public actual var frameRate: Int by distinctConfig(
        value = cameraInfo.maxFPS,
        check = {
            check(it in cameraInfo.minFPS..cameraInfo.maxFPS) {
                "FPS must be in range ${cameraInfo.minFPS..cameraInfo.maxFPS}"
            }
        },
        block = ::setFrameRate
    )
        internal set

    @OptIn(ExperimentalSessionConfig::class)
    public actual var videoStabilizationMode: VideoStabilizationMode by distinctConfig(
        check = { throw NotImplementedError("Video stabilization mode is not implemented in Android yet") },
        value = VideoStabilizationMode.Off,
    ) {
        // FIXME Not supported yet - https://issuetracker.google.com/issues/457465859
    }
        internal set

    init {
        (context as LifecycleOwner).lifecycle.addObserver(CameraConfigSaver())
        controller.setEnabledUseCases(getUseCases())
        controller.isTapToFocusEnabled = isFocusOnTapEnabled
        setCamSelector(camSelector)
    }

    public fun setEffects(effects: Set<CameraEffect>) {
        controller.setEffects(effects)
    }

    public fun clearEffects() {
        controller.clearEffects()
    }

    private fun setCamSelector(selector: CamSelector) {
        controller.cameraSelector = selector.selector
        cameraInfo.rebind()
    }

    private fun setCamFormat(format: CamFormat) {
        format.applyConfigs(
            cameraInfo = cameraInfo,
            controller = controller,
            onFrameRateChanged = ::setFrameRate,
            onStabilizationModeChanged = ::setStabilizationMode,
        )
    }

    private fun setStabilizationMode(mode: VideoStabilizationMode) {
        if (videoStabilizationMode != mode) {
            videoStabilizationMode = mode
            return
        }

        // TODO CameraX controller does not support yet :(
    }

    private fun setFrameRate(fps: Int) {
        if (frameRate != fps) {
            frameRate = fps
            return
        }

        controller.videoCaptureTargetFrameRate = Range(fps, fps)
    }

    private fun setExposureCompensation(exposureCompensation: Float) {
        mainExecutor.execute {
            controller.cameraControl?.setExposureCompensationIndex(
                exposureCompensation.roundToInt(),
            )
        }
    }

    private fun setZoomRatio(zoomRatio: Float) {
        mainExecutor.execute { controller.setZoomRatio(zoomRatio) }
    }

    private fun getUseCases(mode: CaptureMode = captureMode) = when {
        isImageAnalyzerEnabled && mode != CaptureMode.Video -> mode.value or IMAGE_ANALYSIS
        else -> mode.value
    }

    private fun FlashMode.isFlashSupported(): Boolean =
        this == FlashMode.Off || cameraInfo.isFlashSupported

    internal actual fun resetConfig() {
        zoomRatio = cameraInfo.minZoom
        exposureCompensation = 0F
        flashMode = FlashMode.Off
        isTorchEnabled = false
    }

    internal inner class CameraConfigSaver : DefaultLifecycleObserver {

        private var hasPaused: Boolean = false

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if (!hasPaused) return
            setZoomRatio(zoomRatio.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom))
            setExposureCompensation(exposureCompensation)
        }

        override fun onPause(owner: LifecycleOwner) {
            hasPaused = true
            super.onPause(owner)
        }
    }
}

internal actual fun CameraSession.isToResetConfig(
    isCamSelectorChanged: Boolean,
    isCaptureModeChanged: Boolean,
): Boolean = isCamSelectorChanged
