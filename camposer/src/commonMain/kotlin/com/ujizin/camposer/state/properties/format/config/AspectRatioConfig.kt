package com.ujizin.camposer.state.properties.format.config

public class AspectRatioConfig(
    public val aspectRatio: Float,
) : CameraFormatConfig {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AspectRatioConfig) return false

        return aspectRatio == other.aspectRatio
    }

    override fun hashCode(): Int = aspectRatio.hashCode()

    override fun toString(): String {
        val displayAspectRatio = when (aspectRatio) {
            1F -> "1:1"
            in 1.3F..1.4F -> "4:3"
            in 1.7F..1.8F -> "16:9"
            else -> aspectRatio.toString()
        }

        return "AspectRatio(aspectRatio=$displayAspectRatio)"
    }
}