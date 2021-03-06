import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.5.0"
    id("org.jetbrains.dokka") version "1.5.0"
    `maven-publish`
    signing
    base
}

group = "com.nomisrev"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

val dokkaVersion: String = "1.5.0"
configurations { create("toCopy") }

dependencies {
    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")

    implementation("io.arrow-kt:arrow-core:0.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("io.arrow-kt:arrow-fx-coroutines:0.13.2")

    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-base-test-utils:$dokkaVersion")

    "toCopy"("org.jetbrains.kotlin:kotlin-scripting-jsr223-unshaded:1.5.21")
}

val dokkaOutputDir = "$buildDir/dokka"

tasks {
    register("downloadJS233") {
        doLast {
            if (!File("src/main/resources/jsr223/list").exists()) {
                File("src/main/resources/jsr223").also { it.mkdirs() }

                copy {
                    from(configurations.getAt("toCopy")).into("src/main/resources/jsr223")
                }
                exec {
                    workingDir = File("src/main/resources/jsr223")
                    executable = "../../../../JarListScript.sh"
                }
            }
        }
    }

    clean {
        doFirst {
            File("src/main/resources/jsr223").deleteRecursively()
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        dependsOn("downloadJS233")
    }

    dokkaHtml {
        outputDirectory.set(file(dokkaOutputDir))
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
    dependsOn
}

java {
    withSourcesJar()
}

publishing {
    publications {
        val dokkaTemplatePlugin by creating(MavenPublication::class) {
            artifactId = project.name
            from(components["java"])
            artifact(javadocJar.get())

            pom {
                name.set("Dokka template plugin")
                description.set("This is a plugin template for Dokka")
                url.set("https://github.com/Kotlin/dokka-plugin-template/")

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("JetBrains")
                        name.set("JetBrains Team")
                        organization.set("JetBrains")
                        organizationUrl.set("http://www.jetbrains.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Kotlin/dokka-plugin-template.git")
                    url.set("https://github.com/Kotlin/dokka-plugin-template/tree/master")
                }
            }
        }
        signPublicationsIfKeyPresent(dokkaTemplatePlugin)
    }

    repositories {
        maven {
            url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USER")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

fun Project.signPublicationsIfKeyPresent(publication: MavenPublication) {
    val signingKeyId: String? = System.getenv("SIGN_KEY_ID")
    val signingKey: String? = System.getenv("SIGN_KEY")
    val signingKeyPassphrase: String? = System.getenv("SIGN_KEY_PASSPHRASE")

    if (!signingKey.isNullOrBlank()) {
        extensions.configure<SigningExtension>("signing") {
            if (signingKeyId?.isNotBlank() == true) {
                useInMemoryPgpKeys(signingKeyId, signingKey, signingKeyPassphrase)
            } else {
                useInMemoryPgpKeys(signingKey, signingKeyPassphrase)
            }
            sign(publication)
        }
    }
}
