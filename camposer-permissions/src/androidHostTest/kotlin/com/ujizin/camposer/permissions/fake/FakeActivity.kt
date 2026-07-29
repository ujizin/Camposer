package com.ujizin.camposer.permissions.fake

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager

// JVM only: the Activity constructor is a stub here, but needs a Looper on device.
internal class FakeActivity(
  private val isGranted: Boolean,
) : Activity() {
  var startedIntent: Intent? = null
    private set

  override fun checkPermission(
    permission: String,
    pid: Int,
    uid: Int,
  ): Int =
    when {
      isGranted -> PackageManager.PERMISSION_GRANTED
      else -> PackageManager.PERMISSION_DENIED
    }

  override fun startActivity(intent: Intent) {
    startedIntent = intent
  }
}
