plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    alias(libs.plugins.vanniktech.mavenPublish)
}

android {
    namespace = "com.developerstring.ketoy"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)

    // Material Icons (extended set for full icon coverage)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coil for async image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Navigation Compose (Navigation2 library)
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // Dev Tools dependencies
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.animation:animation")

    // Unit tests
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Instrumented / Compose UI tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates("dev.ketoy", "sdk", "0.1-beta")

    pom {
        name = "Ketoy SDUI SDK"
        description = "The open-source, server-driven UI engine for Jetpack Compose. Write K-DSL, " +
                "serialize to JSON, render native UI — no Play Store approval needed. Features: " +
                "15 K-DSL builders (KText, KButton, KCard, KColumn, KRow, KBox, KLazyColumn, " +
                "KLazyRow, KImage, KIcon, KIconButton, KSpacer, KDivider, KComponent, KFunctionCall); " +
                "35+ type-safe theme tokens including Material 3 surface containers and custom " +
                "success tokens; reactive variable system with Compose state-backed revision " +
                "tracking; type-safe navigation (Navigation Compose 2.8+) with server-driven " +
                "nav graph support; custom widget, action, and component registration; multi-content " +
                "screen system with per-block dev-server overrides; production export with screen " +
                "and navigation manifests; cloud service with NETWORK_FIRST/CACHE_FIRST strategies; " +
                "and integrated KetoyDevWrapper with WebSocket hot-reload."
        inceptionYear = "2026"
        url = "https://github.com/KetoyDev/Ketoy/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "developerchunk"
                name = "Developer Chunk"
                url = "https://github.com/developerchunk/"
            }
        }
        scm {
            url = "https://github.com/KetoyDev/Ketoy"
            connection = "scm:git:git://github.com/KetoyDev/Ketoy.git"
            developerConnection = "scm:git:ssh://git@github.com/KetoyDev/Ketoy.git"
        }

        withXml {
            val repo = asNode().appendNode("repositories").appendNode("repository")
            repo.appendNode("name", "Google")
            repo.appendNode("id", "google")
            repo.appendNode("url", "https://maven.google.com/")
        }
    }
}