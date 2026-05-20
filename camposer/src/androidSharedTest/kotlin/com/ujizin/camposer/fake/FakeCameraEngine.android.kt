package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.AndroidCameraEngine
import com.ujizin.camposer.internal.core.CameraEngineImpl
import kotlinx.coroutines.CoroutineDispatcher

internal actual class FakeCameraEngine actual constructor(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : AndroidCameraEngine by CameraEngineImpl(
    cameraXController = cameraTest.cameraXController,
    cameraController = cameraTest.cameraController,
    cameraInfo = cameraTest.cameraInfo,
    dispatcher = testDispatcher,
  )
