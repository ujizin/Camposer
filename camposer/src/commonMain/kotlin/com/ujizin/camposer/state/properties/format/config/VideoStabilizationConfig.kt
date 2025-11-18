package com.ujizin.camposer.state.properties.format.config

import com.ujizin.camposer.state.properties.VideoStabilizationMode

public class VideoStabilizationConfig(
    public val mode: VideoStabilizationMode = VideoStabilizationMode.Standard,
) : CameraFormatConfig {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VideoStabilizationConfig) return false
        return mode == other.mode
    }

    override fun hashCode(): Int = mode.hashCode()

    override fun toString(): String {
        return "VideoStabilizationConfig(mode=$mode)"
    }
}

