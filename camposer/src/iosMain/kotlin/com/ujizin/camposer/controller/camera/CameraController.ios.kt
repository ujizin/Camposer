package com.ujizin.camposer.controller.camera

import androidx.compose.runtime.Stable
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.controller.takepicture.TakePictureCommand

@Stable
public actual class CameraController :
  CommonCameraController<
    RecordController,
    TakePictureCommand,
  >()
