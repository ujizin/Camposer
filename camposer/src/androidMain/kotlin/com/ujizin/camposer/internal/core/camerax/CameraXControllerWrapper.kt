package com.ujizin.camposer.internal.core.camerax

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.util.Range
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.MirrorMode
import androidx.camera.core.Preview
import androidx.camera.core.SessionConfig
import androidx.camera.core.UseCase
import androidx.camera.core.ZoomState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.ujizin.camposer.extensions.compatMainExecutor
import com.ujizin.camposer.internal.CameraLifecycleOwner
import java.util.concurrent.Executor

/**
 * A wrapper class for [CameraController] that implements [CameraXController].
 *
 * This class serves as an abstraction layer over CameraX's `CameraController`,
 * allowing for easier testing and decoupling of the camera implementation details
 * from the rest of the application. It delegates functionality to an underlying
 * `CameraController` instance while adhering to the `CameraXController` interface contract.
 *
 * Session-level configuration (resolution selectors, quality, frame rate, mirror mode,
 * camera selector, capture mode, enabled use cases) is managed internally through
 * [SessionConfig] via [CameraController.setSessionConfig]. Changes are batched during
 * unbind/bind cycles and applied atomically.
 */
internal class CameraXControllerWrapper(
  context: Context,
  override val lifecycleOwner: LifecycleOwner,
) : CameraXController {
  override val contentResolver: ContentResolver = context.contentResolver

  override val mainExecutor = context.compatMainExecutor

  private var cameraLifecycleOwner = CameraLifecycleOwner(lifecycleOwner)

  private val cameraXController by lazy {
    LifecycleCameraController(context)
  }

  // Session config state — stored locally and applied via setSessionConfig()
  private var _previewResolutionSelector: ResolutionSelector? = null
  private var _imageCaptureResolutionSelector: ResolutionSelector? = null
  private var _imageAnalysisResolutionSelector: ResolutionSelector? = null
  private var _videoCaptureQualitySelector: QualitySelector = Recorder.DEFAULT_QUALITY_SELECTOR
  private var _videoCaptureTargetFrameRate: Range<Int> = Range(0, 0)
  private var _videoCaptureMirrorMode: Int = MirrorMode.MIRROR_MODE_OFF
  private var _cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
  private var _imageCaptureMode: Int = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
  private var enabledUseCasesState: Int =
    CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS

  // Analyzer state — re-applied when ImageAnalysis instance is rebuilt
  private var pendingAnalyzer: ImageAnalysis.Analyzer? = null
  private var pendingAnalyzerExecutor: Executor? = null
  private var currentImageAnalysis: ImageAnalysis? = null

  // Batching: unbind() starts batch mode; bindToLifecycle() ends it and applies session config
  private var isBatching = false
  private var isBound = false

  private fun requestSessionUpdate() {
    if (!isBatching && isBound) applySessionConfig()
  }

  private fun applySessionConfig() {
    val useCases = mutableListOf<UseCase>()

    // Preview is mandatory in SessionConfig
    useCases += Preview
      .Builder()
      .apply {
        _previewResolutionSelector?.let { setResolutionSelector(it) }
      }.build()

    if (enabledUseCasesState and CameraController.IMAGE_CAPTURE != 0) {
      useCases += ImageCapture
        .Builder()
        .apply {
          setCaptureMode(_imageCaptureMode)
          _imageCaptureResolutionSelector?.let { setResolutionSelector(it) }
        }.build()
    }

    if (enabledUseCasesState and CameraController.VIDEO_CAPTURE != 0) {
      val recorder = Recorder
        .Builder()
        .setQualitySelector(_videoCaptureQualitySelector)
        .build()
      useCases += VideoCapture
        .Builder(recorder)
        .setMirrorMode(_videoCaptureMirrorMode)
        .apply {
          if (_videoCaptureTargetFrameRate.upper > 0) {
            setTargetFrameRate(_videoCaptureTargetFrameRate)
          }
        }.build()
    }

    if (enabledUseCasesState and CameraController.IMAGE_ANALYSIS != 0) {
      val imageAnalysis = ImageAnalysis
        .Builder()
        .apply {
          _imageAnalysisResolutionSelector?.let { setResolutionSelector(it) }
        }.build()
      pendingAnalyzer?.let { analyzer ->
        imageAnalysis.setAnalyzer(pendingAnalyzerExecutor ?: mainExecutor, analyzer)
      }
      currentImageAnalysis = imageAnalysis
      useCases += imageAnalysis
    } else {
      currentImageAnalysis = null
    }

    val sessionConfig = SessionConfig.Builder(useCases).build()
    cameraXController.setSessionConfig(sessionConfig, _cameraSelector)
  }

  // Session-config properties: store locally + trigger rebuild
  override var previewResolutionSelector: ResolutionSelector?
    get() = _previewResolutionSelector
    set(value) {
      _previewResolutionSelector = value
      requestSessionUpdate()
    }

  override var imageCaptureResolutionSelector: ResolutionSelector?
    get() = _imageCaptureResolutionSelector
    set(value) {
      _imageCaptureResolutionSelector = value
      requestSessionUpdate()
    }

  override var imageAnalysisResolutionSelector: ResolutionSelector?
    get() = _imageAnalysisResolutionSelector
    set(value) {
      _imageAnalysisResolutionSelector = value
      requestSessionUpdate()
    }

  override var videoCaptureQualitySelector: QualitySelector
    get() = _videoCaptureQualitySelector
    set(value) {
      _videoCaptureQualitySelector = value
      requestSessionUpdate()
    }

  override var videoCaptureTargetFrameRate: Range<Int>
    get() = _videoCaptureTargetFrameRate
    set(value) {
      _videoCaptureTargetFrameRate = value
      requestSessionUpdate()
    }

  override var videoCaptureMirrorMode: Int
    get() = _videoCaptureMirrorMode
    set(value) {
      _videoCaptureMirrorMode = value
      requestSessionUpdate()
    }

  override var cameraSelector: CameraSelector
    get() = _cameraSelector
    set(value) {
      _cameraSelector = value
      requestSessionUpdate()
    }

  override var imageCaptureMode: Int
    get() = _imageCaptureMode
    set(value) {
      _imageCaptureMode = value
      requestSessionUpdate()
    }

  override fun setEnabledUseCases(useCases: Int) {
    enabledUseCasesState = useCases
    requestSessionUpdate()
  }

  // Runtime properties: still allowed by CameraX when SessionConfig is active
  override var imageCaptureFlashMode: Int
    get() = cameraXController.imageCaptureFlashMode
    set(value) {
      // CameraX throws if SessionConfig is active without ImageCapture (e.g. video-only mode)
      if (enabledUseCasesState and CameraController.IMAGE_CAPTURE == 0) return
      mainExecutor.execute {
        cameraXController.imageCaptureFlashMode = value
      }
    }

  override var isPinchToZoomEnabled: Boolean
    get() = cameraXController.isPinchToZoomEnabled
    set(value) {
      cameraXController.isPinchToZoomEnabled = value
    }

  override var isTapToFocusEnabled: Boolean
    get() = cameraXController.isTapToFocusEnabled
    set(value) {
      cameraXController.isTapToFocusEnabled = value
    }

  override val zoomState: LiveData<ZoomState>
    get() = cameraXController.zoomState

  override val cameraInfo: CameraInfo?
    get() = cameraXController.cameraInfo

  override fun get(): LifecycleCameraController = cameraXController

  override fun isCameraControllerEquals(controller: CameraController?) =
    cameraXController == controller

  override fun unbind() {
    cameraXController.unbind()
    isBatching = true
    isBound = false
  }

  override fun bindToLifecycle(lifecycle: LifecycleOwner) {
    if (!isBound) {
      cameraXController.bindToLifecycle(lifecycle)
      isBound = true
    }
    isBatching = false
    applySessionConfig()
  }

  override fun attachPreview(view: PreviewView) {
    view.controller = cameraXController
  }

  override fun onInitialize(
    executor: Executor,
    block: () -> Unit,
  ) {
    cameraXController.initializationFuture.addListener(block, executor)
  }

  override fun setImageAnalysisAnalyzer(
    executor: Executor,
    analyzer: ImageAnalysis.Analyzer,
  ) {
    pendingAnalyzer = analyzer
    pendingAnalyzerExecutor = executor
    currentImageAnalysis?.setAnalyzer(executor, analyzer)
  }

  override fun enableTorch(isTorchEnabled: Boolean) {
    mainExecutor.execute {
      cameraXController.enableTorch(isTorchEnabled)
    }
  }

  override fun setExposureCompensationIndex(exposureCompensationIndex: Int) {
    mainExecutor.execute {
      cameraXController.cameraControl?.setExposureCompensationIndex(exposureCompensationIndex)
    }
  }

  override fun setZoomRatio(zoomRatio: Float) {
    mainExecutor.execute {
      cameraXController.setZoomRatio(zoomRatio)
    }
  }

  override fun takePicture(
    outputFileOptions: ImageCapture.OutputFileOptions,
    mainExecutor: Executor,
    callback: ImageCapture.OnImageSavedCallback,
  ) {
    cameraXController.takePicture(outputFileOptions, mainExecutor, callback)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun startRecording(
    fileDescriptorOutputOptions: FileDescriptorOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper =
    RecordingWrapperImpl(
      cameraXController.startRecording(
        fileDescriptorOutputOptions,
        audioConfig,
        mainExecutor,
        { consumerEvent.accept(RecordEvent(it)) },
      ),
    )

  override fun startRecording(
    mediaStoreOutputOptions: MediaStoreOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper =
    RecordingWrapperImpl(
      cameraXController.startRecording(
        mediaStoreOutputOptions,
        audioConfig,
        mainExecutor,
        { consumerEvent.accept(RecordEvent(it)) },
      ),
    )

  override fun startRecording(
    fileOutputOptions: FileOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper =
    RecordingWrapperImpl(
      cameraXController.startRecording(
        fileOutputOptions,
        audioConfig,
        mainExecutor,
        { consumerEvent.accept(RecordEvent(it)) },
      ),
    )

  override fun dispose() {
    cameraLifecycleOwner.dispose()
  }
}
