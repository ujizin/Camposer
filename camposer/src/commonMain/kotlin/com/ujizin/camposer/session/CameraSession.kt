package com.ujizin.camposer.session

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.internal.core.CameraEngine
import com.ujizin.camposer.state.CameraState

/**
 * A session class that holds the current state and information of the camera.
 *
 * This class serves as a "bridge" to access camera properties such as its current configuration
 * state ([CameraState]) and hardware information ([CameraInfo]). It also provides flags
 * indicating the runtime status of the camera, such as whether it is currently streaming
 * or initialized.
 */
public expect class CameraSession {
  /**
   * The current state of the current camera.
   */
  public val state: CameraState

  /**
   * The current info of the current camera.
   */
  public val info: CameraInfo

  /**
   * The current controller bind to the camera.
   * */
  public val controller: CameraController

  /**
   * Check if camera is streaming or not.
   * */
  public var isStreaming: Boolean
    internal set

  /**
   * Check if camera state is initialized or not.
   * */
  public var isInitialized: Boolean
    private set

  internal val cameraEngine: CameraEngine

  internal fun onSessionStarted()
}
