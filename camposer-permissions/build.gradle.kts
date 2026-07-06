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
  set("PUBLISH_ARTIFACT_ID", Config.Artifact.camposerPermissionsId)
  set("PUBLISH_VERSION", Config.versionName)
}

apply(from = "$rootDir/scripts/publish-module.gradle")

kotlin {
  explicitApi()

  abiValidation {
    enabled.set(true)
  }

  androidLibrary {
    namespace = "com.ujizin.camposer.permissions"
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
      baseName = "CamposerPermissionsKit"
      isStatic = true
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.compose.runtime)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    androidMain {
      dependencies {
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.runtime.compose)
      }
    }

    iosMain {
      dependencies {
      }
    }

    getByName("androidDeviceTest") {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.androidx.test.core)
        implementation(libs.androidx.test.rules)
      }
    }
  }
}

dokka {
  moduleName.set("Camposer Permissions")
}
