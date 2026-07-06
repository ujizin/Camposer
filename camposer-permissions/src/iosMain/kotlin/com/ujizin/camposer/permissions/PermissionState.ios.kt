package com.ujizin.camposer.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
public actual fun rememberCameraPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState = rememberPermissionState(AVMediaTypeVideo, onPermissionResult)

@Composable
public actual fun rememberAudioPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState = rememberPermissionState(AVMediaTypeAudio, onPermissionResult)

@Composable
private fun rememberPermissionState(
  mediaType: AVMediaType,
  onPermissionResult: (Boolean) -> Unit,
): PermissionState {
  val state = remember(mediaType) { IOSPermissionState(mediaType) }
  state.onPermissionResult = onPermissionResult

  DisposableEffect(state) {
    val observer = NSNotificationCenter.defaultCenter.addObserverForName(
      name = UIApplicationDidBecomeActiveNotification,
      `object` = null,
      queue = NSOperationQueue.mainQueue,
    ) { _ -> state.refreshStatus() }

    onDispose { NSNotificationCenter.defaultCenter.removeObserver(observer) }
  }

  return state
}

internal class IOSPermissionState(
  private val mediaType: AVMediaType,
) : PermissionState {
  var onPermissionResult: (Boolean) -> Unit = {}

  override var status: PermissionStatus by mutableStateOf(currentStatus())
    private set

  override fun launchPermissionRequest() {
    AVCaptureDevice.requestAccessForMediaType(mediaType) { granted ->
      dispatch_async(dispatch_get_main_queue()) {
        refreshStatus()
        onPermissionResult(granted)
      }
    }
  }

  override fun openAppSettings() {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    UIApplication.sharedApplication.openURL(
      url = url,
      options = emptyMap<Any?, Any>(),
      completionHandler = null,
    )
  }

  fun refreshStatus() {
    status = currentStatus()
  }

  private fun currentStatus(): PermissionStatus =
    AVCaptureDevice.authorizationStatusForMediaType(mediaType).toPermissionStatus()
}
