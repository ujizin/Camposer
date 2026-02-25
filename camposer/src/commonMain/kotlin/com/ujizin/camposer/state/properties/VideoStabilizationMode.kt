package com.ujizin.camposer.state.properties

/**
 * Represents the different modes of video stabilization available for camera recording.
 *
 * Note: This only supports iOS for now, on Android support via CameraX Controller is under development.
 * See https://issuetracker.google.com/issues/230017663
 *
 * This enum defines the stabilization techniques that can be applied to video capture
 * to reduce shakiness and jitter. The availability of these modes depends on the
 * underlying device hardware and camera capabilities.
 *
 * Selecting higher-quality stabilization modes (like Cinematic or Extended) requires significant
 * processing power, which may result in increased latency, higher battery consumption, etc.
 */
public enum class VideoStabilizationMode {
  Off,
  Standard,
  Cinematic,
  CinematicExtended,
  CinematicExtendedEnhanced,
}
