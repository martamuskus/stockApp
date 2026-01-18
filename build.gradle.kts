import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
  kotlin("jvm") version "1.9.21"
  kotlin("plugin.serialization") version "1.9.21"
  id("org.jetbrains.compose") version "1.5.11"
  id("app.cash.sqldelight") version "2.0.1"
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
    attributes["Main-Class"] = "com.stockapp.MainKt"
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

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
  localPropertiesFile.inputStream().use {
    localProperties.load(it)
  }
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
  val ktorVersion = "2.3.13"
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")

  // Serialization & Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

  testImplementation(kotlin("test"))
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.21")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
  testImplementation("io.ktor:ktor-client-mock:2.3.7")
  testImplementation("org.jetbrains.compose.ui:ui-test-junit4:1.5.11")
  testImplementation("io.mockk:mockk:1.13.8")

  // SQLDelight
  implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
  implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
}

sqldelight {
  databases {
    create("StockDatabase") {
      packageName.set("com.stockapp.database")
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.stockapp.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.stockapp"
      packageVersion = "1.0.0"
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = "21"
}
