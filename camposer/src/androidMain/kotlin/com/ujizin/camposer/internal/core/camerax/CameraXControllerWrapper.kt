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
import kotlin.properties.Delegates

/**
 * Wrapper over [CameraController] implementing [CameraXController].
 *
 * Session-level configuration is managed through [SessionConfig] via
 * [CameraController.setSessionConfig]. Changes are batched during unbind/bind
 * cycles and applied atomically.
 */
internal class CameraXControllerWrapper(
  context: Context,
  override val lifecycleOwner: LifecycleOwner,
) : CameraXController {
  override val contentResolver: ContentResolver = context.contentResolver
  override val mainExecutor = context.compatMainExecutor

  private var cameraLifecycleOwner = CameraLifecycleOwner(lifecycleOwner)

  private val cameraXController by lazy { LifecycleCameraController(context) }

  // Session config state — stored locally and rebuilt via applySessionConfig()
  override var previewResolutionSelector: ResolutionSelector? by sessionProp(null)
  override var imageCaptureResolutionSelector: ResolutionSelector? by sessionProp(null)
  override var imageAnalysisResolutionSelector: ResolutionSelector? by sessionProp(null)
  override var videoCaptureQualitySelector: QualitySelector by sessionProp(
    Recorder.DEFAULT_QUALITY_SELECTOR,
  )
  override var videoCaptureTargetFrameRate: Range<Int> by sessionProp(Range(0, 0))
  override var videoCaptureMirrorMode: Int by sessionProp(MirrorMode.MIRROR_MODE_OFF)
  override var isVideoStabilizationEnabled: Boolean by sessionProp(false)
  override var isPreviewStabilizationEnabled: Boolean by sessionProp(false)
  override var cameraSelector: CameraSelector by sessionProp(CameraSelector.DEFAULT_BACK_CAMERA)
  override var imageCaptureMode: Int by sessionProp(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
  private var enabledUseCasesState: Int by sessionProp(
    CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS,
  )

  // Analyzer state — re-applied when ImageAnalysis instance is rebuilt
  private var pendingAnalyzer: ImageAnalysis.Analyzer? = null
  private var pendingAnalyzerExecutor: Executor? = null
  private var currentImageAnalysis: ImageAnalysis? = null

  private var isBound = false

  private fun <T> sessionProp(initial: T) =
    Delegates.observable(initial) { _, _, _ -> requestSessionUpdate() }

  private fun requestSessionUpdate() {
    if (isBound) applySessionConfig()
  }

  private fun onMain(block: () -> Unit) = mainExecutor.execute(block)

  private fun applySessionConfig() {
    val useCases = mutableListOf<UseCase>()

    // Preview is mandatory in SessionConfig
    useCases += Preview
      .Builder()
      .setTargetFrameRate(videoCaptureTargetFrameRate)
      .setPreviewStabilizationEnabled(
        isPreviewStabilizationEnabled && isUseCaseEnabled(CameraController.VIDEO_CAPTURE),
      ).apply {
        previewResolutionSelector?.let { setResolutionSelector(it) }
      }.build()

    if (isUseCaseEnabled(CameraController.IMAGE_CAPTURE)) {
      useCases += ImageCapture
        .Builder()
        .apply {
          setCaptureMode(imageCaptureMode)
          imageCaptureResolutionSelector?.let { setResolutionSelector(it) }
        }.build()
    }

    if (isUseCaseEnabled(CameraController.VIDEO_CAPTURE)) {
      val recorder = Recorder.Builder().setQualitySelector(videoCaptureQualitySelector).build()
      useCases += VideoCapture
        .Builder(recorder)
        .setMirrorMode(videoCaptureMirrorMode)
        .setVideoStabilizationEnabled(isVideoStabilizationEnabled)
        .apply {
          imageCaptureResolutionSelector?.let { setResolutionSelector(it) }
          if (videoCaptureTargetFrameRate.upper > 0) setTargetFrameRate(videoCaptureTargetFrameRate)
        }.build()
    }

    if (isUseCaseEnabled(CameraController.IMAGE_ANALYSIS)) {
      val imageAnalysis = ImageAnalysis
        .Builder()
        .apply {
          imageAnalysisResolutionSelector?.let { setResolutionSelector(it) }
        }.build()
      pendingAnalyzer?.let {
        imageAnalysis.setAnalyzer(
          pendingAnalyzerExecutor ?: mainExecutor,
          it,
        )
      }
      currentImageAnalysis = imageAnalysis
      useCases += imageAnalysis
    } else {
      currentImageAnalysis = null
    }
    cameraXController.setSessionConfig(SessionConfig.Builder(useCases).build(), cameraSelector)
  }

  override fun isUseCaseEnabled(useCase: Int) = enabledUseCasesState and useCase != 0

  override fun setEnabledUseCases(useCases: Int) {
    enabledUseCasesState = useCases
  }

  // Runtime properties: still allowed by CameraX when SessionConfig is active
  override var imageCaptureFlashMode: Int
    get() = cameraXController.imageCaptureFlashMode
    set(value) {
      if (!isUseCaseEnabled(CameraController.IMAGE_CAPTURE)) return
      onMain { cameraXController.imageCaptureFlashMode = value }
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
    isBound = false
  }

  override fun bindToLifecycle(lifecycle: LifecycleOwner) {
    if (!isBound) {
      cameraXController.bindToLifecycle(lifecycle)
      isBound = true
    }
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

  override fun enableTorch(isTorchEnabled: Boolean) =
    onMain { cameraXController.enableTorch(isTorchEnabled) }

  override fun setExposureCompensationIndex(exposureCompensationIndex: Int) =
    onMain {
      cameraXController.cameraControl?.setExposureCompensationIndex(
        exposureCompensationIndex,
      )
    }

  override fun setZoomRatio(zoomRatio: Float) = onMain { cameraXController.setZoomRatio(zoomRatio) }

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
