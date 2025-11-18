package com.ujizin.camposer.state.properties.format.config

public class FrameRateConfig(
    public val minFps: Int,
    public val maxFps: Int,
) : CameraFormatConfig {

    public constructor(fps: Int) : this(minFps = fps, maxFps = fps)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrameRateConfig) return false
        return minFps == other.minFps && maxFps == other.maxFps
    }

    override fun hashCode(): Int {
        return 31 * minFps + maxFps
    }

    override fun toString(): String {
        return "FrameRate(minFps=$minFps, maxFps=$maxFps)"
    }
}
