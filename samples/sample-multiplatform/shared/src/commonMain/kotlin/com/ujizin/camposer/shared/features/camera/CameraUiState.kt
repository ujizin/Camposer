package com.ujizin.camposer.shared.features.camera

import com.ujizin.camposer.shared.utils.QrCorner
import com.ujizin.camposer.shared.utils.QrRect
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.MirrorMode
import com.ujizin.camposer.state.properties.OrientationStrategy
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
  val orientationStrategy: OrientationStrategy = OrientationStrategy.Preview,
  val mirrorMode: MirrorMode = MirrorMode.OnlyInFront,
  val lastThumbnail: ByteArray? = null,
  val qrCodeText: String? = null,
  val qrCodeFrameRect: QrRect? = null,
  val qrCodeCorners: List<QrCorner> = emptyList(),
)
