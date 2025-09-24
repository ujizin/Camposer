package com.ujizin.camposer.state

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.camera.core.CameraEffect
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.TorchState
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.CameraController.OutputSize
import androidx.camera.view.CameraController.OutputSize.UNASSIGNED_ASPECT_RATIO
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.extensions.isImageAnalysisSupported
import com.ujizin.camposer.extensions.toFile
import com.ujizin.camposer.extensions.toPath
import com.ujizin.camposer.result.CaptureResult
import kotlinx.io.files.Path
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executor
import kotlin.math.roundToInt

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraState].
 * */
@Stable
public actual class CameraState(context: Context) {

    /**
     * Main Executor to action as take picture or record.
     * */
    private val mainExecutor: Executor = context.compatMainExecutor

    /**
     * Content resolver to picture and video.
     * */
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Check if focus metering is supported
     * */
    private val MeteringPoint.isFocusMeteringSupported: Boolean
        get() = controller.cameraInfo?.isFocusMeteringSupported(
            FocusMeteringAction.Builder(this).build()
        ) ?: false

    /**
     * Main controller from CameraX. useful in cases that haven't been release some feature yet.
     * */
    public val controller: LifecycleCameraController = LifecycleCameraController(context)

    /**
     * Record controller to video Capture
     * */
    private var recordController: Recording? = null

    /**
     * Get max zoom from camera.
     * */
    public actual var maxZoom: Float by mutableFloatStateOf(
        controller.zoomState.value?.maxZoomRatio ?: INITIAL_ZOOM_VALUE
    )
        internal set

    /**
     * Get min zoom from camera.
     * */
    public actual var minZoom: Float by mutableFloatStateOf(
        controller.zoomState.value?.minZoomRatio ?: INITIAL_ZOOM_VALUE
    )
        internal set


    /**
     * Get range compensation range from camera.
     * */
    private val exposureCompensationRange
        get() = controller.cameraInfo?.exposureState?.exposureCompensationRange

    /**
     * Get min exposure from camera.
     * */
    public actual var minExposure: Float by mutableFloatStateOf(
        exposureCompensationRange?.lower?.toFloat() ?: INITIAL_EXPOSURE_VALUE
    )
        internal set

    /**
     * Get max exposure from camera.
     * */
    public actual var maxExposure: Float by mutableFloatStateOf(
        exposureCompensationRange?.upper?.toFloat() ?: INITIAL_EXPOSURE_VALUE
    )
        internal set

    public actual var isMuted: Boolean by mutableStateOf(false)
    public actual val initialExposure: Float = INITIAL_EXPOSURE_VALUE
        get() = controller.cameraInfo?.exposureState?.exposureCompensationIndex?.toFloat() ?: field

    /**
     * Check if compensation exposure is supported.
     * */
    public actual val isExposureSupported: Boolean by derivedStateOf { maxExposure != INITIAL_EXPOSURE_VALUE }

    /**
     * Check if camera is streaming or not.
     * */
    public actual var isStreaming: Boolean by mutableStateOf(false)
        internal set

    /**
     * Check if zoom is supported.
     * */
    public actual val isZoomSupported: Boolean by derivedStateOf { maxZoom != 1F }

    /**
     * Check if focus on tap supported
     * */
    public actual var isFocusOnTapSupported: Boolean by mutableStateOf(true)

    /**
     * Check if camera state is initialized or not.
     * */
    public actual var isInitialized: Boolean by mutableStateOf(false)
        internal set

    /**
     * Verify if camera has flash or not.
     * */
    public actual var hasFlashUnit: Boolean by mutableStateOf(
        controller.cameraInfo?.hasFlashUnit() ?: true
    )

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
                !isRecording && hasCamera(value) -> {
                    if (controller.cameraSelector != value.selector) {
                        controller.cameraSelector = value.selector
                        field = value
                        rebindCamera()
                    }
                }

                isRecording -> Log.e(TAG, "Device is recording, switch camera is unavailable")
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
    private var imageAnalyzer: ImageAnalysis.Analyzer? = null
        set(value) {
            field = value
            updateImageAnalyzer(value)
        }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    /**
     * Check if image analysis is supported by camera hardware level.
     * */
    public var isImageAnalysisSupported: Boolean by mutableStateOf(
        isImageAnalysisSupported(
            camSelector
        )
    )
        private set

    /**
     * Enable/Disable Image analysis from the camera.
     * */
    internal actual var isImageAnalysisEnabled: Boolean = isImageAnalysisSupported
        set(value) {
            if (field == value) return
            if (!isImageAnalysisSupported) {
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
    internal var imageAnalysisTargetSize: OutputSize?
        get() = controller.imageAnalysisTargetSize
        set(value) {
            if (imageAnalysisTargetSize != value) {
                controller.imageAnalysisTargetSize = value
            }
        }

    /**
     * Image analysis image queue depth, use [rememberImageAnalyzer] to set value.
     * @see rememberImageAnalyzer
     * */
    internal var imageAnalysisImageQueueDepth: Int
        get() = controller.imageAnalysisImageQueueDepth
        set(value) {
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
            if (hasFlashUnit && flashMode != value) {
                controller.imageCaptureFlashMode = value.mode
            }
        }

    public actual var hasTorchAvailable: Boolean by mutableStateOf(hasFlashUnit)
        private set

    /**
     * Enabled/Disable torch from camera.
     * */
    internal actual var enableTorch: Boolean
        get() = controller.torchState.value == TorchState.ON
        set(value) {
            if (enableTorch != value) {
                controller.enableTorch(hasTorchAvailable && value)
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

    /**
     * Return true if it's recording.
     * */
    public actual var isRecording: Boolean by mutableStateOf(controller.isRecording)
        private set

    public actual var exposureCompensation: Float? = null
        set(value) {
            if (value == null || field == value) return
            field = value
            if (value in minExposure..maxExposure) {
                controller.cameraControl?.setExposureCompensationIndex(value.roundToInt())
            }
        }

    init {
        controller.initializationFuture.addListener({
            rebindCamera()
            isInitialized = true
        }, mainExecutor)
    }

    private fun updateCaptureMode() {
        try {
            var useCases = captureMode.value
            if (captureMode == CaptureMode.Image && isImageAnalysisEnabled) {
                useCases = useCases or IMAGE_ANALYSIS
                updateImageAnalyzer(imageAnalyzer)
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
        analyzer: ImageAnalysis.Analyzer? = imageAnalyzer
    ) = with(controller) {
        clearImageAnalysisAnalyzer()
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N && captureMode == CaptureMode.Video) {
            return
        }

        setImageAnalysisAnalyzer(mainExecutor, analyzer ?: return)
    }

    private fun startExposure() {
        minExposure = exposureCompensationRange?.lower?.toFloat() ?: INITIAL_EXPOSURE_VALUE
        maxExposure = exposureCompensationRange?.upper?.toFloat() ?: INITIAL_EXPOSURE_VALUE
    }

    /**
     *  Take a picture with the camera.
     *
     *  @param saveCollection Uri collection where the photo will be saved.
     *  @param contentValues Content values of the photo.
     *  @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    public fun takePicture(
        contentValues: ContentValues,
        saveCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) {
        takePicture(
            outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                contentResolver, saveCollection, contentValues
            ).build(), onResult = onResult
        )
    }

    /**
     * Take a picture with the camera.
     * @param file file where the photo will be saved
     * @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    public fun takePicture(
        file: File, onResult: (CaptureResult<Uri?>) -> Unit
    ) {
        takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), onResult)
    }

    /**
     * Take a picture with the camera.
     *
     * @param outputFileOptions Output file options of the photo.
     * @param onResult Callback called when [CaptureResult] is ready
     * */
    public fun takePicture(
        outputFileOptions: ImageCapture.OutputFileOptions,
        onResult: (CaptureResult<Uri?>) -> Unit,
    ) {
        try {
            controller.takePicture(
                outputFileOptions,
                mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onResult(CaptureResult.Success(outputFileResults.savedUri))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onResult(CaptureResult.Error(exception))
                    }
                })
        } catch (exception: Exception) {
            onResult(CaptureResult.Error(exception))
        }
    }

    public actual fun takePicture(
        path: Path,
        onImageCaptured: (CaptureResult<Path>) -> Unit,
    ): Unit = takePicture(
        ImageCapture.OutputFileOptions.Builder(path.toFile()).build(),
    ) { androidResult ->
        val result = when (androidResult) {
            is CaptureResult.Error -> CaptureResult.Error(androidResult.throwable)
            is CaptureResult.Success -> CaptureResult.Success(androidResult.data!!.toPath())
        }
        onImageCaptured(result)
    }

    /**
     * Set zoom ratio to camera.
     * @param zoomRatio zoomRatio to be added
     * */
    private fun setZoomRatio(zoomRatio: Float) {
        controller.setZoomRatio(zoomRatio.coerceIn(minZoom, maxZoom))
    }

    /**
     * Start recording camera.
     *
     * @param fileOutputOptions file output options where the video will be saved
     * @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = prepareRecording(onResult) {
        Log.i(TAG, "Start recording")
        controller.startRecording(
            fileOutputOptions,
            audioConfig,
            mainExecutor,
            getConsumerEvent(onResult)
        )
    }

    /**
     * Start recording camera.
     *
     * @param fileDescriptorOutputOptions file output options where the video will be saved
     * @param onResult Callback called when [CaptureResult<Uri?>] is ready
     * */
    @RequiresApi(VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = prepareRecording(onResult) {
        controller.startRecording(
            fileDescriptorOutputOptions,
            audioConfig,
            mainExecutor,
            getConsumerEvent(onResult)
        )
    }

    /**
     * Start recording camera.
     *
     *  @param mediaStoreOutputOptions media store output options to the video to be saved.
     *  @param onResult Callback called when [CaptureResult<Uri?>] is ready
     *  */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit,
    ): Unit = prepareRecording(onError = onResult) {
        controller.startRecording(
            mediaStoreOutputOptions,
            audioConfig,
            mainExecutor,
            getConsumerEvent(onResult)
        )
    }

    private fun getConsumerEvent(
        onResult: (CaptureResult<Uri?>) -> Unit
    ): Consumer<VideoRecordEvent> = Consumer { event ->
        Log.i(TAG, "Video Recorder Event - $event")
        if (event is VideoRecordEvent.Finalize) {
            isRecording = false
            val result = when {
                !event.hasError() -> CaptureResult.Success(event.outputResults.outputUri)
                else -> CaptureResult.Error(
                    Exception("Video error code: ${event.error}, cause: ${event.cause}"),
                )
            }
            recordController = null
            onResult(result)
        }
    }

    /**
     * Prepare recording camera.
     *
     * @param onRecordBuild lambda to retrieve record controller
     * @param onError Callback called when thrown error
     * */
    private fun prepareRecording(
        onError: (CaptureResult.Error) -> Unit,
        onRecordBuild: () -> Recording,
    ) {
        try {
            Log.i(TAG, "Prepare recording")
            isRecording = true
            recordController = onRecordBuild()
            muteRecording(isMuted)
        } catch (exception: Exception) {
            Log.e(TAG, "Fail to record! - ${exception.stackTraceToString()}")
            isRecording = false
            onError(CaptureResult.Error(exception))
        }
    }

    /**
     * Stop recording camera.
     * */
    public actual fun stopRecording() {
        Log.i(TAG, "Stop recording")
        recordController?.stop()?.also {
            isRecording = false
        }
    }

    public actual fun pauseRecording() {
        Log.i(TAG, "Pause recording")
        recordController?.pause()
    }

    public actual fun resumeRecording() {
        Log.i(TAG, "Resume recording")
        recordController?.resume()
    }

    public actual fun muteRecording(isMuted: Boolean) {
        this.isMuted = isMuted
        recordController?.mute(isMuted)
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresApi(VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(fileDescriptorOutputOptions, audioConfig, onResult)
        }
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(mediaStoreOutputOptions, audioConfig, onResult)
        }
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (CaptureResult<Uri?>) -> Unit
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(fileOutputOptions, audioConfig, onResult)
        }
    }

    /**
     * Return if has camera selector or not, camera must be initialized, otherwise result is false.
     * */
    public fun hasCamera(cameraSelector: CamSelector): Boolean =
        isInitialized && controller.hasCamera(cameraSelector.selector)

    private fun startZoom() {
        // Turn off is pinch to zoom and use manually
        controller.isPinchToZoomEnabled = false

        val zoom = controller.zoomState.value
        minZoom = zoom?.minZoomRatio ?: INITIAL_ZOOM_VALUE
        maxZoom = zoom?.maxZoomRatio ?: INITIAL_ZOOM_VALUE
    }

    private fun rebindCamera() {
        hasFlashUnit = controller.cameraInfo?.hasFlashUnit() ?: false
        hasTorchAvailable = hasFlashUnit
        isImageAnalysisSupported = isImageAnalysisSupported(camSelector)
        startZoom()
        startExposure()
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

    @SuppressLint("RestrictedApi")
    @VisibleForTesting
    internal fun isImageAnalysisSupported(
        cameraSelector: CamSelector = camSelector
    ): Boolean = cameraManager?.isImageAnalysisSupported(
        cameraSelector.selector.lensFacing
    ) ?: false

    public actual fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) {
        val byteArrayOS = ByteArrayOutputStream()
        takePicture(
            outputFileOptions = ImageCapture.OutputFileOptions.Builder(byteArrayOS).build(),
            onResult = { result ->
                when (result) {
                    is CaptureResult.Error -> CaptureResult.Error(result.throwable)
                    is CaptureResult.Success<Uri?> -> CaptureResult.Success(byteArrayOS.toByteArray())
                }
            },
        )
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public actual fun startRecording(path: Path, onVideoCaptured: (CaptureResult<Path>) -> Unit) {
        startRecording(
            fileOutputOptions = FileOutputOptions.Builder(path.toFile()).build(),
            onResult = { result ->
                when (result) {
                    is CaptureResult.Error -> CaptureResult.Error(result.throwable)
                    is CaptureResult.Success<Uri?> -> CaptureResult.Success(path)
                }
            }
        )
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
        this.isImageAnalysisEnabled = isImageAnalysisEnabled
        this.imageAnalyzer = imageAnalyzer?.analyzer
        this.implementationMode = implementationMode
        this.isFocusOnTapEnabled = isFocusOnTapEnabled
        this.flashMode = flashMode
        this.enableTorch = enableTorch
        this.isFocusOnTapSupported = meteringPoint.isFocusMeteringSupported
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
        private const val INITIAL_ZOOM_VALUE = 1F
        private const val INITIAL_EXPOSURE_VALUE = 0F
    }
}

private fun OutputSize?.toImageTargetSize(): ImageTargetSize? {
    return this?.let {
        if (it.aspectRatio != UNASSIGNED_ASPECT_RATIO) {
            ImageTargetSize(aspectRatio = it.aspectRatio)
        } else {
            ImageTargetSize(size = it.resolution)
        }
    }
}
