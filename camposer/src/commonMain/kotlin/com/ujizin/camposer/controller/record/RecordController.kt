public interface RecordController {
  public val isMuted: Boolean

  public val isRecording: Boolean

  public fun startRecording(
    filename: String,
    onVideoCaptured: (CaptureResult<String>) -> Unit,
  )

  public fun resumeRecording()

  public fun pauseRecording()

  public fun stopRecording()

  public fun muteRecording(isMuted: Boolean)
}
