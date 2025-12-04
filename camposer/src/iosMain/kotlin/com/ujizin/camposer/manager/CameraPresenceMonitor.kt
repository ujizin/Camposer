package com.ujizin.camposer.manager

import com.ujizin.camposer.internal.utils.Debouncer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import platform.AVFoundation.AVCaptureDeviceWasConnectedNotification
import platform.AVFoundation.AVCaptureDeviceWasDisconnectedNotification
import platform.Foundation.NSNotificationCenter
import platform.darwin.NSObject
import platform.darwin.NSObjectProtocol
import kotlin.time.Duration.Companion.milliseconds

internal class CameraPresenceMonitor : NSObject() {
  interface Listener {
    fun onCameraUpdated()
  }

  private val mainScope = MainScope()

  private val debouncer = Debouncer(
    duration = 100.milliseconds,
    scope = mainScope,
  )

  private val notificationCenter = NSNotificationCenter.defaultCenter
  private val listeners: MutableSet<Listener> = mutableSetOf()

  private var connectionObserver: NSObjectProtocol? = null
  private var disconnectionObserver: NSObjectProtocol? = null

  fun addCameraPresenceListener(listener: Listener) {
    if (listeners.contains(listener)) return

    listeners.add(listener)
    if (listeners.size == 1) {
      start()
    }
  }

  fun removeCameraPresenceListener(listener: Listener) {
    if (!listeners.contains(listener)) return

    this.listeners.remove(listener)
    if (listeners.isEmpty()) {
      dispose()
    }
  }

  private fun start() {
    startConnectionObserver()
    startDisconnectionObserver()
  }

  private fun startConnectionObserver() {
    connectionObserver = notificationCenter.addObserverForName(
      name = AVCaptureDeviceWasConnectedNotification,
      `object` = null,
      queue = null,
    ) { notification ->
      debouncer.submit {
        listeners.forEach { it.onCameraUpdated() }
      }
    }
  }

  private fun startDisconnectionObserver() {
    disconnectionObserver = notificationCenter.addObserverForName(
      name = AVCaptureDeviceWasDisconnectedNotification,
      `object` = null,
      queue = null,
    ) { notification ->
      debouncer.submit {
        listeners.forEach { it.onCameraUpdated() }
      }
    }
  }

  private fun dispose() {
    connectionObserver?.let(notificationCenter::removeObserver)
    disconnectionObserver?.let(notificationCenter::removeObserver)
    connectionObserver = null
    disconnectionObserver = null
  }

  internal fun release() {
    dispose()
    mainScope.cancel()
  }
}
