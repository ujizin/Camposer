package com.ujizin.camposer.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Blocks platform-specific imports from appearing in commonMain source files.
 *
 * Violations signal that platform code is leaking into shared code.
 * Fix: inject the dependency via an interface, or use expect/actual.
 *
 * Only applied to the `commonMain` source set via the `detektCommonMain` task.
 */
class NoPlatformImportInCommonMain(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "Platform-specific imports are forbidden in commonMain. Use expect/actual or inject via interface.",
        debt = Debt.TWENTY_MINS,
    )

    private val blockedPrefixes = listOf(
        "androidx.camera",
        "platform.",        // Kotlin/Native iOS framework interop
        "UIKit",
        "AVFoundation",
        "CoreMedia",
        "CoreVideo",
        "CoreGraphics",
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        val importPath = importDirective.importedFqName?.asString() ?: return
        if (blockedPrefixes.any { importPath.startsWith(it) }) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "Platform import '$importPath' is not allowed in commonMain. " +
                        "Use expect/actual or inject the abstraction through an interface.",
                ),
            )
        }
    }
}
