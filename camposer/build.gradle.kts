@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalAbiValidation::class)

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import ujizin.camposer.Config

plugins {
  alias(libs.plugins.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.dokka)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

extra.apply {
  set("PUBLISH_GROUP_ID", Config.groupId)
  set("PUBLISH_ARTIFACT_ID", Config.Artifact.camposerId)
  set("PUBLISH_VERSION", Config.versionName)
}

apply(from = "$rootDir/scripts/publish-module.gradle")

kotlin {
  targets.configureEach {
    compilations.configureEach {
      compilerOptions.configure { freeCompilerArgs.add("-Xexpect-actual-classes") }
    }
  }

  explicitApi()

  abiValidation {
    enabled.set(true)
  }

  androidLibrary {
    namespace = "com.ujizin.camposer"
    compileSdk = Config.compileSdk
    minSdk = Config.minSdk

    withDeviceTestBuilder {
      sourceSetTreeName = "test"
    }.configure { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }

    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
  }

  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "Camposer"
      isStatic = true
    }
    @Suppress("PropertyName")
    iosTarget.compilations.getByName("main") {
      val CMFormat by cinterops.creating
      val NSKeyValueObserving by cinterops.creating
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
    }

    androidMain.dependencies {
      api(libs.bundles.camerax)
    }

    iosMain.dependencies {}

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
    }

    getByName("androidDeviceTest").dependencies {
      implementation(kotlin("test"))
      implementation(libs.androidx.test.core)
      implementation(libs.androidx.test.rules)
      implementation(libs.androidx.core.testing)
      implementation(libs.compose.ui.test)
      implementation(libs.compose.junit4)
    }
  }
}

dokka {
  moduleName.set("Camposer")
}
