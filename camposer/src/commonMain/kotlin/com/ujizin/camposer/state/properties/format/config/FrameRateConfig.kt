package com.ujizin.camposer.state.properties.format.config

public class FrameRateConfig(
    public val fps: Int,
) : CameraFormatConfig {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrameRateConfig) return false
        return fps == other.fps
    }

    override fun hashCode(): Int {
        return 31 * fps
    }

    override fun toString(): String {
        return "FrameRate(fps=$fps)"
    }
}
