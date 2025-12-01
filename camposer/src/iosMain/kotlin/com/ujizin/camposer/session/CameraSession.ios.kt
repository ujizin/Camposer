package com.ujizin.camposer.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.controller.takepicture.DefaultTakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.manager.PreviewManager
import com.ujizin.camposer.state.CameraState
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureSession
import platform.CoreGraphics.CGPoint
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.foundation.NSKeyValueObservingProtocol

@OptIn(ExperimentalForeignApi::class)
public actual class CameraSession private constructor(
  public val captureSession: AVCaptureSession = AVCaptureSession(),
  public val iosCameraSession: IOSCameraSession,
  internal val controller: CameraController,
  internal val previewManager: PreviewManager,
  public actual val info: CameraInfo = CameraInfo(iosCameraSession),
  public actual val state: CameraState = CameraState(iosCameraSession, info),
) {
  public constructor(
    controller: CameraController,
    captureSession: AVCaptureSession = AVCaptureSession(),
  ) : this(
    controller = controller,
    captureSession = captureSession,
    previewManager = PreviewManager(),
  )

  internal constructor(
    controller: CameraController,
    previewManager: PreviewManager = PreviewManager(),
    captureSession: AVCaptureSession = AVCaptureSession(),
    iosCameraSession: IOSCameraSession = IOSCameraSession(captureSession, previewManager),
  ) : this(
    controller = controller,
    previewManager = previewManager,
    iosCameraSession = iosCameraSession,
    captureSession = captureSession,
    info = CameraInfo(iosCameraSession),
  )

  public actual var isInitialized: Boolean by mutableStateOf(false)
    private set
  public actual var isStreaming: Boolean by mutableStateOf(false)
    internal set

  private var runningObserver = object : NSObject(), NSKeyValueObservingProtocol {
    override fun observeValueForKeyPath(
      keyPath: String?,
      ofObject: Any?,
      change: Map<Any?, *>?,
      context: COpaquePointer?,
    ) {
      isStreaming = change?.get(NSKeyValueChangeNewKey) as? Boolean == true
    }
  }

  init {
    setupCamera()
  }

  private fun setupCamera() =
    with(iosCameraSession) {
      controller.initialize(
        recordController = DefaultRecordController(
          iosCameraSession = iosCameraSession,
          cameraState = state,
        ),
        takePictureCommand = DefaultTakePictureCommand(
          iosCameraSession = iosCameraSession,
          cameraState = state,
        ),
        cameraState = state,
        cameraInfo = info,
      )

      setCaptureDevice(device = state.camSelector.captureDevice)
      info.rebind(state.captureMode.output)

      captureSession.addObserver(
        observer = runningObserver,
        forKeyPath = RUNNING_KEY_PATH,
        options = NSKeyValueObservingOptionNew,
        context = null,
      )
      isInitialized = true
    }

  internal fun onSessionStarted() {
    if (controller.isRunning.value) return
    controller.onSessionStarted()
  }

  @OptIn(ExperimentalForeignApi::class)
  internal fun startCamera() =
    iosCameraSession.start(
      captureOutput = state.captureMode.output,
      device = state.camSelector.captureDevice,
      isMuted = controller.isMuted,
    )

  internal fun renderCamera(view: UIView) {
    iosCameraSession.renderPreviewLayer(view = view)
  }

  internal fun setFocusPoint(focusPoint: CValue<CGPoint>) =
    iosCameraSession.setFocusPoint(focusPoint)

  internal fun recoveryState() {
    iosCameraSession.setTorchEnabled(state.isTorchEnabled)
  }

  internal fun dispose() {
    iosCameraSession.captureSession.removeObserver(runningObserver, RUNNING_KEY_PATH)
    iosCameraSession.release()
  }

  internal companion object {
    private const val RUNNING_KEY_PATH = "running"
  }
}
