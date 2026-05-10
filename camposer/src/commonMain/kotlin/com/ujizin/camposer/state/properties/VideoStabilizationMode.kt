package com.ujizin.camposer.state.properties

/**
 * Represents the different modes of video stabilization available for camera recording.
 *
 * This enum defines the stabilization techniques that can be applied to video capture
 * to reduce shakiness and jitter. The availability of these modes depends on the
 * underlying device hardware and camera capabilities.
 *
 * On Android: [Standard] maps to video stabilization; [Cinematic], [CinematicExtended],
 * and [CinematicExtendedEnhanced] map to preview stabilization (both preview and video
 * stabilized at preview quality). On iOS: all modes map to AVFoundation stabilization modes.
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
