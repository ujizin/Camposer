package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.AndroidCameraManagerInternal
import com.ujizin.camposer.internal.core.CameraManagerInternalImpl
import kotlinx.coroutines.CoroutineDispatcher

internal actual class FakeCameraManagerInternal actual constructor(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : AndroidCameraManagerInternal by CameraManagerInternalImpl(
    controller = cameraTest.cameraXController,
    cameraInfo = cameraTest.cameraInfo,
    dispatcher = testDispatcher,
  )
