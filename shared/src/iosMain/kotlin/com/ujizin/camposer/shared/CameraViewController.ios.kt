package com.ujizin.camposer.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun CameraViewController(): UIViewController = ComposeUIViewController {
//    var cameraGranted by remember { mutableStateOf(false) }
//    var audioGranted by remember { mutableStateOf(false) }
//    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo, {
//        cameraGranted = it
//    })
//
//    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio, {
//        audioGranted = it
//    })

    CameraScreen()
}