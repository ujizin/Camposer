@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

apply(from = "${rootDir}/scripts/publish-module.gradle")

kotlin {
    explicitApi()
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Camposer"
            isStatic = true
        }
    }

    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            api(libs.kotlinx.io)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.rules)
            implementation(compose.desktop.uiTestJUnit4)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            api(libs.bundles.internal.camerax)
        }

        iosMain.dependencies {
        }
    }
}

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


    sourceSets {
        getByName("androidTest") {
            manifest.srcFile("src/androidTest/AndroidManifest.xml")
        }
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

//dokka {
//    dokkaSourceSets {
//        named("main") {
//            enableAndroidDocumentationLink.set(true)
//        }
//    }
//}
