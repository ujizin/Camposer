package com.ujizin.camposer.shared.features.camera

import com.ujizin.camposer.codescanner.CornerPointer
import com.ujizin.camposer.codescanner.FrameRect
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CamSelector

/**
 * Represents the UI state for the Camera screen.
 */
data class CameraUiState(
  val camSelector: CamSelector = CamSelector(
    camPosition = CamPosition.Back,
    camLensTypes = listOf(CamLensType.UltraWide, CamLensType.Wide),
  ),
  val captureMode: CaptureMode = CaptureMode.Image,
  val recordingDurationSeconds: Long = 0L,
  val lastThumbnail: ByteArray? = null,
  val videoPath: String = "",
  val codeScanText: String = "",
  val frameRect: FrameRect? = null,
  val corners: List<CornerPointer> = emptyList(),
)
