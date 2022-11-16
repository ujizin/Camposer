import com.ujizin.camposer.Config

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.dokka")
}

extra.apply {
    set("PUBLISH_GROUP_ID", Config.groupId)
    set("PUBLISH_ARTIFACT_ID", Config.artifactId)
    set("PUBLISH_VERSION", Config.versionName)
}

apply(from = "${rootDir}/publish.gradle")

android {
    namespace = "com.ujizin.camposer"
    compileSdk = Config.compileSdk
    defaultConfig {
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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            noAndroidSdkLink.set(false)
        }
    }
}

dependencies {
    implementation(libs.bundles.compose)
    implementation(libs.lifecycle)

    api(libs.bundles.internal.camerax)
}