plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  androidLibrary {
    namespace = "com.ujizin.camposer.shared"
    compileSdk = 36
    minSdk = 23

    withHostTestBuilder {
    }

    withDeviceTestBuilder {
      sourceSetTreeName = "test"
    }.configure {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

  val xcfName = "sharedKit"

  iosX64 {
    binaries.framework {
      baseName = xcfName
    }
  }

  iosArm64 {
    binaries.framework {
      baseName = xcfName
    }
  }

  iosSimulatorArm64 {
    binaries.framework {
      baseName = xcfName
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.bundles.compose.kmp)
      implementation(libs.compose.material3)
      implementation(libs.jetbrains.lifecycle.viewmodel)
      implementation(libs.jetbrains.navigation3.ui)

      // Camposer
      implementation(libs.camposer)
      implementation(libs.camposer.code.scanner)

      // Permissions
      api(libs.moko.permissions)
      api(libs.moko.permissions.compose)

      implementation(libs.moko.permissions.camera)
      implementation(libs.moko.permissions.microphone)

      implementation(libs.media.kmp)
      implementation(libs.kotlinx.io)
    }

    commonTest.dependencies {
    }

    androidMain.dependencies {
    }

    iosMain.dependencies {
    }
  }
}

dependencies {
  "androidRuntimeClasspath"(libs.compose.ui.tooling)
}