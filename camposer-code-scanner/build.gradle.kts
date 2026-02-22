@file:OptIn(ExperimentalAbiValidation::class)

import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import ujizin.camposer.Config

plugins {
  alias(libs.plugins.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.dokka)
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

// Workaround for Compose plugin task config on androidDeviceTest where outputDirectory can be unset.
tasks.configureEach {
  if (!javaClass.name.startsWith("org.jetbrains.compose.resources.CopyResourcesToAndroidAssetsTask")) return@configureEach
  if (!name.contains("AndroidDeviceTestComposeResourcesToAndroidAssets")) return@configureEach

  @Suppress("UNCHECKED_CAST")
  val outputDirectory =
    javaClass.methods
      .firstOrNull { it.name == "getOutputDirectory" }
      ?.invoke(this) as? DirectoryProperty

  outputDirectory?.set(layout.buildDirectory.dir("generated/compose/resource-assets/$name"))
}
