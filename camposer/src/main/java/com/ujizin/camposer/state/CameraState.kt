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
import androidx.annotation.ChecksSdkIntAtLeast
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
import androidx.camera.video.QualitySelector
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.extensions.isImageAnalysisSupported
import java.io.File
import java.util.concurrent.Executor

/**
 * A state object that can be hoisted to control camera, take picture or record video.
 *
 * To be created use [rememberCameraState].
 * */
@Stable
public class CameraState(context: Context) {

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
    public var maxZoom: Float by mutableFloatStateOf(
        controller.zoomState.value?.maxZoomRatio ?: INITIAL_ZOOM_VALUE
    )
        internal set

    /**
     * Get min zoom from camera.
     * */
    public var minZoom: Float by mutableFloatStateOf(
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
    public var minExposure: Int by mutableIntStateOf(
        exposureCompensationRange?.lower ?: INITIAL_EXPOSURE_VALUE
    )
        internal set

    /**
     * Get max exposure from camera.
     * */
    public var maxExposure: Int by mutableIntStateOf(
        exposureCompensationRange?.upper ?: INITIAL_EXPOSURE_VALUE
    )
        internal set

    public val initialExposure: Int = INITIAL_EXPOSURE_VALUE
        get() = controller.cameraInfo?.exposureState?.exposureCompensationIndex ?: field

    /**
     * Check if compensation exposure is supported.
     * */
    public val isExposureSupported: Boolean by derivedStateOf { maxExposure != INITIAL_EXPOSURE_VALUE }

    /**
     * Check if camera is streaming or not.
     * */
    public var isStreaming: Boolean by mutableStateOf(false)
        internal set

    /**
     * Check if zoom is supported.
     * */
    public val isZoomSupported: Boolean by derivedStateOf { maxZoom != 1F }

    /**
     * Check if focus on tap supported
     * */
    public var isFocusOnTapSupported: Boolean by mutableStateOf(true)

    /**
     * Check if camera state is initialized or not.
     * */
    public var isInitialized: Boolean by mutableStateOf(false)
        internal set

    /**
     * Verify if camera has flash or not.
     * */
    public var hasFlashUnit: Boolean by mutableStateOf(
        controller.cameraInfo?.hasFlashUnit() ?: true
    )

    /**
     * Capture mode to be added on camera.
     * */
    internal var captureMode: CaptureMode = CaptureMode.Image
        set(value) {
            if (field != value) {
                field = value
                updateCaptureMode()
            }
        }

    /**
     * Image capture mode to be added on camera.
     * */
    internal var imageCaptureMode: ImageCaptureMode = ImageCaptureMode.MinLatency
        set(value) {
            if (field != value) {
                field = value
                controller.imageCaptureMode = value.mode
            }
        }

    /**
     * Get scale type from the camera.
     * */
    internal var scaleType: ScaleType = ScaleType.FillCenter

    /**
     * Get implementation mode from the camera.
     * */
    internal var implementationMode: ImplementationMode = ImplementationMode.Performance

    internal var videoQualitySelector: QualitySelector
        get() = controller.videoCaptureQualitySelector
        set(value) {
            controller.videoCaptureQualitySelector = value
        }

    /**
     * Camera mode, it can be front or back.
     * @see CamSelector
     * */
    internal var camSelector: CamSelector = CamSelector.Back
        set(value) {
            when {
                value == field -> Unit
                !isRecording && hasCamera(value) -> {
                    if (controller.cameraSelector != value.selector) {
                        controller.cameraSelector = value.selector
                        field = value
                        resetCamera()
                    }
                }

                isRecording -> Log.e(TAG, "Device is recording, switch camera is unavailable")
                else -> Log.e(TAG, "Device does not have ${value.selector} camera")
            }
        }

    /**
     * Set image capture target size on camera
     * */
    internal var imageCaptureTargetSize: ImageTargetSize?
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
    internal var isImageAnalysisEnabled: Boolean = isImageAnalysisSupported
        set(value) {
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
    internal var isFocusOnTapEnabled: Boolean
        get() = controller.isTapToFocusEnabled
        set(value) {
            controller.isTapToFocusEnabled = value
        }

    /**
     * Flash Mode from the camera.
     * @see FlashMode
     * */
    internal var flashMode: FlashMode
        get() = FlashMode.find(controller.imageCaptureFlashMode)
        set(value) {
            if (hasFlashUnit && flashMode != value) {
                controller.imageCaptureFlashMode = value.mode
            }
        }

    /**
     * Enabled/Disable torch from camera.
     * */
    internal var enableTorch: Boolean
        get() = controller.torchState.value == TorchState.ON
        set(value) {
            if (enableTorch != value) {
                controller.enableTorch(hasFlashUnit && value)
            }
        }

    /**
     * Return if video is supported.
     * */

    @ChecksSdkIntAtLeast(VERSION_CODES.N)
    public var isVideoSupported: Boolean = Build.VERSION.SDK_INT >= VERSION_CODES.N

    /**
     * Return true if it's recording.
     * */
    public var isRecording: Boolean by mutableStateOf(controller.isRecording)
        private set

    init {
        controller.initializationFuture.addListener({
            resetCamera()
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
            Log.e(TAG, "Use case Image Analysis not supported")
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
        minExposure = exposureCompensationRange?.lower ?: INITIAL_EXPOSURE_VALUE
        maxExposure = exposureCompensationRange?.upper ?: INITIAL_EXPOSURE_VALUE
    }

    /**
     *  Take a picture with the camera.
     *
     *  @param saveCollection Uri collection where the photo will be saved.
     *  @param contentValues Content values of the photo.
     *  @param onResult Callback called when [ImageCaptureResult] is ready
     * */
    public fun takePicture(
        contentValues: ContentValues,
        saveCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        onResult: (ImageCaptureResult) -> Unit,
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
     * @param onResult Callback called when [ImageCaptureResult] is ready
     * */
    public fun takePicture(
        file: File, onResult: (ImageCaptureResult) -> Unit
    ) {
        takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), onResult)
    }

    /**
     * Take a picture with the camera.
     *
     * @param outputFileOptions Output file options of the photo.
     * @param onResult Callback called when [ImageCaptureResult] is ready
     * */
    public fun takePicture(
        outputFileOptions: ImageCapture.OutputFileOptions,
        onResult: (ImageCaptureResult) -> Unit,
    ) {
        try {
            controller.takePicture(outputFileOptions,
                mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onResult(ImageCaptureResult.Success(outputFileResults.savedUri))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onResult(ImageCaptureResult.Error(exception))
                    }
                })
        } catch (exception: Exception) {
            onResult(ImageCaptureResult.Error(exception))
        }
    }

    /**
     * Set zoom ratio to camera.
     * @param zoomRatio zoomRatio to be added
     * */
    private fun setZoomRatio(zoomRatio: Float) {
        controller.setZoomRatio(zoomRatio.coerceIn(minZoom, maxZoom))
    }

    private fun setExposureCompensation(exposureCompensation: Int) {
        controller.cameraControl?.setExposureCompensationIndex(exposureCompensation)
    }

    /**
     * Start recording camera.
     *
     * @param fileOutputOptions file output options where the video will be saved
     * @param onResult Callback called when [VideoCaptureResult] is ready
     * */
    @RequiresApi(VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (VideoCaptureResult) -> Unit,
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
     * @param onResult Callback called when [VideoCaptureResult] is ready
     * */
    @RequiresApi(VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (VideoCaptureResult) -> Unit,
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
     *  @param onResult Callback called when [VideoCaptureResult] is ready
     *  */
    @RequiresApi(VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (VideoCaptureResult) -> Unit,
    ): Unit = prepareRecording(onError = onResult) {
        controller.startRecording(
            mediaStoreOutputOptions,
            audioConfig,
            mainExecutor,
            getConsumerEvent(onResult)
        )
    }

    @RequiresApi(VERSION_CODES.N)
    private fun getConsumerEvent(
        onResult: (VideoCaptureResult) -> Unit
    ): Consumer<VideoRecordEvent> = Consumer<VideoRecordEvent> { event ->
        Log.i(TAG, "Video Recorder Event - $event")
        if (event is VideoRecordEvent.Finalize) {
            isRecording = false
            val result = when {
                !event.hasError() -> VideoCaptureResult.Success(event.outputResults.outputUri)
                else -> VideoCaptureResult.Error(
                    "Video error code: ${event.error}",
                    event.cause
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
    @RequiresApi(VERSION_CODES.M)
    private fun prepareRecording(
        onError: (VideoCaptureResult.Error) -> Unit,
        onRecordBuild: () -> Recording,
    ) {
        try {
            Log.i(TAG, "Prepare recording")
            isRecording = true
            recordController = onRecordBuild()
        } catch (exception: Exception) {
            Log.i(TAG, "Fail to record! - $exception")
            isRecording = false
            onError(
                VideoCaptureResult.Error(
                    if (!controller.isVideoCaptureEnabled) {
                        "Video capture is not enabled, please set captureMode as CaptureMode.Video - ${exception.message}"
                    } else "${exception.message}", exception
                )
            )
        }
    }

    /**
     * Stop recording camera.
     * */
    @RequiresApi(VERSION_CODES.M)
    public fun stopRecording() {
        Log.i(TAG, "Stop recording")
        recordController?.stop()?.also {
            isRecording = false
        }
    }

    @RequiresApi(VERSION_CODES.M)
    public fun pauseRecording() {
        Log.i(TAG, "Pause recording")
        recordController?.pause()
    }

    @RequiresApi(VERSION_CODES.M)
    public fun resumeRecording() {
        Log.i(TAG, "Resume recording")
        recordController?.resume()
    }

    @RequiresApi(VERSION_CODES.M)
    public fun muteRecording(muted: Boolean) {
        recordController?.mute(muted)
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresApi(VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        fileDescriptorOutputOptions: FileDescriptorOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (VideoCaptureResult) -> Unit
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(fileDescriptorOutputOptions, audioConfig, onResult)
        }
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresApi(VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        mediaStoreOutputOptions: MediaStoreOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (VideoCaptureResult) -> Unit
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(mediaStoreOutputOptions, audioConfig, onResult)
        }
    }

    /**
     * Toggle recording camera.
     * */
    @RequiresApi(VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun toggleRecording(
        fileOutputOptions: FileOutputOptions,
        audioConfig: AudioConfig = AudioConfig.create(true),
        onResult: (VideoCaptureResult) -> Unit
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

    private fun resetCamera() {
        hasFlashUnit = controller.cameraInfo?.hasFlashUnit() ?: false
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

    private fun Set<Int>.sumOr(initial: Int = 0): Int = fold(initial) { acc, current ->
        acc or current
    }

    @SuppressLint("RestrictedApi")
    @VisibleForTesting
    internal fun isImageAnalysisSupported(
        cameraSelector: CamSelector = camSelector
    ): Boolean = cameraManager?.isImageAnalysisSupported(
        cameraSelector.selector.lensFacing
    ) ?: false

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
        exposureCompensation: Int,
        videoQualitySelector: QualitySelector,
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
        this.videoQualitySelector = videoQualitySelector
        setExposureCompensation(exposureCompensation)
        setZoomRatio(zoomRatio)
    }

    private companion object {
        private val TAG = this::class.java.name
        private const val INITIAL_ZOOM_VALUE = 1F
        private const val INITIAL_EXPOSURE_VALUE = 0
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
