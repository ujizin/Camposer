package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.helper.IOSCameraController
import com.ujizin.camposer.mapper.toAVCaptureDevicePosition
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.AVFoundation.exposureTargetOffset
import platform.AVFoundation.hasFlash
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isFlashAvailable
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
public actual class CameraState internal constructor(
    private val controller: IOSCameraController = IOSCameraController(),
) {

    private val mainScope = MainScope()
    private var positionStateJob: Job? = null

    internal actual var camSelector: CamSelector = CamSelector.Back
        set(value) {
            if (value == field) return
            field = value
            controller.switchInputCamera(position = value.toAVCaptureDevicePosition())
        }

    internal actual var captureMode: CaptureMode
        get() = CaptureMode.Image
        set(value) {}

    internal actual var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.MinLatency

    internal actual var imageCaptureTargetSize: ImageTargetSize?
        get() = ImageTargetSize()
        set(value) {}

    internal actual var flashMode: FlashMode = FlashMode.Off
        set(value) {
            if (field == value || !controller.device.isFlashAvailable()) return
            field = value
            controller.setFlashMode(value.mode)
        }

    internal actual var scaleType: ScaleType = ScaleType.FillCenter

    internal actual var implementationMode: ImplementationMode = ImplementationMode.Performance

    internal actual var isImageAnalysisEnabled: Boolean
        get() = true
        set(value) {}

    internal actual var isFocusOnTapEnabled: Boolean = true

    internal actual var enableTorch: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            controller.setTorchEnabled(value)
        }

    internal var zoomRatio: Float
        get() = controller.device.videoZoomFactor.toFloat()
        set(value) {
            if (zoomRatio == value || value !in minZoom..maxZoom) return
            controller.setZoom(value)
        }

    private var exposureCompensation: Int
        get() = controller.device.exposureTargetOffset.roundToInt()
        set(value) {

        }

    public actual val initialExposure: Int by lazy { exposureCompensation }

    public actual val isZoomSupported: Boolean = true

    public actual var maxZoom: Float = 1F
        get() = controller.device.activeFormat.videoMaxZoomFactor.toFloat()
        private set

    public actual var minZoom: Float = 1F
        private set

    public actual var minExposure: Int = 0
        get() = controller.device.minExposureTargetBias.roundToInt()
        private set

    public actual var maxExposure: Int = 0
        get() = controller.device.maxExposureTargetBias.roundToInt()
        private set

    public actual val isExposureSupported: Boolean
        get() = true

    public actual var isStreaming: Boolean = false
        internal set

    public actual var isFocusOnTapSupported: Boolean = false
        get() = controller.isFocusOnTapSupported
        private set

    public actual var isInitialized: Boolean = false
        get() = controller.isRunning
        private set

    public actual var hasFlashUnit: Boolean by mutableStateOf(false)
        private set

    public actual var hasTorchAvailable: Boolean by mutableStateOf(false)
        private set

    public actual var isRecording: Boolean
        get() = false
        set(value) {}


    internal actual var videoQualitySelector: QualitySelector
        get() = QualitySelector()
        set(value) {}

    internal var isPinchToZoomEnabled: Boolean = true
        private set

    init {
        prepareCamera()
    }

    private fun prepareCamera() = with(controller) {
        positionStateJob?.cancel()
        positionStateJob = cameraPositionState.onEach { onCameraSwitched() }.launchIn(mainScope)
        switchInputCamera(position = camSelector.toAVCaptureDevicePosition())
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun startCamera(view: UIView) = controller.start(
        view = view,
        position = camSelector.toAVCaptureDevicePosition(),
    )

    internal fun renderCamera(view: UIView) = controller.render(view)

    internal fun setFocusPoint(focusPoint: CValue<CGPoint>) = controller.setFocusPoint(focusPoint)

    /**
     * Update all values from camera state.
     * */
    internal fun update(
        camSelector: CamSelector,
        captureMode: CaptureMode,
        scaleType: ScaleType,
        imageCaptureTargetSize: ImageTargetSize?,
        isImageAnalysisEnabled: Boolean,
        imageAnalyzer: ImageAnalyzer?,
        implementationMode: ImplementationMode,
        isFocusOnTapEnabled: Boolean,
        flashMode: FlashMode,
        zoomRatio: Float,
        imageCaptureMode: ImageCaptureMode,
        enableTorch: Boolean,
        videoQualitySelector: QualitySelector,
        exposureCompensation: Int,
        isPinchToZoomEnabled: Boolean,
    ) {
        this.camSelector = camSelector
        this.captureMode = captureMode
        this.scaleType = scaleType
        this.imageCaptureTargetSize = imageCaptureTargetSize
        this.isImageAnalysisEnabled = isImageAnalysisEnabled
        this.implementationMode = implementationMode
        this.isFocusOnTapEnabled = isFocusOnTapEnabled
        this.flashMode = flashMode
        this.enableTorch = enableTorch
        this.imageCaptureMode = imageCaptureMode
        this.zoomRatio = zoomRatio
        this.exposureCompensation = exposureCompensation
        this.videoQualitySelector = videoQualitySelector
        this.isPinchToZoomEnabled = isPinchToZoomEnabled
    }

    private fun onCameraSwitched() = with(controller.device) {
        hasFlashUnit = hasFlash && isFlashAvailable()
        hasTorchAvailable = hasTorch && isTorchAvailable()
    }

    internal fun recoveryState() {
        controller.setTorchEnabled(enableTorch)
    }

    internal fun dispose() {
        controller.dispose()
        positionStateJob?.cancel()
        positionStateJob = null
    }
}
