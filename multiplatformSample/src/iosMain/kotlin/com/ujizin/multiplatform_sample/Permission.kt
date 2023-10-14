package com.ujizin.multiplatform_sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.requestAccessForMediaType

@Composable
actual fun Permission(content: @Composable () -> Unit) {
    var granted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { response ->
            granted = response
        }
    }

    if (granted) content()
}