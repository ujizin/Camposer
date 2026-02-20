package com.ujizin.camposer.info

import kotlinx.coroutines.flow.StateFlow

/**
 * Camera information holder for the active camera session.
 *
 * This class exposes a reactive [state] stream with the latest camera capabilities and
 * availability data represented as [CameraInfoState].
 */
public expect class CameraInfo {
  /** Reactive stream of the current camera capability state. */
  public val state: StateFlow<CameraInfoState>
}
