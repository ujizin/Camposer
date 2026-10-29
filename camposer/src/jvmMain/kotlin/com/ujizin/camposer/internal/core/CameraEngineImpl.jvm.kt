package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.isMirrorEnabled
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual class CameraEngineImpl(
  actual override val cameraController: CameraController,
  actual override val cameraInfo: CameraInfo,
  override val jvmCameraController: JvmCameraController,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CameraEngineCore(),
  JvmCameraEngine {
  actual override val cameraState: CameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
  )

  override val sessionTopologyApplier = SessionTopologyApplier(
    cameraState = cameraState,
    cameraInfo = cameraInfo,
    jvmCameraController = jvmCameraController,
  )

  override val previewApplier = PreviewApplier(
    cameraState = cameraState,
  )

  override val analyzerApplier = AnalyzerApplier(
    cameraState = cameraState,
    jvmCameraController = jvmCameraController,
  )

  override val exposureZoomApplier = ExposureZoomApplier(
    cameraState = cameraState,
    jvmCameraController = jvmCameraController,
  )

  override val videoApplier = VideoApplier(
    cameraState = cameraState,
    jvmCameraController = jvmCameraController,
  )

  actual override fun isMirrorEnabled(): Boolean =
    cameraState.mirrorMode.value.isMirrorEnabled(cameraState.camSelector.value.camPosition)
}
