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
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

/**
 * Every `update*()` override in `CameraEngineImpl` must contain an idempotency guard:
 * an `if (...) return` that prevents re-applying a value that is already current.
 *
 * Without this guard, state and hardware may be written redundantly on every
 * composition recomposition, causing jank and spurious camera reconfiguration.
 */
class IdempotencyGuardRequired(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "update*() override in CameraEngineImpl is missing an idempotency guard " +
            "(if (cameraState.x.value == x) return).",
        debt = Debt.TEN_MINS,
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (!function.name.orEmpty().startsWith("update")) return
        if (!function.hasModifier(KtTokens.OVERRIDE_KEYWORD)) return
        if (function.containingClassOrObject?.name != "CameraEngineImpl") return

        val hasGuard = PsiTreeUtil
            .findChildrenOfType(function.bodyExpression, KtIfExpression::class.java)
            .any { ifExpr ->
                ifExpr.then is KtReturnExpression ||
                    PsiTreeUtil.findChildOfType(ifExpr.then, KtReturnExpression::class.java) != null
            }

        if (!hasGuard) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(function),
                    "Function '${function.name}' in CameraEngineImpl is missing an idempotency guard. " +
                        "Add: if (cameraState.<property>.value == <param>) return",
                ),
            )
        }
    }
}
