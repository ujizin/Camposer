import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.ujizin.sample"
  compileSdk = 36
  defaultConfig {
    versionCode = 1
    versionName = "1.0.0"
    minSdk = 23
    targetSdk = 36
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

dependencies {
  implementation(platform(libs.compose.bom))
  implementation(libs.bundles.compose.android)
  implementation(libs.compose.activity)
  implementation(libs.compose.material3.android)
  implementation(libs.compose.material3.icons)
  implementation(libs.compose.navigation.android)
  implementation(libs.compose.lifecycle)
  implementation(libs.accompanist.permissions)
  implementation(libs.coil)
  implementation(libs.coil.video)
  implementation(libs.material)
  implementation(libs.koin)
  implementation(libs.datastore)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.lifecycle)
  implementation(libs.lifecycle.viewmodel)
  implementation(libs.exoplayer)
  implementation(libs.zxing.core)
  implementation(libs.cloudy)
  implementation(libs.camposer)
}
