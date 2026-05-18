plugins {
  kotlin("jvm")
}

kotlin {
  explicitApi()
}

dependencies {
  compileOnly(libs.detekt.api)
  testImplementation(libs.detekt.test)
  testImplementation(kotlin("test-junit5"))
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}
