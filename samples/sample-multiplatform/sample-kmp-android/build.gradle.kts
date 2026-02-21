plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.ujizin.camposer.sample.sample_kmp_android"
  compileSdk {
    version = release(36)
  }

  defaultConfig {
    applicationId = "com.ujizin.camposer.sample.sample_kmp_android"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(libs.lifecycle)
  implementation(libs.compose.activity)
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui.android)
  implementation(libs.compose.ui.preview.android)
  implementation(libs.compose.material3)
  implementation(project(":sample-multiplatform:shared"))
}
