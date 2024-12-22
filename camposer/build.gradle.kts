import ujizin.camposer.Config

plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.compose.compiler)
}

extra.apply {
    set("PUBLISH_GROUP_ID", Config.groupId)
    set("PUBLISH_ARTIFACT_ID", Config.artifactId)
    set("PUBLISH_VERSION", Config.versionName)
}

apply(from = "${rootDir}/scripts/publish-module.gradle")

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

    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=strict"
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
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
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.lifecycle)

    api(libs.bundles.internal.camerax)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.junit)
}
