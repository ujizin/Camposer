@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import ujizin.camposer.Config

plugins {
  alias(libs.plugins.library) // TODO migrate to kmp library
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.dokka)
  alias(libs.plugins.dokka.java.doc)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

extra.apply {
  set("PUBLISH_GROUP_ID", Config.groupId)
  set("PUBLISH_ARTIFACT_ID", Config.artifactId)
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
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }

    instrumentedTestVariant {
      sourceSetTree.set(KotlinSourceSetTree.test)
      dependencies {

        implementation(libs.androidx.test.core)
        implementation(libs.androidx.test.rules)
        implementation(libs.androidx.core.testing)
        implementation(libs.compose.junit4.android)
        debugImplementation(libs.compose.manifest)
      }
    }
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
      implementation(compose.runtime)
      implementation(compose.foundation)
    }

    androidMain.dependencies {
      implementation(compose.preview)
      api(libs.bundles.camerax)
    }

    iosMain.dependencies {}

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android {
  namespace = "com.ujizin.camposer"
  compileSdk = Config.compileSdk
  defaultConfig {
    minSdk = Config.minSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  testOptions {
    unitTests.isReturnDefaultValues = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  publishing {
    singleVariant("release") {
      withJavadocJar()
      withSourcesJar()
    }
  }
}

dokka {
  moduleName.set("Camposer")
  dokkaPublications.html {
    failOnWarning.set(true)
  }
}