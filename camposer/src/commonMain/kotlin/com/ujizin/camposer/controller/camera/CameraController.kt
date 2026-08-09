package com.ujizin.camposer.controller.camera

import androidx.compose.runtime.Stable
import com.ujizin.camposer.annotation.InternalCamposerApi
import kotlinx.coroutines.CoroutineDispatcher

/**
 * A controller that manages the state and interactions of the camera.
 *
 * This class serves as the primary interface for interacting with the underlying camera when bound
 * to a [com.ujizin.camposer.session.CameraSession].
 *
 * It offers methods to capture images, record videos, and manipulate camera parameters such as
 * zoom, exposure, focus, flash modes, and torch settings.
 */
@Stable
@OptIn(InternalCamposerApi::class)
public expect class CameraController : CommonCameraController {
  public constructor()

  internal constructor(dispatcher: CoroutineDispatcher)
}
