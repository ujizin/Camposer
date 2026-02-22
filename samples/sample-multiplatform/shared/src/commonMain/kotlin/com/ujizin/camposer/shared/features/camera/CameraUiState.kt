package com.ujizin.camposer.shared.features.camera

import com.ujizin.camposer.codescanner.CornerPointer
import com.ujizin.camposer.codescanner.FrameRect
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.MirrorMode
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
  val isSettingsVisible: Boolean = false,
  val isVideoStabilizationEnabled: Boolean = false,
  val is60FpsEnabled: Boolean = false,
  val isTapToFocusEnabled: Boolean = true,
  val aspectRatioOption: AspectRatioOption = AspectRatioOption.Full,
  val mirrorMode: MirrorMode = MirrorMode.OnlyInFront,
  val lastThumbnail: ByteArray? = null,
  val qrCodeText: String? = null,
  val qrCodeFrameRect: FrameRect? = null,
  val qrCodeCorners: List<CornerPointer> = emptyList(),
)
