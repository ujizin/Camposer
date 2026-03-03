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
    androidResources { enable = true }
    withHostTestBuilder { }
    withDeviceTestBuilder {
      sourceSetTreeName = "test"
    }.configure {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

  jvm()

  val xcfName = "sharedKit"
  iosX64 { binaries.framework { baseName = xcfName } }
  iosArm64 { binaries.framework { baseName = xcfName } }
  iosSimulatorArm64 { binaries.framework { baseName = xcfName } }

  sourceSets {
    // --- intermediate source set: Android + iOS only (MOKO, code-scanner) ---
    val nonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    androidMain.configure { dependsOn(nonJvmMain) }

    // iosMain is an intermediate source set for iOS-specific actuals.
    // It depends on nonJvmMain so iOS targets also get MOKO + code-scanner.
    // iosArm64Main / iosX64Main / iosSimulatorArm64Main → iosMain → nonJvmMain → commonMain
    val iosMain by creating {
      dependsOn(nonJvmMain)
    }
    getByName("iosX64Main") { dependsOn(iosMain) }
    getByName("iosArm64Main") { dependsOn(iosMain) }
    getByName("iosSimulatorArm64Main") { dependsOn(iosMain) }

    commonMain.dependencies {
      implementation(libs.bundles.compose.kmp)
      implementation(libs.compose.material3)
      implementation(libs.jetbrains.lifecycle.viewmodel)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.navigation3.ui)
      implementation(libs.compose.coil3)

      // Camposer (has JVM target now)
      implementation(libs.camposer)

      implementation(libs.kotlinx.io)

      api(libs.filekit.core)
      api(libs.filekit.dialogs)

      implementation(libs.compose.resources)
    }

    // code-scanner + MOKO only on android + ios
    nonJvmMain.dependencies {
      implementation(libs.camposer.code.scanner)
      api(libs.moko.permissions)
      api(libs.moko.permissions.compose)
      implementation(libs.moko.permissions.camera)
      implementation(libs.moko.permissions.microphone)
    }

    androidMain.dependencies {
      implementation(libs.compose.ui.tooling)
    }

    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.javacv)
      implementation(libs.kotlinx.coroutines.swing)
    }
  }
}
