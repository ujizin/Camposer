package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.command.TakePictureCommand
import com.ujizin.camposer.controller.record.RecordController

internal typealias IOSCameraController = CommonCameraController<RecordController, TakePictureCommand>
