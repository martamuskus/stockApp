import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("jvm") version "1.9.21"
  kotlin("plugin.serialization") version "1.9.21"
  id("org.jetbrains.compose") version "1.5.11"
//  id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

//configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
//  additionalEditorconfig.set(
//    mapOf(
//      "indent_size" to "2",
//    ),
//  )
//}

tasks.jar {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  manifest {
    attributes["Main-Class"] = "MainKt"
  }

  from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
    excludes.add("META-INF/*.SF")
    excludes.add("META-INF/*.DSA")
    excludes.add("META-INF/*.RSA")
  }

  archiveFileName.set("stock-1.0.0.jar")
}

group = "com.stockapp"
version = "1.0.0"

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  google()
}

kotlin {
  jvmToolchain(21)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
  implementation(compose.desktop.currentOs)
  implementation(compose.material3)
  implementation(compose.ui)
  implementation(compose.foundation)
  implementation(compose.runtime)

  // Ktor
  val ktorVersion = "2.3.7"
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")

  // Serialization & Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
}

compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "stock"
      packageVersion = "1.0.0"
    }
  }
}
