package com.ujizin.camposer.info

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode

/**
 * A class containing detailed information about the device's camera capabilities.
 *
 * This class provides read-only access to various camera features such as zoom limits,
 * exposure ranges, flash and torch support, focus capabilities, and supported formats
 * for both photos and videos. It is used to query the camera's hardware specifications
 * and current availability status.
 *
 * Fields are emitted through CameraInfo `state` as a single immutable model.
 *
 * @property isZoomSupported Indicates whether Zoom is supported.
 * @property isExposureSupported Indicates whether exposure compensation is supported.
 * @property minZoom Minimum supported zoom ratio.
 * @property maxZoom Maximum supported zoom ratio.
 * @property minExposure Minimum supported exposure compensation.
 * @property maxExposure Maximum supported exposure compensation.
 * @property isFlashSupported Indicates whether the camera has flash hardware.
 * @property isFlashAvailable Indicates whether flash is currently available.
 * @property isTorchSupported Indicates whether torch mode is supported.
 * @property isTorchAvailable Indicates whether torch mode is currently available.
 * @property isZeroShutterLagSupported Indicates whether Zero Shutter Lag is supported.
 * @property isVideoStabilizationSupported Indicates whether video stabilization is supported.
 * @property isFocusSupported Indicates whether focus operations are supported.
 * @property minFPS Minimum supported frame rate.
 * @property maxFPS Maximum supported frame rate.
 * @property photoFormats Supported photo formats for the current camera.
 * @property videoFormats Supported video formats for the current camera.
 */
public class CameraInfoState(
  public val isZoomSupported: Boolean = false,
  public val isExposureSupported: Boolean = false,
  public val minZoom: Float = 1F,
  public val maxZoom: Float = 1F,
  public val minExposure: Float = 0F,
  public val maxExposure: Float = 0F,
  public val isFlashSupported: Boolean = false,
  public val isFlashAvailable: Boolean = false,
  public val isTorchSupported: Boolean = false,
  public val isTorchAvailable: Boolean = false,
  public val isZeroShutterLagSupported: Boolean = false,
  public val isVideoStabilizationSupported: Boolean = false,
  public val isFocusSupported: Boolean = false,
  public val minFPS: Int = -1,
  public val maxFPS: Int = -1,
  public val photoFormats: List<CameraData> = emptyList(),
  public val videoFormats: List<CameraData> = emptyList(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CameraInfoState) return false

    return isZoomSupported == other.isZoomSupported &&
      isExposureSupported == other.isExposureSupported &&
      minZoom == other.minZoom &&
      maxZoom == other.maxZoom &&
      minExposure == other.minExposure &&
      maxExposure == other.maxExposure &&
      isFlashSupported == other.isFlashSupported &&
      isFlashAvailable == other.isFlashAvailable &&
      isTorchSupported == other.isTorchSupported &&
      isTorchAvailable == other.isTorchAvailable &&
      isZeroShutterLagSupported == other.isZeroShutterLagSupported &&
      isVideoStabilizationSupported == other.isVideoStabilizationSupported &&
      isFocusSupported == other.isFocusSupported &&
      minFPS == other.minFPS &&
      maxFPS == other.maxFPS &&
      photoFormats == other.photoFormats &&
      videoFormats == other.videoFormats
  }

  override fun hashCode(): Int {
    var result = isZoomSupported.hashCode()
    result = 31 * result + isExposureSupported.hashCode()
    result = 31 * result + minZoom.hashCode()
    result = 31 * result + maxZoom.hashCode()
    result = 31 * result + minExposure.hashCode()
    result = 31 * result + maxExposure.hashCode()
    result = 31 * result + isFlashSupported.hashCode()
    result = 31 * result + isFlashAvailable.hashCode()
    result = 31 * result + isTorchSupported.hashCode()
    result = 31 * result + isTorchAvailable.hashCode()
    result = 31 * result + isZeroShutterLagSupported.hashCode()
    result = 31 * result + isVideoStabilizationSupported.hashCode()
    result = 31 * result + isFocusSupported.hashCode()
    result = 31 * result + minFPS
    result = 31 * result + maxFPS
    result = 31 * result + photoFormats.hashCode()
    result = 31 * result + videoFormats.hashCode()
    return result
  }

  override fun toString(): String =
    "CameraInfoState(" +
      "isZoomSupported=$isZoomSupported, " +
      "isExposureSupported=$isExposureSupported, " +
      "minZoom=$minZoom, " +
      "maxZoom=$maxZoom, " +
      "minExposure=$minExposure, " +
      "maxExposure=$maxExposure, " +
      "isFlashSupported=$isFlashSupported, " +
      "isFlashAvailable=$isFlashAvailable, " +
      "isTorchSupported=$isTorchSupported, " +
      "isTorchAvailable=$isTorchAvailable, " +
      "isZeroShutterLagSupported=$isZeroShutterLagSupported, " +
      "isVideoStabilizationSupported=$isVideoStabilizationSupported, " +
      "isFocusSupported=$isFocusSupported, " +
      "minFPS=$minFPS, " +
      "maxFPS=$maxFPS, " +
      "photoFormats=$photoFormats, " +
      "videoFormats=$videoFormats" +
      ")"
}

internal fun List<CameraData>.isVideoStabilizationSupported(): Boolean =
  any { format ->
    format.videoStabilizationModes?.any { mode ->
      mode != VideoStabilizationMode.Off
    } == true
  }
