package com.ujizin.camposer.internal.utils

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
import android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
import android.media.CamcorderProfile
import android.os.Build
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.camera.camera2.internal.Camera2CameraInfoImpl
import androidx.camera.core.CameraInfo
import androidx.camera.core.ExperimentalSessionConfig
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.impl.CameraInfoInternal
import androidx.camera.video.Quality
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapabilities
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamLensType.Telephoto
import com.ujizin.camposer.state.properties.selector.CamLensType.UltraWide
import com.ujizin.camposer.state.properties.selector.CamLensType.Wide
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

@SuppressLint("RestrictedApi")
internal object CameraUtils {
  private const val TAG = "CamUtils"

  fun getPhotoResolutions(cameraInfo: CameraInfo?): List<CameraData> {
    if (cameraInfo !is CameraInfoInternal) {
      Log.w(TAG, "Camera info is not a CameraInfoInternal")
      return emptyList()
    }

    val photoSizes = cameraInfo.getAllSupportedResolutions(ImageFormat.JPEG)
    val isFocusSupported = cameraInfo.isFocusMeteringSupported(createFocusMetering())

    return cameraInfo.getAllCameraData(
      cameraSizes = photoSizes,
      isFocusSupported = isFocusSupported,
    )
  }

  @OptIn(ExperimentalSessionConfig::class)
  fun getVideoResolutions(cameraInfo: CameraInfo?): List<CameraData> {
    if (cameraInfo !is CameraInfoInternal) {
      Log.w(TAG, "Camera info is not a CameraInfoInternal")
      return emptyList()
    }

    val videoCapabilities = Recorder.getVideoCapabilities(
      cameraInfo,
      Recorder.VIDEO_CAPABILITIES_SOURCE_CAMCORDER_PROFILE,
    )

    val supportedQualities = videoCapabilities.getSupportedQualities()
    val videoSizes = supportedQualities.flatMap { (it as Quality.ConstantQuality).typicalSizes }

    val isVideoStabilizationSupported = videoCapabilities.isStabilizationSupported
    val isFocusSupported = cameraInfo.isFocusMeteringSupported(createFocusMetering())

    return cameraInfo.getAllCameraData(
      cameraSizes = videoSizes,
      isFocusSupported = isFocusSupported,
      isVideoStabilizationSupported = isVideoStabilizationSupported,
    )
  }

