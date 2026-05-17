package com.ujizin.camposer.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEngineNotInPublicApiTest {
  private val rule = CameraEngineNotInPublicApi()

  @Test
  fun `flags CameraEngine as public function return type`() {
    val code = """
            class Foo {
                fun getEngine(): CameraEngine = TODO()
            }
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `flags CameraEngine as public property type`() {
    val code = """
            class Foo {
                val engine: CameraEngine = TODO()
            }
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `does not flag CameraEngine in internal function`() {
    val code = """
            class Foo {
                internal fun getEngine(): CameraEngine = TODO()
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `does not flag CameraEngine in private property`() {
    val code = """
            class Foo {
                private val engine: CameraEngine = TODO()
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }

  @Test
  fun `flags CameraEngine as public function parameter type`() {
    val code = """
            class Foo {
                fun configure(engine: CameraEngine) = Unit
            }
    """.trimIndent()
    assertEquals(1, rule.lint(code).size)
  }

  @Test
  fun `does not flag CameraEngine in private function parameter`() {
    val code = """
            class Foo {
                private fun use(engine: CameraEngine) = Unit
            }
    """.trimIndent()
    assertEquals(0, rule.lint(code).size)
  }
}
