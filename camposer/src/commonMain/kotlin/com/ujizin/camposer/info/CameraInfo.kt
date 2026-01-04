package com.ujizin.camposer.info

import com.ujizin.camposer.state.properties.CameraData

/**
 * A class containing detailed information about the device's camera capabilities.
 *
 * This class provides read-only access to various camera features such as zoom limits,
 * exposure ranges, flash and torch support, focus capabilities, and supported formats
 * for both photos and videos. It is used to query the camera's hardware specifications
 * and current availability status.
 *
 * @property isZoomSupported Indicates if the camera supports zoom functionality.
 * @property isExposureSupported Indicates if the camera supports exposure compensation.
 * @property minZoom The minimum zoom ratio supported by the camera.
 * @property maxZoom The maximum zoom ratio supported by the camera.
 * @property minExposure The minimum exposure compensation index or value supported.
 * @property maxExposure The maximum exposure compensation index or value supported.
 * @property isFlashSupported Indicates if the camera hardware has a flash unit.
 * @property isFlashAvailable Indicates if the flash is currently available for use (e.g., not overheated or in a conflicting mode).
 * @property isTorchSupported Indicates if the camera supports torch mode (continuous flash).
 * @property isTorchAvailable Indicates if the torch mode is currently available for use.
 * @property isZeroShutterLagSupported Indicates if the camera supports Zero Shutter Lag (ZSL) to minimize capture delay.
 * @property isFocusSupported Indicates if the camera supports auto-focus or manual focus operations.
 * @property minFPS The minimum frames per second supported by the camera preview/recording.
 * @property maxFPS The maximum frames per second supported by the camera preview/recording.
 * @property photoFormats A list of supported resolutions and formats for capturing photos in current camera.
 * @property videoFormats A list of supported resolutions and formats for recording videos in current camera.
 */
public expect class CameraInfo {
  public val isZoomSupported: Boolean

  public var isExposureSupported: Boolean
    private set
  public var minZoom: Float
    private set
  public var maxZoom: Float
    private set
  public var minExposure: Float
    private set
  public var maxExposure: Float
    private set
  public var isFlashSupported: Boolean
    private set
  public var isFlashAvailable: Boolean
    private set
  public var isTorchSupported: Boolean
    private set
  public var isTorchAvailable: Boolean
    private set
  public var isZeroShutterLagSupported: Boolean
    private set

  public var isFocusSupported: Boolean
    private set
  public var minFPS: Int
    private set
  public var maxFPS: Int
    private set
  public var photoFormats: List<CameraData>
    private set
  public var videoFormats: List<CameraData>
    private set
}
