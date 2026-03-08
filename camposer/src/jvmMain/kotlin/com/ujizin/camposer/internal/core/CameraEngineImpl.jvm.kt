package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.capture.JvmCameraCapture
import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.selector.CamPosition
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class CameraEngineImpl(
  override val cameraController: CameraController,
  override val cameraInfo: CameraInfo,
  override val capture: JvmCameraCapture,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CameraEngineCore(),
  JvmCameraEngine {
  override val cameraState: CameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
  )

  override val sessionTopologyApplier = SessionTopologyApplier(
    cameraState = cameraState,
    capture = capture,
  )

  override val previewApplier = PreviewApplier(
    cameraState = cameraState,
  )

  override val analyzerApplier = AnalyzerApplier(
    cameraState = cameraState,
    capture = capture,
  )

  override val exposureZoomApplier = ExposureZoomApplier(
    cameraState = cameraState,
    capture = capture,
  )

  override val videoApplier = VideoApplier(
    cameraState = cameraState,
    capture = capture,
  )

  init {
    onCameraInitialized()
  }

  override fun isMirrorEnabled(): Boolean =
    when (cameraState.mirrorMode.value) {
      MirrorMode.On -> true
      MirrorMode.Off -> false
      MirrorMode.OnlyInFront -> cameraState.camSelector.value.camPosition == CamPosition.Front
    }
}
