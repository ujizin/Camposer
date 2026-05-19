@file:OptIn(ExperimentalAbiValidation::class)

import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import ujizin.camposer.Config

plugins {
  alias(libs.plugins.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.dokka)
  jacoco
}

extra.apply {
  set("PUBLISH_GROUP_ID", Config.groupId)
  set("PUBLISH_ARTIFACT_ID", Config.Artifact.camposerCodeScannerId)
  set("PUBLISH_VERSION", Config.versionName)
}

apply(from = "$rootDir/scripts/publish-module.gradle")

kotlin {
  explicitApi()

  abiValidation {
    enabled.set(true)
  }

  androidLibrary {
    namespace = "com.ujizin.camposer.code_scanner"
    compileSdk = Config.compileSdk
    minSdk = Config.minSdk

    withHostTestBuilder {
    }

    withDeviceTestBuilder {
      sourceSetTreeName = "test"
      androidResources.enable = true
    }.configure {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "CamposerCodeScannerKit"
      isStatic = true
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(project(":camposer"))
      }
    }

    androidMain {
      dependencies {
        implementation(libs.mlkit.barcode.scanning)
        implementation(libs.androidx.camera.mlkit.vision)
      }
    }

    iosMain {
      dependencies {
      }
    }
  }
}

dokka {
  moduleName.set("Camposer Code Scanner")
}

// Report from Gradle JaCoCo agent output (runs alongside Kover's agent on testAndroidHostTest).
tasks.register<JacocoReport>("jacocoHostTestReport") {
  mustRunAfter("testAndroidHostTest")
  executionData.setFrom(layout.buildDirectory.file("jacoco/testAndroidHostTest.exec"))
  onlyIf { executionData.files.any { it.exists() } }
  classDirectories.setFrom(
    fileTree(layout.buildDirectory.dir("classes/kotlin/android/main")) {
      exclude("**/BuildConfig.class", "androidx/**")
    },
  )
  sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/androidMain/kotlin"))
  reports {
    xml.required.set(true)
    html.required.set(false)
    csv.required.set(false)
  }
}
