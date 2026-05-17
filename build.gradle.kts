plugins {
  alias(libs.plugins.application) apply false
  alias(libs.plugins.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.kotlin.multiplatform.library) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.gradle.nexus)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.dokka)
  alias(libs.plugins.binary.compatibility.validator)
}

apiValidation {
  ignoredProjects += "detekt-rules"
}

tasks.register("checkModuleGraph") {
  group = "verification"
  description = "Asserts :camposer never depends on :camposer-code-scanner (dependency inversion guard)."
  doLast {
    val violations =
      project(":camposer")
        .configurations
        .flatMap { config -> config.dependencies.filterIsInstance<ProjectDependency>() }
        .filter { it.name == "camposer-code-scanner" }

    check(violations.isEmpty()) {
      ":camposer must not depend on :camposer-code-scanner. " +
        "The allowed direction is :camposer-code-scanner → :camposer."
    }
  }
}

dokka {
  dokkaPublications.html {
    moduleName.set("Camposer")
    outputDirectory.set(rootProject.file("docs/api/"))
  }

  dependencies {
    dokka(project(":camposer"))
    dokka(project(":camposer-code-scanner"))
  }
}


subprojects {
  pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    dependencies {
      "detektPlugins"(project(":detekt-rules"))
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
      config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
      buildUponDefaultConfig = false
    }

    tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektCommonMain") {
      description = "Detekt with KMP-strict rules on commonMain source set."
      group = "verification"
      setSource(fileTree("src/commonMain/kotlin"))
      config.setFrom(files("$rootDir/config/detekt/detekt-common.yml"))
      buildUponDefaultConfig = false
      reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
      }
    }
  }

  apply(
    plugin =
      rootProject.libs.plugins.spotless
        .get()
        .pluginId,
  )
  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      ktlint(libs.versions.ktlint.get())
        .setEditorConfigPath("$rootDir/.editorconfig")
      target("src/**/*.kt")
      targetExclude("**/build/**/*.kt")
    }

    kotlinGradle {
      ktlint(libs.versions.ktlint.get())
      target("**/*.gradle.kts")
      targetExclude("**/build/**/*.kts")
    }
  }
}
