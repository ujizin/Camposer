package br.com.devlucasyuji.camposer

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.ExperimentalVideo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import br.com.devlucasyuji.camposer.extensions.roundTo
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

/**
 * CameraState to [CameraPreview] composable
 * */

internal data class CameraStore(
    val cameraSelector: CameraSelector = CameraSelector.Back,
    val scaleType: ScaleType = ScaleType.FillCenter,
    val flashMode: FlashMode = FlashMode.Off,
    val enableTorch: Boolean = false,
    val currentZoomRatio: Float = 1F,
    val isFocusOnTapEnabled: Boolean = true,
    val isPinchToZoomEnabled: Boolean = true
)

class CameraState internal constructor(
    context: Context,
    cameraStore: CameraStore
) {

    /**
     * Main Executor to action as take picture or record.
     * */
    private var mainExecutor: Executor

    /**
     * Content resolver to picture and video.
     * */
    private var contentResolver: ContentResolver

    /**
     * Range between min and max zoom ratio from the camera.
     *
     * Initialized at [CameraState.setupZoom]
     * */
    var zoomRange: Pair<Float, Float> = Pair(UNSET_VALUE, UNSET_VALUE)
        private set

    /**
     * Main controller from CameraX. useful in cases that haven't been release some feature yet.
     * */
    lateinit var controller: LifecycleCameraController
        private set

    /**
     * Return if camera state is initialized or not.
     * */
    var isInitialized: Boolean by mutableStateOf(false)
        internal set

    /**
     * Camera mode, it can be front or back.
     * @see CameraSelector
     * */
    var cameraSelector: CameraSelector = CameraSelector.Back
        internal set(value) { // TODO add blur effect here?
            when {
                hasCamera(value) -> {
                    controller.cameraSelector = value.selector
                    field = value
                }
                else -> Log.e(TAG, "Device does not have ${value.name} camera")
            }
        }

    /**
     * Current zoom ratio of the camera.
     * */
    var currentZoomRatio: Float = 1F
        internal set(value) {
            field = value.roundTo(1)
        }

    /**
     * Get if focus on tap is enabled.
     * */
    var isFocusOnTapEnabled = true
        internal set(value) {
            controller.isTapToFocusEnabled = value
            field = value
        }

    var isPinchToZoomEnabled = true
        internal set(value) {
            controller.isPinchToZoomEnabled = field
            field = value
        }

    /**
     * Get scale type from the camera.
     * */
    var scaleType: ScaleType = ScaleType.FillCenter
        internal set

    /**
     * Return true if it's recording.
     * */
    val isRecording: Boolean
        @ExperimentalVideo
        get() = controller.isRecording

    /**
     * Flash Mode from the camera.
     * @see FlashMode
     * */
    var flashMode: FlashMode = FlashMode.Off
        internal set(value) {
            controller.imageCaptureFlashMode = value.mode
            field = value
        }

    init {
        mainExecutor = ContextCompat.getMainExecutor(context)
        controller = LifecycleCameraController(context)
        contentResolver = context.contentResolver

        controller.initializationFuture.addListener({
            cameraStore.let { store ->
                currentZoomRatio = store.currentZoomRatio
                scaleType = store.scaleType
                flashMode = store.flashMode
                cameraSelector = store.cameraSelector
                enableTorch = store.enableTorch
                isPinchToZoomEnabled = store.isPinchToZoomEnabled
                isFocusOnTapEnabled = store.isFocusOnTapEnabled
            }
            setupZoom()
            isInitialized = true
        }, mainExecutor)
    }

    /**
     *  Take a picture on the camera.
     *
     *  @param onResult Callback called when [PhotoResult] is ready
     * */
    fun takePicture(name: String? = null, onResult: (PhotoResult) -> Unit) {
        controller.takePicture(
            createOutputFile(name),
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

    /**
     * Toggle camera, it can be front or back camera
     * */
    fun toggleCamera() {
        cameraSelector = when (cameraSelector) {
            CameraSelector.Front -> CameraSelector.Back
            else -> CameraSelector.Front
        }
    }

    /**
     * Toggle Flash, it can be on or off, this case auto is ignored.
     * */
    fun toggleFlash() {
        flashMode = when (flashMode) {
            FlashMode.On -> FlashMode.Off
            else -> FlashMode.On
        }
    }

    /**
     * Set zoom ratio to camera.
     * @param zoomRatio zoomRatio to be added
     * */
    fun setZoomRatio(zoomRatio: Float) {
        controller.setZoomRatio(zoomRatio)
    }

    /**
     * Set linear ratio zoom to camera.
     *
     * @param linearZoom linearZoom to be added
     * */
    fun setLinearRatio(linearZoom: Float) {
        controller.setLinearZoom(linearZoom)
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
     * Enabled/Disable torch from camera.
     * */
    var enableTorch: Boolean = false
        set(value) {
            controller.enableTorch(value)
            field = value
        }

    /**
     * Return if has camera selector or not.
     * */
    fun hasCamera(cameraSelector: CameraSelector) = controller.hasCamera(cameraSelector.selector)

    /**
     *  Bind zoom values from live data.
     * */
    private fun setupZoom() {
        if (zoomRange == Pair(UNSET_VALUE, UNSET_VALUE)) {
            val zoomState = controller.zoomState.value ?: return
            zoomRange = Pair(zoomState.minZoomRatio, zoomState.maxZoomRatio)
        }
    }

    internal fun dispatchZoom(zoom: Float, block: (Float) -> Unit?) {
        currentZoomRatio = zoom.roundTo(1)
        block(currentZoomRatio)
    }

    companion object {
        private val TAG = this::class.java.name

        private const val UNSET_VALUE = -1F
        private const val DEFAULT_DATE_FORMAT = "YYYY-HH:MM:SS"
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val VIDEO_MIME_TYPE = "video/avc"

        // FIXME set app name from the app or searching another way to set name
        private const val RELATIVE_PATH = "Pictures/Camposer"

        @Suppress("UNCHECKED_CAST")
        fun getSaver(context: Context): Saver<CameraState, *> = listSaver(save = {
            listOf(
                it.cameraSelector,
                it.scaleType,
                it.flashMode,
                it.enableTorch,
                it.currentZoomRatio
            )
        }, restore = {
            CameraState(
                context,
                CameraStore(
                    cameraSelector = it[0] as CameraSelector,
                    scaleType = it[1] as ScaleType,
                    flashMode = it[2] as FlashMode,
                    enableTorch = it[3] as Boolean,
                    currentZoomRatio = it[4] as Float,
                )
            )
        })
    }
}
