package com.ujizin.camposer.state.properties.selector

import com.ujizin.camposer.state.properties.selector.CamPosition.Back
import com.ujizin.camposer.state.properties.selector.CamPosition.External
import com.ujizin.camposer.state.properties.selector.CamPosition.Front
import com.ujizin.camposer.state.properties.selector.CamPosition.Unknown

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
public expect enum class CamPosition {
  Back,
  Front,
  External,
  Unknown,
}
