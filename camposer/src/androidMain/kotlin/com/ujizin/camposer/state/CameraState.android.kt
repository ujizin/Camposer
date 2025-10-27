package com.ujizin.camposer.state

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraEffect
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
import com.ujizin.camposer.state.properties.CamSelector
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.ResolutionPreset
import com.ujizin.camposer.state.properties.ScaleType
import java.util.concurrent.Executor
import kotlin.math.roundToInt

@SuppressLint("UnsafeOptInUsageError")
public actual class CameraState internal constructor(
    context: Context,
    private val mainExecutor: Executor,
    private val controller: CameraController,
    private val cameraInfo: CameraInfo,
) {

    public actual var captureMode: CaptureMode by config(CaptureMode.Image) { mode ->
        controller.setEnabledUseCases(getUseCases(mode))
    }
        internal set

    public actual var imageCaptureStrategy: ImageCaptureStrategy by config(ImageCaptureStrategy.Balanced) { value ->
        val mode = when {
            value == ImageCaptureStrategy.MinLatency && !cameraInfo.isZeroShutterLagSupported -> value.fallback
            else -> value.mode
        }
        controller.imageCaptureMode = mode
    }
        internal set

    public actual var camSelector: CamSelector by config(
        value = CamSelector.Back,
        predicate = { old, new ->
            old != new && controller.hasCamera(new.selector) && !controller.isRecording
        },
        block = { controller.cameraSelector = it.selector }
    )
        internal set

    public actual var scaleType: ScaleType = ScaleType.FillCenter
        internal set

    public actual var flashMode: FlashMode by config(
        value = FlashMode.find(controller.imageCaptureFlashMode),
        predicate = { old, new -> old != new && new.isFlashSupported() }
    ) {
        mainExecutor.execute { controller.imageCaptureFlashMode = it.mode }
    }
        internal set

    public actual var resolutionPreset: ResolutionPreset by config(ResolutionPreset.Default) { value ->
        value.getQualitySelector()?.let { controller.videoCaptureQualitySelector = it }
        controller.imageCaptureResolutionSelector = value.getResolutionSelector()
        controller.previewResolutionSelector = value.getResolutionSelector()
    }
        internal set

    public actual var implementationMode: ImplementationMode = ImplementationMode.Performance
        internal set

    public actual var imageAnalyzer: ImageAnalyzer? by config(null) { analyzer ->
        controller.setImageAnalysisAnalyzer(mainExecutor, analyzer?.analyzer ?: return@config)
    }
        internal set

    public actual var isImageAnalyzerEnabled: Boolean by config(value = imageAnalyzer != null) {
        controller.setEnabledUseCases(getUseCases())
    }
        internal set

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(true)
        internal set

    public actual var isFocusOnTapEnabled: Boolean by config(true) {
        controller.isTapToFocusEnabled = it
    }
        internal set

    public actual var exposureCompensation: Float by config(
        value = 0F,
        onSet = { it.fastCoerceIn(cameraInfo.minExposure, cameraInfo.maxExposure) },
        block = ::setExposureCompensation,
    )
        internal set

    public actual var isTorchEnabled: Boolean by config(
        value = controller.torchState.value == TorchState.ON,
        predicate = { old, new -> old != new && (!new || cameraInfo.isTorchAvailable) },
        block = { mainExecutor.execute { controller.enableTorch(it) } }
    )
        internal set

    public actual var zoomRatio: Float by config(
        value = cameraInfo.minZoom,
        onSet = { it.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom) },
        block = ::setZoomRatio,
    )
        internal set

    public actual var orientationStrategy: OrientationStrategy by config(
        value = OrientationStrategy.Device,
        block = { /** Disable https://issuetracker.google.com/issues/449573627*/ }
    )
        internal set

    init {
        (context as LifecycleOwner).lifecycle.addObserver(CameraConfigSaver())
        controller.setEnabledUseCases(getUseCases())
    }

    public fun setEffects(effects: Set<CameraEffect>) {
        controller.setEffects(effects)
    }

    public fun clearEffects() {
        controller.clearEffects()
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

    internal actual fun resetConfig() {
        zoomRatio = cameraInfo.minZoom
        exposureCompensation = 0F
        flashMode = FlashMode.Off
        isTorchEnabled = false
    }
}

internal actual fun CameraSession.isToUpdateCameraInfo(
    isCamSelectorChanged: Boolean,
    isCaptureModeChanged: Boolean,
): Boolean = isCamSelectorChanged
