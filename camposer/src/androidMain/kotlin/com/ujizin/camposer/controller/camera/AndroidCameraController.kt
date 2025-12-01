package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.controller.record.AndroidRecordController
import com.ujizin.camposer.controller.takepicture.AndroidTakePictureCommand

internal typealias AndroidCameraController = CommonCameraController<
  AndroidRecordController,
  AndroidTakePictureCommand,
>
