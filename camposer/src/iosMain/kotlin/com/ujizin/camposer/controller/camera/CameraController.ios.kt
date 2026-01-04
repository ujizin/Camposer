package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.controller.takepicture.TakePictureCommand

public actual class CameraController :
  CommonCameraController<
    RecordController,
    TakePictureCommand,
  >()
