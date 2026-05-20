package com.ujizin.camposer.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

/**
 * Every `apply*()` method in a class ending with `Applier` must call
 * `cameraState.update*()` at least once (directly or inside a lambda).
 *
 * Appliers own the hardware write AND the state write. Missing the state
 * write means the in-memory state drifts from actual hardware state.
 */
public class ApplierMustCallStateUpdate(
  config: Config = Config.empty,
) : Rule(config) {
  override val issue: Issue = Issue(
    id = javaClass.simpleName,
    severity = Severity.Defect,
    description = "apply*() method in an Applier class must call cameraState.update*() " +
      "to keep in-memory state in sync with hardware.",
    debt = Debt.TEN_MINS,
  )

  override fun visitNamedFunction(function: KtNamedFunction) {
    super.visitNamedFunction(function)

    if (!function.name.orEmpty().startsWith("apply")) return
    if (function.containingClassOrObject?.name?.endsWith("Applier") != true) return
    if (!function.hasBody()) return
    if (function.containingClassOrObject?.hasModifier(KtTokens.EXPECT_KEYWORD) == true) return

    val hasStateUpdate = PsiTreeUtil
      .findChildrenOfType(function, KtCallExpression::class.java)
      .any { call ->
        val receiverText = when (val parent = call.parent) {
          is KtDotQualifiedExpression -> parent.receiverExpression.text
          is KtSafeQualifiedExpression -> parent.receiverExpression.text
          else -> return@any false
        }
        val callName = call.calleeExpression?.text.orEmpty()
        (receiverText == "cameraState" || receiverText.endsWith(".cameraState")) &&
          callName.startsWith("update")
      }

    if (!hasStateUpdate) {
      report(
        CodeSmell(
          issue,
          Entity.from(function),
          "Function '${function.name}' in ${function.containingClassOrObject?.name} " +
            "does not call cameraState.update*(). Add the matching state update call.",
        ),
      )
    }
  }
}
