package com.ujizin.camposer.controller.camera

import androidx.compose.runtime.Stable
import com.ujizin.camposer.annotation.InternalCamposerApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Stable
@OptIn(InternalCamposerApi::class)
public actual class CameraController internal actual constructor(
  dispatcher: CoroutineDispatcher,
) : CommonCameraController(dispatcher) {
  public actual constructor() : this(Dispatchers.Main)
}
