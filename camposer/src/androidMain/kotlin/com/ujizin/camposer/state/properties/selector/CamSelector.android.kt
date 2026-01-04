package com.ujizin.camposer.state.properties.selector

import android.annotation.SuppressLint
import androidx.camera.camera2.internal.Camera2CameraInfoImpl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import com.ujizin.camposer.internal.utils.CameraUtils
import com.ujizin.camposer.manager.CameraDevice

/**
 * Camera Selector.
 *
 * Defines criteria for selecting a specific camera based on its position (Front/Back)
 * and lens type (e.g., Wide, UltraWide, Telephoto).
 *
 * This class wraps the internal [CameraSelector] from CameraX to provide a unified
 * selector.
 *
 * @see CameraSelector
 */
public actual class CamSelector {
  public actual val camPosition: CamPosition

  public actual val camLensTypes: List<CamLensType>

  internal val selector: CameraSelector by lazy { createCameraSelector() }

  internal var cameraId: CameraId? = null

  public actual constructor(camPosition: CamPosition, camLensTypes: List<CamLensType>) {
    this.camPosition = camPosition
    this.camLensTypes = camLensTypes.ifEmpty { listOf(CamLensType.Wide) }
  }

  public actual constructor(cameraDevice: CameraDevice) : this(
    camPosition = cameraDevice.position,
    camLensTypes = cameraDevice.lensType,
  ) {
    this.cameraId = cameraDevice.cameraId
  }

  @SuppressLint("RestrictedApi")
  private fun createCameraSelector(): CameraSelector =
    CameraSelector
      .Builder()
      .addCameraFilter { cameraInfos ->
        if (cameraId != null) {
          return@addCameraFilter cameraInfos.filter { info ->
            info.cameraIdentifier == cameraId?.identifier
          }
        }

        val lensFilter = cameraInfos.filterIsInstance<Camera2CameraInfoImpl>().filter { info ->
          camPosition.value == info.lensFacing
        }

        val logicalSenseFilter = lensFilter
          .map { info -> info to CameraUtils.getCamLensTypes(info) }
          .filter { (_, lenses) -> lenses.containsAll(camLensTypes) }
          .maxWithOrNull(
            compareBy<Pair<CameraInfo, List<CamLensType>>> { (_, lenses) ->
              lenses.size == camLensTypes.size
            }.thenBy { (_, lenses) -> lenses.size },
          )?.first

        listOfNotNull(logicalSenseFilter).ifEmpty { lensFilter }.ifEmpty { cameraInfos }
      }.requireLensFacing(camPosition.value)
      .build()

  actual override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CamSelector) return false

    if (camPosition != other.camPosition) return false
    return camLensTypes == other.camLensTypes
  }

  actual override fun hashCode(): Int {
    var result = camPosition.hashCode()
    result = 31 * result + camLensTypes.hashCode()
    return result
  }

  actual override fun toString(): String =
    "CamSelector(camPosition=$camPosition, camLensType=$camLensTypes)"

  public actual companion object {
    public actual val Front: CamSelector = CamSelector(CamPosition.Front)
    public actual val Back: CamSelector = CamSelector(CamPosition.Back)
  }
}