  internal fun getMinFov(info: CameraInfo): Float {
    return (info as Camera2CameraInfoImpl)
      .cameraCharacteristicsMap
      .mapNotNull { (_, characteristics) ->
        val sensorSize = characteristics.get(SENSOR_INFO_PHYSICAL_SIZE) ?: return@mapNotNull null
        val focalLengths = characteristics.get(LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
          ?: return@mapNotNull null

        getFOV(focalLengths = focalLengths, sensorSize = sensorSize)
      }.minOrNull()
      ?.toFloat() ?: 100F
  }

  internal fun getCamLensTypes(info: CameraInfo): List<CamLensType> {
    return (info as Camera2CameraInfoImpl)
      .cameraCharacteristicsMap
      .map { (_, characteristics) ->
        val sensorSize = characteristics.get(SENSOR_INFO_PHYSICAL_SIZE) ?: return@map Wide
        val focalLengths = characteristics.get(LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
          ?: return@map Wide

        val fov = getFOV(focalLengths, sensorSize)
        when {
          fov > UltraWide.minFov -> UltraWide
          fov > Wide.minFov -> Wide
          else -> Telephoto
        }
      }.distinct()
  }

  private fun getFOV(
    focalLengths: FloatArray,
    sensorSize: SizeF,
  ): Double {
    val focalLength = focalLengths.minOrNull() ?: return 0.0

    if ((sensorSize.width == 0f) || (sensorSize.height == 0f)) {
      return 0.0
    }

    val sensorDiagonal = hypot(sensorSize.width.toDouble(), sensorSize.height.toDouble())
    return Math.toDegrees(2.0 * atan2(sensorDiagonal, 2.0 * focalLength))
  }

  private fun CameraInfoInternal.getAllCameraData(
    cameraSizes: List<Size>,
    isFocusSupported: Boolean,
    isVideoStabilizationSupported: Boolean? = null,
    ignoreFps: Boolean = false,
  ): List<CameraData> {
    val frameRateRanges = listOf(supportedFrameRateRanges).flatten()

    var minFps = frameRateRanges.minOf { it.lower }
    var maxFps = frameRateRanges.maxOf { it.upper }

    return cameraSizes.mapNotNull { videoSize ->
      try {
        if (ignoreFps) {
          val (min, max) = getFps(videoSize, minFps, maxFps)
          minFps = min
          maxFps = max
        }

        CameraData(
          width = videoSize.width,
          height = videoSize.height,
          minFps = minFps.takeUnless { ignoreFps },
          maxFps = maxFps.takeUnless { ignoreFps },
          videoStabilizationModes = VideoStabilizationMode.entries.takeIf {
            isVideoStabilizationSupported == true
          },
          isFocusSupported = isFocusSupported,
        )
      } catch (e: Exception) {
        Log.e(TAG, "Error in getting video resolution: ${e.stackTraceToString()}")
        null
      }
    }
  }

  internal fun createFocusMetering(): FocusMeteringAction {
    val point = SurfaceOrientedMeteringPointFactory(1.0f, 1.0f).createPoint(0.5f, 0.5f)
    return FocusMeteringAction.Builder(point).build()
  }

  private fun CameraInfoInternal.getFps(
    videoSize: Size,
    minFps: Int,
    maxFps: Int,
  ): Pair<Int, Int> {
    var maxFps = maxFps
    try {
      val quality = findClosestCamcorderProfileQuality(cameraId, videoSize)
      maxFps = getMaximumFps(cameraId, quality) ?: maxFps
    } catch (e: Exception) {
      Log.e(TAG, "Error in get FPS, ${e.stackTraceToString()}")
    }

    return min(minFps, maxFps) to maxFps
  }

  private fun getMaximumFps(
    cameraId: String,
    quality: Int,
  ): Int? {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val profiles = CamcorderProfile.getAll(cameraId, quality)
        if (profiles?.videoProfiles != null) {
          return profiles.videoProfiles.maxOf { it.frameRate }
        }
      }

      cameraId.toIntOrNull()?.let { id ->
        return CamcorderProfile.get(id, quality).videoFrameRate
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error in getting max fps from size: ${e.stackTraceToString()}")
      null
    }

    Log.w(TAG, "No max fps found for quality: $quality")

    return null
  }

  private fun VideoCapabilities.getSupportedQualities(): List<Quality?> =
    supportedDynamicRanges.flatMap(::getSupportedQualities)

  private fun CameraInfoInternal.getAllSupportedResolutions(imageFormat: Int): List<Size> {
    val highResolutions = getSupportedHighResolutions(imageFormat).filterNotNull()
    val resolutions = getSupportedResolutions(imageFormat).filterNotNull()
    return (highResolutions + resolutions).sortedBy { it.width * it.height }
  }

  private fun findClosestCamcorderProfileQuality(
    cameraId: String,
    resolution: Size,
  ): Int {
    val targetResolution = resolution.width * resolution.height
    val cameraIdInt = cameraId.toIntOrNull()

    @SuppressLint("InlinedApi")
    val qualities = CamcorderProfile.QUALITY_QCIF..CamcorderProfile.QUALITY_8KUHD

    var profiles = qualities.filter { profile ->
      when {
        cameraIdInt != null -> CamcorderProfile.hasProfile(cameraIdInt, profile)
        else -> CamcorderProfile.hasProfile(profile)
      }
    }

    profiles = profiles.filter { profile ->
      val currentResolution = getResolutionForCamcorderProfileQuality(profile)
      currentResolution <= targetResolution
    }

    val closestProfile = profiles.minBy { profile ->
      val currentResolution = getResolutionForCamcorderProfileQuality(profile)
      abs(currentResolution - targetResolution)
    }

    return closestProfile
  }

  private fun getResolutionForCamcorderProfileQuality(camcorderProfile: Int): Int =
    when (camcorderProfile) {
      CamcorderProfile.QUALITY_QCIF -> 176 * 144
      CamcorderProfile.QUALITY_QVGA -> 320 * 240
      CamcorderProfile.QUALITY_CIF -> 352 * 288
      CamcorderProfile.QUALITY_VGA -> 640 * 480
      CamcorderProfile.QUALITY_480P -> 720 * 480
      CamcorderProfile.QUALITY_720P -> 1280 * 720
      CamcorderProfile.QUALITY_1080P -> 1920 * 1080
      CamcorderProfile.QUALITY_2K -> 2048 * 1080
      CamcorderProfile.QUALITY_QHD -> 2560 * 1440
      CamcorderProfile.QUALITY_2160P -> 3840 * 2160
      CamcorderProfile.QUALITY_4KDCI -> 4096 * 2160
      CamcorderProfile.QUALITY_8KUHD -> 7680 * 4320
      else -> throw Error("Invalid CamcorderProfile \"$camcorderProfile\"!")
    }
}
