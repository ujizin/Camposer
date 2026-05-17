package com.ujizin.camposer.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class IdempotencyGuardRequiredTest {

    private val rule = IdempotencyGuardRequired()

    @Test
    fun `flags update function in CameraEngineImpl with no guard`() {
        val code = """
            class CameraEngineImpl {
                override fun updateFlashMode(flashMode: FlashMode) {
                    applier.applyFlashMode(flashMode)
                }
            }
        """.trimIndent()
        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not flag update function with if-return guard`() {
        val code = """
            class CameraEngineImpl {
                override fun updateFlashMode(flashMode: FlashMode) {
                    if (cameraState.flashMode.value == flashMode) return
                    applier.applyFlashMode(flashMode)
                }
            }
        """.trimIndent()
        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not flag update function where guard comes after clamping logic`() {
        val code = """
            class CameraEngineImpl {
                override fun updateZoomRatio(zoomRatio: Float) {
                    val clamped = zoomRatio.coerceIn(0f, 10f)
                    if (cameraState.zoomRatio.value == clamped) return
                    applier.applyZoomRatio(clamped)
                }
            }
        """.trimIndent()
        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not flag update function outside CameraEngineImpl`() {
        val code = """
            class SomeOtherClass {
                override fun updateFlashMode(flashMode: FlashMode) {
                    applier.applyFlashMode(flashMode)
                }
            }
        """.trimIndent()
        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not flag non-update function in CameraEngineImpl`() {
        val code = """
            class CameraEngineImpl {
                fun onCameraInitialized() {
                    appliers.forEach { it.onCameraInitialized() }
                }
            }
        """.trimIndent()
        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not flag isMirrorEnabled in CameraEngineImpl`() {
        val code = """
            class CameraEngineImpl {
                fun isMirrorEnabled(): Boolean = true
            }
        """.trimIndent()
        assertEquals(0, rule.lint(code).size)
    }
}
