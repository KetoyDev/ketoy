plugins {
    kotlin("jvm") version "2.0.21"
    id("com.gradle.plugin-publish") version "1.3.1"
    signing
}

group = "dev.ketoy"
version = "0.1-beta"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Test
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ── Gradle Plugin Portal ─────────────────────────────────────────
// com.gradle.plugin-publish automatically applies:
//   - java-gradle-plugin  (builds the plugin + marker artifacts)
//   - maven-publish        (generates POM metadata)
// Sources & Javadoc JARs are also published automatically.
// Credentials come from gradle.properties:
//   gradle.publish.key / gradle.publish.secret
// ──────────────────────────────────────────────────────────────────
gradlePlugin {
    website.set("https://ketoy.dev")
    vcsUrl.set("https://github.com/KetoyDev/ketoy")

    plugins {
        create("ketoyDev") {
            id = "dev.ketoy.devtools"
            implementationClass = "dev.ketoy.gradle.KetoyDevPlugin"
            displayName = "Ketoy Dev – Server Driven UI for Android"
            description = "Gradle plugin for managing Ketoy SDUI screens — push, pull, rollback, " +
                    "list, and delete cloud-hosted server-driven UI layouts from the command line."
            tags.set(listOf("android", "sdui", "server-driven-ui", "jetpack-compose", "ketoy", "ui", "cloud"))
        }
    }
}

// ── Signing (GPG keys from gradle.properties) ────────────────────
// Applies automatically to plugin-publish artifacts when the
// signing plugin is present.  Keys are read from:
//   signing.keyId / signing.password / signing.secretKeyRingFile
signing {
    sign(publishing.publications)
}

// ── POM metadata (enriches the auto-generated publication) ───────
publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Ketoy Dev Gradle Plugin")
            description.set(
                "Gradle plugin for managing Ketoy SDUI screens. Push, pull, rollback, " +
                        "list, and delete cloud-hosted server-driven UI layouts from the CLI."
            )
            url.set("https://ketoy.dev")
            inceptionYear.set("2025")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("developerchunk")
                    name.set("Developer Chunk")
                    url.set("https://github.com/developerchunk/")
                }
            }
            scm {
                url.set("https://github.com/KetoyDev/ketoy")
                connection.set("scm:git:git://github.com/KetoyDev/ketoy.git")
                developerConnection.set("scm:git:ssh://git@github.com/KetoyDev/ketoy.git")
            }
        }
    }
}
