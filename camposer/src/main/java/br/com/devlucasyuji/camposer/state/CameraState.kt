package br.com.devlucasyuji.camposer.state

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.os.Build
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
import java.text.SimpleDateFormat
import java.util.Locale
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
            useCases.setUseCase(value, IMAGE_ANALYSIS)
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
     *  Take a picture on the camera.
     *
     *  @param onResult Callback called when [PhotoResult] is ready
     * */
    fun takePicture(name: String? = null, onResult: (PhotoResult) -> Unit) {
        controller.takePicture(createOutputFile(name),
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onResult(PhotoResult.Success)
                }

                override fun onError(exception: ImageCaptureException) {
                    onResult(PhotoResult.Error(exception))
                }
            })
    }

    /**
     * Create output file directory to camera.
     * */
    private fun createOutputFile(
        name: String?,
        contentValues: ContentValues = getContentValues(
            name = name ?: SimpleDateFormat(
                DEFAULT_DATE_FORMAT, Locale.US
            ).format(System.currentTimeMillis()),
            mimeType = JPEG_MIME_TYPE,
            relativePath = RELATIVE_PATH
        ),
    ): ImageCapture.OutputFileOptions = ImageCapture.OutputFileOptions.Builder(
        contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
    ).build()

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

    /**
     * Get content values to output file.
     * */
    private fun getContentValues(name: String, mimeType: String, relativePath: String) =
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            }
        }

    private fun MutableSet<Int>.setUseCase(value: Boolean, useCase: Int) {
        if (value) add(useCase) else remove(useCase)
        val useCases = fold(0) { acc, current -> acc or current }
        controller.setEnabledUseCases(useCases)
    }

    companion object {
        private val TAG = this::class.java.name

        private const val INITIAL_ZOOM_VALUE = 1F
        private const val DEFAULT_DATE_FORMAT = "YYYY-HH:MM:SS"
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val VIDEO_MIME_TYPE = "video/avc"

        // FIXME set app name from the app or searching another way to set name
        private const val RELATIVE_PATH = "Pictures/Camposer"

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
