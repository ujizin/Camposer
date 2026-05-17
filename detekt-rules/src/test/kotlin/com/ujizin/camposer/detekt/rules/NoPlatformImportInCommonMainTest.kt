package com.ujizin.camposer.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class NoPlatformImportInCommonMainTest {
  private val rule = NoPlatformImportInCommonMain()

  @Test
  fun `flags androidx camera import`() {
    val code = """
            package com.ujizin.camposer
            import androidx.camera.core.ImageCapture
            class Foo
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `flags platform AVFoundation import`() {
    val code = """
            package com.ujizin.camposer
            import platform.AVFoundation.AVCaptureSession
            class Foo
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `flags UIKit import`() {
    val code = """
            package com.ujizin.camposer
            import UIKit.UIView
            class Foo
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `does not flag compose import`() {
    val code = """
            package com.ujizin.camposer
            import androidx.compose.runtime.Composable
            class Foo
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag camposer internal import`() {
    val code = """
            package com.ujizin.camposer
            import com.ujizin.camposer.state.CameraState
            class Foo
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag kotlin stdlib import`() {
    val code = """
            package com.ujizin.camposer
            import kotlin.math.roundToInt
            class Foo
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }
}
