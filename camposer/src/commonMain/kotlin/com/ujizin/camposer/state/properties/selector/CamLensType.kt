package com.ujizin.camposer.state.properties.selector

import com.ujizin.camposer.state.properties.selector.CamLensType.Telephoto
import com.ujizin.camposer.state.properties.selector.CamLensType.UltraWide
import com.ujizin.camposer.state.properties.selector.CamLensType.Wide

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
public expect enum class CamLensType {
  Wide,
  UltraWide,
  Telephoto,
}
