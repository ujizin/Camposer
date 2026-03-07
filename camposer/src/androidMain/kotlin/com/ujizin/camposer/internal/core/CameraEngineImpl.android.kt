package com.ujizin.camposer.internal.core

import android.content.ContentResolver
import androidx.camera.core.MirrorMode.MIRROR_MODE_OFF
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
import com.ujizin.camposer.internal.core.camerax.CameraXController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.selector.CamPosition
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executor

internal class CameraEngineImpl(
  override val cameraController: CameraController,
  override val cameraInfo: CameraInfo,
  override val cameraXController: CameraXController,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CameraEngineCore(), AndroidCameraEngine {
  override val mainExecutor: Executor
    get() = cameraXController.mainExecutor

  override val contentResolver: ContentResolver
    get() = cameraXController.contentResolver

  override val cameraState: CameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
  )

  override val sessionTopologyApplier = SessionTopologyApplier(
    cameraState = cameraState,
    cameraInfo = cameraInfo,
    cameraXController = cameraXController,
  )

  override val previewApplier = PreviewApplier(
    cameraState = cameraState,
    cameraXController = cameraXController,
  )

  override val analyzerApplier = AnalyzerApplier(
    cameraState = cameraState,
    cameraXController = cameraXController,
  )

  override val exposureZoomApplier = ExposureZoomApplier(
    cameraState = cameraState,
    cameraXController = cameraXController,
  )

  override val videoApplier = VideoApplier(
    cameraInfo = cameraInfo,
    cameraState = cameraState,
    cameraXController = cameraXController,
  )

  override fun onCameraInitialized() {
    cameraXController.lifecycleOwner.lifecycle.addObserver(CameraLifecycleObserver())
    super.onCameraInitialized()
  }

  override fun isMirrorEnabled(): Boolean =
    when (cameraXController.videoCaptureMirrorMode) {
      MIRROR_MODE_ON -> true
      MIRROR_MODE_OFF -> false
      else -> cameraState.camSelector.value.camPosition == CamPosition.Front
    }

  internal inner class CameraLifecycleObserver : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
      super.onResume(owner)
      onCameraResumed()
    }

    override fun onPause(owner: LifecycleOwner) {
      onCameraPaused()
      super.onPause(owner)
    }
  }
}
