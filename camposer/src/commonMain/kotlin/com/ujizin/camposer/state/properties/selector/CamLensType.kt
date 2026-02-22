package com.ujizin.camposer.state.properties.selector

/**
 * Represents the type of camera lens used for capturing images or video.
 *
 * This enum defines the standard categories of lenses available on most modern devices,
 * allowing for selection between different fields of view and zoom capabilities.
 *
 * @property Wide The standard wide-angle lens, typically the default camera lens.
 * @property UltraWide An ultra-wide-angle lens, providing a much broader field of view than the standard wide lens.
 * @property Telephoto A telephoto lens, providing optical zoom capabilities for capturing distant subjects.
 */
public enum class CamLensType {
  Wide,
  UltraWide,
  Telephoto,
}
