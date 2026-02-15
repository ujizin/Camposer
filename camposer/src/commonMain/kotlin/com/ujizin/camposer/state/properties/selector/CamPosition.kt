package com.ujizin.camposer.state.properties.selector

/**
 * Enum representing the position (lens facing) of the camera.
 *
 * This class is used to select which camera to use, such as the rear-facing (Back)
 * or front-facing (Front) camera.
 *
 * @property Back Represents the rear-facing camera (default for most main photography).
 * @property Front Represents the front-facing camera (selfie camera).
 * @property External Represents an external camera device (e.g., USB webcam).
 * @property Unknown Represents a camera position that could not be determined.
 */
public enum class CamPosition {
  Back,
  Front,
  External,
  Unknown,
  ;

  public companion object
}
