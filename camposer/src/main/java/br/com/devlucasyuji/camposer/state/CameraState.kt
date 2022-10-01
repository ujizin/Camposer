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
import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.OutputSize
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.ExperimentalVideo
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
    val camSelector: CamSelector = CamSelector.Back,
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
    private var captureMode: Int = IMAGE_CAPTURE
        set(value) {
            field = value
            updateUseCases()
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
            if (value) useCases += IMAGE_ANALYSIS else useCases -= IMAGE_ANALYSIS
            updateUseCases()
        }

    private fun updateUseCases() {
        controller.setEnabledUseCases(captureMode or useCases.sumOr())
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
    internal var isPinchToZoomEnabled: Boolean
        get() = controller.isPinchToZoomEnabled
        set(value) {
            controller.isPinchToZoomEnabled = value
        }

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
    val isRecording: Boolean
        @ExperimentalVideo get() = controller.isRecording

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
     *  @param saveCollection Uri collection where photo will be saved.
     *  @param contentValues Content values of the photo.
     *  @param onResult Callback called when [PhotoResult] is ready
     * */
    fun takePicture(
        contentValues: ContentValues,
        saveCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        onResult: (PhotoResult) -> Unit,
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
     * @param file file where photo will be saved
     * @param onResult Callback called when [PhotoResult] is ready
     * */
    fun takePicture(
        file: File,
        onResult: (PhotoResult) -> Unit
    ) {
        takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), onResult)
    }

    /**
     * Take a picture with the camera.
     *
     * @param outputFileOptions Output file options of the photo.
     * @param onResult Callback called when [PhotoResult] is ready
     * */
    fun takePicture(
        outputFileOptions: ImageCapture.OutputFileOptions,
        onResult: (PhotoResult) -> Unit,
    ) {
        controller.takePicture(
            outputFileOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onResult(PhotoResult.Success(outputFileResults.savedUri))
                }

                override fun onError(exception: ImageCaptureException) {
                    onResult(PhotoResult.Error(exception))
                }
            })
    }

    /**
     * Set zoom ratio to camera.
     * @param zoomRatio zoomRatio to be added
     * */
    internal fun setZoomRatio(zoomRatio: Float) {
        controller.setZoomRatio(zoomRatio.coerceIn(minZoom, maxZoom))
    }

    /**
     * Start recording camera.
     * */
    // TODO add start recording
    fun startRecording() {
//        controller.startRecording()
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
    fun toggleRecording() {
        when (isRecording) {
            true -> stopRecording()
            false -> startRecording()
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
        this@CameraState.isFocusOnTapEnabled = isFocusOnTapEnabled
        this@CameraState.enableTorch = enableTorch
        setZoomRatio(currentZoomRatio)
    }

    private fun resetCamera() {
        controller.setZoomRatio(INITIAL_ZOOM_VALUE)
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
//                it.cameraSelector,
                    it.scaleType, it.flashMode, it.enableTorch, it.currentZoom
                )
            }, restore = {
                CameraState(
                    lifecycleOwner, CameraStore(
//                    cameraSelector = it[0] as CameraSelector,
                        scaleType = it[0] as ScaleType,
                        flash = it[1] as FlashMode,
                        enableTorch = it[2] as Boolean,
                        currentZoomRatio = it[3] as Float,
                    )
                )
            })
    }
}
