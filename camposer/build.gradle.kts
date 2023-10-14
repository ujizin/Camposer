import com.ujizin.camposer.Config

plugins {
    kotlin("multiplatform")
//    id("org.jetbrains.dokka")
    id("com.android.library")
    id("org.jetbrains.compose")
}

extra.apply {
    set("PUBLISH_GROUP_ID", Config.groupId)
    set("PUBLISH_ARTIFACT_ID", Config.artifactId)
    set("PUBLISH_VERSION", Config.versionName)
}

// apply(from = "${rootDir}/scripts/publish-module.gradle")

android {
    namespace = "com.ujizin.camposer"
    compileSdk = Config.compileSdk
    defaultConfig {
        minSdk = Config.minSdk
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
     targetHierarchy.default()


    androidTarget {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs += "-Xexplicit-api=strict"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.compilations.all {
            kotlinOptions {
                freeCompilerArgs += "-Xexplicit-api=strict"
            }
        }
        it.binaries.framework { baseName = "camposer" }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
//                implementation(compose.desktop.uiTestJUnit4)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.lifecycle)
                api(libs.bundles.internal.camerax)
            }
        }

//        val iosX64Main by getting
//        val iosArm64Main by getting
//        val iosSimulatorArm64Main by getting
//        val iosMain by creating {
//            dependsOn(commonMain)
//            iosX64Main.dependsOn(this)
//            iosArm64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
//        }
    }
}
//tasks.dokkaHtml.configure {
//    dokkaSourceSets {
//        named("main") {
//            noAndroidSdkLink.set(false)
//        }
//    }
//}