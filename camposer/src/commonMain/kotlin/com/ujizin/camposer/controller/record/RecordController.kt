package com.ujizin.camposer.controller.record

import com.ujizin.camposer.CaptureResult

/**
 * Controller interface for managing video recording operations.
 *
 * This interface provides methods to control the lifecycle of video recording,
 * such as starting, pausing, resuming, and stopping the recording, as well as
 * managing the audio state (mute/unmute).
 *
 * Note: This interface defines the underlying contract for recording operations.
 * For standard usage in an application, please use the implementation provided by [com.ujizin.camposer.controller.camera.CameraController].
 */
public interface RecordController {
  public val isMuted: Boolean

  public val isRecording: Boolean

  public fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  )

  public fun resumeRecording(): Result<Boolean>

  public fun pauseRecording(): Result<Boolean>

  public fun stopRecording(): Result<Boolean>

  public fun muteRecording(isMuted: Boolean): Result<Boolean>
}
