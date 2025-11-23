package com.ujizin.camposer.manager

import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceWasConnectedNotification
import platform.AVFoundation.AVCaptureDeviceWasDisconnectedNotification
import platform.Foundation.NSNotificationCenter
import platform.darwin.NSObject
import platform.darwin.NSObjectProtocol

// TODO still needs to be tested
internal class CameraPresenceMonitor : NSObject() {

    interface Listener {
        fun onCameraAdded(device: AVCaptureDevice)
        fun onCameraRemoved(device: AVCaptureDevice)
    }

    private val notificationCenter = NSNotificationCenter.defaultCenter
    private val listeners: MutableSet<Listener> = mutableSetOf()

    private var connectionObserver: NSObjectProtocol? = null
    private var disconnectionObserver: NSObjectProtocol? = null

    fun addCameraPresenceListener(listener: Listener) {
        if (listeners.contains(listener)) return

        this.listeners.add(listener)
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
            queue = null
        ) { notification ->
            val device = notification?.userInfo?.get("AVCaptureDevice") as? AVCaptureDevice
            if (device != null) {
                listeners.forEach { it.onCameraAdded(device) }
            }
        }
    }

    private fun startDisconnectionObserver() {
        disconnectionObserver = notificationCenter.addObserverForName(
            name = AVCaptureDeviceWasDisconnectedNotification,
            `object` = null,
            queue = null
        ) { notification ->
            val device = notification?.userInfo?.get("AVCaptureDevice") as? AVCaptureDevice
            if (device != null) {
                listeners.forEach { it.onCameraRemoved(device) }
            }
        }
    }

    private fun dispose() {
        connectionObserver?.let(notificationCenter::removeObserver)
        disconnectionObserver?.let(notificationCenter::removeObserver)
        connectionObserver = null
        disconnectionObserver = null
    }
}