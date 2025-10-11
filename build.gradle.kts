// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatform.library) apply false
    alias(libs.plugins.gradle.nexus)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.java.doc)
    alias(libs.plugins.binary.compatibility.validator)
}

apply(from = "${rootDir}/scripts/publish-root.gradle")

apiValidation {
    ignoredProjects.addAll(listOf("sample", "shared"))
}
