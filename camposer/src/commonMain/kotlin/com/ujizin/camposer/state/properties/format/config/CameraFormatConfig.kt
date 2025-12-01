package com.ujizin.camposer.state.properties.format.config

/**
 * Configuration interface for Camera Format settings.
 *
 * This sealed interface serves as a base for defining specific configurations related to camera formats,
 * allowing the definition of resolution or aspect ratio strategies for camera capture.
 *
 * Implementing classes or objects usually represent specific strategies, such as:
 * - [ResolutionConfig] for defining specific size targets.
 * - [AspectRatioConfig] for defining aspect ratio targets.
 * - [FrameRateConfig] for defining frame rate targets.
 * - [VideoStabilizationConfig] for defining video stabilization modes.
 *
 * Usage of this configuration helps in determining how the camera stream and output should be sized.
 *
 * @see ResolutionConfig
 * @see AspectRatioConfig
 * @see FrameRateConfig
 * @see VideoStabilizationConfig
 */
public sealed interface CameraFormatConfig
