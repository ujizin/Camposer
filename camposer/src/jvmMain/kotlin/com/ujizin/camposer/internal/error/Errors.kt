package com.ujizin.camposer.internal.error

internal open class CameraException(
  message: String,
) : Exception("Camera exception: $message")

internal class CameraDeviceNotFoundException(
  deviceIndex: Int,
) : CameraException("No camera found at device index $deviceIndex")

internal class CameraNotStreamingException : CameraException("Camera is not streaming")

internal class RecordingInProgressException :
  CameraException("A recording is already in progress")

internal class ErrorEncodePictureException : CameraException("Failed to encode picture as JPEG")
