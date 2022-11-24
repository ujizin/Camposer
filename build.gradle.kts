// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.gradle.nexus)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
}

apply(from = "${rootDir}/scripts/publish-root.gradle")