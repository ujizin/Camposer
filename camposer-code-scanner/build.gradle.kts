import ujizin.camposer.Config

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.dokka)
  alias(libs.plugins.dokka.java.doc)
}

kotlin {
  explicitApi()
  androidLibrary {
    namespace = "com.ujizin.camposer.code_scanner"
    compileSdk = Config.compileSdk
    minSdk = Config.minSdk

    withHostTestBuilder {
    }

    withDeviceTestBuilder {
      sourceSetTreeName = "test"
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
        implementation(compose.runtime)
        implementation(compose.foundation)
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
  dokkaPublications.html {
    failOnWarning.set(true)
  }
}