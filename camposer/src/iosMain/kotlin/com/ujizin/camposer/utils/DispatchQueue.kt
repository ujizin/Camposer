package com.ujizin.camposer.utils

import platform.darwin.dispatch_queue_create

internal object DispatchQueue {
    internal val configurationQueue = dispatch_queue_create(
        label = "Camposer/configuration_queue",
        attr = null,
    )

    internal val cameraQueue = dispatch_queue_create(
        label = "Camposer/camera_queue",
        attr = null,
    )

}
