package br.com.devlucasyuji.camposer.state

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.TorchState
import androidx.camera.core.ZoomState
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.CameraController.OutputSize
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.ExperimentalVideo
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import br.com.devlucasyuji.camposer.extensions.asContext
import br.com.devlucasyuji.camposer.extensions.roundedZoomRatio
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executor

@Immutable
internal data class CameraStore(
    val scaleType: ScaleType = ScaleType.FillCenter,
    val flash: FlashMode = FlashMode.Off,
    val enableTorch: Boolean = false,
    val currentZoomRatio: Float = 1F,
    val isFocusOnTapEnabled: Boolean = true,
    val isPinchToZoomEnabled: Boolean = true
)

/**
 * CameraState to [CameraPreview] composable
 * */
class CameraState internal constructor(
    lifecycleOwner: LifecycleOwner,
    cameraStore: CameraStore,
    context: Context = lifecycleOwner.asContext()
) {

    /**
     * Main Executor to action as take picture or record.
     * */
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    /**
     * Content resolver to picture and video.
     * */
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Main controller from CameraX. useful in cases that haven't been release some feature yet.
     * */
    internal val controller: LifecycleCameraController = LifecycleCameraController(context)

    /**
     * Get max zoom from camera.
     * */
    var maxZoom: Float by mutableStateOf(
        controller.zoomState.value?.maxZoomRatio ?: INITIAL_ZOOM_VALUE
    )
        internal set

    /**
     * Get min zoom from camera.
     * */
    var minZoom: Float by mutableStateOf(
        controller.zoomState.value?.minZoomRatio ?: INITIAL_ZOOM_VALUE
    )
        internal set

    /**
     * Get current zoom from camera.
     * */
    var currentZoom: Float by mutableStateOf(
        controller.zoomState.value?.roundedZoomRatio ?: INITIAL_ZOOM_VALUE
    )
        private set

    /**
     * Check if pinch to zoom is in progress.
     * */
    var isPinchZoomInProgress by mutableStateOf(false)

    /**
     * Check if camera is streaming or not.
     * */
    var isStreaming by mutableStateOf(false)
        internal set

    /**
     * Check if zoom is supported.
     * */
    val isZoomSupported: Boolean
        get() = maxZoom != 1F

    /**
     * Return if camera state is initialized or not.
     * */
    var isInitialized: Boolean by mutableStateOf(false)
        internal set

    /**
     * Verify if camera has flash or not.
     * */
    var hasFlashUnit by mutableStateOf(controller.cameraInfo?.hasFlashUnit() ?: false)

    /**
     * Capture mode to be added on camera.
     * */
    internal var captureMode: CaptureMode = CaptureMode.Image
        set(value) {
            if (field != value) {
                field = value
                updateUseCases()
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

    /**
     * Camera mode, it can be front or back.
     * @see CamSelector
     * */
    internal var camSelector: CamSelector = CamSelector.Back
        set(value) {
            when {
                hasCamera(value) -> {
                    if (controller.cameraSelector != value.selector) {
                        controller.cameraSelector = value.selector
                        field = value
                        resetCamera()
                    }
                }

                else -> Log.e(TAG, "Device does not have ${value.selector} camera")
            }
        }

    /**
     * Get Image Analyzer from camera.
     * */
    internal var imageAnalyzer: ImageAnalysis.Analyzer? = null
        set(value) {
            field = value
            with(controller) {
                clearImageAnalysisAnalyzer()
                setImageAnalysisAnalyzer(mainExecutor, value ?: return)
            }
        }

    /**
     * CameraX's use cases captures.
     * */
    private val useCases: MutableSet<Int> = mutableSetOf()

    /**
     * Enable/Disable Image analysis from the camera.
     * */
    internal var isImageAnalysisEnabled: Boolean
        get() = controller.isImageAnalysisEnabled
        set(value) {
            if (isImageAnalysisEnabled != value) {
                if (value) useCases += IMAGE_ANALYSIS else useCases -= IMAGE_ANALYSIS
                updateUseCases()
            }
        }

    private fun updateUseCases() {
        controller.setEnabledUseCases(0)
        controller.setEnabledUseCases(captureMode.value or useCases.sumOr())
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
     * Get if pinch to zoom is enabled from cameraX.
     * */
    internal var isPinchToZoomEnabled: Boolean = true

    /**
     * Flash Mode from the camera.
     * @see FlashMode
     * */
    internal var flashMode: FlashMode
        get() = FlashMode.find(controller.imageCaptureFlashMode)
        set(value) {
            if (flashMode != value && !hasFlashUnit && flashMode != FlashMode.Off) {
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
     * Return true if it's recording.
     * */
    var isRecording: Boolean by mutableStateOf(controller.isRecording)
        private set

    init {
        controller.initializationFuture.addListener({
            cameraStore.restoreSettings()
            controller.torchState.observe(lifecycleOwner) { enableTorch = it == TorchState.ON }
            isInitialized = true
        }, mainExecutor)
    }

    /**
     *  Take a picture with the camera.
     *
     *  @param saveCollection Uri collection where the photo will be saved.
     *  @param contentValues Content values of the photo.
     *  @param onResult Callback called when [ImageCaptureResult] is ready
     * */
    fun takePicture(
        contentValues: ContentValues,
        saveCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        onResult: (ImageCaptureResult) -> Unit,
    ) {
        takePicture(
            outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                saveCollection,
                contentValues
            ).build(),
            onResult = onResult
        )
    }

    /**
     * Take a picture with the camera.
     * @param file file where the photo will be saved
     * @param onResult Callback called when [ImageCaptureResult] is ready
     * */
    fun takePicture(
        file: File,
        onResult: (ImageCaptureResult) -> Unit
    ) {
        takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), onResult)
    }

    /**
     * Take a picture with the camera.
     *
     * @param outputFileOptions Output file options of the photo.
     * @param onResult Callback called when [ImageCaptureResult] is ready
     * */
    fun takePicture(
        outputFileOptions: ImageCapture.OutputFileOptions,
        onResult: (ImageCaptureResult) -> Unit,
    ) {
        try {
            controller.takePicture(
                outputFileOptions,
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
    internal fun setZoomRatio(zoomRatio: Float) {
        controller.setZoomRatio(zoomRatio/*.coerceIn(minZoom, maxZoom)*/)
    }

    /**
     * Start recording camera.
     *
     * @param file file where the video will be saved
     * @param onResult Callback called when [VideoCaptureResult] is ready
     * */
    @ExperimentalVideo
    fun startRecording(file: File, onResult: (VideoCaptureResult) -> Unit) {
        startRecording(OutputFileOptions.builder(file).build(), onResult)
    }

    /**
     * Start recording camera.
     *
     *  @param saveCollection Uri collection where the video will be saved.
     *  @param contentValues Content values of the video.
     *  @param onResult Callback called when [VideoCaptureResult] is ready
     *  */
    @ExperimentalVideo
    fun startRecording(
        saveCollection: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        contentValues: ContentValues,
        onResult: (VideoCaptureResult) -> Unit,
    ) {
        startRecording(
            OutputFileOptions.builder(contentResolver, saveCollection, contentValues).build(),
            onResult
        )
    }

    /**
     * Start recording camera.
     *
     * @param outputFileOptions Output file options of the video.
     * @param onResult Callback called when [VideoCaptureResult] is ready
     * */
    @ExperimentalVideo
    fun startRecording(
        outputFileOptions: OutputFileOptions,
        onResult: (VideoCaptureResult) -> Unit,
    ) {
        try {
            isRecording = true
            controller.startRecording(
                outputFileOptions,
                mainExecutor,
                object : OnVideoSavedCallback {
                    override fun onVideoSaved(outputFileResults: OutputFileResults) {
                        isRecording = false
                        VideoCaptureResult.Success(outputFileResults.savedUri)
                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        isRecording = false
                        VideoCaptureResult.Error(videoCaptureError, message, cause)
                    }
                })
        } catch (exception: Exception) {
            isRecording = false
            onResult(
                VideoCaptureResult.Error(
                    OnVideoSavedCallback.ERROR_UNKNOWN,
                    if (!controller.isVideoCaptureEnabled) {
                        "Video capture is not enabled, please set captureMode as CaptureMode.Video"
                    } else "${exception.message}",
                    exception
                )
            )
        }
    }

    /**
     * Stop recording camera.
     * */
    @ExperimentalVideo
    fun stopRecording() {
        controller.stopRecording()
    }

    /**
     * Toggle recording camera.
     * */
    @ExperimentalVideo
    fun toggleRecording(
        contentValues: ContentValues,
        saveCollection: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        onResult: (VideoCaptureResult) -> Unit
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(saveCollection, contentValues, onResult)
        }
    }

    /**
     * Toggle recording camera.
     * */
    @ExperimentalVideo
    fun toggleRecording(
        outputFileOptions: OutputFileOptions,
        onResult: (VideoCaptureResult) -> Unit
    ) {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording(outputFileOptions, onResult)
        }
    }

    /**
     * Return if has camera selector or not, camera must be initialized, otherwise result is false.
     * */
    fun hasCamera(cameraSelector: CamSelector) =
        isInitialized && controller.hasCamera(cameraSelector.selector)

    internal fun dispatchZoom(zoom: ZoomState, block: (Float) -> Unit?) {
        minZoom = zoom.minZoomRatio
        maxZoom = zoom.maxZoomRatio
        currentZoom = zoom.roundedZoomRatio
        block(currentZoom)
    }

    private fun CameraStore.restoreSettings() {
        this@CameraState.camSelector = camSelector
        this@CameraState.isPinchToZoomEnabled = isPinchToZoomEnabled
        this@CameraState.scaleType = scaleType
        this@CameraState.flashMode = flashMode
        this@CameraState.isFocusOnTapEnabled = isFocusOnTapEnabled
        this@CameraState.enableTorch = enableTorch
        setZoomRatio(currentZoomRatio)
    }

    private fun resetCamera() {
        hasFlashUnit = controller.cameraInfo?.hasFlashUnit() ?: false
        flashMode = FlashMode.Off
        enableTorch = false
    }

    private fun Set<Int>.sumOr(initial: Int = 0): Int = fold(initial) { acc, current ->
        acc or current
    }

    internal suspend fun updatePinchZoomInProgress() {
        isPinchZoomInProgress = true
        delay(PINCH_ZOOM_IN_PROGRESS_DELAY)
        isPinchZoomInProgress = false
    }

    companion object {
        private val TAG = this::class.java.name
        private const val PINCH_ZOOM_IN_PROGRESS_DELAY = 1000L
        private const val INITIAL_ZOOM_VALUE = 1F

        @Suppress("UNCHECKED_CAST")
        internal fun getSaver(lifecycleOwner: LifecycleOwner): Saver<CameraState, *> =
            listSaver(save = {
                listOf(
                    it.scaleType,
                    it.flashMode,
                    it.enableTorch,
                    it.currentZoom,
                    it.isFocusOnTapEnabled,
                    it.isPinchToZoomEnabled
                )
            }, restore = {
                CameraState(
                    lifecycleOwner, CameraStore(
                        scaleType = it[0] as ScaleType,
                        flash = it[1] as FlashMode,
                        enableTorch = it[2] as Boolean,
                        currentZoomRatio = it[3] as Float,
                        isFocusOnTapEnabled = it[4] as Boolean,
                        isPinchToZoomEnabled = it[5] as Boolean,
                    )
                )
            })
    }
}
