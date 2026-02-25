package com.ujizin.camposer.controller.camera

import androidx.compose.runtime.Stable
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.controller.takepicture.TakePictureCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Stable
public actual class CameraController actual constructor(
  dispatcher: CoroutineDispatcher,
) : CommonCameraController<
    RecordController,
    TakePictureCommand,
  >(dispatcher) {
  public actual constructor() : this(Dispatchers.Main)
}
