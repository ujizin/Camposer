package com.ujizin.camposer.internal.utils

import platform.darwin.dispatch_queue_create

internal object DispatchQueue {
  internal val cameraQueue = dispatch_queue_create(
    label = "Camposer/camera_queue",
    attr = null,
  )
}
