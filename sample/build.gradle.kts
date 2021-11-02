import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    linuxX64()
    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("io.arrow-kt:arrow-core:1.0.0")
                implementation("io.arrow-kt:arrow-optics:1.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                implementation("io.arrow-kt:arrow-fx-coroutines:1.0.0")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

val ank: Configuration by configurations.creating

dependencies {
    dokkaHtmlPlugin(project(":"))
    ank(project(":sample"))
}

tasks.withType<DokkaTask>().configureEach {
    dependsOn("build")
    dokkaSourceSets {
        named("commonMain") {
            classpath.from(ank.files.first())
            moduleName.set("Dokka Gradle Example")
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(URL("https://github.com/Kotlin/dokka/tree/master/" +
                        "examples/gradle/dokka-gradle-example/src/main/kotlin"
                ))
                remoteLineSuffix.set("#L")
            }
        }
    }
}
