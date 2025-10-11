package com.ujizin.camposer.config

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
import com.ujizin.camposer.config.properties.CamSelector
import com.ujizin.camposer.config.properties.CaptureMode
import com.ujizin.camposer.config.properties.FlashMode
import com.ujizin.camposer.config.properties.ImageAnalyzer
import com.ujizin.camposer.config.properties.ImageCaptureStrategy
import com.ujizin.camposer.config.properties.ImplementationMode
import com.ujizin.camposer.config.properties.ResolutionPreset
import com.ujizin.camposer.config.properties.ScaleType
import com.ujizin.camposer.info.CameraInfo
import java.util.concurrent.Executor
import kotlin.math.roundToInt

@SuppressLint("UnsafeOptInUsageError")
public actual class CameraConfig internal constructor(
    context: Context,
    private val mainExecutor: Executor,
    private val controller: CameraController,
    private val cameraInfo: CameraInfo,
    internal var rebindCamera: () -> Unit = {},
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
        block = {
            controller.cameraSelector = it.selector
            rebindCamera()
        }
    )
        internal set

    public actual var scaleType: ScaleType = ScaleType.FillCenter
        internal set

    public actual var flashMode: FlashMode by config(
        value = FlashMode.find(controller.imageCaptureFlashMode),
        predicate = { old, new -> cameraInfo.isFlashSupported && old != new }
    ) {
        controller.imageCaptureFlashMode = it.mode
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

    public actual var exposureCompensation: Float? by config(
        value = null,
        predicate = { old, new -> new != null && old != new },
        block = ::setExposureCompensation,
    )
        internal set

    public actual var isTorchEnabled: Boolean by config(
        value = controller.torchState.value == TorchState.ON,
        predicate = { old, new -> old != new && cameraInfo.isTorchSupported },
        block = controller::enableTorch
    )
        internal set

    public actual var zoomRatio: Float by config(cameraInfo.minZoom, block = ::setZoomRatio)
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

    private fun setExposureCompensation(exposureCompensation: Float?) {
        if (exposureCompensation == null) return
        controller.cameraControl?.setExposureCompensationIndex(
            exposureCompensation
                .fastCoerceIn(cameraInfo.minExposure, cameraInfo.maxExposure)
                .roundToInt(),
        )
    }

    private fun setZoomRatio(zoomRatio: Float) {
        controller.setZoomRatio(zoomRatio.fastCoerceIn(cameraInfo.minZoom, cameraInfo.maxZoom))
    }

    private fun getUseCases(mode: CaptureMode = captureMode) = when {
        isImageAnalyzerEnabled && mode != CaptureMode.Video -> mode.value or IMAGE_ANALYSIS
        else -> mode.value
    }

    internal fun rebind() {
        setExposureCompensation(exposureCompensation)
    }

    internal inner class CameraConfigSaver : DefaultLifecycleObserver {

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            setZoomRatio(zoomRatio)
            setExposureCompensation(exposureCompensation)
        }
    }
}
