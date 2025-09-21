package com.ujizin.camposer.error

import kotlinx.io.files.Path
import platform.Foundation.NSError

internal open class NSErrorException(nsError: NSError) : Exception(nsError.localizedDescription)

internal open class CameraException(message: String) : Exception("Camera exception: $message")

internal open class CameraNSErrorException(
    message: String,
    nsError: NSError,
) : Exception("Camera exception: $message. Error: $nsError")

internal class ErrorRecordVideoException(nsError: NSError) : CameraNSErrorException(
    message = "Error recording video",
    nsError = nsError
)

internal class ErrorTakePhotoException(nsError: NSError) : CameraNSErrorException(
    message = "Error take photo",
    nsError = nsError
)

internal class ErrorWritePhotoPathException(path: Path) : CameraException(
    message = "Error writing picture to path: $path",
)

internal class NSDataNotFoundException() : CameraException("NSData not found")

internal class CameraNotRunningException() : CameraException("Camera is not running")

internal class VideoOutputNotFoundException() : CameraException("Video output not found")

internal class PhotoOutputNotFoundException() : CameraException("Photo output not found")

internal class AudioInputNotFoundException() : CameraException("Audio input not found")
