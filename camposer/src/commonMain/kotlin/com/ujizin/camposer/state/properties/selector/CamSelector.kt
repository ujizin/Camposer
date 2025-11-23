package com.ujizin.camposer.state.properties.selector

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import com.ujizin.camposer.manager.CameraDevice

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
            }
        )
    }

/**
 * Inverse camera selector. Works only with default Front & Back Selector.
 * */
public val CamSelector.inverse: CamSelector
    get() = when (camPosition) {
        CamPosition.Front -> CamSelector(CamPosition.Back, camLensTypes)
        else -> CamSelector(CamPosition.Front, camLensTypes)
    }