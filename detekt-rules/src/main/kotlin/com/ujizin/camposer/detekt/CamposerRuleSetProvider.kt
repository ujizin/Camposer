package com.ujizin.camposer.detekt

import com.ujizin.camposer.detekt.rules.ApplierMustCallStateUpdate
import com.ujizin.camposer.detekt.rules.CameraEngineNotInPublicApi
import com.ujizin.camposer.detekt.rules.IdempotencyGuardRequired
import com.ujizin.camposer.detekt.rules.NoPlatformImportInCommonMain
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CamposerRuleSetProvider : RuleSetProvider {
  override val ruleSetId: String = "camposer"

  override fun instance(config: Config): RuleSet =
    RuleSet(
      ruleSetId,
      listOf(
        NoPlatformImportInCommonMain(config),
        CameraEngineNotInPublicApi(config),
        IdempotencyGuardRequired(config),
        ApplierMustCallStateUpdate(config),
      ),
    )
}
