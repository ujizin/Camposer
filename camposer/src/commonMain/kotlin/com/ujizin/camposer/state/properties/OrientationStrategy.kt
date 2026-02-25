package com.ujizin.camposer.state.properties

import com.ujizin.camposer.state.properties.OrientationStrategy.Device
import com.ujizin.camposer.state.properties.OrientationStrategy.Preview

/**
 * Strategy to determine the output orientation of the media (images or videos) captured by the camera.
 *
 * This strategy defines how the rotation metadata or the physical pixel rotation is applied
 * to the resulting file or stream based on different sources of truth.
 *
 * Note: Android images often use EXIF tags for orientation. Ensure you use image loading libraries
 * like Coil, Glide, or ExoPlayer that support reading EXIF data to display the image/video correctly.
 *
 * @property Preview The output orientation matches the current UI orientation of the camera preview.
 * This is useful for "what you see is what you get" behavior, regardless of how the device
 * is physically held (e.g., capturing a landscape image while the UI is locked in portrait).
 *
 * @property Device The output orientation is determined by the physical orientation sensors of the device.
 * This ensures that if the user holds the phone in landscape, the image is tagged as landscape,
 * even if the UI is locked to portrait.
 */
public enum class OrientationStrategy {
  Preview,
  Device,
}
