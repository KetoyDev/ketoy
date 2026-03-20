import org.gradle.plugin.compatibility.compatibility

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.gradle.plugin-publish") version "2.1.1"
    id("io.github.goooler.shadow") version "8.1.8"
    signing
}

group = "dev.ketoy"
version = "0.1.5-beta.10"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

// ── Bundle configuration ─────────────────────────────────────────
// Shadow processes this configuration instead of runtimeClasspath.
// This prevents gradleApi() file-dependencies (the entire Gradle API,
// ~88 MB) from being bundled into the plugin JAR.
// Only add dependencies here that must be physically embedded.
val bundleInJar: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(gradleApi())

    // WebSocket library for the embedded dev server.
    // `implementation` keeps it on the compile classpath.
    // `bundleInJar` tells Shadow to embed it into the fat JAR so it is
    // always available when external developers apply the plugin,
    // regardless of Gradle's plugin classloader isolation.
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    bundleInJar("org.java-websocket:Java-WebSocket:1.5.7")

    // Test
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// com.gradle.plugin-publish 2.x requires a javadoc JAR for publication.
// Kotlin uses KDoc rather than Javadoc, so the standard javadoc task
// produces warnings/errors on Kotlin source files. Suppress all doclint
// checks so the task succeeds and produces a valid (non-empty) JAR.
tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

// ── Shadow JAR: embed only Java-WebSocket ────────────────────────
// Uses the `bundleInJar` configuration (not runtimeClasspath) so that
// gradleApi() file-dependencies are never picked up by Shadow.
// The Gradle API, Kotlin stdlib, and SLF4J are provided by Gradle at
// runtime and must not be bundled.
tasks.shadowJar {
    archiveClassifier.set("") // replaces the regular JAR as the primary artifact

    // Process only the bundleInJar configuration — not runtimeClasspath.
    configurations = listOf(bundleInJar)

    // SLF4J is a transitive dep of Java-WebSocket but Gradle provides it.
    dependencies {
        exclude(dependency("org.slf4j:.*"))
    }

    // Relocate to avoid version conflicts if another plugin in the same
    // build also bundles java-websocket.
    relocate("org.java_websocket", "dev.ketoy.embedded.java_websocket")

    mergeServiceFiles()
}

// com.gradle.plugin-publish 2.x auto-detects the shadow plugin and uses
// the shadowJar output as the primary artifact automatically.

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
            // Plugin uses afterEvaluate and cross-project references (rootProject.findProject),
            // which are not configuration-cache compatible.
            compatibility {
                features {
                    configurationCache = false
                }
            }
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
