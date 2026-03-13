plugins {
    kotlin("jvm") version "2.0.21"
    id("com.gradle.plugin-publish") version "1.3.1"
    signing
}

group = "dev.ketoy"
version = "0.1.1-beta"

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

    // Embedded dev server (HTTP + WebSocket)
    implementation("org.java-websocket:Java-WebSocket:1.5.7")

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
            displayName = "Ketoy – Server-Driven UI Dev Toolchain for Android"
            description = "Gradle plugin for the Ketoy SDUI SDK. 11 built-in tasks: cloud push, " +
                    "pull, rollback, list, delete; DSL-to-JSON export (dev + prod with manifests); " +
                    "and an HTTP + WebSocket dev server with file & source watchers for live " +
                    "hot-reload preview of Jetpack Compose screens."
            tags.set(listOf(
                "android", "sdui", "server-driven-ui", "jetpack-compose", "compose",
                "ketoy", "ui", "cloud", "hot-reload", "devtools", "export", "k-dsl"
            ))
        }
    }
}

// ── Signing (GPG keys from gradle.properties) ────────────────────
// Only sign when publishing to Maven Central (requires signing.keyId,
// signing.password, signing.secretKeyRingFile in gradle.properties).
// Gradle Plugin Portal does NOT require GPG signing.
signing {
    isRequired = gradle.taskGraph.allTasks.any { it.name.contains("publishToMavenCentral") }
    sign(publishing.publications)
}

// ── POM metadata (enriches the auto-generated publication) ───────
publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Ketoy Dev Gradle Plugin")
            description.set(
                "The official Gradle plugin for the Ketoy server-driven UI SDK. Provides 11 " +
                        "tasks across three groups: cloud (ketoyPush, ketoyPushAll, ketoyListScreens, " +
                        "ketoyScreenVersions, ketoyScreenDetails, ketoyRollback, ketoyDeleteScreen), " +
                        "export (ketoyExport, ketoyExportProd with screen + navigation manifests), " +
                        "and dev server (ketoyServe, ketoyDev). Features an HTTP + WebSocket server " +
                        "with JSON file watcher (100 ms debounce), Kotlin source watcher (1500 ms " +
                        "debounce) that triggers auto-export on .kt/.kts changes, long-poll endpoint, " +
                        "and real-time hot-reload broadcast to KetoyDevWrapper in the running app."
            )
            url.set("https://ketoy.dev")
            inceptionYear.set("2026")

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
