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
import com.ujizin.camposer.extensions.compatMainExecutor
import java.util.concurrent.Executor

/**
 * A wrapper class for [CameraController] that implements [CameraXController].
 *
 * This class serves as an abstraction layer over CameraX's `CameraController`,
 * allowing for easier testing and decoupling of the camera implementation details
 * from the rest of the application. It delegates functionality to an underlying
 * `CameraController` instance while adhering to the `CameraXController` interface contract.
 */
internal class CameraXControllerWrapper(
  context: Context,
) : CameraXController {
  override val lifecycleOwner = (context as LifecycleOwner)

  override val contentResolver: ContentResolver = context.contentResolver

  override val mainExecutor = context.compatMainExecutor

  private val cameraXController by lazy {
    LifecycleCameraController(context)
  }

  override var videoCaptureMirrorMode: Int
    get() = cameraXController.videoCaptureMirrorMode
    set(value) {
      cameraXController.videoCaptureMirrorMode = value
    }

  override var isTapToFocusEnabled: Boolean
    get() = cameraXController.isTapToFocusEnabled
    set(value) {
      cameraXController.isTapToFocusEnabled = value
    }

  override var previewResolutionSelector: ResolutionSelector?
    get() = cameraXController.previewResolutionSelector
    set(value) {
      cameraXController.previewResolutionSelector = value
    }

  override var imageCaptureResolutionSelector: ResolutionSelector?
    get() = cameraXController.imageCaptureResolutionSelector
    set(value) {
      cameraXController.imageCaptureResolutionSelector = value
    }

  override var imageAnalysisResolutionSelector: ResolutionSelector?
    get() = cameraXController.imageAnalysisResolutionSelector
    set(value) {
      cameraXController.imageAnalysisResolutionSelector = value
    }

  override var videoCaptureQualitySelector: QualitySelector
    get() = cameraXController.videoCaptureQualitySelector
    set(value) {
      if (cameraXController.videoCaptureQualitySelector == value) return

      cameraXController.videoCaptureQualitySelector = value
    }

  override var cameraSelector: CameraSelector
    get() = cameraXController.cameraSelector
    set(value) {
      cameraXController.cameraSelector = value
    }

  override var videoCaptureTargetFrameRate: Range<Int>
    get() = cameraXController.videoCaptureTargetFrameRate
    set(value) {
      cameraXController.videoCaptureTargetFrameRate = value
    }

  override var imageCaptureFlashMode: Int
    get() = cameraXController.imageCaptureFlashMode
    set(value) {
      mainExecutor.execute {
        cameraXController.imageCaptureFlashMode = value
      }
    }
  override var isPinchToZoomEnabled: Boolean
    get() = cameraXController.isPinchToZoomEnabled
    set(value) {
      cameraXController.isPinchToZoomEnabled = value
    }

  override val zoomState: LiveData<ZoomState>
    get() = cameraXController.zoomState

  override val cameraInfo: CameraInfo?
    get() = cameraXController.cameraInfo

  override var imageCaptureMode: Int
    get() = cameraXController.imageCaptureMode
    set(value) {
      cameraXController.imageCaptureMode = value
    }

  override fun get(): LifecycleCameraController = cameraXController

  override fun isCameraControllerEquals(controller: CameraController?) =
    cameraXController == controller

  override fun takePicture(
    outputFileOptions: ImageCapture.OutputFileOptions,
    mainExecutor: Executor,
    callback: ImageCapture.OnImageSavedCallback,
  ) {
    cameraXController.takePicture(outputFileOptions, mainExecutor, callback)
  }

  override fun unbind() {
    cameraXController.unbind()
  }

  override fun bindToLifecycle(lifecycle: LifecycleOwner) {
    cameraXController.bindToLifecycle(lifecycle)
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
    cameraXController.setImageAnalysisAnalyzer(executor, analyzer)
  }

  override fun setEnabledUseCases(useCases: Int) {
    cameraXController.setEnabledUseCases(useCases)
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
}
