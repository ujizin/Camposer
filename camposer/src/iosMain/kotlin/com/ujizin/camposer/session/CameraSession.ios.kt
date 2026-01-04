package com.ujizin.camposer.session

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.NONE
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.controller.takepicture.DefaultTakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.CameraManagerInternal
import com.ujizin.camposer.internal.core.CameraManagerInternalImpl
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
public actual class CameraSession private constructor(
  internal val iosCameraController: IOSCameraController,
  public actual val controller: CameraController,
  public actual val info: CameraInfo = CameraInfo(iosCameraController),
  internal actual val cameraManager: CameraManagerInternal = CameraManagerInternalImpl(
    cameraController = iosCameraController,
    cameraInfo = info,
  ),
  public actual val state: CameraState = cameraManager.cameraState,
) {
  @get:RestrictTo(RestrictTo.Scope.LIBRARY)
  public val cameraController: DefaultIOSCameraController
    get() = iosCameraController as DefaultIOSCameraController

  public constructor(
    controller: CameraController,
    iosCameraSession: IOSCameraController = DefaultIOSCameraController(
      captureSession = AVCaptureSession(),
      previewManager = PreviewManager(),
    ),
  ) : this(controller = controller, iosCameraController = iosCameraSession)

  @VisibleForTesting(NONE)
  internal constructor(
    controller: CameraController,
    iosCameraSession: IOSCameraController,
    cameraInfo: CameraInfo,
    cameraManagerInternal: CameraManagerInternal,
  ) : this(
    controller = controller,
    iosCameraController = iosCameraSession,
    info = cameraInfo,
    cameraManager = cameraManagerInternal,
  )

  public actual var isInitialized: Boolean by mutableStateOf(false)
    private set
  public actual var isStreaming: Boolean by mutableStateOf(false)
    internal set

  init {
    setupCamera()
  }

  private fun setupCamera() =
    with(iosCameraController) {
      controller.initialize(
        recordController = DefaultRecordController(cameraManager),
        takePictureCommand = DefaultTakePictureCommand(cameraManager),
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
