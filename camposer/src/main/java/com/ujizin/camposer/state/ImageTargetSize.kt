package com.ujizin.camposer.state

import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController.OutputSize

/**
 * Image Analysis target size is used to target the size of image analysis, accepting [AspectRatio]
 * or [Size].
 * */
public class ImageTargetSize {

    private var aspectRatio: Int? = null
    private var size: Size? = null
    private var outputSize: OutputSize? = null

    /**
     * Image analysis target size using [AspectRatio].
     * */
    public constructor(@AspectRatio.Ratio aspectRatio: Int?) {
        this.aspectRatio = aspectRatio
    }

    /**
     * Image analysis target size using [Size].
     * */
    public constructor(size: Size?) {
        this.size = size
    }

    /**
     * Internal constructor to use default [OutputSize] from cameraX.
     * */
    internal constructor(outputSize: OutputSize?) {
        this.outputSize = outputSize
    }

    internal fun toOutputSize(): OutputSize? {
        return outputSize ?: aspectRatio?.let { OutputSize(it) } ?: size?.let { OutputSize(it) }
    }
}
