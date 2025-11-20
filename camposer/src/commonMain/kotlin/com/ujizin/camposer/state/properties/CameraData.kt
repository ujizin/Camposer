package com.ujizin.camposer.state.properties

import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.FrameRateConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.properties.format.config.VideoStabilizationConfig

public class CameraData(
    public val width: Int,
    public val height: Int,
    public val isFocusSupported: Boolean = false,
    public val minFps: Int? = null,
    public val maxFps: Int? = null,
    public val videoStabilizationModes: List<VideoStabilizationMode>? = null,
    internal val metadata: HashMap<String, Any> = hashMapOf(),
) {

    public fun toCameraFormat(): CamFormat = CamFormat(
        configs = buildList {
            this += ResolutionConfig(width = width, height = height)
            this += AspectRatioConfig(aspectRatio = width.toFloat() / height.toFloat())
            if (minFps != null && maxFps != null) {
                this += FrameRateConfig(fps = maxFps)
            }

            if (videoStabilizationModes != null && videoStabilizationModes.isNotEmpty()) {
                this += VideoStabilizationConfig(mode = videoStabilizationModes.last())
            }

        }.toTypedArray()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CameraData) return false

        if (width != other.width) return false
        if (height != other.height) return false
        if (minFps != other.minFps) return false
        if (maxFps != other.maxFps) return false
        if (videoStabilizationModes != other.videoStabilizationModes) return false
        if (isFocusSupported != other.isFocusSupported) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + (minFps ?: 0)
        result = 31 * result + (maxFps ?: 0)
        result = 31 * result + (videoStabilizationModes?.hashCode() ?: 0)
        result = 31 * result + isFocusSupported.hashCode()
        return result
    }

    override fun toString(): String {
        return "CameraData(width=$width, height=$height, minFps=$minFps, maxFps=$maxFps, isVideoStabilizationSupported=$videoStabilizationModes, isFocusSupported=$isFocusSupported)"
    }

    internal companion object {
        internal const val DEVICE_FORMAT = "device_format"
    }
}

