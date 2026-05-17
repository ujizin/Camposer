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
      buildUponDefaultConfig = true
    }

    tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektCommonMain") {
      description = "Detekt with KMP-strict rules on commonMain source set."
      group = "verification"
      setSource(fileTree("src/commonMain/kotlin"))
      config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
      buildUponDefaultConfig = true
      baseline = file("$projectDir/config/detekt/baseline-commonMain.xml")
      reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
      }
    }

    tasks.register<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineCommonMain") {
      description = "Creates detekt baseline for commonMain source set."
      group = "verification"
      setSource(fileTree("src/commonMain/kotlin"))
      config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
      buildUponDefaultConfig = true
      baseline = file("$projectDir/config/detekt/baseline-commonMain.xml")
    }

    if (file("src/iosMain/kotlin").exists()) {
      tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektIosMain") {
        description = "Detekt with KMP-strict rules on iosMain source set."
        group = "verification"
        setSource(fileTree("src/iosMain/kotlin"))
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        baseline = file("$projectDir/config/detekt/baseline-iosMain.xml")
        reports {
          html.required.set(false)
          xml.required.set(false)
          txt.required.set(false)
        }
      }

      tasks.register<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineIosMain") {
        description = "Creates detekt baseline for iosMain source set."
        group = "verification"
        setSource(fileTree("src/iosMain/kotlin"))
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        baseline = file("$projectDir/config/detekt/baseline-iosMain.xml")
      }
    }

    afterEvaluate {
      tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detektAndroidMain") {
        baseline = file("$projectDir/config/detekt/baseline-androidMain.xml")
      }
      tasks.named<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>("detektBaselineAndroidMain") {
        baseline = file("$projectDir/config/detekt/baseline-androidMain.xml")
      }
      tasks.named("detekt") {
        dependsOn("detektCommonMain", "detektAndroidMain")
        if (tasks.findByName("detektIosMain") != null) dependsOn("detektIosMain")
      }
      tasks.named("detektBaseline") {
        dependsOn("detektBaselineCommonMain", "detektBaselineAndroidMain")
        if (tasks.findByName("detektBaselineIosMain") != null) dependsOn("detektBaselineIosMain")
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
