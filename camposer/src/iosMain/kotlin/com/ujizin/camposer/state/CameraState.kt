package com.ujizin.camposer.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.extensions.captureDeviceInput
import com.ujizin.camposer.mapper.toAVCaptureDeviceInput
import com.ujizin.camposer.mapper.toAVCaptureFlashMode
import com.ujizin.camposer.mapper.toCamSelector
import com.ujizin.camposer.mapper.toFlashMode
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.exposureTargetOffset
import platform.AVFoundation.flashMode
import platform.AVFoundation.hasFlash
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.isExposurePointOfInterestSupported
import platform.AVFoundation.maxAvailableVideoZoomFactor
import platform.AVFoundation.minAvailableVideoZoomFactor
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.torchMode
import platform.AVFoundation.videoZoomFactor
import kotlin.math.roundToInt

public actual class CameraState {

//    internal val session by lazy { AVCaptureSession() }

//    private val device: AVCaptureDevice?
//        get() = session.captureDeviceInput?.device

    internal actual var camSelector: CamSelector
        get() = CamSelector.Back //session.captureDeviceInput.toCamSelector()
        set(value) {
//            configureSession {
//                captureDeviceInput?.let(::removeInput)
//                value.toAVCaptureDeviceInput()?.let(session::addInput)
//            }
        }

    internal actual var captureMode: CaptureMode
        get() = TODO("Not yet implemented")
        set(value) {}

    internal actual var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.MinLatency

    internal actual var imageCaptureTargetSize: ImageTargetSize?
        get() = TODO("Not yet implemented")
        set(value) {}

    internal actual var flashMode: FlashMode
        get() = FlashMode.Off //device?.flashMode?.toFlashMode() ?: FlashMode.Off
        set(value) {
//            device?.flashMode = value.toAVCaptureFlashMode()
        }

    internal actual var scaleType: ScaleType = ScaleType.FillCenter

    internal actual var implementationMode: ImplementationMode = ImplementationMode.Performance

    internal actual var isImageAnalysisEnabled: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    internal actual var isFocusOnTapEnabled: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    internal actual var enableTorch: Boolean
        get() = false // device?.torchMode == AVCaptureTorchModeOn
        set(value) {
//            device?.torchMode = when {
//                value -> AVCaptureTorchModeOn
//                else -> AVCaptureTorchModeOff
//            }
        }

    private var zoomRatio: Float
        get() = 1F // device?.videoZoomFactor?.toFloat() ?: 1F
        set(value) {
//            device?.videoZoomFactor = value.toDouble()
        }

    private var exposureCompensation: Int
        get() = 1 // device?.exposureTargetOffset?.roundToInt() ?: initialExposure
        set(value) {
        }

    public actual val initialExposure: Int = 1 // device?.exposureTargetOffset?.roundToInt() ?: 1

    public actual val isZoomSupported: Boolean
        get() = true

    /**
     * Get max zoom from camera.
     * */
    public actual var maxZoom: Float = 1F /*by mutableStateOf(device?.maxAvailableVideoZoomFactor?.toFloat() ?: 1F)*/
        private set

    /**
     * Get min zoom from camera.
     * */
    public actual var minZoom: Float = 1F // by mutableStateOf(device?.minAvailableVideoZoomFactor?.toFloat() ?: 1F)
        private set

    /**
     * Get min exposure from camera.
     * */
    public actual var minExposure: Int = 1 // by mutableStateOf(device?.minExposureTargetBias?.roundToInt() ?: 1)
        private set

    /**
     * Get max exposure from camera.
     * */
    public actual var maxExposure: Int = 1 //by mutableStateOf(device?.minExposureTargetBias?.roundToInt() ?: 1)
        private set

    /**
     * Check if compensation exposure is supported.
     * */
    public actual val isExposureSupported: Boolean
        get() = true

    /**
     * Check if camera is streaming or not.
     * */
    public actual var isStreaming: Boolean = false
        internal set

    /**
     * Check if focus on tap supported
     * */
    public actual var isFocusOnTapSupported: Boolean = true
        private set

    /**
     * Check if camera state is initialized or not.
     * */
    public actual var isInitialized: Boolean = false // by mutableStateOf(device?.isConnected() ?: false)
        private set

    /**
     * Verify if camera has flash or not.
     * */
    public actual var hasFlashUnit: Boolean = true // device?.hasFlash ?: false
        get() = false // device?.hasFlash ?: false
        private set

    /**
     * Return true if it's recording.
     * */
    public actual var isRecording: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

//    private fun configureSession(block: AVCaptureSession.() -> Unit) = session.apply {
//        beginConfiguration()
//        block()
//        commitConfiguration()
//    }

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
        exposureCompensation: Int
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
    }
}
