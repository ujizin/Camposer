package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.CameraManagerInternalImpl
import com.ujizin.camposer.internal.core.IOSCameraManagerInternal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal actual class FakeCameraManagerInternal actual constructor(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : IOSCameraManagerInternal by CameraManagerInternalImpl(
    cameraController = cameraTest.fakeIosCameraController,
    cameraInfo = cameraTest.cameraInfo,
    dispatcher = testDispatcher,
  )
