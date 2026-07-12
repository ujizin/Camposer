import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask

/**
 * Workaround: KGP's built-in `abiValidation` ignores the AGP KMP android target
 * (`com.android.kotlin.multiplatform.library`), leaving the Android JVM ABI unchecked
 * (see KT-71172). This plugin dumps and checks it manually with BCV task classes,
 * following BCV's layout convention: `api/android/<module>.api`.
 *
 * Remove once KGP supports the AGP KMP android target.
 */

val libs = the<VersionCatalogsExtension>().named("libs")
val kotlinVersion = libs.findVersion("kotlin").get().requiredVersion
val asmVersion = libs.findVersion("asm").get().requiredVersion

val androidAbiRuntime: Configuration by configurations.creating {
    isCanBeConsumed = false
    isVisible = false
}

dependencies {
    androidAbiRuntime("org.ow2.asm:asm:$asmVersion")
    androidAbiRuntime("org.ow2.asm:asm-tree:$asmVersion")
    androidAbiRuntime("org.jetbrains.kotlin:kotlin-metadata-jvm:$kotlinVersion")
}

val androidApiBuild by tasks.registering(KotlinApiBuildTask::class) {
    inputClassesDirs.from(tasks.named("compileAndroidMain").map { it.outputs.files })
    outputApiFile.set(layout.buildDirectory.file("kotlin/abi-android/${project.name}.api"))
    runtimeClasspath.from(androidAbiRuntime)
}

val androidApiCheck by tasks.registering(KotlinApiCompareTask::class) {
    projectApiFile.set(layout.projectDirectory.file("api/android/${project.name}.api"))
    generatedApiFile.set(androidApiBuild.flatMap { it.outputApiFile })
}

val androidApiDump by tasks.registering(Copy::class) {
    from(androidApiBuild.flatMap { it.outputApiFile })
    into(layout.projectDirectory.dir("api/android"))
}

tasks.matching { it.name == "checkLegacyAbi" }.configureEach { dependsOn(androidApiCheck) }
tasks.matching { it.name == "updateLegacyAbi" }.configureEach { dependsOn(androidApiDump) }
