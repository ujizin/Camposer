import ujizin.camposer.Config

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ujizin.sample"
    compileSdk = Config.compileSdk
    defaultConfig {
        versionCode = Config.versionCode
        versionName = Config.versionName
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.compose.activity)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.icons)
    implementation(libs.compose.navigation)
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

    implementation(project(":camposer"))
}
