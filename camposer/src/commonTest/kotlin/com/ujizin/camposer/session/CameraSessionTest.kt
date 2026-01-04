package com.ujizin.camposer.session

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.fake.FakeCameraTest
import com.ujizin.camposer.fake.createCameraSession
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.ImplementationMode
import com.ujizin.camposer.state.properties.ScaleType
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.selector.CamSelector
import com.ujizin.camposer.state.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class CameraSessionTest {
  protected val testDispatcher = UnconfinedTestDispatcher()

  protected val cameraTest: FakeCameraTest by lazy { FakeCameraTest() }

  protected val cameraSession by lazy {
    createCameraSession(
      fakeCameraTest = cameraTest,
      testDispatcher = testDispatcher,
    )
  }

  protected val controller: CameraController
    get() = cameraSession.controller

  /**
   * optional, useful if it needs to start cameraSession eagerly
   * */
  protected fun initCameraSession() {
    cameraSession.onSessionStarted()
  }

  protected fun updateSession(
    camSelector: CamSelector = cameraSession.state.camSelector,
    captureMode: CaptureMode = cameraSession.state.captureMode,
    scaleType: ScaleType = cameraSession.state.scaleType,
    imageAnalyzer: ImageAnalyzer? = cameraSession.state.imageAnalyzer,
    isImageAnalysisEnabled: Boolean = imageAnalyzer != null,
    implementationMode: ImplementationMode = cameraSession.state.implementationMode,
    isFocusOnTapEnabled: Boolean = cameraSession.state.isFocusOnTapEnabled,
    imageCaptureStrategy: ImageCaptureStrategy = cameraSession.state.imageCaptureStrategy,
    camFormat: CamFormat = cameraSession.state.camFormat,
    isPinchToZoomEnabled: Boolean = cameraSession.state.isPinchToZoomEnabled,
  ) = cameraSession.update(
    camSelector = camSelector,
    captureMode = captureMode,
    scaleType = scaleType,
    isImageAnalysisEnabled = isImageAnalysisEnabled,
    imageAnalyzer = imageAnalyzer,
    implementationMode = implementationMode,
    isFocusOnTapEnabled = isFocusOnTapEnabled,
    imageCaptureStrategy = imageCaptureStrategy,
    camFormat = camFormat,
    isPinchToZoomEnabled = isPinchToZoomEnabled,
  )
}
