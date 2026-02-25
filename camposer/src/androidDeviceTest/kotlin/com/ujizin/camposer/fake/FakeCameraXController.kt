package com.ujizin.camposer.fake

import android.content.ContentResolver
import android.net.Uri
import android.util.Range
import android.util.Rational
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ExposureState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ZoomState
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.core.net.toUri
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.ujizin.camposer.fake.data.dummyImageProxy
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.internal.core.camerax.RecordEvent
import com.ujizin.camposer.internal.core.camerax.RecordingWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executor

internal class FakeCameraXController : CameraXController {
  override var videoCaptureMirrorMode: Int = 0
  override var isTapToFocusEnabled: Boolean = false
  override var isPinchToZoomEnabled: Boolean = false
  override var previewResolutionSelector: ResolutionSelector? = null
  override var imageCaptureResolutionSelector: ResolutionSelector? = null
  override var imageAnalysisResolutionSelector: ResolutionSelector? = null
  override var videoCaptureQualitySelector: QualitySelector = QualitySelector.from(Quality.UHD)
  override var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
  override var videoCaptureTargetFrameRate: Range<Int> = Range(0, 0)
  override var imageCaptureFlashMode: Int = ImageCapture.FLASH_MODE_OFF
  override var imageCaptureMode: Int = CAPTURE_MODE_MINIMIZE_LATENCY

  private var fakeZoomRatio: Float = 1F
  private var fakeExposureCompensationIndex: Int = 1

  var fakeMinExposure = 0
  var fakeMaxExposure = 100
  var fakeMinZoom = 0.6F
  var fakeMaxZoom = 8F
  var analyzer: ImageAnalysis.Analyzer? = null
    private set
  var useCases: Int = 0
    private set
  var isTorchEnabled: Boolean = false
    private set

  var isZSLSupported: Boolean = true
    internal set

  var hasFlashUnit: Boolean = true
    internal set
  var isExposureSupported: Boolean = true
    internal set

  var hasErrorInRecording = false
    internal set
  var isRecording = false
    internal set

  private var exposureState: ExposureState = object : ExposureState {
    override fun getExposureCompensationIndex(): Int = fakeExposureCompensationIndex

    override fun getExposureCompensationRange(): Range<Int> =
      Range(fakeMinExposure, fakeMaxExposure)

    override fun getExposureCompensationStep(): Rational = Rational.ZERO

    override fun isExposureCompensationSupported(): Boolean = isExposureSupported
  }

  var previewView: PreviewView? = null
    private set

  var unbindCount = 0
    private set

  var bindCount = 0
    private set

  override val lifecycleOwner: LifecycleOwner
    get() = object : LifecycleOwner {
      override val lifecycle: Lifecycle
        get() = object : Lifecycle() {
          override fun addObserver(observer: LifecycleObserver) {
            // no-op
          }

          override fun removeObserver(observer: LifecycleObserver) {
            // no-op
          }

          override val currentState: State = State.CREATED
        }
    }

  override val contentResolver: ContentResolver
    get() = TODO("Fake Won't be implemented")

  override val mainExecutor: Executor
    get() = CameraXExecutors.directExecutor()

  override val zoomState = MutableLiveData<ZoomState>().apply {
    runBlocking(Dispatchers.Main) {
      // Post value does not work ._.
      value = object : ZoomState {
        override fun getZoomRatio(): Float = fakeZoomRatio

        override fun getMinZoomRatio(): Float = fakeMinZoom

        override fun getMaxZoomRatio(): Float = fakeMaxZoom

        override fun getLinearZoom(): Float = fakeZoomRatio
      }
    }
  }

  override val cameraInfo: CameraInfo
    get() = object : CameraInfo {
      override fun isZslSupported(): Boolean = isZSLSupported

      override fun getSensorRotationDegrees(): Int = -1

      override fun getSensorRotationDegrees(relativeRotation: Int): Int = -1

      override fun hasFlashUnit(): Boolean = hasFlashUnit

      override fun getTorchState(): LiveData<Int?> = liveData { emit(imageCaptureFlashMode) }

      override fun getZoomState(): LiveData<ZoomState> = this@FakeCameraXController.zoomState

      override fun getExposureState(): ExposureState = this@FakeCameraXController.exposureState

      override fun getCameraState(): LiveData<CameraState?> = liveData { null }

      override fun getImplementationType(): String = "Performance"

      override fun getCameraSelector(): CameraSelector = cameraSelector

      override fun getSupportedFrameRateRanges(): Set<Range<Int>?> =
        setOf(
          Range(1, 30),
        )
    }

  init {
    updateExposureState(fakeMinExposure)
  }

  override fun get(): LifecycleCameraController {
    TODO("Fake will not be implemented")
  }

  override fun isCameraControllerEquals(controller: CameraController?): Boolean = false

  override fun attachPreview(view: PreviewView) {
    previewView = view
  }

  override fun onInitialize(
    executor: Executor,
    block: () -> Unit,
  ) {
    block()
  }

  override fun setImageAnalysisAnalyzer(
    executor: Executor,
    analyzer: ImageAnalysis.Analyzer,
  ) {
    this.analyzer = analyzer
    analyzer.analyze(dummyImageProxy)
  }

  override fun setEnabledUseCases(useCases: Int) {
    this.useCases = useCases
  }

  override fun enableTorch(isTorchEnabled: Boolean) {
    mainExecutor.execute {
      this.isTorchEnabled = isTorchEnabled
    }
  }

  override fun setExposureCompensationIndex(exposureCompensationIndex: Int) {
    updateExposureState(exposureCompensationIndex)
  }

  private fun updateExposureState(exposureCompensationIndex: Int) {
    fakeExposureCompensationIndex = exposureCompensationIndex
  }

  override fun setZoomRatio(zoomRatio: Float) {
    fakeZoomRatio = zoomRatio
  }

  override fun startRecording(
    fileDescriptorOutputOptions: FileDescriptorOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper =
    createRecording(
      outputUri = Uri.EMPTY,
      consumerEvent = consumerEvent,
    )

  override fun startRecording(
    mediaStoreOutputOptions: MediaStoreOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper =
    createRecording(
      outputUri = mediaStoreOutputOptions.collectionUri,
      consumerEvent = consumerEvent,
    )

  override fun startRecording(
    fileOutputOptions: FileOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper =
    createRecording(
      outputUri = fileOutputOptions.file.toUri(),
      consumerEvent = consumerEvent,
    )

  override fun takePicture(
    outputFileOptions: ImageCapture.OutputFileOptions,
    mainExecutor: Executor,
    callback: ImageCapture.OnImageSavedCallback,
  ) {
    val savedUri: Uri = outputFileOptions.file?.toUri() ?: Uri.EMPTY
    callback.onImageSaved(ImageCapture.OutputFileResults(savedUri))
  }

  override fun unbind() {
    unbindCount++
  }

  override fun bindToLifecycle(lifecycle: LifecycleOwner) {
    bindCount++
  }

  private fun createRecording(
    outputUri: Uri,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper {
    isRecording = true
    consumerEvent.accept(
      RecordEvent(
        isFinalized = false,
        hasError = false,
        outputUri = Uri.EMPTY,
        isStarted = true,
      ),
    )
    return FakeRecordingWrapper(
      onRecord = {
        isRecording = false
        consumerEvent.accept(
          RecordEvent(
            isFinalized = true,
            hasError = hasErrorInRecording,
            outputUri = outputUri,
          ),
        )
      },
    )
  }
}
