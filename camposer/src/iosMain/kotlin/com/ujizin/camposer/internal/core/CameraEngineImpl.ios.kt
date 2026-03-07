package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.applier.AnalyzerApplier
import com.ujizin.camposer.internal.core.applier.ExposureZoomApplier
import com.ujizin.camposer.internal.core.applier.PreviewApplier
import com.ujizin.camposer.internal.core.applier.SessionTopologyApplier
import com.ujizin.camposer.internal.core.applier.VideoApplier
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.internal.extensions.toVideoOrientation
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.OrientationStrategy
import com.ujizin.camposer.state.properties.isMirrorEnabled
import com.ujizin.camposer.state.properties.selector.CamSelector
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
internal class CameraEngineImpl(
  override val cameraController: CameraController,
  override val iOSCameraController: IOSCameraController,
  override val cameraInfo: CameraInfo,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CameraEngineCore(), IOSCameraEngine {
  override val cameraState = CameraState(
    cameraInfo = cameraInfo,
    dispatcher = dispatcher,
  )

  override val sessionTopologyApplier = SessionTopologyApplier(
    cameraState = cameraState,
    cameraInfo = cameraInfo,
    iOSCameraController = iOSCameraController,
  )

  override val previewApplier = PreviewApplier(
    cameraState = cameraState,
    iOSCameraController = iOSCameraController,
  )

  override val analyzerApplier = AnalyzerApplier(
    cameraState = cameraState,
  )

  override val exposureZoomApplier = ExposureZoomApplier(
    cameraState = cameraState,
    iOSCameraController = iOSCameraController,
  )

  override val videoApplier = VideoApplier(
    cameraState = cameraState,
    iOSCameraController = iOSCameraController,
  )

  init {
    onCameraInitialized()
  }

  // Needs to be override due to race conditions
  override fun updateCaptureMode(captureMode: CaptureMode) {
    sessionTopologyApplier.applyCaptureMode(captureMode = captureMode)
  }

  // Needs to be override due to race conditions
  override fun updateCamSelector(camSelector: CamSelector) {
    sessionTopologyApplier.applyCamSelector(camSelector = camSelector)
  }

  // TODO refactor removing this method
  override fun isMirrorEnabled(): Boolean =
    cameraState.mirrorMode.value.isMirrorEnabled(iOSCameraController.getCurrentPosition())

  // TODO refactor removing this method
  override fun getCurrentVideoOrientation() =
    when (cameraState.orientationStrategy.value) {
      OrientationStrategy.Device -> iOSCameraController.getCurrentDeviceOrientation()
      OrientationStrategy.Preview -> UIApplication.sharedApplication.statusBarOrientation
    }.toVideoOrientation()
}
