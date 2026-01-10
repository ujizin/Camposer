package com.ujizin.camposer.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.ujizin.camposer.shared.features.camera.CameraScreen
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun CameraViewController(): UIViewController =
  ComposeUIViewController {
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
