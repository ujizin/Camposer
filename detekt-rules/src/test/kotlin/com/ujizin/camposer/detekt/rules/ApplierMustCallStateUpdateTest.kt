package com.ujizin.camposer.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplierMustCallStateUpdateTest {
  private val rule = ApplierMustCallStateUpdate()

  @Test
  fun `flags apply function with no cameraState update call`() {
    val code = """
            class ExposureZoomApplier {
                fun applyFlashMode(flashMode: FlashMode) {
                    controller.setFlashMode(flashMode)
                }
            }
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `does not flag apply function that calls cameraState update`() {
    val code = """
            class ExposureZoomApplier {
                fun applyFlashMode(flashMode: FlashMode) {
                    controller.setFlashMode(flashMode)
                    cameraState.updateFlashMode(flashMode)
                }
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag apply function that calls cameraState update inside lambda`() {
    val code = """
            class SessionTopologyApplier {
                fun applyCamFormat(camFormat: CamFormat) {
                    camFormat.applyConfigs(
                        onFrameRateChanged = { cameraState.updateFrameRate(it) }
                    )
                    cameraState.updateCamFormat(camFormat)
                }
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag non-apply function in Applier class`() {
    val code = """
            class ExposureZoomApplier {
                override fun onCameraInitialized() {
                    controller.setZoom(1f)
                }
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag apply function outside Applier class`() {
    val code = """
            class SomeHelper {
                fun applyTheme(theme: Theme) {
                    view.setTheme(theme)
                }
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag abstract apply function in interface`() {
    val code = """
            interface SessionTopologyApplier {
                fun applyCaptureMode(captureMode: CaptureMode)
                fun applyCamSelector(camSelector: CamSelector)
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag apply function in expect class`() {
    val code = """
            expect class SessionTopologyApplier : CameraStateApplier {
                fun applyCaptureMode(captureMode: CaptureMode)
                fun applyCamSelector(camSelector: CamSelector)
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `flags apply function with block body and no cameraState update call`() {
    val code = """
            class ExposureZoomApplier {
                fun applyFlashMode(flashMode: FlashMode) {
                    controller.setFlashMode(flashMode)
                }
            }
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `does not flag apply function with block body that calls cameraState update`() {
    val code = """
            class ExposureZoomApplier {
                fun applyFlashMode(flashMode: FlashMode) {
                    controller.setFlashMode(flashMode)
                    cameraState.updateFlashMode(flashMode)
                }
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }
}
