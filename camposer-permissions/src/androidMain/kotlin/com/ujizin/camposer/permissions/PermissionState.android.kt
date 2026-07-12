package com.ujizin.camposer.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ujizin.camposer.permissions.internal.findActivity

@Composable
public actual fun rememberCameraPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA, onPermissionResult)

@Composable
public actual fun rememberAudioPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState =
  rememberPermissionState(android.Manifest.permission.RECORD_AUDIO, onPermissionResult)

@Composable
private fun rememberPermissionState(
  permission: String,
  onPermissionResult: (Boolean) -> Unit,
): PermissionState {
  val context = LocalContext.current
  val state = remember(permission, context) { AndroidPermissionState(permission, context) }
  SideEffect { state.onPermissionResult = onPermissionResult }

  val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { granted -> state.onRequestResult(granted) }

  DisposableEffect(state, launcher) {
    state.launcher = launcher
    onDispose { state.launcher = null }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    state.refreshStatus()
  }

  return state
}

internal class AndroidPermissionState(
  private val permission: String,
  private val context: Context,
) : PermissionState {
  var launcher: ActivityResultLauncher<String>? = null
  var onPermissionResult: (Boolean) -> Unit = {}

  override var status: PermissionStatus by mutableStateOf(
    checkStatus(canRequestAgain = true),
  )
    private set

  override fun launchPermissionRequest() {
    launcher?.launch(permission)
  }

  override fun openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", context.packageName, null)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  }

  fun onRequestResult(granted: Boolean) {
    status = when {
      granted -> {
        PermissionStatus.Granted
      }

      else -> {
        val activity = context.findActivity()
        val canRequestAgain = activity
          ?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permission) }
          ?: true
        PermissionStatus.Denied(canRequestAgain)
      }
    }
    onPermissionResult(granted)
  }

  fun refreshStatus() {
    val canRequestAgain = (status as? PermissionStatus.Denied)?.canRequestAgain ?: true
    status = checkStatus(canRequestAgain)
  }

  private fun checkStatus(canRequestAgain: Boolean): PermissionStatus {
    val granted = ContextCompat.checkSelfPermission(context, permission) ==
      PackageManager.PERMISSION_GRANTED
    return if (granted) PermissionStatus.Granted else PermissionStatus.Denied(canRequestAgain)
  }
}
