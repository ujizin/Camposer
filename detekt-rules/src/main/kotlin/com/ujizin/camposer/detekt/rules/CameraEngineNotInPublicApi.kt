package com.ujizin.camposer.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Blocks `CameraEngine` from appearing in public function signatures or public properties.
 *
 * `CameraEngine` is an internal abstraction. Exposing it in the public API couples
 * consumers to implementation details and breaks the library's encapsulation contract.
 *
 * Applied to all source sets via the default `detekt` task.
 */
class CameraEngineNotInPublicApi(
  config: Config = Config.empty,
) : Rule(config) {
  override val issue = Issue(
    id = javaClass.simpleName,
    severity = Severity.Defect,
    description = "CameraEngine must never be exposed through a public type. " +
      "Mark the declaration internal or private.",
    debt = Debt.TEN_MINS,
  )

  override fun visitNamedFunction(function: KtNamedFunction) {
    super.visitNamedFunction(function)
    if (function.isNonPublic()) return
    val returnType = function.typeReference?.text ?: ""
    val paramTypes = function.valueParameters.mapNotNull { it.typeReference?.text }
    if ("CameraEngine" in returnType || paramTypes.any { "CameraEngine" in it }) {
      report(CodeSmell(issue, Entity.from(function), issue.description))
    }
  }

  override fun visitProperty(property: KtProperty) {
    super.visitProperty(property)
    if (property.isNonPublic()) return
    val typeRef = property.typeReference?.text ?: return
    if ("CameraEngine" in typeRef) {
      report(CodeSmell(issue, Entity.from(property), issue.description))
    }
  }

  private fun KtNamedFunction.isNonPublic() =
    hasModifier(KtTokens.INTERNAL_KEYWORD) ||
      hasModifier(KtTokens.PRIVATE_KEYWORD) ||
      hasModifier(KtTokens.PROTECTED_KEYWORD)

  private fun KtProperty.isNonPublic() =
    hasModifier(KtTokens.INTERNAL_KEYWORD) ||
      hasModifier(KtTokens.PRIVATE_KEYWORD) ||
      hasModifier(KtTokens.PROTECTED_KEYWORD)
}
