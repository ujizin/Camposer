package com.ujizin.camposer.internal.core.camerax

import android.content.ContentResolver
import android.util.Range
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ZoomState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.QualitySelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import java.util.concurrent.Executor

internal interface CameraXController {
  val lifecycleOwner: LifecycleOwner
  val contentResolver: ContentResolver
  val mainExecutor: Executor
  var videoCaptureMirrorMode: Int
  var isTapToFocusEnabled: Boolean
  var isPinchToZoomEnabled: Boolean

  var previewResolutionSelector: ResolutionSelector?
  var imageCaptureResolutionSelector: ResolutionSelector?
  var imageAnalysisResolutionSelector: ResolutionSelector?
  var videoCaptureQualitySelector: QualitySelector
  var cameraSelector: CameraSelector
  var videoCaptureTargetFrameRate: Range<Int>

  var imageCaptureFlashMode: Int

  var imageCaptureMode: Int

  val zoomState: LiveData<ZoomState>

  val cameraInfo: CameraInfo?

  fun get(): LifecycleCameraController

  fun isCameraControllerEquals(controller: CameraController?): Boolean

  fun onInitialize(
    executor: Executor = mainExecutor,
    block: () -> Unit,
  )

  fun setImageAnalysisAnalyzer(
    executor: Executor,
    analyzer: ImageAnalysis.Analyzer,
  )

  fun setEnabledUseCases(useCases: Int)

  fun enableTorch(isTorchEnabled: Boolean)

  fun setExposureCompensationIndex(exposureCompensationIndex: Int)

  fun setZoomRatio(zoomRatio: Float)

  fun startRecording(
    fileDescriptorOutputOptions: FileDescriptorOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper

  fun startRecording(
    mediaStoreOutputOptions: MediaStoreOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper

  fun startRecording(
    fileOutputOptions: FileOutputOptions,
    audioConfig: AudioConfig,
    mainExecutor: Executor,
    consumerEvent: Consumer<RecordEvent>,
  ): RecordingWrapper

  fun takePicture(
    outputFileOptions: ImageCapture.OutputFileOptions,
    mainExecutor: Executor,
    callback: ImageCapture.OnImageSavedCallback,
  )

  fun unbind()

  fun bindToLifecycle(lifecycle: LifecycleOwner)

  fun attachPreview(view: PreviewView)
}
