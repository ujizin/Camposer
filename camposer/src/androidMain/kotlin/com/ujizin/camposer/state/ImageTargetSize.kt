package com.ujizin.camposer.state

import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController.OutputSize

/**
 * Image Analysis target size is used to target the size of image analysis, accepting [AspectRatio]
 * or [Size].
 * */
public data class ImageTargetSize(
    private var aspectRatio: Int? = null,
    private var size: Size? = null,
    private var outputSize: OutputSize? = null
) {

    /**
     * Image analysis target size using [AspectRatio].
     * */
    public constructor(@AspectRatio.Ratio aspectRatio: Int?) : this(
        aspectRatio = aspectRatio,
        size = null,
        outputSize = null
    )

    /**
     * Image analysis target size using [Size].
     * */
    public constructor(size: Size?) : this(
        aspectRatio = null,
        size = size,
        outputSize = null
    )

    /**
     * Internal constructor to use default [OutputSize] from cameraX.
     * */
    internal constructor(outputSize: OutputSize?) : this(
        aspectRatio = null,
        size = null,
        outputSize = outputSize
    )

    internal fun toOutputSize(): OutputSize? {
        return outputSize ?: aspectRatio?.let { OutputSize(it) } ?: size?.let { OutputSize(it) }
    }
}
