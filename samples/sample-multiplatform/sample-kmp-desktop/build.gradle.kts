import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  jvm()

  sourceSets {
    jvmMain.dependencies {
      implementation(project(":sample-multiplatform:shared"))
      implementation(compose.desktop.currentOs)
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.ujizin.camposer.sample.desktop.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "CamposerSample"
      packageVersion = "1.0.0"
    }
  }
}
