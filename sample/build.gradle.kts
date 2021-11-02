import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

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

dependencies {
    dokkaHtmlPlugin("com.nomisrev:ank-dokka-plugin:1.1-SNAPSHOT")
}

tasks.withType<DokkaTask>().configureEach {
//    dependsOn("jar") // Build jar to include into Dokka's classpath
    dokkaSourceSets {
        named("commonMain") {
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