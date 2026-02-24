package com.ujizin.camposer.state.properties.selector

import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import com.ujizin.camposer.manager.CameraDevice

/**
 * Selector used to choose the camera sensor (Front/Back) and lens type (Wide, UltraWide, etc).
 *
 * This class encapsulates the criteria for selecting a camera device. It primarily determines which direction
 * the camera faces ([CamPosition]) and which lens configurations are preferred ([CamLensType]).
 *
 * @property camPosition The position of the camera (e.g., [CamPosition.Front] or [CamPosition.Back]).
 * @property camLensTypes A list of preferred lens types (e.g., [CamLensType.Wide], [CamLensType.Telephoto]).
 *                        Defaults to a list containing only [CamLensType.Wide].
 *
 * @see CamPosition
 * @see CamLensType
 */
@Stable
public expect class CamSelector {
  public val camPosition: CamPosition
  public val camLensTypes: List<CamLensType>

  public constructor(
    camPosition: CamPosition,
    camLensTypes: List<CamLensType> = listOf(CamLensType.Wide),
  )

  public constructor(cameraDevice: CameraDevice)

  override fun equals(other: Any?): Boolean

  override fun hashCode(): Int

  override fun toString(): String

  public companion object {
    public val Front: CamSelector
    public val Back: CamSelector
  }
}

@Suppress("UNCHECKED_CAST")
public val CamSelector.Companion.Saver: Saver<CamSelector, Any>
  get() {
    val camPositionKey = "camera_position"
    val camLensTypeKey = "camera_lens_type"
    return mapSaver(
      save = { mapOf(camPositionKey to it.camPosition, camLensTypeKey to it.camLensTypes) },
      restore = {
        CamSelector(
          camPosition = it[camPositionKey] as CamPosition,
          camLensTypes = it[camLensTypeKey] as List<CamLensType>,
        )
      },
    )
  }

/**
 * Returns a new [CamSelector] with the inverted camera position (Front to Back, or Back to Front)
 * while maintaining the same preferred lens types.
 */
public val CamSelector.inverse: CamSelector
  get() = CamSelector(
    camPosition = if (camPosition == CamPosition.Front) CamPosition.Back else CamPosition.Front,
    camLensTypes = camLensTypes,
  )
