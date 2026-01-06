package com.ujizin.camposer.session

import androidx.annotation.RestrictTo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.controller.takepicture.DefaultTakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.core.IOSCameraEngine
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.manager.PreviewManager
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.properties.selector.getCaptureDevice
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureSession
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
public actual class CameraSession internal constructor(
  internal actual val cameraEngine: CameraEngine,
  internal val iosCameraController: IOSCameraController = (cameraEngine as IOSCameraEngine)
    .iOSCameraController,
  public actual val controller: CameraController = cameraEngine.cameraController,
  public actual val info: CameraInfo = cameraEngine.cameraInfo,
  public actual val state: CameraState = cameraEngine.cameraState,
) {
  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public val cameraController: DefaultIOSCameraController
    get() = iosCameraController as DefaultIOSCameraController

  public actual var isInitialized: Boolean by mutableStateOf(false)
    private set

  public actual var isStreaming: Boolean by mutableStateOf(false)
    internal set

  public constructor(
    controller: CameraController,
    iosCameraSession: IOSCameraController = DefaultIOSCameraController(
      captureSession = AVCaptureSession(),
      previewManager = PreviewManager(),
    ),
  ) : this(
    cameraEngine = CameraEngineImpl(
      cameraController = controller,
      iOSCameraController = iosCameraSession,
      cameraInfo = CameraInfo(iosCameraSession),
    ),
  )

  init {
    setupCamera()
  }

  private fun setupCamera() =
    with(iosCameraController) {
      controller.initialize(
        recordController = DefaultRecordController(cameraEngine),
        takePictureCommand = DefaultTakePictureCommand(cameraEngine),
        cameraState = state,
        cameraInfo = info,
      )

      setCaptureDevice(
        device = iosCameraController.getCaptureDevice(state.camSelector),
      )
      info.rebind(state.captureMode.output)
      isInitialized = true
    }

  internal actual fun onSessionStarted() {
    if (controller.isRunning.value) return
    controller.onSessionStarted()
  }

  @OptIn(ExperimentalForeignApi::class)
  internal fun startCamera() =
    iosCameraController.start(
      captureOutput = state.captureMode.output,
      device = iosCameraController.getCaptureDevice(state.camSelector),
      isMuted = controller.isMuted,
      onRunningChanged = { isStreaming = it },
    )

  internal fun renderCamera(view: UIView) {
    iosCameraController.renderPreviewLayer(view = view)
  }

  internal fun setFocusPoint(focusPoint: CValue<CGPoint>) =
    iosCameraController.setFocusPoint(focusPoint)

  internal fun recoveryState() {
    iosCameraController.setTorchEnabled(state.isTorchEnabled)
  }

  internal fun dispose() {
    iosCameraController.release()
  }
}
