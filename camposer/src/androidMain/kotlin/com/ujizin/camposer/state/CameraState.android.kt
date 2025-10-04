package com.ujizin.camposer.state

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.camera.core.CameraEffect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.command.AndroidTakePictureCommand
import com.ujizin.camposer.command.DefaultTakePictureCommand
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.AndroidRecordController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.info.AndroidCameraInfo
import com.ujizin.camposer.info.CameraInfo
import java.util.concurrent.Executor
import kotlin.math.roundToInt

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraState].
 * */
@Stable
public actual class CameraState private constructor(
    context: Context,
    public val controller: LifecycleCameraController,
    private val cameraController: CameraController,
    private val mainExecutor: Executor = context.compatMainExecutor,
    private val androidRecordController: AndroidRecordController,
    private val androidTakePictureCommand: AndroidTakePictureCommand,
    public actual val info: CameraInfo,
) {

    public constructor(context: Context, cameraController: CameraController) : this(
        context = context,
        cameraController = cameraController,
        controller = LifecycleCameraController(context),
    )

    internal constructor(
        context: Context,
        cameraController: CameraController,
        controller: LifecycleCameraController,
    ) : this(
        context = context,
        controller = controller,
        cameraController = cameraController,
        mainExecutor = context.compatMainExecutor,
        androidRecordController = DefaultRecordController(
            cameraController = controller,
            mainExecutor = context.compatMainExecutor,
        ),
        androidTakePictureCommand = DefaultTakePictureCommand(
            controller = controller,
            mainExecutor = context.compatMainExecutor,
            contentResolver = context.contentResolver,
        ),
        info = CameraInfo(
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager,
            cameraInfo = AndroidCameraInfo(controller),
        )
    )

    private var lastMeteringPoint: MeteringPoint? = null

    public actual var isMuted: Boolean by mutableStateOf(false)

    /**
     * Check if camera is streaming or not.
     * */
    public actual var isStreaming: Boolean by mutableStateOf(false)
        internal set

    /**
     * Check if camera state is initialized or not.
     * */
    public actual var isInitialized: Boolean by mutableStateOf(false)
        internal set

    /**
     * Capture mode to be added on camera.
     * */
    internal actual var captureMode: CaptureMode = CaptureMode.Image
        set(value) {
            if (field == value) return
            field = value
            updateCaptureMode()
        }

    /**
     * Image capture mode to be added on camera.
     * */
    @SuppressLint("UnsafeOptInUsageError")
    internal actual var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.Balanced
        set(value) {
            if (field != value) {
                field = value
                val isZslSupported = controller.cameraInfo?.isZslSupported == true
                var mode = value.mode
                if (value == ImageCaptureMode.MinLatency && !isZslSupported) {
                    mode = value.fallback
                }
                controller.imageCaptureMode = mode
            }
        }

    /**
     * Get scale type from the camera.
     * */
    internal actual var scaleType: ScaleType = ScaleType.FillCenter

    /**
     * Get implementation mode from the camera.
     * */
    internal actual var implementationMode: ImplementationMode = ImplementationMode.Performance

    /**
     * Camera mode, it can be front or back.
     * @see CamSelector
     * */
    internal actual var camSelector: CamSelector = CamSelector.Back
        set(value) {
            when {
                value == field -> Unit
                !cameraController.isRecording && hasCamera(value) -> {
                    if (controller.cameraSelector != value.selector) {
                        controller.cameraSelector = value.selector
                        field = value
                        rebindCamera()
                    }
                }

                cameraController.isRecording -> Log.e(
                    TAG,
                    "Device is recording, switch camera is unavailable"
                )

                else -> Log.e(TAG, "Device does not have ${value.selector} camera")
            }
        }

    /**
     * Set image capture target size on camera
     * */
    @Deprecated("Use ResolutionPreset instead")
    internal actual var imageCaptureTargetSize: ImageTargetSize?
        get() = controller.imageCaptureTargetSize.toImageTargetSize()
        set(value) {
            if (value != imageCaptureTargetSize) {
                controller.imageCaptureTargetSize = value?.toOutputSize()
            }
        }

    /**
     * Get Image Analyzer from camera.
     * */
    internal actual var imageAnalyzer: ImageAnalyzer? = null
        set(value) {
            field = value
            if (!isImageAnalyzerEnabled) return
            updateImageAnalyzer(value?.analyzer)
        }

    /**
     * Enable/Disable Image analysis from the camera.
     * */
    internal actual var isImageAnalyzerEnabled: Boolean = false
        set(value) {
            if (field == value) return
            if (!info.isImageAnalyzerSupported) {
                Log.e(TAG, "Image analysis is not supported")
                return
            }
            field = value
            updateCaptureMode()
        }

    /**
     * Image analysis backpressure strategy, use [rememberImageAnalyzer] to set value.
     * */
    internal var imageAnalysisBackpressureStrategy: Int
        get() = controller.imageAnalysisBackpressureStrategy
        set(value) {
            if (imageAnalysisBackpressureStrategy != value) {
                controller.imageAnalysisBackpressureStrategy = value
            }
        }

    /**
     * Image analysis target size, use [rememberImageAnalyzer] to set value.
     * @see rememberImageAnalyzer
     * */
    internal var imageAnalysisResolutionSelector: ResolutionSelector?
        get() = controller.imageAnalysisResolutionSelector
        set(value) {
            // TODO check if this works as expected
            if (value != null && imageAnalysisResolutionSelector != value) {
                controller.imageAnalysisResolutionSelector = value
            }
        }

    /**
     * Image analysis image queue depth, use [rememberImageAnalyzer] to set value.
     * @see rememberImageAnalyzer
     * */
    public var imageAnalysisImageQueueDepth: Int
        get() = controller.imageAnalysisImageQueueDepth
        internal set(value) {
            if (imageAnalysisImageQueueDepth != value) {
                controller.imageAnalysisImageQueueDepth = value
            }
        }


    /**
     * Get if focus on tap is enabled from cameraX.
     * */
    internal actual var isFocusOnTapEnabled: Boolean
        get() = controller.isTapToFocusEnabled
        set(value) {
            if (value == controller.isTapToFocusEnabled) return
            controller.isTapToFocusEnabled = value
        }

    /**
     * Flash Mode from the camera.
     * @see FlashMode
     * */
    internal actual var flashMode: FlashMode
        get() = FlashMode.find(controller.imageCaptureFlashMode)
        set(value) {
            if (info.isFlashSupported && flashMode != value) {
                controller.imageCaptureFlashMode = value.mode
            }
        }

    /**
     * Enabled/Disable torch from camera.
     * */
    internal actual var enableTorch: Boolean
        get() = controller.torchState.value == TorchState.ON
        set(value) {
            if (enableTorch != value) {
                controller.enableTorch(info.isTorchSupported && value)
            }
        }

    internal actual var resolutionPreset: ResolutionPreset = ResolutionPreset.Default
        set(value) {
            if (field == value) return
            field = value
            setResolutionPreset(value)
        }

    /**
     * Return if video is supported.
     * */
    public var isVideoSupported: Boolean = true

    public actual var isPinchToZoomEnabled: Boolean by mutableStateOf(false)

    public actual var exposureCompensation: Float? = null
        set(value) {
            if (value == null || field == value) return
            field = value
            if (value in info.minExposure..info.maxExposure) {
                controller.cameraControl?.setExposureCompensationIndex(value.roundToInt())
            }
        }

    init {
        controller.initializationFuture.addListener({
            cameraController.initialize(
                recordController = androidRecordController,
                takePictureCommand = androidTakePictureCommand,
            )
            rebindCamera()
            isInitialized = true
        }, mainExecutor)
    }

    private fun updateCaptureMode() {
        try {
            var useCases = captureMode.value
            if (captureMode == CaptureMode.Image && isImageAnalyzerEnabled) {
                useCases = useCases or IMAGE_ANALYSIS
                updateImageAnalyzer(imageAnalyzer?.analyzer)
            } else {
                updateImageAnalyzer(null)
            }
            controller.setEnabledUseCases(useCases)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Use case Image Analysis not supported, ${exception.stackTraceToString()}")
            controller.setEnabledUseCases(captureMode.value)
        }
    }

    private fun updateImageAnalyzer(
        analyzer: ImageAnalysis.Analyzer?,
    ) = with(controller) {
        clearImageAnalysisAnalyzer()
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N && captureMode == CaptureMode.Video) {
            return
        }

        setImageAnalysisAnalyzer(mainExecutor, analyzer ?: return)
    }

    /**
     * Set zoom ratio to camera.
     * @param zoomRatio zoomRatio to be added
     * */
    private fun setZoomRatio(zoomRatio: Float) {
        controller.setZoomRatio(zoomRatio.coerceIn(info.minZoom, info.maxZoom))
    }

    /**
     * Return if has camera selector or not, camera must be initialized, otherwise result is false.
     * */
    public fun hasCamera(cameraSelector: CamSelector): Boolean =
        isInitialized && controller.hasCamera(cameraSelector.selector)

    @SuppressLint("RestrictedApi")
    private fun rebindCamera() {
        // Disable from pinch to zoom from cameraX controller
        controller.isPinchToZoomEnabled = false
        info.rebind(
            lensFacing = camSelector.selector.lensFacing,
            meteringPoint = lastMeteringPoint,
        )
    }

    /**
     *  Set effects on camera
     * */
    public fun setEffects(effects: Set<CameraEffect>) {
        controller.setEffects(effects)
    }

    /**
     *  Set effects on camera
     * */
    public fun clearEffects() {
        controller.clearEffects()
    }

    private fun setResolutionPreset(value: ResolutionPreset) {
        value.getQualitySelector()?.let { controller.videoCaptureQualitySelector = it }
        controller.imageCaptureResolutionSelector = value.getResolutionSelector()
        controller.previewResolutionSelector = value.getResolutionSelector()
    }

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
        meteringPoint: MeteringPoint,
        exposureCompensation: Float?,
        resolutionPreset: ResolutionPreset,
        isPinchToZoomEnabled: Boolean,
    ) {
        this.camSelector = camSelector
        this.captureMode = captureMode
        this.scaleType = scaleType
        this.imageCaptureTargetSize = imageCaptureTargetSize
        this.isImageAnalyzerEnabled = isImageAnalysisEnabled
        this.imageAnalyzer = imageAnalyzer
        this.implementationMode = implementationMode
        this.isFocusOnTapEnabled = isFocusOnTapEnabled
        this.flashMode = flashMode
        this.enableTorch = enableTorch
        this.lastMeteringPoint = meteringPoint
        this.imageCaptureMode = imageCaptureMode
        this.resolutionPreset = resolutionPreset
        this.isPinchToZoomEnabled = isPinchToZoomEnabled
        this.exposureCompensation = exposureCompensation
        setZoomRatio(zoomRatio)
    }

    internal fun dispose() {
        controller.unbind()
    }

    private companion object {
        private val TAG = this::class.java.name
    }
}
