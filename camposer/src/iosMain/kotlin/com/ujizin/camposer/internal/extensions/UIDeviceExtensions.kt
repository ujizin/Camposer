package com.ujizin.camposer.internal.extensions

import platform.UIKit.UIDevice

internal val UIDevice.Companion.systemVersion: Double
    get() = UIDevice.currentDevice.systemVersion.toDoubleOrNull() ?: -1.0